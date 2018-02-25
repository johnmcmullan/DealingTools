class OPError < RuntimeError
  attr_reader :error
  def initialize(error)
    @error = error
  end
end

class OPStaticDataError < RuntimeError
end
