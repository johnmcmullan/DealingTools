require 'monitor'
require 'timeout'

class PriceCache < Monitor
  def initialize(orc)
    @orc = orc
    @cache = Hash.new
    @cond = new_cond

    super()

    @private = @orc.register_callback do
      |msg|
      if !msg.message_info.nil? &&
          (msg.message_info.message_type == "price_feed")
        tag = msg.instrument_id.instrument_tag
        synchronize do
          @cache[tag] = msg
          @cond.signal
        end
      end
    end
  end

  def[](instrument_tag)
    synchronize do
      if @cache.has_key?(instrument_tag)
        return @cache[instrument_tag]
      end
      # missed the cache!
      price = 0
      Timeout::timeout(30) do
        while price == 0
          @cache[instrument_tag] = nil
          @orc.send("price_get",
                    "instrument_id={instrument_tag=#{instrument_tag}}",
                    @private)
          begin
            Timeout::timeout(10) do
              @cond.wait_while { @cache[instrument_tag].nil? }
            end
          rescue Timeout::Error => e
            $log.write(e)
            $log.debug("Waiting for price for #{instrument_tag}")
            return nil
          end
          msg = @cache[instrument_tag]
          if msg.has_key?("ask_ytm")
            price = msg.ask_ytm.to_i + msg.bid_ytm.to_i
          else
            price = msg.bid.to_i + msg.ask.to_i
          end
        end
      end
      return @cache[instrument_tag]
    end
  end
end
