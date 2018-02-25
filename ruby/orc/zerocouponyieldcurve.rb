require 'set'
require 'date'

require 'gsl'
require 'postgres'

require 'orc/op'
require 'orc/op/opinstrument'
require 'orc/pricecache'
require 'dt/base'

class ReutersCode
  def initialize(code)
    @code = code
  end

  def to_days
    return 1 if code == "TN"
    days = 0.0
    prev = "0"
    code.split(//).each do
      |c|
      if c.to_i == 0
        case c
        when "W": days += prev.to_i * 7
        when "M": days += (prev.to_i * (365/12))
        when "Y": days += prev.to_i * 365.25
        else c = prev + c
        end
        prev = "0"
      end
      prev += c
    end
    return days.to_i
  end
end

class ZeroCouponYieldCurve
  attr_accessor :price_range, :vol_range

  def initialize(@orc, yield_curve_name)
    @orc = orc
    @name = yield_curve_name
  end
end
