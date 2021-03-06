require 'date'

require 'inline'

require 'dt/base'
require 'orc/op/operror'

class String
  def to_key
    self.downcase.tr(' ', '_')
  end
end

class Array
  def to_opstr(key)
    return nil if empty?
    item_key = key.gsub(/ies$/, 'y')
    item_key = item_key.gsub(/s$/, '')
    items = Array.new
    i = 1
    self.each { |item| items << "#{item_key}#{i}=#{item}"; i += 1}
    return "#{key}={#{items.join("|")}}"
  end
end

class Time
  def orcdate
    t = dup.utc
    "%04d-%02d-%02d" % [ t.year, t.mon, t.day ]
  end
end

class OPMessage
  include Enumerable

  def initialize(val = nil)
    if val.nil?
      @data = Hash.new
      return
    end

    if val.kind_of?(Hash)
      @data = val
    elsif val.kind_of?(OPMessage)
      @data = val.data
    else
      @data = Hash.new
      if val.kind_of?(IO) || val.kind_of?(StringIO)
        parse(val)
      end
    end
  end
  
  inline do
    |builder|
    builder.add_link_flags("-lruby") if `uname -s`.chomp == "Darwin"
    builder.prefix("#define add_to_buf(c) if ((buf == 0) || (pos == (bufsize - 1))) { \
          bufsize += extra; \
          buf = realloc(buf, bufsize); \
          key = realloc(key, bufsize); \
        } \
        buf[pos++] = c")
    builder.include("<stdio.h>")
    builder.include("<stdlib.h>")
    builder.c_raw %q{
      void socketparse(int argc, VALUE *argv, VALUE self) {
        VALUE msg, hash;
        size_t bufsize, pos;
        const size_t extra = 64;
        short looking_for_key;
        char *key, *buf;
        char c;

        FILE *file;
        Data_Get_Struct(argv[0], FILE, file);
        hash = rb_iv_get(self, "@data");

        bufsize = pos = 0;
        buf = key = 0;
        looking_for_key = 1;
        while ((c = getc(file)) != EOF) {
          switch (c) {
          case '{':
            if (key != 0) {
              /* not initial opening brace */
              msg = rb_class_new_instance(0, 0,
                                          rb_const_get(rb_cObject,
                                                       rb_intern("OPMessage")));
              socketparse(1, argv, msg);
              rb_hash_aset(hash, rb_str_new2(key), msg);
            }
	    looking_for_key = 1;
	    pos = 0;
	    break;
          case '|':
	    if (pos != 0) {
	      buf[pos] = 0;
              rb_hash_aset(hash, rb_str_new2(key),
                           rb_funcall(rb_str_new(buf, pos),
                                      rb_intern("strip"), 0));
	    }
	    looking_for_key = 1;
	    pos = 0;
	    break;
          case '=':
	    if (looking_for_key) {
	      buf[pos] = 0;
	      memcpy(key, buf, pos + 1);
	      looking_for_key = 0;
	      pos = 0;
	    } else {
	      add_to_buf(c);
	    }
	    break;
          case '}':
            if (pos != 0) {
              buf[pos] = 0;
              rb_hash_aset(hash, rb_str_new2(key),
                           rb_funcall(rb_str_new(buf, pos),
                                      rb_intern("strip"), 0));
            }
            free(buf);
            free(key);
 	    return;
          case ' ':
	    if (looking_for_key)
	      /* ignore whitespace */
	      break;
	    /* fall through to default */
          default:
	    add_to_buf(c);
          }
        }
      }
    }
  end

  def parse(stream)
    looking_for_key = true
    key = buf = nil
    stream.each_byte do
      |c|
      c = "%c" % c
      case c
      when '{'
        if key != nil
          # not initial opening brace
          @data[key] = OPMessage.new(stream)
          key = nil
        end
        looking_for_key = true

      when '|'
        if key != nil
          @data[key] = buf.strip
          key = buf = nil
        end
        looking_for_key = true

      when '='
        if looking_for_key
          key = buf.strip
          buf = nil
          looking_for_key = false
        else
          buf << c
        end

      when '}'
        if key != nil
          @data[key] = buf.strip
        end
        return

      when ' '
        if !looking_for_key
          buf << c
        end

      else
        if buf == nil
          buf = c
        else
          buf << c
        end
      end
    end
  end

  def OPMessage.enum_to_key(enum)
    enum.downcase.tr(' ', '_')
  end

  def merge!(msg)
    return if msg.nil?
    msg.each do
      |key, value|
      if !@data.has_key?(key)
        @data[key] = value
      else
        data = @data[key]
        if data.kind_of?(OPMessage)
          @data[key].merge!(value)
        end
      end
    end
  end

  def add!(msg, recursive = false)
    return if msg.nil?
    msg.each do
      |key, value|
      if !@data.has_key?(key)
        @data[key] = value
      else
        data = @data[key]
        if data.kind_of?(OPMessage)
          @data[key].add!(value) if recursive
        else
          begin
            v = Float(value)
            begin
              v += Float(@data[key])
            rescue TypeError => e
            end
            @data[key] = v.to_s
          rescue ArgumentError => e
            @data[key] = value
          end
        end
      end
    end
  end

  def diff(msg, recurse = true)
    return @msg if msg.nil?
    reply = OPMessage.new
    msg.each do
      |key, value|
      if !@data.has_key?(key)
        if !value.kind_of?(OPMessage)
          v = value.to_f
          if (v != 0.0) && v.finite?
            reply.send(key.to_sym, v.to_s)
          else
            reply.send(key.to_sym, v)
          end
        end
      else
        data = @data[key]
        if data.kind_of?(OPMessage)
          reply.send(key.to_sym, data.diff(value)) if recurse
        else
          begin
            v = Float(value)
            begin
              v -= Float(data)
            rescue TypeError => e
              v *= -1
            end
            reply.send(key.to_sym, v.to_s) if v != 0.0
          rescue ArgumentError => e
          end
        end
      end
    end
    return reply
  end

  def traverse(&block)
    self.each do
      |key, value|
      if value.kind_of?(OPMessage)
        value.traverse { |key, value| block.call(key, value) }
      else
        block.call(key, value)
      end
    end
  end

  def snip(key)
    new_data = Hash.new(@data)
    new_data.delete(key)
    return OPMessage.new(new_data)
  end

  def each(&block)
    @data.each { |key, value| block.call(key, value) }
  end

  def each_value(&block)
    @data.each_value { |value| block.call(value) }
  end

  def each_key(&block)
    @data.each_key { |key| block.call(key) }
  end

  def has_key?(key)
    return @data.has_key?(key)
  end

  def empty?
    return @data.empty?
  end

  def to_s
    result = ""
    @data.each do |key, val|
      result << key << "="
      if val.kind_of?(OPMessage)
        result << "{" << val.to_s << "}"
      else
        result << val.to_s
      end
      result << "|"
    end

    result.chop!
    return result
  end

  def to_csvheader
    result = ""
    @data.each do
      |key, value|
      if value.kind_of?(OPMessage)
        result << value.to_csvheader << ","
      else
        result << key << ","
      end
    end
    result.chop!
    return result
  end

  def to_csv
    result = ""
    @data.each_value do
      |value|
      if value.kind_of?(OPMessage)
        result << value.to_csv << ","
      else
        result << value << ","
      end
    end
    result.chop!
    return result
  end

  def method_missing(symbol, *val)
    str = symbol.id2name.delete("=")
    if val.size > 0
      @data[str] = val[0]
    end
    return @data[str]
  end

end
