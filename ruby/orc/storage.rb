#    0 = {
#        asset = 0; 
#        currency = EUR; 
#        date = 20070420; 
#        description = "2007-04-20"; 
#        type = 4; 
#        underlying = ESX; 
#    }; 
#}

class StorageRecord
  include Enumerable

  def initialize(val = nil)
    if val.nil?
      @data = Hash.new
      return
    end

    if val.kind_of?(Hash)
      @data = val
    elsif val.kind_of?(StorageRecord)
      @data = val.data
    elsif val.kind_of?(String)
      @data = Hash.new
      parse(StringIO.new(val))
    else
      @data = Hash.new
      if val.kind_of?(IO) || val.kind_of?(StringIO)
        parse(val)
      end
    end
  end
  
  def parse(stream)
    begin
      looking_for_key = true
      key = buf = nil
      stream.each_byte do
        |c|
        c = "%c" % c
        case c
        when '{'
          if key != nil
            # not initial opening brace
            @data[key] = StorageRecord.new(stream)
            key = nil
          end
          looking_for_key = true

        when ';'
          if key != nil
            @data[key] = buf.strip
            key = buf = nil
          end
          looking_for_key = true

        when '='
          if looking_for_key
            key = buf.strip
            buf = nil
            looking_for_key = false
          else
            buf << c
          end

        when '}'
          return

        when ' '
          if !looking_for_key && !buf.nil?
            buf << c
          end

        when '\n'
        when '"'
          # ignore it

        else
          if buf == nil
            buf = c
          else
            buf << c
          end
        end
      end
    rescue Exception => e
      $log.write(e.to_s)
      $log.debug(e.backtrace.join("\n"))
    end
  end

  def each(&block)
    @data.each { |key, value| block.call(key, value) }
  end

  def each_value(&block)
    @data.each_value { |value| block.call(value) }
  end

  def each_key(&block)
    @data.each_key { |key| block.call(key) }
  end

  def has_key?(key)
    return @data.has_key?(key)
  end

  def empty?
    return @data.empty?
  end

  def [](key)
    return @data[key]
  end

  def to_s
    result = ""
    @data.each do |key, val|
      result << key << " = "
      if val.kind_of?(StorageRecord)
        result << "\n" << val.to_s << "}"
      else
        if val.index(" ").nil?
          result << val.to_s
        else
          result << "\"" << val.to_s << "\""
        end
      end
      result << ";\n"
    end
    return result
  end

  def method_missing(symbol, *val)
    str = symbol.id2name.delete("=")
    if val.size > 0
      @data[str] = val[0]
    end
    return @data[str]
  end

end
