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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Message implements Iterable<Entry<String,Object> >, Comparable<Message> {
	private TreeMap<String,Object> data = null;
	public static String orcDateFormat = "yyyy-MM-dd";
	public static String orcTimeFormat = "HH:mm:ss";

	@SuppressWarnings("unchecked")
	public Message(TreeMap<String,Object> data) {
    	this.data = (TreeMap<String,Object>) data.clone();
    }
    public Message(Reader in) throws DeapiException, IOException {
    	this.data = new TreeMap<String,Object>();
    	parse(in);
    }
    public Message(String buf) throws DeapiException, IOException {
    	this(new StringReader(buf));
    }
    public Message(Message msg) {
    	this(msg.data);
    }
    public Message() {
    	data = new TreeMap<String,Object>();
    }

    private Message parse(Reader in) throws DeapiException, IOException {
    	StringBuffer buf;
    	String key;
    	int c;
    	boolean lookingForKey;

    	key = null;
    	lookingForKey = true;
    	buf = new StringBuffer();
    	do {
    		c = in.read();
    		switch (c) {
    		case '{':
    			if (key != null)
    				data.put(key, new Message(in));
    			lookingForKey = true;
    			break;
    		case '|':
    			// if buf is empty we just added an embedded message
    			if (buf.length() > 0) {
    				data.put(key, buf.toString().trim());
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
    			if (buf.length() > 0)
    				data.put(key, buf.toString().trim());
    			return this;
    		case ' ':
    			if (lookingForKey)
    				// ignore whitespace
    				break;
    			// fall through
    		default:
    			if (c > 0)
    				buf.append((char) c);
    		}
    	} while (c != -1);
    	throw new DeapiException(DeapiErrors.PARSING, "Failed to get a byte. Orc OP gone?");
    }

    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	Object k, o;
    	Iterator<String> p = data.keySet().iterator();

    	while (p.hasNext()) {
    		k = p.next();
    		o = data.get(k);
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
     
    public String toHtml() {
    	StringBuffer buf = new StringBuffer();
    	Object k, o;
    	Iterator<String> p = data.keySet().iterator();

    	while (p.hasNext()) {
    		k = p.next();
    		o = data.get(k);
    		if (o instanceof Message) {
    			Message m = (Message) o;
    			buf.append("<b>" + k + "</b> = <b>{</b>");
    			buf.append(m.toHtml());
    			buf.append("<b>}</b>");
    		} else {
    			buf.append("<b>" + k + "</b> = " + o);
    		}
    		if (p.hasNext()) {
    			buf.append(", ");
    		}
    	}
    	return buf.toString();
    }
    
    public String toVerboseString() {
    	StringBuffer buf = new StringBuffer();
    	Object k, o;
    	Iterator<String> p = data.keySet().iterator();

    	while (p.hasNext()) {
    		k = p.next();
    		o = data.get(k);
    		if (o instanceof Message) {
    			Message m = (Message) o;
    			buf.append(k + " = {");
    			buf.append(m.toVerboseString());
    			buf.append("} ");
    		} else {
    			buf.append(k + " = " + o);
    		}
    		if (p.hasNext()) {
    			buf.append(", ");
    		}
    	}
    	return buf.toString();
    }
    
    public String toXml(String objectName) {
    	StringBuffer buf = new StringBuffer("<?xml version=\"1.0\"?>\n");
    	buf.append("<" + objectName + " " );
    	buf.append(toXml());
    	buf.append("\\>");
    	return buf.toString();
    }

    private StringBuffer toXml() {
    	Object k, o;
    	Iterator<String> p = data.keySet().iterator();

    	StringBuffer buf = new StringBuffer();
    	while (p.hasNext()) {
    		k = p.next();
    		o = data.get(k);
    		if (o instanceof Message) {
    			buf.append("<" + k + " ");
    			buf.append(((Message) o).toXml());
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

    public Set<String> keySet() {
    	return data.keySet();
    }
    
    // This is used by various Comm classes to implement their
    // select functions which are used to do things like return
    // a list of things that match a certain criteria
    public boolean match(Message spec) {
    	// make sure we match on ALL the keys
    	Set<String> keys = new HashSet<String>(spec.keySet());

    	boolean matched = match(keys, spec);

    	if (!(keys.isEmpty())) {
    		// we didn't get all the keys
    		return false;
    	}
    	return matched;
    }

    private boolean match(Set<String> keys, Message spec) {
    	Object o = null;

    	// Make sure we get all the messages
    	Iterator<String> p = data.keySet().iterator();
    	while (p.hasNext()) {
    		o = data.get(p.next());
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
    		key = p.next();
    		o = data.get(key);
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

    public Message add(Message other) {
    	Message result = new Message(this.data);
    	for (Entry<String, Object> entry : result) {
    		if (entry.getValue() instanceof Message)
    			continue;
    		try {
    			double value = result.getDouble(entry.getKey());
    			if (other.containsKey(entry.getKey())) {
                    double otherValue = other.getDouble(entry.getKey());
                    result.put(entry.getKey(),
                    		String.format("%f", value + otherValue));
    			}
    		} catch (DeapiException ignore) {
    			
    		} 			
    	}
    	for (Entry<String, Object> entry : other) {
    		if (entry.getValue() instanceof Message)
    			continue;
    		if (!result.containsKey(entry.getKey())) {
    			result.data.put(entry.getKey(), entry.getValue());
    		}
    	}
    	return result;
    }
    
    public void put(String key, String value) {
    	data.put(key, value);
    }
    public void put(String key, Message value) {
    	data.put(key, value);
    }
    public void put(String key, Date value) {
    	SimpleDateFormat odf = new SimpleDateFormat(orcDateFormat); 
    	data.put(key, odf.format(value));
    }
    public void put(String key, double value) {
    	data.put(key, Double.valueOf(value).toString());
    }
    public void put(String key, boolean value) {
    	data.put(key, Boolean.valueOf(value).toString());
    }
    
    public boolean containsKey(String key) {
    	return data.containsKey(key);
    }
    
    public void remove(String key) {
    	data.remove(key);
    }
    
    public Object get(String key) {
    	return data.get(key);
    }

    public String getString(String key) throws DeapiException {
    	// it would be really nice if Java did an implicit conversion
    	// from a Message to a string if I use getString... here's hoping
    	String value = (String)get(key);
    	if (value == null) {
    		// throw an exception?
    		throw new DeapiException(DeapiErrors.UNKNOWN_KEY,
    				"Cannot find key [" + key + " in message");
    	}
    	return value;
    }

    public Message getMessage(String key) throws DeapiException {
    	Object o = get(key);
    	if (o == null) {
    		// throw an exception?
    		throw new DeapiException(DeapiErrors.UNKNOWN_KEY,
    				"Cannot find key [" + key + "] in message");
    	}
    	Message value = null;
    	try {
    		value = (Message) o;
    	} catch (ClassCastException e) {
    		throw new DeapiException(DeapiErrors.ILLEGAL_VALUE,
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
    
    public double getDouble(String key) throws DeapiException {
    	Double number;
    	String value = getString(key);
    	try {
    		number = new Double(value);
    	} catch (NumberFormatException e) {
    		throw new DeapiException(DeapiErrors.ILLEGAL_VALUE, "Value ["
    				+ value + "] for [" + key
				    + "] was not a float");
    	}
    	return number.doubleValue();
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
    	String dateInt = value.substring(0,4) + value.substring(5,2) + value.substring(7,2);
    	try {
    		number = new Integer(dateInt).intValue();
    	} catch (NumberFormatException e) {
    		throw new DeapiException(DeapiErrors.ILLEGAL_VALUE,
    				"Value [" + value + "] cannot be cast to a "
    				+ "Date integer type for key [" + key + "]");
    	}
    	return number;
    }
    
    public Date getDate(String key) throws DeapiException {
    	String value = getString(key);
    	try {
        	SimpleDateFormat odf = new SimpleDateFormat(orcDateFormat); 
			return odf.parse(value);
		} catch (ParseException e) {
			throw new DeapiException(DeapiErrors.ILLEGAL_VALUE,
    				"Value [" + value + "] cannot be cast to a "
    				+ "Date type for key [" + key + "]");
		}
    }
    
    public Date getTime(String key) throws DeapiException {
    	String value = getString(key);
    	try {
        	SimpleDateFormat otf = new SimpleDateFormat(orcTimeFormat); 
			return otf.parse(value);
		} catch (ParseException e) {
			throw new DeapiException(DeapiErrors.ILLEGAL_VALUE,
    				"Value [" + value + "] cannot be cast to a "
    				+ "Date type for key [" + key + "]");
		}
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
    	data.remove("message_info");
    	data.remove("reply_to");
	
    	data.remove("error");
    	data.remove("error_description");
    	return this;
    }

	public Iterator<Entry<String,Object> > iterator() {
		return data.entrySet().iterator();
	}
	public int compareTo(Message other) {
		return this.toString().compareTo(other.toString());
	}
}


/*
  $log$
*/
