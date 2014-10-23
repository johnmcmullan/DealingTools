/* ----------------------------------------------------------------------
   Price - 

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/yahoo/java/main/dealingtools/yahoo/finance/Price.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package dealingtools.yahoo.finance;

import java.util.*;

public class Price extends HashMap {
    private static HashMap priceFormats = null;

    public Price() {
	initialize();
    }

    private void initialize() {
	if (priceFormats == null) {
	    priceFormats = new HashMap();
	    // database one day...
	    priceFormats.put("s","Symbol");
	    priceFormats.put("n","Name");
	    priceFormats.put("l","Last Trade (With Time)");
	    priceFormats.put("l1","Last Trade (Price Only)");
	    priceFormats.put("d1","Last Trade Date");
	    priceFormats.put("t1","Last Trade Time");
	    priceFormats.put("k3","Last Trade Size");
	    priceFormats.put("c","Change and Percent Change");
	    priceFormats.put("c1","Change");
	    priceFormats.put("p2","Change in Percent");
	    priceFormats.put("t7","Ticker Trend");
	    priceFormats.put("v","Volume");
	    priceFormats.put("a2","Average Daily Volume");
	    priceFormats.put("i","More Info");
	    priceFormats.put("t6","Trade Links");
	    priceFormats.put("b","Bid");
	    priceFormats.put("b6","Bid Size");
	    priceFormats.put("a","Ask");
	    priceFormats.put("a5","Ask Size");
	    priceFormats.put("p","Previous Close");
	    priceFormats.put("o","Open");
	    priceFormats.put("m","Day's Range");
	    priceFormats.put("w","52-week Range");
	    priceFormats.put("j5","Change From 52-wk Low");
	    priceFormats.put("j6","Pct Chg From 52-wk Low");
	    priceFormats.put("k4","Change From 52-wk High");
	    priceFormats.put("k5","Pct Chg From 52-wk High");
	    priceFormats.put("e","Earnings/Share");
	    priceFormats.put("r","P/E Ratio");
	    priceFormats.put("s7","Short Ratio");
	    priceFormats.put("r1","Dividend Pay Date");
	    priceFormats.put("q","Ex-Dividend Date");
	    priceFormats.put("d","Dividend/Share");
	    priceFormats.put("y","Dividend Yield");
	    priceFormats.put("f6","Float Shares");
	    priceFormats.put("j1","Market Capitalization");
	    priceFormats.put("t8","1yr Target Price");
	    priceFormats.put("e7","EPS Est. Current Yr");
	    priceFormats.put("e8","EPS Est. Next Year");
	    priceFormats.put("e9","EPS Est. Next Quarter");
	    priceFormats.put("r6","Price/EPS Est. Current Yr");
	    priceFormats.put("r7","Price/EPS Est. Next Yr");
	    priceFormats.put("r5","PEG Ratio");
	    priceFormats.put("b4","Book Value");
	    priceFormats.put("p6","Price/Book");
	    priceFormats.put("p5","Price/Sales");
	    priceFormats.put("j4","EBITDA");
	    priceFormats.put("m3","50-day Moving Avg");
	    priceFormats.put("m7","Change From 50-day Moving Avg");
	    priceFormats.put("m8","Pct Chg From 50-day Moving Avg");
	    priceFormats.put("m4","200-day Moving Avg");
	    priceFormats.put("m5","Change From 200-day Moving Avg");
	    priceFormats.put("m6","Pct Chg From 200-day Moving Avg");
	    priceFormats.put("s1","Shares Owned");
	    priceFormats.put("p1","Price Paid");
	    priceFormats.put("c3","Commission");
	    priceFormats.put("v1","Holdings Value");
	    priceFormats.put("w1","Day's Value Change");
	    priceFormats.put("g1","Holdings Gain Percent");
	    priceFormats.put("g4","Holdings Gain");
	    priceFormats.put("d2","Trade Date");
	    priceFormats.put("g3","Annualized Gain");
	    priceFormats.put("l2","High Limit");
	    priceFormats.put("l3","Low Limit");
	    priceFormats.put("n4","Notes");
	    priceFormats.put("k1","Last Trade (Real-time) with Time");
	    priceFormats.put("b3","Bid (Real-time)");
	    priceFormats.put("b2","Ask (Real-time)");
	    priceFormats.put("k2","Change Percent (Real-time)");
	    priceFormats.put("c6","Change (Real-time)");
	    priceFormats.put("v7","Holdings Value (Real-time)");
	    priceFormats.put("w4","Day's Value Change (Real-time)");
	    priceFormats.put("g5","Holdings Gain Pct (Real-time)");
	    priceFormats.put("g6","Holdings Gain (Real-time)");
	    priceFormats.put("m2","Day's Range (Real-time)");
	    priceFormats.put("j3","Market Cap (Real-time)");
	    priceFormats.put("r2","P/E (Real-time)");
	    priceFormats.put("c8","After Hours Change (Real-time)");
	    priceFormats.put("i5","Order Book (Real-time)");
	    priceFormats.put("x","Stock Exchange");
	}
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
	    // OK, it"s already an Instrument, cast it
	    id = (Instrument) message;
	} else {
	    // It"s not already an instrument, construct an instrument
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
