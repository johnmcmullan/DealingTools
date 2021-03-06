require 'orc/op/opinstrument'

class RiskTree
  attr_reader :actions, :orc, :tree

  def initialize(orc, portfolio_name, actions)
    @orc = orc
    @actions = actions
    @tree = PortfolioNode.new(portfolio_name, self)
  end

  def to_s
    reply = "ROOT+\n" + @tree.to_s
  end
end

class RiskNode
  include Enumerable
  include Comparable

  attr_accessor :name, :msg
  attr_reader :parent

  def initialize(name, parent = nil)
    @name = name
    @root = @parent = parent
    @root = @root.parent until !@root.kind_of?(RiskNode)
    @subnodes = Hash.new
    @msg = OPMessage.new
  end

  def [](key)
    @subnodes[key]
  end

  def []=(key, data)
    @subnodes[key] = data
  end

  def each(&block)
    @subnodes.each { |key, value| block.call(key, value) }
  end

  def each_value(&block)
    @subnodes.each_value { |value| block.call(value) }
  end

  def each_key(&block)
    @subnodes.each_key { |key| block.call(key) }
  end

  def has_key?(key)
    @subnodes.has_key?(key)
  end

  def size
    @subnodes.size
  end

  def keys
    @subnodes.keys
  end

  def <=>(other)
    @name <=> other.name
  end

  def to_s
    reply = ""
    node = self
    while node.kind_of?(RiskNode) do
      reply += node.name + "/"
      node = node.parent
    end
    reply += " = #{@msg}\n"
    @subnodes.each_value { |n| reply << n.to_s }
    return reply
  end
  
  def refresh(msg)
    return if msg.empty?
    @msg.add!(msg)
    @parent.refresh(msg) if @parent.kind_of? RiskNode
  end
end

# Portfolio ---------------------------------------------------------


class PortfolioNode < RiskNode
  def initialize(portfolio_name, parent)
    super(portfolio_name, parent)
    msg = @root.orc.portfolio_get("portfolio_name=#{portfolio_name}")
    if !msg.portfolio_positions.nil?
      msg.portfolio_positions.each_value { |p| update(p) }
    end
  end
  
  def update(position)
    position.portfolio_name = name
    return if position.instrument_id.nil?
    underlying_name = position.instrument_id.underlying
    return if underlying_name.nil?
    if !@subnodes.has_key?(underlying_name)
      @subnodes[underlying_name] =
        UnderlyingNode.new(underlying_name, self)
    end
    @subnodes[underlying_name].update(position)
  end
  
end

# Underlying ---------------------------------------------------------

class UnderlyingNode < RiskNode
  def initialize(underlying_name, parent)
    super(underlying_name, parent)
  end
  
  def update(position)
    expirydate = position.instrument_id.expirydate
    return if expirydate.nil?

    # ignore expired positions
    now = Time.new
    if Time.local(expirydate[0,4].to_i, expirydate[5,2].to_i,
                  expirydate[8,2].to_i, 18, 0, 0, 0) < now
      return
    end

    if !@subnodes.has_key? expirydate
      @subnodes[expirydate] = ExpiryNode.new(expirydate, self)
    end
    @subnodes[expirydate].update(position)
  end

end

# Expiry ---------------------------------------------------------

class ExpiryNode < RiskNode
  def initialize(expirydate, parent)
    super(expirydate, parent)
  end
  
  def update(position)
    return if position.volume == "0"
    id = position.instrument_id.strikeprice ||
      position.instrument_id.kind
    
    if !@subnodes.has_key?(id)
      @subnodes[id] = StrikepriceNode.new(id, self)
    end
    @subnodes[id].update(position)
  end
end

# Strikeprice ---------------------------------------------------------

class StrikepriceNode < RiskNode
  def initialize(strikeprice, parent)
    super(strikeprice, parent)
  end
  
  def update(position)
    instrument_id = position.instrument_id
    instrument = OPInstrument.new
    instrument.instrument_id = instrument_id
    instrument_name = instrument.dt_instrument_id
    if !@subnodes.has_key? instrument_name
      @subnodes[instrument_name] =
        ContractNode.new(instrument_name, self)
    end
    @subnodes[instrument_name].update(position)
  end

  def <=>(other)
    return @name.to_f <=> other.name.to_f
  end

end

# Contract ---------------------------------------------------------

class ContractNode < RiskNode

  def initialize(instrument_name, parent)
    super(instrument_name, parent)
  end
  
  def update(position)
    if @msg.empty?
      position.calls = position.puts = 0
      case position.instrument_id.kind
      when "Call": position.calls = position.volume
      when "Put": position.puts = position.volume
      end
      refresh(position)
    end

    @instrument_tag = @msg.instrument_id.instrument_tag

    action_number = 1
    actions_str = ""
    @root.actions.each do
      |action|
      actions_str += "action#{action_number}=#{action}|"
      action_number += 1
    end
    actions_str.chop!
    
    inmsg = "instrument_id={instrument_tag=" +
      @instrument_tag + "}|" + "actions={" + actions_str + "}|" +
      "portfolio_volume=" + @msg.volume + "|" +
      "portfolio_accrued=" + @msg.accrued + "|" +
      "portfolio_invested=" + @msg.invested + "|" +
      "async=true|feed=false"

    msg = @root.orc.theoretical_calculation_group(inmsg)
    if !msg.calculation_results.nil?
      results_msg = OPMessage.new
      msg.calculation_results.each_value do
        |result|
        action = result.action.downcase.tr(' ', '_').to_sym
        results_msg.send(action, result.result)
      end
      refresh(results_msg)
    end
  end

end
