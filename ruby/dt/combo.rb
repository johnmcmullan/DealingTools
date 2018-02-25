require 'parsedate'
require 'set'

class ComboError < RuntimeError
  def initialize(string)
    @bad_string = string
  end

  def to_s
    return "Parse Error: " + super.to_s + " [" + @bad_string + "]"
  end
end

class Contract
  @@MONTHS = { "jan" => 1, "feb" => 2, "mar" => 3, "apr" => 4,
    "may" => 5, "jun" => 6, "jul" => 7, "aug" => 8,
    "sep" => 9, "oct" => 10, "nov" => 11, "dec" => 12 }

  attr_reader :ratio, :expiration_month, :expiration_year, :strike, :kind

  def initialize(combo)
    @combo = combo
    @ratio = 1
    @expiration_month = 0
    @expiration_year = Time.now.year
    @strike = 0.0
    @kind = "f"
  end

  def orc_kind
    case @kind
      when "f": "Future"
      when "c": "Call"
      when "p": "Put"
    else
      "Unknown"
    end
  end

  def parse_date(str)
    ratio = "%c" % str[0]
    if (ratio == "+") or (ratio == "-")
      str = str[1..-1]
      if ratio == "-"
        @ratio *= -1
      end
    end
    @expiration_month = @@MONTHS.fetch(str[0, 3], 0)
    if @expiration_month != 0
      if str.length > 3
        str[0, 3] = ""
        if str.to_i != 0
          # allow them to use 006, 06 or 6 as the year
          # but ref it to this century
          century = @expiration_year.to_s[0, 4 - str.length]
          @expiration_year = century.concat(str).to_i
        end
      end
    else
      date = ParseDate.parsedate(str, true)
      if !date.nil?
        if date[0].nil?
          raise ComboError.new(str), "Unable to parse date", caller
        end
        if date[0] > @expiration_year
          @expiration_year = date[0]
        end
        @expiration_month = date[1]
      else
        @expiration_month = str.to_i
      end
    end
  end

  def parse_strike(str)
    tokens = str.split(':')
    if tokens[0] == "-"
      @ratio *= -1
    end
    @strike = tokens[1].to_f
    @kind = tokens[2]
  end

  def get_orc_contract(orc, &block)
    expirydate_start = "%4.4d-%02.2d-01" % [ @expiration_year, @expiration_month ]
    expirydate_end = "%4.4d-%02.2d-30" % [ @expiration_year, @expiration_month ]
    strike = @strike
    strike_fudge = strike * 0.001
    spec = { "underlying" => @combo.underlying,
      "strikeprice_min" => strike - strike_fudge,
      "strikeprice_max" => strike + strike_fudge,
      "expirydate_start" => expirydate_start,
      "expirydate_end" => expirydate_end,
      "kind" => orc_kind,
      "ignore_case" => "true"
    }
    orc.send_with_block("instrument_download", spec) do
      |msg|
      if msg.instrument_list.size > 1
        msg.instrument_list.each do
          |key, val|
          puts key + "=" + val
        end
      else
        @orc_contract = msg.instrument_list.instrument_id1
        puts @orc_contract.to_s
      end
#      puts msg.instrument_list.to_s
      block.call()
    end
  end

  def to_s
    result = "%d %4.4d-%02.2d " %
      [ @ratio, @expiration_year, @expiration_month ]
    if @strike != 0.0
      result << @strike.to_s
    end
    result << @kind
  end

end
    
class Combo
  include Enumerable
  @@underlyings = nil

  attr_reader :source, :underlying, :tied_spot

  def initialize(str)
    if @@underlyings.nil?
      raise ComboError(str), "Valid underlyings have not been set", caller
    end

    # hang onto the str so we can show it to them again
    @str = str
    @source = "unknown"
    @underlying = "[none]"
    @contracts = Array.new
    @tied_spot = 0.0
    @bid_price = 0.0
    @ask_price = 0.0
    @volume = 0
    @timestamp = Time.now

    parse(str)
  end

  def Combo.set_valid_underlyings(underlyings)
    @@underlyings = underlyings
  end

  def parse(str)
    # handle alias for risk-reversal
    if str =~ /rr/
      str.sub!(/rr/, "c-p")
    end

    tokens = str.split()

    if tokens.size < 2
      raise ComboError.new(str), "not enough tokens", caller
    end

    first_token = tokens.shift.upcase
    if !@@underlyings.member?(first_token)
      @source = first_token
      @underlying = tokens.shift.upcase
      if tokens.empty?
        raise ComboError.new(str), "only source and name specified", caller
      end
    else
      @underlying = first_token
    end

    # parse the expiration dates
    token = tokens.shift.downcase
    token.gsub!(/([-+])/, ':\1')
    dates = token.split(':')
    dates.size.times do |n|
      if @contracts[n].nil?
        @contracts[n] = Contract.new(self)
      end
      @contracts[n].parse_date(dates[n])
    end

    if tokens.size == 0
      return
    end

    # strikes + kinds + ratios
    token = tokens.shift.gsub(/([\+\-]?)(\d*\.?\d*)([pc]?)/, '\1:\2:\3|')
    # remove potential trailing nil strike
    token.gsub!(/\|::\|/, '|')

    # make sure each strike is fully specified
    default = token[/:\d+\.?\d*:[pc]\|/]
    if default.nil?
      raise ComboError.new(str), "Bad strike spec", caller
    end
    token.gsub!(/::\|/, default)
    default = token[/:\d+\.?\d*:/]
    token.gsub!(/::/, default)
    # remove final delimeter to avoid nil strike
    token.chomp('|')

    strikes = token.split(/\|/)

    # make sure we have the same number of contracts as strikes
    while strikes.size > @contracts.size
      @contracts.push(@contracts.last.dup)
    end
    while @contracts.size > strikes.size
      strikes.push(strikes.last.dup)
    end

    @contracts.size.times { |n| @contracts[n].parse_strike(strikes[n]) }
    
    if tokens.size == 0
      return
    end

    # potential tied spot
    if tokens.first == "@"
      tokens.shift
    end

    if tokens.size == 0
      return
    end

    token = tokens.shift;
    @tied_spot = token.delete("@").to_f

    if tokens.size == 0
      return
    end

    price = tokens.shift.split('-')
    @bid_price = price[0].to_f
    @ask_price = price[1].to_f

    if tokens.size == 0
      return
    end

    @volume = tokens.shift.to_i
  end

  def each(&block)
    @contracts.each { |contract| block.call(contract) }
  end
  alias each_contract each

  def num_contracts
    return @contracts.size
  end

  def get_orc_combo(orc, &block)
    num_contracts = @contracts.size
    @contracts.each do
      |contract|
      contract.get_orc_contract(orc) do
        num_contracts = num_contracts - 1
        if num_contracts == 0
          block.call()
        end
      end
    end
  end

  def to_s
    result = @timestamp.to_s << ": " << @underlying << " "
    @contracts.each { |contract| result << contract.to_s << " " }
    if @tied_spot != 0.0
      result << " @ " << @tied_spot.to_s
    end
    result << " from " << @source
    if @volume != 0
      result << " " << @volume.to_s
    end
    if @bid_price != 0.0 or @ask_price != 0.0
      result << " " << @bid_price.to_s << " vs " << @ask_price.to_s
    end
    return result
  end
end
