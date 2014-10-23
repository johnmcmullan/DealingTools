/* ----------------------------------------------------------------------
   MessageQueue - Dealing Tools message callback definition

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/c++/main/dealingtools/orc/deapi/MessageCallback.h,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#ifndef __DEAPI_MESSAGECALLBACK
#define __DEAPI_MESSAGECALLBACK

namespace deapi {

  class MessageCallback {
    MessageCallback() {};
    virtual void OnMsg(Message *message) = 0;
    virtual void OnError(int error, std::string errorDescription) = 0;
    virtual ~MessageCallback() = 0;
  }

}

/*
  $log$
*/
