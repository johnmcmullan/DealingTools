/* ----------------------------------------------------------------------
   Connection - deapi connection implementation

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 09:08:12 $
   $Source: /usr/export/cvsroot/deapi/c++/main/dealingtools/orc/deapi/Connection.cc,v $
   $Revision: 1.1 $

   ---------------------------------------------------------------------- */

static char rcsid[] = "$Id: Connection.cc,v 1.1 2004/06/03 09:08:12 john Exp $";

#include <dt/util.h>

#include <pthread.h>

#include <string>
#include <iosfwd>
#include <fstream>

#include "Connection.h"

namespace deapi {

  static const LenSize = 10;

  Connection::Connection(std::string& hostname, std::string& service)
    : deapi(hostname, service), outbuf(deapi), inbuf(deapi) {
    pthread_mutex_init(&inlock, NULL);
    pthread_mutex_init(&outlock, NULL);
 }

  void Connection::Send(std::string& msg) {
    pthread_mutex_lock(&outlock);

    outbuf << std::setfill('0')
	   << std::setw(LenSize)
	   << msg.length()
	   << msg
	   << std::flush;

    pthread_mutex_unlock(&outlock);
  }

  // note: you are responsible for deleteing the message returned
  Message *Recieve(std::string& messageType, std::string& privateId) {
    pthread_mutex_lock(&inlock);

    inbuf.ignore(LenSize);  // skip over the header
    Message *reply = new Message(inbuf);
    // skip over stupid, undocumented '\n'
    inbuf.ignore();

    pthread_mutex_unlock(&inlock);

    return reply;
  }

}

#endif

/*
  $log$
*/
