/* ----------------------------------------------------------------------
   Connection - deapi connection interface

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/c++/main/dealingtools/orc/deapi/Connection.h,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#ifndef __DEAPI_CONNECTION
#define __DEAPI_CONNECTION

#include <dt/util.h>

#include <pthread.h>

#include <string>
#include <iosfwd>
#include <fstream>

namespace deapi {

  class Connection {
  private:
    dt::Socket deapi;
    std::ofstream outbuf;
    std::ifstream inbuf;
    pthread_mutex_t inlock;
    pthread_mutex_t outlock;
  public:
    Connection(std::string hostname, std::string service);
    void Send(std::string& msg);
    Message *Recieve(std::string& messageType, std::string& privateId = "");
  };

}

#endif

/*
  $log$
*/
