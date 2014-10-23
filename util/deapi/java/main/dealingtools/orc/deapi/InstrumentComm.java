/* ----------------------------------------------------------------------
   InstrumentComm - talks with Orc about Instruments

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/main/dealingtools/orc/deapi/InstrumentComm.java,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

import java.util.*;

public class InstrumentComm {
    private MessageQueue orc;
    private Message cache;

    public InstrumentComm(MessageQueue orcInstance) {
	orc = orcInstance;
	Initialize();
    }
    private void Initialize() {
	cache = new Message();
    }

    public Instrument get(String instrumentIdMsg) throws DeapiException {
	Instrument instrument;
	String messageType = "instrument_get";

	// it's a string, can't check the cache (well, too hard)

	// grab it from Orc
	orc.send(messageType, "instrument_id={" + instrumentIdMsg + "}}");
	instrument = new Instrument(orc.recieve(messageType));

	putInCache(instrument);
	return instrument;
    }

    public Instrument get(int tag) throws DeapiException {
	Instrument instrument;

	// check the cache first
	if ((instrument = getFromCache(tag)) != null) {
	    return instrument;
	}

	// not there? grab it from Orc
	return get("instrument_tag=" + tag);
    }

    public Instrument get(Instrument instrumentId) throws DeapiException {
	// assume there is no tag to check the cache
	// if there was they should have used the other method

	// did they send us an instrument or an instrument_id?
	try {
	    instrumentId.instrumentId();
	} catch (DeapiException e) {
	    return get(instrumentId.toString());
	}
	return get(instrumentId.instrumentId().toString());
    }

    public Message download(Message spec) throws DeapiException {
	return download(spec.toString());
    }
    public Message download(String spec) throws DeapiException {
	String messageType = "instrument_download";
	Message reply, myReply, instrumentList;
	String key;
	Instrument i;

	myReply = new Message();
	orc.send(messageType, spec);
	reply = orc.recieve(messageType);
	instrumentList = (Message) reply.get("instrument_list");
	if (instrumentList == null) {
	    instrumentList = reply.getMessage("full_instrument_list");
	}
	
	Iterator l = instrumentList.keySet().iterator();
	while (l.hasNext()) {
	    key = (String) l.next();
	    i = new Instrument(instrumentList.getMessage(key));
	    myReply.put(new Integer(i.tag()), i);
	    putInCache(i);
	}
	return myReply;
    }

    public void create(Instrument instrument) throws DeapiException {
	String messageType = "instrument_create";
	Message reply;
	Object o;
	Instrument newInstrument;

	// remove the instrumentID as it's not needed
	o = instrument.get("instrument_id");
	instrument.remove("instrument_id");

	try {
	    orc.send(messageType, instrument.toString());
	    reply = orc.recieve(messageType);
	} catch (DeapiException e) {
	    // oops, better put that instrumentId back
	    if (o != null) {
		instrument.put("instrument_id", o);
	    }
	    throw e;
	}
	newInstrument = new Instrument(reply);
	instrument.put("instrument_id", newInstrument.instrumentId());
    }

    public void delete(int tag) throws DeapiException {
	delete("instrument_tag=" + tag);
    }
    public void delete(String instrumentIdMsg) throws DeapiException {
	String messageType = "instrument_delete";

	orc.send(messageType, "instrument_id={" + instrumentIdMsg + "}}");
	orc.recieve(messageType);
    }
    public void delete(Instrument instrumentId) throws DeapiException {
	// did they send us an instrument or an instrument_id?
	try {
	    instrumentId.instrumentId();
	} catch (DeapiException e) {
	    delete(instrumentId.toString());
	}
	delete(instrumentId.instrumentId().toString());
    }

    public Message select(String spec) throws DeapiException {
	return select(new Message(spec));
    }
    public Message select(Message spec) throws DeapiException {
	Message reply = new Message();

	Message m = null;
	Integer tag;
	Iterator p = cache.keySet().iterator();
	while (p.hasNext()) {
	    tag = (Integer) p.next();
	    m = (Message) cache.get(tag);
	    if (m.match(spec)) {
		reply.put(tag, m);
	    }
	}
	return reply;
    }
    public Message select() {
	return cache;
    }

    public Message selectAsTree(String spec) throws DeapiException {
	return selectAsTree(new Message(spec));
    }
    public Message selectAsTree(Message spec) throws DeapiException {
	Message reply = new Message();

	Message m = null;
	Iterator p = cache.values().iterator();
	while (p.hasNext()) {
	    m = (Message) p.next();
	    if (m.match(spec)) {
		addToTree(reply, (Instrument) m);
	    }
	}
	return reply;
    }
    public Message selectAsTree() throws DeapiException {
	Message reply = new Message();

	Iterator p = cache.values().iterator();
	while (p.hasNext()) {
	    addToTree(reply, (Instrument) p.next());
	}
	return reply;
    }	    

    private void putInCache(Instrument instrument) {
	Object o;
	Integer tag = null;

	try {
	    tag = new Integer(instrument.tag());
	} catch (DeapiException e) {
	    // yikes... no tag? We'll we can't do much with this
	    return;
	}
	// if it's already there then we only replace it if the one we're
	// sticking in is a full instrument, not just an instrument_id
	if (cache.get(tag) != null) {
	    if (instrument.get("parameters") != null) {
		return;
	    }
	}
	cache.put(tag, instrument);
    }

    private Instrument getFromCache(int tag) {
	return (Instrument) cache.get(new Integer(tag));
    }

    // Tree = UNDERLYING -> EXPIRYDATE -> KIND [-> STRIKEPRICE]
    // of course this only does instruments with expirations
    private static void addToTree(Message root, Instrument i)
	throws DeapiException {

	// all instruments have an underlying
	Message underlying = (Message) root.get(i.underlying());
	if (underlying == null) {
	    underlying = new Message();
	    root.put(i.underlying(), underlying);
	}
	if (i.hasExpiration()) {
	    Message expiration = (Message) underlying.get(i.expirydate());
	    if (expiration == null) {
		expiration = new Message();
		underlying.put(i.expirydate(), expiration);
	    }

	    if (!i.isOption()) {
		expiration.put(i.kind(), i);
	    } else {
		// one more level
		Message kind = (Message) expiration.get(i.optionKind());
		if (kind == null) {
		    kind = new Message();
		    expiration.put(i.optionKind(), kind);
		}
		kind.put(i.strikeprice(), i);
	    }
	}
    }
}


/*
  $log$
*/




