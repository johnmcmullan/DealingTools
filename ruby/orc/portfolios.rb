require 'singleton'

require 'orc/op'
require 'orc/op/opinstrument'

class Portfolios
  include Singleton

  def initialize
    raise OPError, "Orc global is not defined" if $orc.nil?
    @orc = $orc
    @feeds = Hash.new
    @private_id = @orc.register_callback do
      |msg|
      @feeds.each_value { |block| block.call(msg) }
    end
  end

  def names
    result = Array.new
    msg = @orc.portfolio_download("ignore_deleted=true")
    msg.portfolios.each_value { |portfolio| result << portfolio.portfolio_name }
    return result
  end

  def add_position_update_observer(sender, &block)
    return if @feeds.has_key?(sender.object_id)
    if @feeds.size == 0
      @orc.send("portfolio_position_feed_toggle", "toggle=on", @private_id)
    end
    @feeds[sender.object_id] = block
  end

  def delete_position_update_observer(sender)
    @feeds.delete(sender.object_id)
    if @feeds.size == 0
      @orc.send("portfolio_position_feed_toggle", "toggle=off", "0")
    end
  end

  # these return a hash, the structure of he hash depends on the info
  # you give the function.... it could look like this...
  # [GBP][FTSE][6225.0][.095][delta_of_position] = 134.0
  # OR this...
  # [GBP][FTSE][2007-03-16][5075][FTSE5075.C07][6225.0][0.95][delta_of_position] = 134.0
  # OR this...
  # [GBP][FTSE][2007-03-16][delta_of_position] = 134.0
  # OR....

  def stresstest_by_underlying(portfolio_name, actions,
                               simulated_prices = nil, price_mode = nil,
                               simulated_volatilities = nil, vol_mode = nil,
                               underlying_names = nil,
                               currency = nil, expirydate = nil, &block)
    stresstest(portfolio_name, actions, "Underlying", simulated_prices,
               price_mode, simulated_volatilities, vol_mode, underlying_names,
               currency, expirydate, block)
  end

  def stresstest_by_position(portfolio_name, actions,
                             simulated_prices = nil, price_mode = nil,
                             simulated_volatilities = nil, vol_mode = nil,
                             underlying_names = nil,
                             currency = nil, expirydate = nil, &block)
    stresstest(portfolio_name, actions, "Position", simulated_prices,
               price_mode, simulated_volatilities, vol_mode, underlying_names,
               currency, expirydate, block)
  end

  private
  def stresstest(portfolio_name, actions, group_results_by,
                 simulated_prices = nil, price_mode = nil,
                 simulated_volatilities = nil, vol_mode = nil,
                 underlying_names = nil, currency = nil, expirydate = nil,
                 proc = nil)
    price_mode = "Fixed" if price_mode.nil?
    vol_mode = "Percentage" if vol_mode.nil?

    msg = "portfolio_name=#{portfolio_name}|"
    msg += actions.to_opstr("actions")
    if simulated_prices.nil? or simulated_prices.empty?
      msg += "|simulated_prices={simulated_price1=0}|simulated_price_mode=Offset"
    else
      msg += "|#{simulated_prices.to_opstr("simulated_prices")}|simulated_price_mode=#{price_mode}"
    end
    if simulated_volatilities.nil? or simulated_volatilities.empty?
      msg += "|simulated_volatilities={simulated_volatility1=0}|simulated_volatility_mode=Offset"
    else
      msg += "|#{simulated_volatilities.to_opstr("simulated_volatilities")}|simulated_volatility_mode=#{vol_mode}"
    end
    if !underlying_names.nil? && !underlying_names.empty?
      msg += "|#{underlying_names.to_opstr("underlying_filters")}"
    end
    msg += "|group_results_by=#{group_results_by}"
    msg += "|result_currency=#{currency}" unless currency.nil?
    msg += "|expirydate_start=#{expirydate}|expirydate_end=#{expirydate}" unless expirydate.nil?

    if proc.nil?
      return process_stresstest(@orc.stresstest(msg),
                                simulated_prices, simulated_volatilities)
    else
      @orc.send_with_block("stresstest", msg) do
        |msg|
        process_stresstest(msg, simulated_prices, simulated_volatilities, proc)
      end
    end
  end

  def process_stresstest(stresstest, simulated_prices, simulated_volatilities,
                         proc = nil)
    result = Hash.new(&(p=lambda{|h,k| h[k] = Hash.new(&p)}))
    if !stresstest.stress_matrix.nil?
      stresstest.stress_matrix.each_value do
        |se|
        if se.instrument_id.nil?
          e = result[se.result_currency][se.underlying]
        else
          i = OPInstrument.new
          i.instrument_id = se.instrument_id
          expirydate = i.expirydate || i.kind
          strikeprice = i.strikeprice || i.kind
          e = result[se.result_currency][se.underlying][expirydate][strikeprice][i.dt_instrument_id]
        end
        se.simulation_results.each_value do
          |sr|
          if simulated_prices.nil? && simulated_volatilities.nil?
            r = e
          elsif !simulated_prices.nil? && !simulated_volatilities.nil?
            r = e[sr.simulated_price.to_f.dt_round.to_s][sr.simulated_volatility.to_f.dt_round.to_s]
          elsif simulated_prices.nil?
            r = e[sr.simulated_volatility.to_f.dt_round.to_s]
          else
            r = e[sr.simulated_price.to_f.dt_round.to_s]
          end
          sr.calculation_results.each_value do
            |ca|
            r[OPMessage.enum_to_key(ca.action)] = ca.result.to_f
          end
        end
      end
    end

    if proc.nil?
      return result
    else
      proc.call(result)
    end
  end
end

