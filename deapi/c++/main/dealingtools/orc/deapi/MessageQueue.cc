/* ----------------------------------------------------------------------
   MessageQueue - deapi message queue implementation

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 09:08:12 $
   $Source: /usr/export/cvsroot/deapi/c++/main/dealingtools/orc/deapi/MessageQueue.cc,v $
   $Revision: 1.1 $

   ---------------------------------------------------------------------- */

static char rcsid[] = "$Id: MessageQueue.cc,v 1.1 2004/06/03 09:08:12 john Exp $";

#include <dt/util.h>

#include <pthread.h>

#include <string>
#include <hash_map>
#include <queue>

#include "Message.h"
#include "MessageQueue.h"
#include "Exception.h"
#include "Connection.h"

static const char *module = "MessageQueue";

using namespace dt;
using namespace std;

namespace deapi {

  MessageQueue::MessageQueue(const string& hostname,
			     const string& service,
			     const string& password,
			     bool debug)
    : deapi(hostname, service) {
    Initialize();
    Login(loginid, password, debug);
  }

  MessageQueue::MessageQueue(const string& hostname,
			     const string& service,
			     const string& loginid,
			     const string& password)
    : deapi(hostname, service) {
    Initialize();
    Login(loginid, password, false);
  }

  void MessageQueue::Initialize() {
    pthread_t tid;
    pthread_attr_t attr;
    pthread_attr_init(&attr);

    pthread_mutex_init(&lock, 0);
    pthread_mutex_init(&goLock, 0);
    pthread_cond_init(&go, 0);

    // kick off our recieve thread
    if ((int ret =
	 pthread_create(&tid, &attr, MessageQueue::Run, this)) != 0) {
      switch (ret) {
      case EAGAIN:
	throw Exception(ERR_SYSERR, strerror(ret));
	break;
      case EINVAL:
	throw Exception(ERR_SYSERR, strerror(ret));
	break;
      }
    }
  }

  void MessageQueue::Login(const string& loginid,
			   const string& password,
			   bool debug) {
    string loginmsg = "{message_info={message_type=login}|login_id=";
    loginmsg += loginId + "|password=";
    loginmsg += password;
    if (debug) {
      loginmsg += "|debug=on";
    }
    loginmsg += "}";

    Send(loginmsg);
    Recieve("login");
  }

  void MessageQueue::Logout() {
    try {
      Send("{message_info={message_type=logout}}");
    } catch (...) {
      // I can't think why I need to worry...
    }
  }

  void MessageQueue::Run(void *closure) {
    MessageQueue *self = static_cast<MessageQueue *>(closure);
    Log *log = Log::Instance();

    while (true) {
      try {
	Message *reply = self->deapi.recieve();
	string id =
	  MessageQueue::MessageId(reply.Type(), reply.PrivateId());
      } catch (...) {
	log->Debug(module, "returning from Run() thread\n");
	pthread_exit();
      }
      
      pthread_mutex_lock(&lock);
      MessageCallback *callback = messageCallbacks.find(id);
      if (callback != NULL) {
	// there was a callback registered
	try {
	  if ((int err = reply->Error()) != 0) {
	    callback->OnError(err, reply->ErrorDescription());
	  } else {
	    callback->OnMessage(reply);
	  }
	} catch (...) {
	  log->Warning(module, "Unable to get error status from message\n");
	}
      } else {
	// no callback, stick it in the queue and give that a poke
	MsgQueue *msgQueue = messageQueues.find(id);
	if (msgQueue = NULL) {
	  msgQueue = new MsgQueue();
	  messageQueues[id] = msgQueue;
	}
	
	// poke
	msgQueue.push(reply);
	pthread_cond_signal(&go);
      }
      pthread_mutex_unlock(&goLock);
    }
  }

  void MessageQueue::RegisterCallback(const string& messageType,
				      const string& privateId,
				      MessageCallback *callback) {
    string id = MessageQueue::MessageId(messageType, privateId);
    pthread_mutex_lock(&lock);
    MessageCallback *old = messageCallbacks.find(id);
	if old
      delete old;
    messageCallbacks[id] = callback;
    pthread_mutex_unlock(&lock);
  }
  void MessageQueue::RegisterCallback(string& messageType,
				      MessageCallback *callback) {
    RegisterCallback(messageType, "", callback);
  }

  void MessageQueue::RemoveCallback(string& messageType,
				    string& privateId) {
    string id = MessageQueue::MessageId(messageType, privateId);
    pthread_mutex_lock(&lock);
    MessageCallback *callback = messageCallbacks.find(id);
    if callback {
      delete callback;
      messageCallbacks.erase(id);
    }
    pthread_mutex_unlock(&lock);
  }
  void MessageQueue::RemoveCallback(string& messageType) {
    RemoveCallback(messageType, "");
  }

  Message *MessageQueue::Receive(string& messageType,
				 string& privateId) {
    string id = MessageQueue::MessageId(messageType, privateId);
    while (true) {
      pthread_mutex_lock(&lock);
      MsqQueue *msgQueue = messageQueues.find(id);
      if ((msgQueue != NULL) && (!msgQueue.empty())) {
	Message *reply = msgQueue.pop();
	pthread_mutex_unlock(&lock);
	if (reply.Error() != 0) {
	  pthread_mutex_unlock(&lock);
	  throw Exception(reply.Error(), reply.ErrorDescription);
	}
	pthread_mutex_unlock(&lock);
	return reply.Clean();
      }
      // hmmm... wait for a new message on the queue and check that
      pthread_cond_wait(&go, &lock);
      // implicit unlock()
    }
  }
  Message *MessageQueue::Receive(string& messageType) {
    return Recieve(messageType, "");
  }

  void MessageQueue::Send(string& message) {
    try {
      deapi.Send(message);
    } catch (...) {
      throw Exception(ERR_SYSERR, "Unable to send message to Orc");
    }
  }
  void MessageQueue::Send(string& messageType, string& message) {
    Send(messageType, "", message);
  }
  void MessageQueue::Send(string& messageType, string& privateId,
			  string& message) {
    string wholeMessage = "{message_info={message_type=";
    wholeMessage += messageType;
    if (!privateId.empty()) {
      wholeMessage += "|private_id=";
      wholeMessage += privateId;
    }
    wholeMessage += "}";
    wholeMessage += message + "}";
    Send(message);
  }
      
  MessageQueue::~MessageQueue() {
    Logout();
  }

}

#endif

/*
  $log$
*/
