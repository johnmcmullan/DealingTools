require 'pdf/writer'
require 'pdf/simpletable'

require 'orc/risk/risktree'
require 'orc/portfolios'

class UnderlyingReport

  def initialize(orc, portfolio_name, underlying_name)
    @orc = orc
    @portfolio_name = portfolio_name
    @underlying_name = underlying_name
    @actions = [ "Delta of position",
                 "Gamma of position", "Vega of position",
                 "Theta of position", "Rho of position" ]
    @portfolios = Portfolios.instance
    @risktree =
      RiskTree.new(@orc, @portfolio_name, @actions + ["Actual Volatility"])
  end

  def to_pdf
    pdf = PDF::Writer.new(:paper => "A4")
    pdf.select_font("Times-Roman")

    report = underlying_by_price_movement
    
    report = strikes_by_underlying
    report.each do
      |table_data|
      next if table_data["data"].size == 1
      table = PDF::SimpleTable.new
      table.title = table_data["title"]
      table.column_order = table_data["column_order"]
      table.data = table_data["data"]
      table.render_on(pdf)
    end

    return pdf
  end

  def underlying_by_price_movement
    price_offsets = [ "-1", "0", "+1" ]
    stresstest =
      @portfolios.stresstest_by_underlying(@portfolio_name, @actions,
                                           price_offsets, "Offset", nil, nil,
                                           [ @underlying_name ])
    table = Hash.new
    table["title"] = "#{@underlying_name}"
    table["data"] = Array.new
    table["column_order"] = [ "under" ]
#    table["column_order"] +=
#      @actions.collect { |a| OPMessage.enum_to_key(a)[/^[^_]+/] }
#    stresstest.each_value do
#      |node|
#      node.each_value do
#        |node|
#        row = 

    return table
  end

  def strikes_by_underlying
    report = Array.new
    @risktree.tree.each do
      |underlying_name, underlying_node|
      next unless underlying_name == @underlying_name
      underlying_node.each do
        |expirydate, expiry_node|
        table = Hash.new
        table["title"] = "#{@underlying_name} #{expirydate}"
        table["data"] = Array.new
        table["column_order"] = [ "strike", "vol", "calls", "puts", "options" ]
        table["column_order"] +=
          @actions.collect { |a| OPMessage.enum_to_key(a)[/^[^_]+/] }

        expiry_node.keys.sort.each do
          |strikeprice|
          next if strikeprice.to_f == 0.0
          strike_node = expiry_node[strikeprice]
          row = Hash.new
          row["strike"] = strikeprice
          vol = strike_node.msg.actual_volatility.to_f / strike_node.size
          row["vol"] = "%0.4f" % vol
          row["calls"] = strike_node.msg.calls
          row["puts"] = strike_node.msg.puts
          row["options"] = strike_node.msg.volume
          @actions.each do
            |action|
            action_str = OPMessage.enum_to_key(action)
            val = strike_node.msg.send(action_str.to_sym).to_f
            row[action_str[/^[^_]+/]] = "%8.2f" % val
          end
          table["data"] << row
        end
        row = Hash.new
        row["calls"] = expiry_node.msg.calls
        row["puts"] = expiry_node.msg.puts
        row["options"] = expiry_node.msg.volume
        @risktree.actions.each do
          |action|
          action_str = OPMessage.enum_to_key(action)
          val = expiry_node.msg.send(action_str.to_sym).to_f
          row[action_str[/^[^_]+/]] = "%8.2f" % val
        end
        table["data"] << row
        report << table
      end
    end
    return report
  end
end
