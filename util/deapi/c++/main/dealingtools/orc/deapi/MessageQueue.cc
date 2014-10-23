/* ----------------------------------------------------------------------
   MessageQueue - deapi message queue implementation

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/c++/main/dealingtools/orc/deapi/MessageQueue.cc,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

static char rcsid[] = "$Id: MessageQueue.cc,v 1.1.1.1 2004/05/10 14:06:02 john Exp $";

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

namespace deapi {

  MessageQueue::MessageQueue(const std::string& hostname,
			     const std::string& service,
			     const std::string& password,
			     bool debug)
    : deapi(hostname, service) {
    Initialize();
    Login(loginid, password, debug);
  }

  MessageQueue::MessageQueue(const std::string& hostname,
			     const std::string& service,
			     const std::string& loginid,
			     const std::string& password)
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

  void MessageQueue::Login(const std::string& loginid,
			   const std::string& password,
			   bool debug) {
    std::string loginmsg = "{message_info={message_type=login}|login_id=";
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
    dt::Log *log = dt::Log::Instance();

    while (true) {
      try {
	Message *reply = self->deapi.recieve();
	std::string id =
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

  void MessageQueue::RegisterCallback(const std::string& messageType,
				      const std::string& privateId,
				      MessageCallback *callback) {
    std::string id = MessageQueue::MessageId(messageType, privateId);
    pthread_mutex_lock(&lock);
    MessageCallback *old = messageCallbacks.find(id);
    if (old != 0)
      delete old;
    messageCallbacks[id] = callback;
    pthread_mutex_unlock(&lock);
  }
  void MessageQueue::RegisterCallback(std::string& messageType,
				      MessageCallback *callback) {
    RegisterCallback(messageType, "", callback);
  }

  void MessageQueue::RemoveCallback(std::string& messageType,
				    std::string& privateId) {
    std::string id = MessageQueue::MessageId(messageType, privateId);
    pthread_mutex_lock(&lock);
    MessageCallback *callback = messageCallbacks.find(id);
    if (callback != 0) {
      delete callback;
      messageCallbacks.erase(id);
    }
    pthread_mutex_unlock(&lock);
  }
  void MessageQueue::RemoveCallback(std::string& messageType) {
    RemoveCallback(messageType, "");
  }

  Message *MessageQueue::Receive(std::string& messageType,
				 std::string& privateId) {
    std::string id = MessageQueue::MessageId(messageType, privateId);
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
  Message *MessageQueue::Receive(std::string& messageType) {
    return Recieve(messageType, "");
  }

  void MessageQueue::Send(std::string& message) {
    try {
      deapi.Send(message);
    } catch (...) {
      throw Exception(ERR_SYSERR, "Unable to send message to Orc");
    }
  }
  void MessageQueue::Send(std::string& messageType, std::string& message) {
    Send(messageType, "", message);
  }
  void MessageQueue::Send(std::string& messageType, std::string& privateId,
			  std::string& message) {
    std::string wholeMessage = "{message_info={message_type=";
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
