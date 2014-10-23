/* ----------------------------------------------------------------------
   AdditionalData - 

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/main/dealingtools/orc/deapi/AdditionalData.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

public class AdditionalData extends Message {
    public AdditionalData(Message message) {
	super(message);
    }

    public AdditionalData(String msg) throws DeapiException {
	super(msg);
    }

    public AdditionalData() { }

    private AdditionalData(String key, String value) {
	put("key", key);
	put("value", value);
    }

    private AdditionalData additionalDataEntry(int n) {
	AdditionalData entry;
	// try to get the Message...
	Object o = get("additional_data_entry" + new Integer(n));
	if (o == null)
	    return null;
	if (o instanceof AdditionalData) {
	    // OK, it's already AdditionalData, cast it
	    entry = (AdditionalData) o;
	} else {
	    // It's not already AdditionalData, construct an instrument
	    entry = new AdditionalData((Message) o);
	    // and replace the message with AdditionalData
	    additionalDataEntry(n, entry);
	}
	return entry;
    }
    private void additionalDataEntry(int n, AdditionalData data) {
	put("additional_data_entry" + new Integer(n), data);
    }
    private AdditionalData additionalDataEntryCreate(int n) {
	AdditionalData entry = null;
	entry = additionalDataEntry(n);
	if (entry == null) {
	    entry = new AdditionalData();
	    additionalDataEntry(n, entry);
	}
	return entry;
    }

    // much nicer interface for dealing with additional_data_entries
    // these do not throw exceptions as we're often just testing to
    // see if a key is present...

    public String entry(String key) {
	AdditionalData entry;
	for (int n = 1; (entry = additionalDataEntry(n)) != null; n++) {
	    if (((String) entry.get("key")).equalsIgnoreCase(key)) {
		return (String) entry.get("value");
	    }
	}
	return null;
    }

    public void entry(String key, String value) {
	AdditionalData entry;
	int n;
	// replace value if key is present, otherwise, append
	for (n = 1; (entry = additionalDataEntry(n)) != null; n++) {
	    if (((String) entry.get("key")).equalsIgnoreCase(key)) {
		entry.put("value", value);
	    }
	}
	additionalDataEntry(n, new AdditionalData(key, value));
    }
}

/*
  $log$
*/





