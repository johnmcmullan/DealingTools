require 'date'

class Float
  def round_to(x)
    (self * 10**x).round.to_f / 10**x
  end

  def ceil_to(x)
    (self * 10**x).ceil.to_f / 10**x
  end

  def floor_to(x)
    (self * 10**x).floor.to_f / 10**x
  end

  def dt_round
    self.round_to(Constants::FLOAT_SIGNIFICANT_FIGURES)
  end
end

class Hash
  def to_s
    result = ""
    self.each do |key, val|
      result << "#{key}="
      if val.kind_of?(Hash)
        result << "{#{val}}"
      else
        result << val.to_s
      end
      result << "|"
    end
    result.chop!
    return result
  end
end

class DateTime
  def expired?
    tmp = DateTime.now
    self < DateTime.new(tmp.year, tmp.mon, tmp.day,
                        Constants::EXPIRY_HOUR, Constants::EXPIRY_MINUTE)
  end
end

class Time
  def expired?
    self < Time.now
  end
end
