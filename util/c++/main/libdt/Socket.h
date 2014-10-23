/* ----------------------------------------------------------------------
   Socket - Dealing Tools socket class definition

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:10:56 $
   $Source: /usr/export/cvsroot/util/c++/main/libdt/Socket.h,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#ifndef __DT_SOCKET
#define __DT_SOCKET

#include <string>

namespace dt {

  class Socket {
  protected:
    std::string hostname;
    std::string service;
    int socket;
    Socket(std::string& _hostname, std::string& _service);
  public:
    static Socket& TcpClient(std::string& _hostname, std::string& _service);
    void SetSynchronous();
    void SetAsynchronous();
    virtual ~Socket();
  };

}

#endif

/*
  $log$
*/
