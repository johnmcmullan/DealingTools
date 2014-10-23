/* ----------------------------------------------------------------------
   TradeComm - talks with Orc about Trades

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/18 01:32:00 $
   $Source: /usr/export/cvsroot/deapi/java/main/dealingtools/orc/deapi/TradeComm.java,v $
   $Revision: 1.2 $

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

import java.util.*;
import java.text.SimpleDateFormat;

public class TradeComm {
    private MessageQueue orc;
    private Message cache;

    public TradeComm(MessageQueue orcInstance) {
	orc = orcInstance;
	cache = new Message();
    }

    // this one is for getting trade_feed messages, make sure you've
    // turned on the trade_feed toggle, this will block like a
    // bulldog at a playground
    public Trade get() throws DeapiException {
	Trade trade;
	trade = new Trade(orc.recieve("trade_feed"));
	putInCache(trade);
	return trade;
    }

    public void toggle(Instrument instrumentId, boolean flag)
	throws DeapiException {
	String messageType = "tradefeed_toggle";

	orc.send(messageType, "toggle=" + (flag ? "on" : "off"));
	orc.recieve(messageType);
    }

    public Message download() throws DeapiException {
	return download(new Message());
    }
    public Message download(Message spec) throws DeapiException {
	if (spec.get("startdate") == null) {
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    Date date = new Date();
	    String today = formatter.format(date);
	    spec.put("startdate", today);
	    spec.put("enddate", today);
	}

	return download(spec.toString());
    }
    public Message download(String spec) throws DeapiException {
	String messageType = "trade_range_get";
	Message reply, myReply, tradeReplies;
	String key;
	Trade t;

	myReply = new Message();
	orc.send(messageType, spec);
	reply = orc.recieve(messageType);
	tradeReplies = reply.getMessage("trade_replies");
	
	Iterator l = tradeReplies.keySet().iterator();
	while (l.hasNext()) {
	    key = (String) l.next();
	    t = new Trade(tradeReplies.getMessage(key));
	    myReply.put(new Integer(t.tag()), t);
	    putInCache(t);
	}
	return myReply;
    }

    public void verify(int tag) throws DeapiException {
	String messageType = "trade_verify";

	orc.send(messageType, "trade_tag=" + new Integer(tag));
	orc.recieve(messageType);
    }

    public void delete(int tag) throws DeapiException {
	String messageType = "trade_delete";

	orc.send(messageType, "trade_tag=" + new Integer(tag));
	orc.recieve(messageType);
    }

    public void insert(Trade trade) throws DeapiException {
	String messageType = "trade_insert";

	// don't forget that the trade object is actually really a
	// trade_reply which contrains a trade...
	orc.send(messageType, trade.trade().toString());
	orc.recieve(messageType);
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

    private void putInCache(Trade trade) {
	Object o;
	Integer tag = null;

	try {
	    tag = new Integer(trade.tag());
	} catch (DeapiException e) {
	    // yikes... no tag? We'll we can't do much with this
	    return;
	}
	cache.put(tag, trade);
    }

    private Trade getFromCache(int tag) {
	return (Trade) cache.get(new Integer(tag));
    }
}


/*
  $log$
*/




