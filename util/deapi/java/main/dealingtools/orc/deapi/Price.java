/* ----------------------------------------------------------------------
   Price - 

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/main/dealingtools/orc/deapi/Price.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

public class Price extends Message {
    public Price(Message message) {
	super(message);
    }

    public Price(String msg) throws DeapiException {
	super(msg);
    }

    public Price() { }

    public Instrument instrumentId() throws DeapiException {
	Instrument id;
	// try to get the Message...
	Message message = getMessage("instrument_id");
	if (message instanceof Instrument) {
	    // OK, it's already an Instrument, cast it
	    id = (Instrument) message;
	} else {
	    // It's not already an instrument, construct an instrument
	    id = new Instrument(message);
	    // and replace the message with an instrument
	    instrumentId(id);
	}
	return id;
    }
    public void instrumentId(Instrument id) throws DeapiException { 
	put("instrument_id", id);
    }

    public float bid() throws DeapiException {
	return getFloat("bid");
    }
    public void bid(String bid) throws DeapiException {
	put("bid", bid);
    }

    public float bidVolume() throws DeapiException {
	return getFloat("bid_volume");
    }
    public void bidVolume(String bidVolume) throws DeapiException {
	put("bid_volume", bidVolume);
    }

    public float askVolume() throws DeapiException {
	return getFloat("ask_volume");
    }
    public void askVolume(String askVolume) throws DeapiException {
	put("ask_volume", askVolume);
    }

    public float ask() throws DeapiException {
	return getFloat("ask");
    }
    public void ask(String ask) throws DeapiException {
	put("ask", ask);
    }

    public float bidYtm() throws DeapiException {
	return getFloat("bid_ytm");
    }
    public void bidYtm(String bidYTM) throws DeapiException {
	put("bid_ytm", bidYTM);
    }

    public float askYtm() throws DeapiException {
	return getFloat("ask_ytm");
    }
    public void askYtm(String askYTM) throws DeapiException {
	put("ask_ytm", askYTM);
    }

    public float lastVolume() throws DeapiException {
	return getFloat("last_volume");
    }
    public void lastVolume(String lastVolume) throws DeapiException {
	put("last_volume", lastVolume);
    }

    public float last() throws DeapiException {
	return getFloat("last");
    }
    public void last(String last) throws DeapiException {
	put("last", last);
    }

    public float high() throws DeapiException {
	return getFloat("high");
    }
    public void high(String high) throws DeapiException {
	put("high", high);
    }

    public float low() throws DeapiException {
	return getFloat("low");
    }
    public void low(String low) throws DeapiException {
	put("low", low);
    }

    public float close() throws DeapiException {
	return getFloat("close");
    }
    public void close(String close) throws DeapiException {
	put("close", close);
    }

    public String tradingStatus() throws DeapiException {
	return getString("trading_status");
    }
    public void tradingStatus(String tradingStatus) throws DeapiException {
	put("trading_status", tradingStatus);
    }

    public String lastTradeTime() throws DeapiException {
	return getString("last_trade_time");
    }
    public void lastTradeTime(String lastTradeTime) throws DeapiException {
	put("last_trade_time", lastTradeTime);
    }

    public float bidValuation() throws DeapiException {
	return getFloat("bid_valuation");
    }
    public void bidValuation(String bidValuation) throws DeapiException {
	put("bid_valuation", bidValuation);
    }

    public float askValuation() throws DeapiException {
	return getFloat("ask_valuation");
    }
    public void askValuation(String askValuation) throws DeapiException {
	put("ask_valuation", askValuation);
    }
}

/*
  $log$
*/
