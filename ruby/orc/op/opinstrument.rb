require 'orc/op/operror'
require 'orc/op/opmessage'
require 'orc/op/opqueue'

require 'dt/instrument'

class OPInstrument < OPMessage
  include Instrument

  def initialize(val = nil)
    super(val)
  end
  
  def full?
    @data["parameters"].kind_of? OPMessage ? true : false
  end

  def dt_instrument_id
    return instrument_str(instrument_id.underlying,
                          instrument_id.expirydate,
                          instrument_id.kind,
                          instrument_id.strikeprice)
  end
end

class OPInstrumentCache
  include Enumerable

  def initialize(orc)
    @orc = orc
    @cache = Hash.new
    # by name, expiration, strike => bucket
    @index = Hash.new(Hash.new(Hash.new(Array.new)))
  end

  def each(&block)
    return @cache.each { |instrument| block.call(instrument) }
  end

  def download(spec)
    query = spec
    l = @orc.instrument_download(query)
    l.instrument_list.each do
      |key, id|
      tag = id.instrument_tag
      id = OPMessage.new( { "instrument_id" => id } )
      if @cache[tag] and @cache[tag].instrument_id
        @cache[tag].merge(id)
      else
        @cache[tag] = id
        @index[id.underlying][id.expirydate][id.strikeprice].unshift(tag)
      end
    end
  end

  def get(spec)
    if spec.kind_of? Numeric
      # check the cache first
      tag = spec
      if @cache.has_key?(tag) and @cache[tag].full?
        return @cache[tag]
      end
      query = OPMessage.new
      query.instrument_tag(spec)
    else
      query = spec
    end
    i = @orc.instrument_get(query)
    id = i.instrument_id
    tag = id.instrument_tag
    if @cache[tag] != nil
      @index[id.underlying][id.expirydate][id.strikeprice].unshift(tag)
    end
    @cache[tag] = i
    return i
  end
end

