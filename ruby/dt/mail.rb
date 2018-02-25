class Notify
  def initialize(*extra_options)
    @options = Hash.new
    @progname = `basename $0`.chomp!

    get_options(extra_options)
    
    if (self.help?)
      puts "Usage: #{@options["progname"]}"
      puts "[--orc_server=hostname]"
      puts "[--orc_port=port]"
      puts "--orc_login_id=login_id"
      puts "--orc_password=password"
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
               [ "--orc_server", GetoptLong::OPTIONAL_ARGUMENT, "ophost" ],
               [ "--orc_port", GetoptLong::OPTIONAL_ARGUMENT, "6980"],
               [ "--orc_login_id", GetoptLong::REQUIRED_ARGUMENT, "unknown" ],
               [ "--orc_password", GetoptLong::OPTIONAL_ARGUMENT, "" ],
               [ "--logname", GetoptLong::OPTIONAL_ARGUMENT,  @progname],
               [ "--debug", GetoptLong::NO_ARGUMENT, false ],
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
