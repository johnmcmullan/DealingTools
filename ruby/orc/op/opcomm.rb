require 'orc/op/opconnection'
require 'orc/op/operror'

class OPComm < OPInlineConnection
  def initialize(login_id, password, server = "localhost", debug = false,
                 service = "6980")
    @@private = 1
    @msg_queues = Hash.new { |h,k| h[k] = Array.new }
    @msg_callbacks = Hash.new
    super(login_id, password, server, debug, service)
  end

  def process_messages
    begin
      loop do
        msg = recv_nonblock()
        return if msg.nil?
        if !msg.reply_to.nil?
          message_info = msg.reply_to
        else
          message_info = msg.message_info
        end

        private = message_info.private
        if !private.nil?
          # The special private id 0 means... I don't care about a reply!
          next if private == "0"
          callback = @msg_callbacks[private]
          if !callback.nil?
            callback.call(msg)
            next
          end
        end
        type = message_info.message_type
        msg_queue = @msg_queues[type] << msg
      end
    rescue Exception => e
      $log.write(e)
      $log.debug(e.backtrace.join("\n"))
    end
  end

  def send_with_block(type, msg, &block)
    # wrap the block up in something to delete the callback
    key = @@private.to_s
    @msg_callbacks[key] = Proc.new do
      |msg|
      private = key
      block.call(msg)
      @msg_callbacks.delete(private)
    end
    @@private += 1
    send(type, msg, key)
  end

  def recv_from_queue(type = "[none]")
    process_messages
    msg_queue = @msg_queues[type]
    msg = nil
    while (msg = msg_queue.shift).nil?
      sleep 0.02
      process_messages
    end
    # strip the reply dictionary out
    msg.delete("reply_to")
    return msg
  end

  def register_callback(&block)
    private = ""
    private = @@private.to_s
    @msg_callbacks[private] = block
    @@private += 1
    return private
  end

  def deregister_callback(private)
    @msg_callbacks.delete(private)
  end

  def method_missing(id, message = nil, &block)
    message_type = id.id2name

    if block.nil?
      send(message_type, message)
      reply = recv_from_queue(message_type)
      error = reply.error.to_i
      if error == 0
        # strip the error info out
        reply.delete("error")
        reply.delete("error_description")
      elsif error < 2000
        raise OPError.new(reply.error), reply.error_description, caller
      end
      return reply
    else
      # pointless trying to throw from here as the stack trace will
      # point back to us and not the caller...
      send_with_block(message_type, message) { |reply| block.call(reply) }
    end
  end

end
