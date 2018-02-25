require 'date'
require 'socket'

require 'dt/constants'

class DTProcess
  attr_reader :process_id, :start_times, :stop_time, :days, :executable,
  :database, :opuser, :description, :logname, :pids

  def initialize(process_id, start_time, stop_time, days, executable,
                 database, ophost, opuser, description, logname, extra_switches,
                 start_script_path, stop_script_path)
    @process_id = process_id
    @start_times = start_time.split(",")
    @stop_time = stop_time
    @days = days
    @executable = executable
    @database = database
    @ophost = ophost
    @opuser = opuser
    @description = description
    @logname = logname
    @extra_switches = extra_switches
    @start_script_path = start_script_path
    @stop_script_path = stop_script_path
    @pids = Array.new
  end

  def running?
    return !@pids.empty?
  end

  def add_pid(pid)
    @pids.push(pid)
  end

  def reason_not_allowed_to_run
    # check that it's an OK time and day to run
    now = DateTime.now
    if (@start_times.size == 1) && !@stop_time.empty?
      # We need to check the start time
      nowtime = "%02d%02d" % [ now.hour, now.min ]
      if (nowtime.to_i < @start_times.first.to_i) ||
          (nowtime.to_i > @stop_time.to_i)
        return "#{nowtime} is outside #{@start_times.first} - #{@stop_time}"
      end
    end

    if @days =~ /\d\-\d/
      # Range of days, e.g. 1-5
      start_day, end_day = days.split("-")
      if (now.wday < start_day.to_i) || (now.wday > end_day.to_i)
        return "day %d is outside sd - %s" % [ now.wday, start_day, end_day ]
      end
    elsif !@days.empty? && !@days.split(",").include?(now.wday.to_s)
      return "day %d is not in %s" % [ @days ]
    end
    return "OK"
  end

  def kill
    return "Not running" if !running?

    @pids.each { |pid| Process.kill("SIGINT", pid) }
    @pids.clear
    return "Done"
  end

  def command_line
    result = $DT_DIR + "/bin/" + @executable + " --procname=" +
      @process_id + " " + @extra_switches.to_s
    if !@database.empty?
      result += " --database=" + @database
    end
    if !@ophost.empty?
      result += " --orc_server=" + @ophost
      # fixme
      result += " --orc_port=" + Constants::OP_PORT
    end
    if !@opuser.empty?
      result += " --orc_login_id=" + @opuser
      # fixme
      result += " --orc_password=" + Constants::PASSWORD
    end
    if !@logname.empty?
      result += " --logname=" + @logname
    end
    return result + "\n"
  end

  def crontab_string
    entries = ""
    @start_times.each do
      |start_time|
      entries += "%s %s * * %s %s %s\n" %
        [ start_time[2..3], start_time[0..1], @days, @start_script_path,
          @process_id ]
    end
    if !@stop_time.empty?
      entries += "%s %s * *  %s %s %s\n" %
        [ stop_time[2..3], stop_time[0..1], @days, @stop_script_path,
          @process_id ]
    end
    return entries
  end

end

class Config
  include Enumerable

  @@start_script_path = $DT_DIR + "/bin/start_process"
  @@stop_script_path = $DT_DIR + "/bin/stop_process"

  def Config.start_script_path
    return @@start_script_path
  end

  def Config.stop_script_path
    return @@stop_script_path
  end

  def initialize
    conf_filename = $DT_DIR + "/etc/dt.conf"

    @processes = Hash.new
    @hostname = Constants::HOSTNAME

    File.open(conf_filename, "r") do
      |file|
      while !file.eof?
        line = file.gets.chomp
        case line
        when /^ENV:/
          # environment information
          var, value = line.gsub("ENV:", "").split("=")
          var.strip!
          value.strip!
          # environment variable substitutions
          match_key = "%#{var}%"
          original_value = ENV[var]
#          ENV[var] = value.gsub(match_key, original_value)
          ENV[var] = value
        when /^PROC:/
          # process information
          type, hostname, process_id, start_time, stop_time, days, executable,
          database, ophost, opuser, description, logname,
          extra_switches = line.split(":")
          if hostname == @hostname
            process = DTProcess.new(process_id, start_time, stop_time, days,
                                     executable, database, ophost, opuser,
                                     description, logname, extra_switches,
                                     @@start_script_path, @@stop_script_path)
            @processes[process_id] = process
          end
        end
      end
    end

    IO.popen("ps -axww | grep procname | egrep -v grep", "r") do
      |io|
      while !io.eof?
        line = io.gets
        next if line.empty?
        args = line.split
        pid = args.shift
        args.each do
          |arg|
          var, val = arg.split("=")
          if (var == "--procname") && (@processes.has_key?(val))
            @processes[val].add_pid(pid.to_i)
          end
        end
      end
    end
  end

  def process(process_id)
    return @processes[process_id]
  end

  def each(&block)
    @processes.each { |process_id, process| block.call(process_id, process) }
  end

  def each_value(&block)
    @processes.each_value { |process| block.call(process) }
  end
end
