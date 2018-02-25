require 'monitor'

require 'orc/op/operror'
require 'orc/op/opmessage'
require 'orc/op/opqueue'

require 'orc/underlying'

class StressTest
  attr_accessor :price_range, :vol_range

  def initialize(orc)
    @orc = orc
    @price_range = Array.new
    @vol_range = Array.new
  end

  

  def test(actions, portfolio_name, underlying_names, expirydate = nil)
    underlying_base_prices = Hash.new
    underlying_names.each do
      |underlying_name|
      atm_vols = Hash.new

      msg = $orc.volatility_raw_surface_get("underlying=#{underlying_name}|surface=Risk-Expiry")
      msg.volatility_raw_surface.each_value do
        |entry|
        atm_vols[entry.expiry] = entry.volatility
      end

      base_contract = underlyings.get_base_contract(underlying_name, expirydate)
      
      priv = @orc.register_callback do
        |msg|
        return if msg.bid.nil?
        underlying_base_prices[msg.instrument_id.underlying] =
          (msg.bid.to_f + msg.ask.to_f) / 2
      end
      @orc.send("price_get", "instrument_id={#{base_contract.instrument_tag}",
                priv)
    
    
  end

end
