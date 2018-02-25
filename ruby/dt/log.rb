require 'thread'
require 'monitor'

require 'dt/constants'

class Log
  @@max_file_size = 1024*1024*25 # 25MB
  @@default_log_dir = "#{$DT_DIR}/log"
  @@log_suffix = "log"
  @@months = Array.new(["Jan", "Feb", "Mar", "Apr", "May", "Jun",
		  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"])

  attr_accessor :debug_on

  def initialize(log_name = nil, hostname = nil, debug_on = false)
    @hostname = hostname || Constants::HOSTNAME
    @debug_on = debug_on
    @lock = Monitor.new

    if log_name.nil?
      @log_name = ""
      @log_dir = @@default_log_dir
      @log_file = nil
      @debug_on && puts("There is no log file")
    else
      @log_name = File.basename(log_name.to_s)
      if log_name[0, 1] == "/"
        @log_dir = File.dirname(log_name)
        @log_name = File.basename(log_name)
      else
        @log_dir = @@default_log_dir
        @log_name = log_name
      end
      @log_file = "%s/%s.%s" % [ @log_dir, @log_name, @@log_suffix ]
      @debug_on && puts("Log file = #{@log_file}")
    end

    open

    return self
  end

  def open
    if !@log_name.empty?
      if !File.directory?(@log_dir)
        @debug_on && puts("Creating new log directory: #{@log_dir}")
        Dir.mkdir(@log_dir, 0775)
      end

      log = File.new(@log_file, File::CREAT|File::APPEND|File::RDWR, 0644)
      STDOUT.reopen(log)
      STDERR.reopen(STDOUT)
      STDOUT.sync = true
      puts("-- NEW LOG --------------------------------------------------")
    end
  end

  def debug(message)
    write(message, "debug") if @debug_on
  end

  def warn(message)
    write(message, "warning")
  end

  def error(message)
    write(message, "error")
  end

  def close
    if !@log_name.nil?
      puts("-- END OF LOG --------------------------------------------------")
    end
  end

  def write(message, level = "info")
    @lock.synchronize do
      # Check log file size
      if !@log_name.empty?
        log_file = @log_file
        if File.size(log_file) > @@max_file_size
          old_log_file = log_file + "-old"
          puts("rotating logfile #{log_file} to #{old_log_file}")
          close
          File.rename(log_file, old_log_file)
          open
        end
      end

      time = Time.new
      puts "%s %s %s[%d]/%s %s\n" %
        [ time.strftime("%b %d %H:%M:%S"), @hostname, @log_name, $$,
          level, message ]
    end
  end

  alias info write

end

