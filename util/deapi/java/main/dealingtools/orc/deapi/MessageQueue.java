/* ----------------------------------------------------------------------
   MessageQueue - makes asynchronous deapi look synchronous

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/main/dealingtools/orc/deapi/MessageQueue.java,v $
   $Revision: 1.1.1.1 $
   

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

import java.util.*;
import java.io.*;

public class MessageQueue extends Thread {
    private HashMap messageQueues;
    private HashMap messageCallbacks;
    private Connection deapi;

    public MessageQueue(String hostname, String service, String loginId,
			String password, boolean debug)
	throws DeapiException {
	initializeConnection(hostname, service);
	login(loginId, password, debug);
    }

    public MessageQueue(String hostname, String service, String loginId,
			String password) throws DeapiException {
	initializeConnection(hostname, service);
	login(loginId, password, false);
    }

    private void initializeConnection(String hostname, String service)
    throws DeapiException {
	try {
	    deapi = new Connection(hostname, service);
	} catch (IOException e) {
	    throw new DeapiException(DeapiErrors.NO_ORC,
				     "The Orc API is not running on ["
				     + hostname + ":" + service + "]");
	}
	messageQueues = new HashMap();
	messageCallbacks = new HashMap();

	// kick off recieve thread
	setDaemon(true);
	start();
    }

    public void run() {
	Message reply;
	LinkedList mQueue;
	MessageCallback callback;
	String id;

	while (true) {
	    try {
		reply = deapi.recieve();
		id = messageId(reply.type(), reply.privateId());
	    } catch (java.net.SocketException se) {
		// someone has closed our socket... finish...
		return;
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }

	    synchronized(messageCallbacks) {
		callback = (MessageCallback) messageCallbacks.get(id);
	    }
	    if (callback != null) {
		try {
		    if (reply.error() != 0) {
			callback.onError(reply.error(),
					 reply.errorDescription());
		    } else {
			callback.onMessage(reply.clean());
		    }
		} catch (DeapiException de) {
		    callback.onError(DeapiErrors.SYSERR,
				     "Unable to get error from message");
		}
	    } else {
		synchronized(messageQueues) {
		    // not for us, put it on the message queue, it might
		    // be a synchronous message
		    if ((mQueue = (LinkedList) messageQueues.get(id))
			== null) {
			mQueue = new LinkedList();
			messageQueues.put(id, mQueue);
		    }
		    mQueue.addFirst(reply);
		    // inform the recieve method that there's new message
		    // in case it's blocking on that one
		    messageQueues.notify();
		}
	    }
	}
    }
    
    public void registerCallback(String messageType, String privateId,
				 MessageCallback callback)
	throws DeapiException {
	String id = messageId(messageType, privateId);
	synchronized(messageCallbacks) {
	    messageCallbacks.put(id, callback);
	}
    }
    public void registerCallback(String messageType, MessageCallback callback)
	throws DeapiException {
	String id = messageId(messageType, null);
	synchronized(messageCallbacks) {
	    messageCallbacks.put(id, callback);
	}
    }

    public void removeCallback(String messageType, String privateId)
	throws DeapiException {
	String id = messageId(messageType, privateId);
	synchronized(messageCallbacks) {
	    messageCallbacks.remove(id);
	}
    }
    public void removeCallback(String messageType)
	throws DeapiException {
	String id = messageId(messageType, null);
	synchronized(messageCallbacks) {
	    messageCallbacks.remove(id);
	}
    }

    public void logout() {
	try {
	    send("{message_info={message_type=logout}}");
	    deapi.close();
	} catch (DeapiException de) {
	    // I can't think why I need to worry...
	} catch (IOException e) {
	    // ditto
	}
    }

    public void login(String loginId, String password, boolean debug)
	throws DeapiException {
	StringBuffer loginmsg =	new
	    StringBuffer("{message_info={message_type=login}|login_id=");
	loginmsg.append(loginId);
	loginmsg.append("|password=");
	loginmsg.append(password);
	if (debug) {
	    loginmsg.append("|debug=on");
	}
	loginmsg.append("}");

	send(loginmsg.toString());
	recieve("login");
    }

    public Message recieve(String messageType) throws DeapiException {
	return recieve(messageType, null);
    }
    public Message recieve(String messageType, String privateId)
			   throws DeapiException {
	Message reply;
	LinkedList mQueue;
	String id = messageId(messageType, privateId);

	while (true) {
	    synchronized(messageQueues) {
		reply = null;
		// look in our message queues for one of these
		if ((mQueue = (LinkedList) messageQueues.get(id)) != null) {
		    if (!mQueue.isEmpty()) {
			reply = (Message) mQueue.removeLast();
			if (reply.error() != 0) {
			    throw new DeapiException(reply.error(),
						     reply.errorDescription());
			}
			return reply.clean();
		    }
		}
		// hmmm wait until someone puts another message on the
		// queues
		try {
		    messageQueues.wait();
		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}
	    }
	}
    }

    public void send(String message) throws DeapiException {
	try {
	    deapi.send(message);
	} catch (IOException e) {
	    throw new DeapiException(DeapiErrors.NO_ORC,
				     "Our Orc connection has gone!");
	}
    }
    public void send(String messageType, String message)
	throws DeapiException {
	String privateId = null;
	send(messageType, privateId, message);
    }
    public void send(String messageType, String privateId, String message)
	throws DeapiException {
	StringBuffer msg = new StringBuffer("{message_info={message_type=");
	msg.append(messageType);
	if (privateId != null) {
	    msg.append("|private_id = ");
	    msg.append(privateId);
	}
	msg.append("}|");
	msg.append(message);
	msg.append("}");
	try {
	    deapi.send(msg.toString());
	} catch (IOException e) {
	    throw new DeapiException(DeapiErrors.NO_ORC,
				     "Our Orc connection has gone!");
	}
    }

    public static String messageId(String messageType, String privateId) {
	if ((privateId == null) || (privateId.length() == 0)) {
	    return messageType;
	}
	return messageType + ":" + privateId;
    }
}

/*
  $log$
*/









