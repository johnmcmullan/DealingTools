require 'monitor'

require 'orc/op/operror'
require 'orc/op/opmessage'
require 'orc/op/opqueue'

class FXCache < Monitor
  def initialize
    @cache = Hash.new
    super
  end
  def [](name)
    synchronize do
      return @cache[name]
    end
  end
  def []=(name, rate)
    synchronize do
      @cache[name] = rate
    end
  end
end

class FX
  def initialize(orc)
    @orc = orc
    @rates = FXCache.new

    private = @orc.register_callback do
      |m|
      if !m.bid.nil?
        @rates[m.instrument_id.underlying] = m.bid.to_f
      end
    end

    # download all the currencies
    msg = $orc.instrument_download("assettype=Currencies|market=SFC")
    msg.instrument_list.each_value do
      |instrument_id|
      tag = instrument_id.instrument_tag
      @orc.send("pricefeed_toggle",
                "instrument_id={instrument_tag=#{tag}}|toggle=on|best_only=true",
                private)
      # The callback will handle the reply because of the private id...
    end
  end

  def rate(underlying)
    curr1, curr2 = underlying.split('/')
    return 1.0 if curr1 == curr2
    return @rates[underlying]
  end
end




