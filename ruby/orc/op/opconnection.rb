require 'socket'
require 'stringio'
require 'benchmark'
require 'io/nonblock'

require 'inline'

require 'orc/op/operror'

module OPLogin
  def login(login_id, password, debug)
    msg = "login_id = " << login_id << "| password = " << password
    msg << "| debug = " << (debug ? "yes" : "no")
    send("login", msg)
    reply = recv()

    if reply.error != "0"
      raise OPError.new(reply.error.to_i), reply.error_description, caller
    end
  end

  def logout
    send("logout")
  end
end

class OPPureConnection
  include OPLogin

  def initialize(login_id, password, server = "localhost", debug = false,
                 service = "6980")
    @server = server
    @service = service
    @socket = TCPSocket.new(@server, @service)
    @socket.sync = true
    login(login_id, password, debug)
  end

  def recv_nonblock
    begin
      @socket.nonblock { len = @socket.read(10) }
      return OPMessage.new(@socket)
    rescue Errno::EAGAIN
      return nil
    end
  end

  def recv
    @socket.read(10)
    #len = @socket.read(10).to_i
    #msg = OPMessage.new(StringIO.new(@socket.read(len)))
    OPMessage.new(@socket)
  end

  def send(type, msg = nil, private = nil)
    out = "{ message_info = { message_type = " << type
    if !private.nil?
      out << "| private = " << private
    end
    out << "} "
    if !msg.nil?
      out << "|" << msg.to_s
    end
    out << "}"

    @socket.print("%010.10d" % out.length)
    @socket.print(out)
  end
end

class OPInlineConnection
  include OPLogin
  include Benchmark

  def initialize(login_id, password, server = "localhost", debug = false,
                 service = "6980")
    @server = server
    @service = service
    @socket = new_client_socket(@server, @service)
    @infd = std_file_open(@socket, "r")
    @outfd = std_file_open(@socket, "w")
    login(login_id, password, debug)
  end

  inline do
    |builder|
    builder.add_link_flags("-lruby") if `uname -s`.chomp == "Darwin"
    builder.include("<netdb.h>")
    builder.include("<sys/types.h>")
    builder.include("<sys/socket.h>")
    builder.include("<netinet/in.h>")
    builder.include("<stdio.h>")
    builder.include("<stdlib.h>")
    builder.include("<string.h>")
    builder.include("<sys/errno.h>")
    builder.c %q{
      int new_client_socket(VALUE hostname, VALUE service) {
        struct sockaddr_in sa;
        struct hostent *hp;
        struct servent _sp;
        struct servent *sp = &_sp;
        int err;
        int port;
        int s;

        /* get our host details */
        err = 0;
        hp = gethostbyname(StringValuePtr(hostname));
        if (h_errno < 0) {
          rb_fatal("Internal error [gethostbyname]");
          return -1;
        } else if (err > 0) {
          errno = h_errno;
          rb_sys_fail("gethostbyname");
          return -1;
        }
        /* and service port number */
        /* check that we haven\'t actually been given a port number on
           the command line... */
        port = atoi(StringValuePtr(service));
        if (port == 0) {
          /* Nope... it had better be a portname then I guess */
          sp = getservbyname(StringValuePtr(service), 0);
          if (sp->s_port == 0) {
            rb_sys_fail("getservbyname");
            return -1;
          }
        } else {
          sp->s_port = port;
        }

        memset(&sa, 0, sizeof(sa));
        memcpy((char *) &sa.sin_addr, hp->h_addr, hp->h_length);
        sa.sin_family = hp->h_addrtype;
        sa.sin_port = htons((u_short) sp->s_port);

        if ((s = socket(hp->h_addrtype, SOCK_STREAM, 0)) < 0) {
          rb_sys_fail("socket");
          return -1;
        }
        if (connect(s, (struct sockaddr *) &sa, sizeof(sa)) < 0) {
          rb_sys_fail("connect");
          close(s);
          return -1;
        }

        /* we're in! */
        return s;
      }
    }
  end

  inline do
    |builder|
    builder.add_link_flags("-lruby") if `uname -s`.chomp == "Darwin"
    builder.include("<stdio.h>")
    builder.c %q{
      VALUE std_file_open(int fd, VALUE mode) {
        VALUE obj = Data_Wrap_Struct(rb_cObject, 0, 0,
                                     fdopen(fd, StringValuePtr(mode)));
        return obj;
      }
    }
  end

  inline do
    |builder|
    builder.add_link_flags("-lruby") if `uname -s`.chomp == "Darwin"
    builder.include("<stdio.h>")
    builder.c_raw %q{
      void msg_send(int argc, VALUE *argv, VALUE self) {
        FILE *outfd;
        Data_Get_Struct(argv[0], FILE, outfd);
        VALUE buf = argv[1];
        fprintf(outfd, "%010.10d%s", RSTRING(buf)->len,
                StringValuePtr(buf));
        fflush(outfd);
      }    
    }
  end

  inline do
    |builder|
    builder.add_link_flags("-lruby") if `uname -s`.chomp == "Darwin"
    builder.include("<stdio.h>")
    builder.c_raw %q{
      VALUE msg_receive(int argc, VALUE *argv, VALUE self) {
        FILE *infd;
        Data_Get_Struct(argv[0], FILE, infd);
        unsigned long len = NUM2UINT(argv[1]);
        char *buf = ALLOCA_N(char, len + 1);
        if (fgets(buf, len + 1, infd) == 0)
          return Qnil;
        return rb_str_new(buf, len);
      }
    }
  end

  inline do
    |builder|
    builder.add_link_flags("-lruby") if `uname -s`.chomp == "Darwin"
    builder.include("<fcntl.h>")
    builder.c %q{
      void setnonblock(int fd) {
        int flags;
#ifdef F_GETFL
        flags = fcntl(fd, F_GETFL);
        if (flags == -1) {
          rb_sys_fail("setnonblock: F_GETFL");
        }
#else
        flags = 0;
#endif
        if ((flags & O_NONBLOCK) == 0) {
          flags |= O_NONBLOCK;
          if (fcntl(fd, F_SETFL, flags) == -1) {
              rb_sys_fail("setnonblock: F_SETFL");
          }
        }
      }
    }     
  end

  inline do
    |builder|
    builder.add_link_flags("-lruby") if `uname -s`.chomp == "Darwin"
    builder.include("<fcntl.h>")
    builder.c %q{
      void setblock(int fd) {
        int flags;
#ifdef F_GETFL
        flags = fcntl(fd, F_GETFL);
        if (flags == -1) {
          rb_sys_fail("setblock: F_GETFL");
        }
#else
        flags = 0;
#endif
        if ((flags & O_NONBLOCK) != 0) {
          flags &= ~O_NONBLOCK;
          if (fcntl(fd, F_SETFL, flags) == -1) {
              rb_sys_fail("setblock: F_SETFL");
          }
        }
      }
    }     
  end

  def recv_nonblock
    begin
      setnonblock(@socket)
      buf = msg_receive(@infd, 10)
      setblock(@socket)
      return nil if buf.nil?
      msg = OPMessage.new()
      msg.socketparse(@infd)
      msg
    end
  end

  def recv
    buf = msg_receive(@infd, 10)
    msg = OPMessage.new()
    msg.socketparse(@infd)
    msg
  end

  def recv_raw
    len = msg_receive(@infd, 10)
    msg_receive(@infd, len.to_i)
  end

  def send(type, msg = nil, private = nil)
    out = "{ message_info = { message_type = " << type
    if !private.nil?
      out << "| private = " << private
    end
    out << "} "
    if !msg.nil?
      out << "|" << msg.to_s
    end
    out << "}"

    msg_send(@outfd, out)
  end
end
