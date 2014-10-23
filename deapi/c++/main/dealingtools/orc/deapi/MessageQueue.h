/* ----------------------------------------------------------------------
   MessageQueue - Dealing Tools message queue definition

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 09:08:12 $
   $Source: /usr/export/cvsroot/deapi/c++/main/dealingtools/orc/deapi/MessageQueue.h,v $
   $Revision: 1.1 $

   ---------------------------------------------------------------------- */

#ifndef __DEAPI_MESSAGEQUEUE
#define __DEAPI_MESSAGEQUEUE

#include <string>
#include <hash_map>
#include <queue>

#include "Connection.h"
#include "MessageCallback.h"

namespace deapi {

  typedef std::queue<Message *> MsgQueue;
  typedef std::hash_map<std::string, MsgQueue *> MsgQueues;
  typedef std::hash_map<std::string, MessageCallback *> MsgCallbacks;

  class MessageQueue {
  private:
    Connection deapi;
    MsgQueues messageQueues;
    MsgCallbacks messageCallbacks;
    pthread_mutex_t lock;
    pthread_cond_t go;
  protected:
    void Initialize();
    void Login(const std::string& loginid, const std::string& password,
	       bool debug);
    void Logout();
    static void Run(void *closure);
  public:
    MessageQueue(const std::string& hostname, const std::string& service,
		 const std::string& loginid, const std::string& password,
		 bool debug);
    MessageQueue(const std::string& hostname, const std::string& service,
		 const std::string& loginid, const std::string& password);
    void RegisterCallback(const std::string& messageType,
			  const std::string& privateId,
			  MessageCallback *callback);
    void RegisterCallback(const std::string& messageType,
			  MessageCallback *callback);
    void RemoveCallback(const std::string& messageType,
			const std::string privateId);
    void RemoveCallback(const std::string& messageType);
    Message *Receive(const std::string& messageType,
		     const std::string& privateId);
    Message *Receive(const std::string& messageType);
    void Send(const std::string& message);
    void Send(const std::string& messageType, const std::string& message);
    void Send(const std::string& messageType, const std::string& privateId,
	      const std::string& message);
    ~MessageQueue();
  }
}

/*
  $log$
*/
