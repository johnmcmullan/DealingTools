module Instrument
  CALL_CODES = ("A".."L").to_a.unshift(nil)
  PUT_CODES = ("M".."X").to_a.unshift(nil)
  FUT_CODES = [ nil, "F", "G", "H", "J", "K", "M",
                "O", "P", "U", "V", "X", "Z" ]

  # dates to YYYYMMDD format (obviously the DD is superflous)
  def exp_code(date, kind)
    _date = date.delete("-/")
    return "%s%s" % [ _date[5,1], _date[2,2] ] if kind.nil?
    case kind[0,1].upcase
      when "C": return "%s%s" % [ CALL_CODES[_date[4,2].to_i], _date[2,2] ]
      when "P": return "%s%s" % [ PUT_CODES[_date[4,2].to_i], _date[2,2] ]
      when "F": return "%s%s" % [ FUT_CODES[_date[4,2].to_i], _date[2,2] ]
    else
      return "%s%s" % [ _date[5,1], _date[2,2] ]
    end
  end

  # Use American spelling....
  def normalized_3sf(number)
    num = number.to_f

    # adjust strike to be the three most significant digits...
    # if strike is < 100 then use unadjusted strike
    while num >= 1000
      num /= 10
    end
    while (num < 100) && (num > 0)
      num *= 10
    end
    return num.to_s[0,3]
  end

  def instrument_str(underlying, expirydate = nil, kind = nil,
                     strikeprice = 0, expirytype = nil, multiplier = nil)
    
    return underlying if expirydate.nil?

    if strikeprice.to_i == 0
      return "%s.%s" % [ underlying, exp_code(expirydate, kind) ]
    end

    ret = "%s%s.%s" % [ underlying,
                        ("%f" % strikeprice.to_f).gsub(/0+$/, "").delete("."),
                        exp_code(expirydate, kind)
                      ]
    ret += expirytype[0,1] if !expirytype.nil?
    ret += multiplier.to_s if !multiplier.nil?
    return ret
  end
end

