/* ----------------------------------------------------------------------
   PriceComm - talks with Orc about Prices

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/main/dealingtools/orc/deapi/PriceComm.java,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

public class PriceComm {
    private MessageQueue orc;

    public PriceComm(MessageQueue orcInstance) {
	orc = orcInstance;
    }

    public Price get(Instrument instrumentId) throws DeapiException {
	orc.send("{message_info={message_type=price_get}|" +
		 "instrument_id={" + instrumentId + "}}");
	orc.recieve("price_get");
	return new Price(orc.recieve("price_feed"));
    }

    public void toggle(Instrument instrumentId, boolean flag)
	throws DeapiException {
	orc.send("{message_info={message_type=pricefeed_toggle}|"
		 + "toggle=" + (flag ? "on" : "off") + "|instrument_id={" +
		 instrumentId + "}}");
	orc.recieve("pricefeed_toggle");
    }

    public void toggle(Instrument instrumentId, MessageCallback mc)
	throws DeapiException {
	orc.registerCallback("price_feed", mc);
	toggle(instrumentId, true);
    }

    // this one is for getting price_feed messages, make sure you've
    // turned on the price_feed toggle, this will block like a
    // 91 bus in Tottenham Lane on Friday night, better to use the callbacks
    public Price get() throws DeapiException {
	return new Price(orc.recieve("price_feed"));
    }

    public void broadcast(Instrument instrument, Price price)
	throws DeapiException {
	price.instrumentId(instrument.instrumentId());
	broadcast(price);
    }
    public void broadcast(Price price) throws DeapiException {
	orc.send("{message_info={message_type=price_broadcast}|"
		 + price + "}");
	orc.recieve("price_broadcast");	
    }
}


/*
  $log$
*/
