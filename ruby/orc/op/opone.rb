require 'singleton'

require 'orc/op/operror'
require 'orc/op/opqueue'

class OPOne
  attr_accessor :orc
  include Singleton

  def OPOne.start(login_id, password, server = "localhost",
                  debug = false, service = "6980")
    @orc = OPQueue.new(login_id, password, server, debug, service)
  end
end
