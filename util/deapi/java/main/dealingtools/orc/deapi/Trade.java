/* ----------------------------------------------------------------------
   Trade - trade, instrument_id

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/main/dealingtools/orc/deapi/Trade.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

public class Trade extends Message {
    public Trade(Message message) {
	super(message);
    }

    public Trade(String msg) throws DeapiException {
	super(msg);
    }

    public Trade() { }

    public Trade trade() throws DeapiException {
	Trade trade;
	// try to get the Message...
	Message message = getMessage("trade");
	if (message instanceof Trade) {
	    // OK, it's already an Trade, cast it
	    trade = (Trade) message;
	} else {
	    // It's not already a trade, construct a trade
	    trade = new Trade(message);
	    // and replace the message with a trade
	    trade(trade);
	}
	return trade;
    }
    public void trade(Trade trade) throws DeapiException { 
	put("trade", trade);
    }
    public Trade tradeCreate() {
	Trade trade = null;
	try {
	    trade = trade();
	} catch (DeapiException e) {
	    trade = new Trade();
	    put("trade", trade);
	}
	return trade;
    }

    public int tradeTag() throws DeapiException {
	return getInt("trade_tag");
    }
    public int tag() throws DeapiException {
	return tradeTag();
    }

    public String tradeTime() throws DeapiException {
	return getString("trade_time");
    }
    public void tradeTime(String time) throws DeapiException {
	put("trade_time", time);
    }

    public int parentTag() throws DeapiException {
	return getInt("parent_tag");
    }

    public boolean deleted() throws DeapiException {
	return getBoolean("deleted");
    }
    public void deleted(boolean flag) throws DeapiException {
	put("deleted", flag ? "True" : "False");
    }

    public boolean verified() throws DeapiException {
	return getBoolean("verified");
    }
    public void verified(boolean flag) throws DeapiException {
	put("verified", flag ? "True" : "False");
    }

    public boolean counterpartTrade() throws DeapiException {
	return getBoolean("counterpart_trade");
    }
    public void counterpartTrade(boolean flag) throws DeapiException {
	put("counterpart_trade", flag ? "True" : "False");
    }

    public String whyChanged() throws DeapiException {
	return getString("why_changed");
    }
    public void whyChanged(String why) throws DeapiException {
	put("why_changed", why);
    }

    public Instrument instrumentId() throws DeapiException {
	Instrument id;
	// try to get the Message...
	Message message = trade().getMessage("instrument_id");
	if (message instanceof Instrument) {
	    // OK, it's already an Instrument, cast it
	    id = (Instrument) message;
	} else {
	    // It's not already an instrument, construct an instrument
	    id = new Instrument(message);
	    // and replace the message with an instrument
	    tradeCreate().put("instrument_id", id);
	}
	return id;
    }
    public void instrumentId(Instrument id) throws DeapiException { 
	tradeCreate().put("instrument_id", id);
    }

    public AdditionalData additionalData() throws DeapiException {
	AdditionalData data;
	// try to get the Message...
	Message message = trade().getMessage("additional_data");
	if (message instanceof AdditionalData) {
	    // OK, it's already AdditionalData, cast it
	    data = (AdditionalData) message;
	} else {
	    // It's not already AdditionalData, construct an instrument
	    data = new AdditionalData(message);
	    // and replace the message with AdditionalData
	    tradeCreate().put("additional_data", data);
	}
	return data;
    }
    public void additionalData(AdditionalData data) throws DeapiException { 
	tradeCreate().put("additional_data", data);
    }

    public String price() throws DeapiException {
	return trade().getString("price");
    }
    public void price(String price) throws DeapiException {
	tradeCreate().put("price", price);
    }

    public String volume() throws DeapiException {
	return trade().getString("volume");
    }
    public void volume(String volume) throws DeapiException {
	tradeCreate().put("volume", volume);
    }

    public String buyOrSell() throws DeapiException {
	return trade().getString("buy_or_sell");
    }
    public void buyOrSell(String buyOrSell) throws DeapiException {
	tradeCreate().put("buy_or_sell", buyOrSell);
    }

    public String buyer() throws DeapiException {
	return trade().getString("buyer");
    }
    public void buyer(String buyer) throws DeapiException {
	tradeCreate().put("buyer", buyer);
    }

    public String seller() throws DeapiException {
	return trade().getString("seller");
    }
    public void seller(String seller) throws DeapiException {
	tradeCreate().put("seller", seller);
    }

    public String counterpart() throws DeapiException {
	return trade().getString("counterpart");
    }
    public void counterpart(String counterpart) throws DeapiException {
	tradeCreate().put("counterpart", counterpart);
    }

    public String exchangeOrderId() throws DeapiException {
	return trade().getString("exchange_order_id");
    }
    public void exchangeOrderId(String exchangeOrderId) throws DeapiException {
	tradeCreate().put("exchange_order_id", exchangeOrderId);
    }

    public String exchangeTradeId() throws DeapiException {
	return trade().getString("exchange_trade_id");
    }
    public void exchangeTradeId(String exchangeTradeId) throws DeapiException {
	tradeCreate().put("exchange_trade_id", exchangeTradeId);
    }

    public String settlementDays() throws DeapiException {
	return trade().getString("settlement_days");
    }
    public void settlementDays(String settlementDays) throws DeapiException {
	tradeCreate().put("settlement_days", settlementDays);
    }

    public String exchangeTimestamp() throws DeapiException {
	return trade().getString("exchange_timestamp");
    }
    public void exchangeTimestamp(String exchangeTimestamp)
	throws DeapiException {
	tradeCreate().put("exchange_timestamp", exchangeTimestamp);
    }

    public String tradeDate() throws DeapiException {
	return trade().getString("trade_date");
    }
    public void tradeDate(String tradeDate) throws DeapiException {
	tradeCreate().put("trade_date", tradeDate);
    }

    public String nxorcUser() throws DeapiException {
	return trade().getString("nxorc_user");
    }
    public void nxorcUser(String nxorcUser) throws DeapiException {
	tradeCreate().put("nxorc_user", nxorcUser);
    }

    public String portfolio() throws DeapiException {
	return trade().getString("portfolio");
    }
    public void portfolio(String portfolio) throws DeapiException {
	tradeCreate().put("portfolio", portfolio);
    }

    public String invested() throws DeapiException {
	return trade().getString("invested");
    }
    public void invested(String invested) throws DeapiException {
	tradeCreate().put("invested", invested);
    }

    public String accrued() throws DeapiException {
	return trade().getString("accrued");
    }
    public void accrued(String accrued) throws DeapiException {
	tradeCreate().put("accrued", accrued);
    }

    public String fee() throws DeapiException {
	return trade().getString("fee");
    }
    public void fee(String fee) throws DeapiException {
	tradeCreate().put("fee", fee);
    }

    public String fxRate() throws DeapiException {
	return trade().getString("fx_rate");
    }
    public void fxRate(String fxRate) throws DeapiException {
	tradeCreate().put("fx_rate", fxRate);
    }

    public String comment() throws DeapiException {
	return trade().getString("comment");
    }
    public void comment(String comment) throws DeapiException {
	tradeCreate().put("comment", comment);
    }

    public String dateCreated() throws DeapiException {
	return trade().getString("date_created");
    }
    public void dateCreated(String dateCreated) throws DeapiException {
	tradeCreate().put("date_created", dateCreated);
    }

    public String timeCreated() throws DeapiException {
	return trade().getString("time_created");
    }
    public void timeCreated(String timeCreated) throws DeapiException {
	tradeCreate().put("time_created", timeCreated);
    }

    public String timeChanged() throws DeapiException {
	return trade().getString("time_changed");
    }
    public void timeChanged(String timeChanged) throws DeapiException {
	tradeCreate().put("time_changed", timeChanged);
    }

    public String customerReference() throws DeapiException {
	return trade().getString("customer_reference");
    }
    public void customerReference(String customerReference)
	throws DeapiException {
	tradeCreate().put("customer_reference", customerReference);
    }

    public String currency() throws DeapiException {
	return trade().getString("currency");
    }
    public void currency(String currency) throws DeapiException {
	tradeCreate().put("currency", currency);
    }

    public String market() throws DeapiException {
	return trade().getString("market");
    }
    public void market(String market) throws DeapiException {
	tradeCreate().put("market", market);
    }
}

/*
  $log$
*/





