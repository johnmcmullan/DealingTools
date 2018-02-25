require 'orc/op/operror'
require 'orc/op/opmessage'
require 'orc/op/opqueue'


class Underlying < OPMessage
  def initialize(val = nil)
    super(val)
  end
end

class Underlyings
  def initialize(orc)
    @orc = orc
    @cache = Hash.new { |h,k| h[k] = Hash.new }
    download
    return self
  end

  private
  def download
    ["Equities", "Interest Rate", "Commodities"].each do
      |assettype|
      msg = @orc.underlying_download("assettype=#{assettype}")
      next if msg.underlyings.nil?
      msg.underlyings.each_value do
        |underlying|
        next if underlying.underlying_name == "[none]"
        @cache[underlying.underlying_name][underlying.assettype] =
          Underlying.new(underlying)
      end
    end
  end

  public
  def list(assettype = nil)
    return @cache.keys if assettype.nil?
    underlying_names = Array.new
    @cache.each_key do
      |underlying_name|
      if @cache[underlying_name].has_key?(assettype)
        underlying_names << underlying_name
      end
    end
    return underlying_names
  end

  def get(underlying_name, assettype = nil)
    rec = @cache[underlying_name]
    return nil if (rec.size > 1) && assettype.nil?
    if rec.size < 1
      rec.each_value { |u| return u }
    end
    return rec[assettype]
  end

  def get_basecontract(underlying_name, expirydate = nil, assettype = nil,
                       &block)
    request = "underlying=#{underlying_name}"
    if !expirydate.nil?
      request << "|expirydate_start=#{expirydate}|expirydate_end=#{expirydate}"
    end
    request << "|assettype=#{assettype}" if !assettype.nil?
    begin
      if block.nil?
        msg = @orc.instrument_group_parameters_get(request)
        min = msg.parameters_min.basecontract
        return msg.parameters_max.basecontract if min.nil?
        return min
      else
        @orc.instrument_group_parameters_get(request) do
          |msg|
          begin
            min = msg.parameters_min.basecontract
            block.call(msg.parameters_max.basecontract) if min.nil?
            block.call(min)
          rescue Exception => e
            $log.debug("#{e.to_s} when trying to find basecontract for #{underlying_name}/#{expirydate}")
            $log.debug(msg.to_s)
            $log.debug(e.backtrace.join("\n"))
            block.call(nil)
          end
        end
      end
    rescue OPError => e
      return nil
    end
  end

end
