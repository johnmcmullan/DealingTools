require 'singleton'

require 'orc/op'
require 'orc/op/opinstrument'

class PriceFeed
  include Singleton

  def initialize
    raise OPError, "Orc global is not defined" if $orc.nil?
    @orc = $orc
    @feeds = Hash.new { |h,k| h[k] = Hash.new }
    @private_id = @orc.register_callback do
      |msg|
      tag = msg.instrument_id.instrument_tag
      @feeds[tag].each_value { |block| block.call(msg) }
    end
  end

  def add_observer(sender, instrument_tag, &block)
    return if @feeds[instrument_tag].has_key?(sender.object_id)
    if @feeds[instrument_tag].size == 0
      @orc.send("pricefeed_toggle",
                "toggle=on|instrument_id={instrument_tag=#{instrument_tag}}",
                @private_id)
    end
    @feeds[instrument_tag][sender.object_id] = block
  end

  def delete_observer(sender, instrument_tag)
    @feeds[instrument_tag].delete(sender.object_id)
    if @feeds[instrument_tag].size == 0
      @orc.send("pricefeed_toggle",
                "toggle=off|instrument_id={instrument_tag=#{instrument_tag}}",
                "0")
    end
  end
end


