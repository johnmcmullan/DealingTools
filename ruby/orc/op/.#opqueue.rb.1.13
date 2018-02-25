require 'thread'
require 'monitor'

require 'orc/op/opconnection'
require 'orc/op/operror'

class OPQueue < OPPureConnection
  attr_accessor :process_callback_events_from_io_thread

  @@private = 1

  def initialize(login_id, password, server = "localhost", debug = false,
                 service = "6980")
    @io_thread = nil
    @msg_queues = Hash.new() { |h,k| h[k] = SizedQueue.new(128) }
    @msg_event_queue = Array.new
    @lock = Monitor.new
    @msg_callbacks = Hash.new
    @process_callback_events_from_io_thread = true
    super(login_id, password, server, debug, service)

    Thread.abort_on_exception = true
    start
  end

  def start
    @io_thread = Thread.new do
      loop do
        begin
          msg = recv()
          if !msg.reply_to.nil?
            message_info = msg.reply_to
          else
            message_info = msg.message_info
          end

          private = message_info.private
          if !private.nil?
            # The special private id 0 means... I don't care about a reply!
            next if private == "0"
            callback = nil
            @lock.synchronize { callback = @msg_callbacks[private] }
            if !callback.nil?
              if @process_callback_events_from_io_thread
                callback.call(msg)
              else
                @msg_event_queue << msg
              end
              next
            end
          end
          type = message_info.message_type
          msg_queue = nil
          @lock.synchronize { msg_queue = @msg_queues[type] }
          msg_queue.enq(msg)

        rescue RuntimeError => e
          $log.write(e.to_s)
        end
      end
    end
  end

  def join_io_thread
    @io_thread.join
  end

  # if process_callback_events_in_io_thread is false we need to call this
  # from someone else's event loop to make sure that callbacks get called
  def process_messages
    msg = nil
    @lock.synchronize { msg = @msg_event_queue.shift }
    while !msg.nil?
      if !msg.reply_to.nil?
        message_info = msg.reply_to
      else
        message_info = msg.message_info
      end
      private = message_info.private
      begin
        if @msg_callbacks[private].nil?
          $log.debug("no callback registered for #{private}") unless $log.nil?
        else
          @msg_callbacks[private].call(msg)
        end
      rescue Exception => e
        $log.write(e.to_s)
        $log.debug(e.backtrace.join("\n"))
      end
      @lock.synchronize { msg = @msg_event_queue.shift }
    end
  end

  def send_with_block(type, msg, &block)
    # wrap the block up in something to delete the callback
    key = nil
    @lock.synchronize do
      key = @@private.to_s
      @msg_callbacks[key] = Proc.new do
        |msg|
        private = key
        block.call(msg)
        @msg_callbacks.delete(private)
      end
      @@private += 1
    end
    send(type, msg, key)
  end

  def recv_from_queue(type = "[none]")
    msg_queue = nil
    @lock.synchronize { msg_queue = @msg_queues[type] }
    msg = msg_queue.deq
    # strip the reply dictionary out
    msg.delete("reply_to")
    return msg
  end

  def register_callback(&block)
    private = ""
    @lock.synchronize do
      private = @@private.to_s
      @msg_callbacks[private] = block
      @@private += 1
    end
    return private
  end

  def deregister_callback(private)
    @lock.synchronize { @msg_callbacks.delete(private) }
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
