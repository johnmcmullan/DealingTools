require 'getoptlong'
require 'socket'

require 'dt/constants'

class GetoptLong
  #
  # Set options
  #
  def my_set_options(arguments)
    #
    # The method is failed if option processing has already started.
    #
    if @status != STATUS_YET
      raise RuntimeError, 
	"invoke set_options, but option processing has already started"
    end

    #
    # Clear tables of option names and argument flags.
    #
    @canonical_names.clear
    @argument_flags.clear

    arguments.each do |arg|
      #
      # Each argument must be an Array.
      #
      if !arg.is_a?(Array)
	raise ArgumentError, "the option list contains non-Array argument"
      end

      #
      # Find an argument flag and it set to `argument_flag'.
      #
      argument_flag = nil
      arg.each do |i|
	if ARGUMENT_FLAGS.include?(i)
	  if argument_flag != nil
	    raise ArgumentError, "too many argument-flags"
	  end
	  argument_flag = i
	end
      end
      raise ArgumentError, "no argument-flag" if argument_flag == nil

      canonical_name = nil
      arg.each do |i|
	#
	# Check an option name.
	#
	next if i == argument_flag
	begin
	  if !i.is_a?(String) || i !~ /^-([^-]|-.+)$/
	    raise ArgumentError, "an invalid option `#{i}'"
	  end
	  if (@canonical_names.include?(i))
	    raise ArgumentError, "option redefined `#{i}'"
	  end
	rescue
	  @canonical_names.clear
	  @argument_flags.clear
	  raise
	end

	#
	# Register the option (`i') to the `@canonical_names' and 
	# `@canonical_names' Hashes.
	#
	if canonical_name == nil
	  canonical_name = i
	end
	@canonical_names[i] = canonical_name
	@argument_flags[i] = argument_flag
      end
      raise ArgumentError, "no option name" if canonical_name == nil
    end
    return self
  end
end

class Args
  attr_reader :progname, :hostname
  def initialize(*extra_options)
    @options = Hash.new
    @progname = File.basename($0)
    @hostname = Constants::HOSTNAME

    get_options(extra_options)
    
    if (self.help?)
      puts "Usage: #{@options["progname"]}"
      puts "[--orc_server=hostname]"
      puts "[--orc_port=port]"
      puts "--orc_login_id=login_id"
      puts "--orc_password=password"
      puts "[--database=dbname@dbhost]"
      puts "[--logname=path]"
      if !extra_options.nil?
        extra_options.each do
          |option|
          case option.last
          when GetoptLong::NO_ARGUMENT
            puts "#{option.first}"
          when GetoptLong::OPTIONAL_ARGUMENT
            puts "[#{option.first}=value]"
          else
            puts "#{option.first}=value"
          end
        end
      end
      puts "[--debug]"
      puts "[--help]"
      exit
    end
  end

  def get_options(extra_options)
    options = [
               [ "--orc_server", GetoptLong::OPTIONAL_ARGUMENT,
                 Constants::OP_HOST ],
               [ "--orc_port", GetoptLong::OPTIONAL_ARGUMENT,
                 Constants::OP_PORT ],
               [ "--orc_login_id", GetoptLong::REQUIRED_ARGUMENT,
                 Constants::USER ],
               [ "--orc_password", GetoptLong::OPTIONAL_ARGUMENT,
                 Constants::PASSWORD ],
               [ "--database", GetoptLong::OPTIONAL_ARGUMENT,
                 "dt@%s" % Constants::DB_HOST ],
               [ "--logname", GetoptLong::OPTIONAL_ARGUMENT,  @progname],
               [ "--debug", GetoptLong::NO_ARGUMENT, false ],
               [ "--daemon", GetoptLong::NO_ARGUMENT, false ],
               [ "--help", GetoptLong::NO_ARGUMENT, false ],
               [ "--procname", GetoptLong::REQUIRED_ARGUMENT, @progname ]
              ] | extra_options

    options.each do |option|
      key = option.first.sub(/--/, '')
      default = option.pop
      @options[key] = default
    end

    parser = GetoptLong.new

    parser.my_set_options(options)
    parser.each do
      |opt, arg|
      @options[opt.sub(/--/, '')] = arg
    end
  end

  def database
    return @options["database"].split("@")[0]
  end

  def dbhost
    return @options["database"].split("@")[1]
  end

  def dbport
    return Constants::DB_PORT
  end

  def to_s
    result = String.new
    @options.each { |key, val| result << "#{key} = #{val}" }
  end

  def method_missing(symbol)
    option_name = symbol.id2name
    # allow us to call methods like 'debug?'
    option_name.sub!(/\?/, '')
    if @options.has_key?(option_name)
      return @options[option_name]
    end
    super(symbol)
  end
end
