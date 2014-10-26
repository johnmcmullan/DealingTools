/* ----------------------------------------------------------------------
   MessageQueue - makes asynchronous deapi look synchronous

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/24 01:19:43 $
   $Source: /usr/export/cvsroot/deapi/java/main/dealingtools/orc/deapi/MessageQueue.java,v $
   $Revision: 1.8 $
   

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import dealingtools.util.Log;


public class MessageQueue extends Thread {
	public interface Callback {
	    void onMessage(Message message);
	    void onError(int error, String errorDescription);
	    boolean isPersistent();
	}
	
	public class LogErrorsCallback implements Callback {
		public void onError(int error, String errorDescription) {
			Log.logger().severe(errorDescription);
		}
		public void onMessage(Message message) {}
		public boolean isPersistent() { return true; }
	}
	public class DebugCallback implements Callback {
		public void onError(int error, String errorDescription) {
			Log.logger().severe(errorDescription);
		}
		public void onMessage(Message message) {
			Log.logger().info(message.toString());
		}
		public boolean isPersistent() { return true; }
	}
	
    private HashMap<String,ArrayBlockingQueue<Message> > messageQueues;
    private HashMap<String,Callback> messageCallbacks;
    private Connection op;
    private int nextPrivateId;
    private String logErrorsCallbackId;
    private String debugCallbackId;
    private boolean disconnecting;

    public MessageQueue(String hostname, String service, String loginId,
			String password, boolean debug) throws DeapiException {
    	initializeConnection(hostname, service, loginId, password, debug);
    }

    public MessageQueue(String hostname, String service, String loginId,
			String password) throws DeapiException {
    	initializeConnection(hostname, service, loginId, password, false);
    }

    private void initializeConnection(String hostname, String service, String loginId,
			String password, boolean debug) throws DeapiException {
    	nextPrivateId = 1;
    	disconnecting = false;
    	Log.logger().info(String.format("Connecting to Orc[%s:%s] as [%s]", hostname, service, loginId));
    	try {
    		op = new Connection(hostname, service);
    	} catch (IOException e) {
    		String error = String.format("The Orc API is not running on [%s:%s]",
    				hostname, service);
    		Log.logger().severe(error);
    		throw new DeapiException(DeapiErrors.NO_ORC, error);
    	}
    	messageQueues = new HashMap<String, ArrayBlockingQueue<Message> >();
    	messageCallbacks = new HashMap<String, Callback>();
    	
    	login(loginId, password, debug);
    	Log.logger().info("Connected");
    	
    	logErrorsCallbackId = registerCallback(new LogErrorsCallback());
    	debugCallbackId = registerCallback(new DebugCallback());
    	
    	setDaemon(true);
    	start();
    }

    public void run() {
    	ArrayBlockingQueue<Message> mQueue = null;
    	
    	Log.logger().fine("Beginning the Orc IO thread");
    	while (true) {
    		Message reply = null;
    		Callback callback = null;
        	String messageType = null;
        	String privateId = null;
    		try {
    			if (disconnecting)
    				return;
    			Log.logger().finer("Waiting on the Orc socket for a new message");
    			reply = op.recieve();
    		} catch (IOException e) {
    			Log.logger().warning(e.getMessage());
    			Log.logger().severe("Orc OP connection has gone, exiting");
    			return;
    		} catch (DeapiException e) {
    			Log.logger().severe(e.getMessage());
    			throw new RuntimeException(e);
    		}
    		Log.logger().finest(String.format("Got a new msg[%s]", reply.toString()));

    		if (disconnecting)
    			return;
    		
    		try {
    			// privateId will just be null, type will throw
    			privateId = reply.privateId();
    			messageType = reply.type();
    		} catch (DeapiException ope) {
    			Log.logger().warning(ope.getMessage());
    			Log.logger().warning(String.format("unable to get message_type from msg[%s] so ignoring it",
    					reply.toString()));
    			continue;
    		}
    		
    		// Lock for a callback for this ID
    		if (privateId != null) {
    			if (privateId.equals("0")) {
    				Log.logger().finer(String.format("Ignoring msg[%s] as private == 0",
    						reply.toString()));
    				continue;
    			}
    			synchronized(messageCallbacks) {
    				callback = messageCallbacks.get(privateId);
    			}
    		}
    		if (callback != null) {
    			Log.logger().fine(String.format("Got a callback for [%s]", privateId));
    			// Found a callback, call either onMessage or onError
    			try {
    				if (reply.error() != 0) {
    					callback.onError(reply.error(), reply.errorDescription());
    				} else {
    					callback.onMessage(reply.clean());
    				}
    				if (!callback.isPersistent())
    					removeCallback(privateId);
    			} catch (DeapiException de) {
    				String error = String.format("Unable to get error from msg[%s]", reply.toString());
    				Log.logger().warning(error);
    				callback.onError(DeapiErrors.SYSERR, error);
    			}
    		} else {
    			// not for us, put it on the message queue, it might
    			// be a synchronous message
    			Log.logger().finest("No callback.... enquing");
    			// TODO simplify this - arrayblockingqueue does not need all this
    			synchronized(messageQueues) {
    				// work out which messageQueue it goes on
    				if ((mQueue = messageQueues.get(messageType)) == null) {
    					mQueue = new ArrayBlockingQueue<Message>(100);
    					Log.logger().finest(String.format("Creating a [%s] queue", messageType));
    					messageQueues.put(messageType, mQueue);
    				}
    				try {
    					Log.logger().finest(String.format("Enquing a [%s] message", messageType));
						mQueue.put(reply);
					} catch (InterruptedException e) {
						Log.logger().severe(String.format("We timed out trying to put this msg[%s] on the in queue", reply.toString()));
					}
    				// inform the receive method that there's new message
    				// in case it's blocking on that one
    				messageQueues.notify();
    			}
    		}
    	}
    }
    
    public String registerCallback(Callback callback) throws DeapiException {
    	String privateId = String.format("%d", nextPrivateId++);
    	synchronized(messageCallbacks) {
    		messageCallbacks.put(privateId, callback);
    	}
    	return privateId;
    }
    public void removeCallback(String privateId) throws DeapiException {
    	synchronized(messageCallbacks) {
    		messageCallbacks.remove(privateId);
    	}
    }

    public void logout() {
    	try {
    		send("logout");
    		receive("logout");
    		disconnecting = true;
    		op.close();
    		op = null;
    		disconnecting = false;
    	} catch (DeapiException de) {
    		// I can't think why I need to worry...
    	} catch (IOException e) {
    		// ditto
    	}
    }

    public void login(String loginId, String password, boolean debug) throws DeapiException {
    	StringBuffer loginmsg =
    		new StringBuffer("login_id=");
		loginmsg.append(loginId);
		loginmsg.append("|password=");
		loginmsg.append(password);
		if (debug)
			loginmsg.append("|debug=on");

		send("login", loginmsg.toString());
		try {
			Message msg = op.recieve();
			if (msg.error() != 0)
				throw new DeapiException(msg.error(), msg.errorDescription());
		} catch (IOException e) {
			throw new DeapiException(DeapiErrors.SYSERR, "We sent a login message but got no reply");
		}
    }

    public Message receive(String messageType) throws DeapiException {
    	Message reply;
    	ArrayBlockingQueue<Message> mQueue;

    	while (true) {
    		synchronized(messageQueues) {
    			reply = null;
    			// look in our message queues for one of these
    			if ((mQueue = messageQueues.get(messageType)) != null) {
					reply = mQueue.poll();
    				if (reply != null) {
    					if (reply.error() != DeapiErrors.OK)
    						if (reply.error() < DeapiErrors.WARN)
    							throw new DeapiException(reply.error(), reply.errorDescription());
    						else
    							return reply;
    					return reply.clean();
    				}
    			}
    			// hmmm wait until someone puts another message on the queues
    			try {
    				messageQueues.wait();
    			} catch (InterruptedException e) {
    				throw new RuntimeException(e);
    			}
    		}
    	}
    }

    public void send(String messageType) throws DeapiException {
    	send(messageType, null, null);
    }
    
    public void send(String messageType, String message) throws DeapiException {
    	send(messageType, null, message);
    }
    public void send(String messageType, String privateId, String message) throws DeapiException {
    	StringBuffer msg = new StringBuffer("{message_info={message_type=");
    	msg.append(messageType);
    	if (privateId != null) {
    		msg.append("|private=");
    		msg.append(privateId);
    	}	
    	msg.append("}");
    	if (message != null) {
    		msg.append("|");
    		msg.append(message);
    	}
    	msg.append("}");
    	try {
    		op.send(msg.toString());
    	} catch (IOException e) {
    		throw new DeapiException(DeapiErrors.NO_ORC, "Our Orc OP connection has gone!");
    	}
    }
    public Message sendAndReceive(String messageType) throws DeapiException {
    	send(messageType);
    	return receive(messageType);
    }
    public Message sendAndReceive(String messageType, String message) throws DeapiException {
    	send(messageType, message);
    	return receive(messageType);
    }
    public void sendAndForget(String messageType, String message) throws DeapiException {
    	send(messageType, "0", message);
    }
    public void sendAndForget(String messageType) throws DeapiException {
    	send(messageType, "0", null);
    }

	public String getLogErrorsCallbackId() {
		return logErrorsCallbackId;
	}

	public String getDebugCallbackId() {
		return debugCallbackId;
	}
}

/*
  $log$
*/









