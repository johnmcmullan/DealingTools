/* ----------------------------------------------------------------------
   Socket - Dealing Tools socket class implementation

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:10:56 $
   $Source: /usr/export/cvsroot/util/c++/main/libdt/Socket.cc,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#include <string>
#include <stdexcept>

#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <fcntl.h>
#include <netdb.h>
#include <errno.h>

#include "StrConv.h"
#include "Socket.h"

namespace dt {

  Socket::Socket(std::string& _hostname, std::string& _service) {
    struct addrinfo hints, *res, *res0;
    int err;

    ::memset(&hints, 0, sizeof(hints));
    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    err = ::getaddrinfo(_hostname.c_str(), _service.c_str(), &hints, &res0);
    if (err != 0)
      throw std::invalid_argument(gai_strerror(err));

    int s = -1;
    std::string cause = "no addresses";
    errno = EADDRNOTAVAIL;
    for (res = res0; res; res = res->ai_next) {
      s = ::socket(res->ai_family, res->ai_socktype, res->ai_protocol);
      if (s < 0) {
	cause = "Socket: ";
	cause += strerror(errno);
	continue;
      }

      if (::connect(s, res->ai_addr, res->ai_addrlen) < 0) {
	cause = "connect: ";
	cause += strerror(errno);
	::close(s);
	s = -1;
	continue;
      }
      break; // Yay... got one!
    }
    if (s < 0)
      throw std::invalid_argument(cause);

    freeaddrinfo(res0);

    socket = s;
  }


  Socket &Socket::TcpClient(std::string& _hostname, std::string& _service) {
    Socket *sockobj = new Socket(_hostname, _service);
    sockobj->SetSynchronous();
    return *sockobj;
  }

  void Socket::SetSynchronous() {
    fcntl(socket, F_SETFL, 0);
  }
  void Socket::SetAsynchronous() {
    fcntl(socket, F_SETFL, O_NONBLOCK);
  }

  Socket::~Socket() {
    ::close(socket);
  }

}

/*
  $log$
*/
