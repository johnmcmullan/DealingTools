/* ----------------------------------------------------------------------
   Message - base class for storing Orc messages, extends HashMap

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/24 01:19:43 $
   $Source: /usr/export/cvsroot/deapi/java/main/dealingtools/orc/deapi/Message.java,v $
   $Revision: 1.10 $

   Constructors for turning a stream or string into a Message
   toString for outputting the message in the DEAPI format

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class Message extends TreeMap {
    public Message(TreeMap m) {
	super(m);
    }

    public Message(InputStream in) throws DeapiException {
	parse(in);
    }

    public Message(String buf) throws DeapiException {
	this(new StringBufferInputStream(buf));
    }

    public Message() {
	super();
    }

    private Message parse(InputStream in) throws DeapiException {
	StringBuffer buf;
	String key;
	int c;
	boolean lookingForKey;

	key = null;
	lookingForKey = true;
	buf = new StringBuffer();
	do {
	    try {
		c = in.read();
	    } catch (IOException e) {
		throw new DeapiException(DeapiErrors.PARSING, e.toString());
	    }
	    switch (c) {
	    case '{':
		if (key != null) {
		    // not initializing the opening brace
		    // also, we use Java's reflection here to work out what
		    // we're actually instantiating as this method is
		    // called by subclasses that expect us to create
		    // the right embedded object, not just Message
		    try {
			put(key, ((Message)
				  this.getClass().newInstance()).parse(in));
		    } catch (InstantiationException ie) {
			// this is so not going to happen
			// the exception will only be raised if we try to
			// instantiate something that can't be instantiated
		    } catch (IllegalAccessException iae) {
			// Ditto but this is raised if we used a string
			// to instantiate the object, which we didn't
		    }
		}
		lookingForKey = true;
		break;
	    case '|':
		// if buf is empty we just added an embedded message
		if (buf.length() > 0) {
		    put(key, buf.toString().trim());
		    lookingForKey = true;
		    buf.setLength(0);
		}
		break;
	    case '=':
		if (lookingForKey) {
		    // no need to trim if we were looking for key
		    key = buf.toString();
		    buf.setLength(0);
		    lookingForKey = false;
		} else {
		    buf.append((char) c);
		}
		break;
	    case '}':
		// if buf is empty we just added an embedded message
		if (buf.length() > 0) {
		    put(key, buf.toString().trim());
		}
		return this;
	    case ' ':
		if (lookingForKey) {
		    // ignore whitespace
		    break;
		}
		// fall through
	    default:
		if (c > 0) {
		    buf.append((char) c);
		}
	    }
	} while (c != -1);

	if (buf.length() > 0) {
	    put(key, buf.toString().trim());
	}

	return this;
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	Object k, o;
	Iterator p = this.keySet().iterator();

	while (p.hasNext()) {
	    k = p.next();
	    o = this.get(k);
	    if (o instanceof Message) {
		buf.append(k + "={");
		buf.append(o);
		buf.append("}");
	    } else {
		buf.append(k + "=" + o);
	    }
	    if (p.hasNext()) {
		buf.append("|");
	    }
	}
	return buf.toString();
    }

    public String toXML(String objectName) {
	StringBuffer buf = new StringBuffer("<?xml version=\"1.0\"?>\n");
	buf.append("<" + objectName + " " );
	buf.append(toXML());
	buf.append("\\>");
	return buf.toString();
    }

    private StringBuffer toXML() {
	Object k, o;
	Iterator p = this.keySet().iterator();

	StringBuffer buf = new StringBuffer();
	while (p.hasNext()) {
	    k = p.next();
	    o = this.get(k);
	    if (o instanceof Message) {
		buf.append("<" + k + " ");
		buf.append(((Message) o).toXML());
		buf.append("\\>");
	    } else {
		buf.append(k + "=\"" + o + "\"");
	    }
	    if (p.hasNext()) {
		buf.append(" ");
	    }

	}
	return buf;
    }

    // This is used by various Comm classes to implement their
    // select functions which are used to do things like return
    // a list of things that match a certain criteria
    public boolean match(Message spec) {
	// make sure we match on ALL the keys
	Set keys = new HashSet(spec.keySet());

	boolean matched = match(keys, spec);

	if (!(keys.isEmpty())) {
	    // we didn't get all the keys
	    return false;
	}
	return matched;
    }

    private boolean match(Set keys, Message spec) {
	Object o = null;

	// Make sure we get all the messages
	Iterator p = this.keySet().iterator();
	while (p.hasNext()) {
	    o = this.get(p.next());
	    if (o instanceof Message) {
		if (((Message) o).match(keys, spec) == false) {
		    return false;
		}
	    }
	}

	// Now do the data itself, convert to strings for comparison
	String key = null;
	p = keys.iterator();
	while (p.hasNext()) {
	    key = (String) p.next();
	    o = this.get(key);
	    if ((o != null) && (!(o instanceof Message))) {
		if (!(o.toString().equals(spec.get(key).toString()))) {
		    return false;
		}
		// safely remove this item
		p.remove();
	    }
	}
	return true;
    }


    public String getString(String key) throws DeapiException {
	// it would be really nice if Java did an implicit conversion
	// from a Message to a string if I use getString... here's hoping
	String value = (String) get(key);
	if (value == null) {
	    // throw an exception?
	    throw new DeapiException(DeapiErrors.UNKNOWN_KEY,
				     "Cannot find key [" + key
				     + "] in message [" + this + "]");
	}
	return value;
    }

    public Message getMessage(String key) throws DeapiException {
	Object o = get(key);
	if (o == null) {
	    // throw an exception?
	    throw new DeapiException(DeapiErrors.UNKNOWN_KEY,
				     "Cannot find key [" + key + "]");
	}
	Message value = null;
	try {
	    value = (Message) o;
	} catch (ClassCastException e) {
	    throw new
		DeapiException(DeapiErrors.ILLEGAL_VALUE,
			       "Value [" + value + "] for [" + key
			       + "] was not a message");
	}

	return value;
    }

    public int getInt(String key) throws DeapiException {
	Integer number;
	String value = getString(key);
	try {
	    number = new Integer(value);
	} catch (NumberFormatException e) {
	    throw new DeapiException(DeapiErrors.ILLEGAL_VALUE, "Value ["
				     + value + "] for [" + key
				     + "] was not an integer");
	}
	return number.intValue();
    }

    public float getFloat(String key) throws DeapiException {
	Float number;
	String value = getString(key);
	try {
	    number = new Float(value);
	} catch (NumberFormatException e) {
	    throw new DeapiException(DeapiErrors.ILLEGAL_VALUE, "Value ["
				     + value + "] for [" + key
				     + "] was not a float");
	}
	return number.floatValue();
    }

    public boolean getBoolean(String key) throws DeapiException {
	String value = getString(key);
	if (value.equalsIgnoreCase("true") ||
	    value.equalsIgnoreCase("on") ||
	    value.equalsIgnoreCase("yes")) {
	    return true;
	}
	if (value.equalsIgnoreCase("false") ||
	    value.equalsIgnoreCase("off") ||
	    value.equalsIgnoreCase("no")) {
	    return false;
	}
	throw new DeapiException(DeapiErrors.ILLEGAL_VALUE,
				 "Value [" + value + "] cannot be cast to a "
				 + "boolean type for key [" + key + "]");
    }

    public int getDateInt(String key) throws DeapiException {
	int number = 0;
	String value = getString(key);
	String dateInt = value.substring(0,4) + value.substring(5,2)
	    + value.substring(7,2);
	try {
	    number = new Integer(dateInt).intValue();
	} catch (NumberFormatException e) {
	    throw new DeapiException(DeapiErrors.ILLEGAL_VALUE,
				 "Value [" + value + "] cannot be cast to a "
				 + "Date integer type for key [" + key + "]");
	}
	return number;
    }

    public int error() throws DeapiException {
	return getInt("error");
    }
    public String errorDescription() throws DeapiException {
	return getString("error_description");
    }
    public String type() throws DeapiException {
	Message replyTo = null;
	try {
	    replyTo = getMessage("reply_to");
	} catch (DeapiException e) {
	    if (e.error() == DeapiErrors.UNKNOWN_KEY) {
		replyTo = getMessage("message_info");
	    }
	}
	return replyTo.getString("message_type");
    }

    public String privateId() throws DeapiException {
	Message replyTo = null;
	try {
	    replyTo = getMessage("reply_to");
	} catch (DeapiException e) {
	    if (e.error() == DeapiErrors.UNKNOWN_KEY) {
		replyTo = getMessage("message_info");
	    }
	}
	// note this method does not throw an exception if the key is missing
	// private is mostly internal to the API and we handle null privates
	return (String) replyTo.get("private");
    }

    // clean the routing and error info out of a message
    public Message clean() {
	// it'll be one or the other, always do both...
	remove("message_info");
	remove("reply_to");
	
	remove("error");
	remove("error_description");
	return this;
    }

}

/*
  $log$
*/
