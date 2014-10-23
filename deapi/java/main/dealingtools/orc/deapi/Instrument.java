/* ----------------------------------------------------------------------
   Instrument - instrument_id, attributes + parameters

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 09:03:38 $
   $Source: /usr/export/cvsroot/deapi/java/main/dealingtools/orc/deapi/Instrument.java,v $
   $Revision: 1.10 $


   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

public class Instrument extends Message {
    public Instrument(Message message) {
	super(message);
    }

    public Instrument(String msg) throws DeapiException {
	super(msg);
    }

    public Instrument() { }

    public Instrument(int tag) {
	super();
	Instrument instrumentId = new Instrument();
	instrumentId.put("instrument_tag", new Integer(tag).toString());
	put("instrument_id", instrumentId);
    }

    public void reset() {
	Instrument instrumentId;

	// remove stuff that doesn't travel well
	try {
	    instrumentId().remove("instrument_tag");
	} catch (DeapiException it) {}
	try {
	    instrumentId().remove("underlying");
	    instrumentAttributes().remove("underlying");
	} catch (DeapiException u) {}
	try {
	    parameters().remove("dividendcontract");
	} catch (DeapiException dc) {}
	try {
	    basecontract().remove("instrument_tag");
	} catch (DeapiException bc) {}
	// failure to remove is OK
    }

    public void resetId() {
	Instrument instrumentId;

	// remove stuff that makes this instrument unique
	try {
	    instrumentId().remove("instrument_tag");
	} catch (DeapiException it) {}
	try {
	    instrumentId().remove("underlying");
	    instrumentAttributes().remove("underlying");
	} catch (DeapiException u) {}
	try {
	    instrumentId().remove("enforced_cutomer_unique_id");
	    parameters().remove("enforced_customer_unique_id");
	} catch (DeapiException u) {}
	try {
	    instrumentId().remove("feedcode");
	    parameters().remove("feedcode");
	} catch (DeapiException fc) {}
	try {
	    parameters().remove("basecontract");
	} catch (DeapiException bc) {}
	// failure to remove is OK
    }

    public boolean isOption() {
	try {
	    strikeprice();
	} catch (DeapiException e) {
	    return false;
	}
	return true;
    }

    public boolean hasExpiration() {
	try {
	    expirydate();
	} catch (DeapiException e) {
	    return false;
	}
	return true;
    }

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
    private Instrument instrumentIdCreate() {
	Instrument id = null;
	try {
	    id = instrumentId();
	} catch (DeapiException e) {
	    id = new Instrument();
	    put("instrument_id", id);
	}
	return id;
    }

    public Instrument instrumentAttributes() throws DeapiException {
	Instrument attrs;
	Message message = getMessage("instrument_attributes");
	if (message instanceof Instrument) {
	    attrs = (Instrument) message;
	} else {
	    attrs = new Instrument(message);
	    instrumentAttributes(attrs);
	}
	return attrs;
    }
    public void instrumentAttributes(Instrument attrs) throws DeapiException { 
	put("instrument_attributes", attrs);
    }
    private Instrument instrumentAttributesCreate() {
	Instrument attrs = null;
	try {
	    attrs = instrumentAttributes();
	} catch (DeapiException e) {
	    attrs = new Instrument();
	    put("instrument_attributes", attrs);
	}
	return attrs;
    }

    public Instrument parameters() throws DeapiException {
	Instrument params;
	Message message = getMessage("parameters");
	if (message instanceof Instrument) {
	    params = (Instrument) message;
	} else {
	    params = new Instrument(message);
	    parameters(params);
	}
	return params;
    }
    public void parameters(Instrument params) throws DeapiException { 
	put("parameters", params);
    }
    private Instrument parametersCreate() {
	Instrument params = null;
	try {
	    params = parameters();
	} catch (DeapiException e) {
	    params = new Instrument();
	    put("parameters", params);
	}
	return params;
    }

    // to keep the instruments simple we try to treat the instrument
    // and the instrument_id as the same thing even though they're not
    // this requires a bit of jigery pokery for when someone has an
    // instrument_id from somewhere like a Trade and they want to
    // treat it like an instrument... there will be no instrument_id
    // so we watch out for that, only for the instrument_id fields though

    public int instrumentTag() throws DeapiException {
	if (get("instrument_id") != null) {
	   return instrumentId().getInt("instrument_tag");
	} else {
	    return getInt("instrument_tag");
	}
    }
    public int tag() throws DeapiException {
	return instrumentTag();
    }

    public String market() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("market");
	} else {
	    return getString("market");
	}
    }
    public void market(String market) throws DeapiException {
	instrumentIdCreate().put("market", market);
	parametersCreate().put("market", market);
    }
    
    public String feedcode() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("feedcode");
	} else {
	    return getString("feedcode");
	}
    }
    public void feedcode(String feedcode) throws DeapiException {
	instrumentIdCreate().put("feedcode", feedcode);
	parametersCreate().put("feedcode", feedcode);
    }

    public String underlying() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("underlying");
	} else {
	    return getString("underlying");
	}
    }
    public void underlying(String underlying) throws DeapiException {
	instrumentIdCreate().put("underlying", underlying);
	instrumentAttributesCreate().put("underlying", underlying);
    }
    
    public String enforcedCustomerUniqueId() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("enforced_customer_unique_id");
	} else {
	    return getString("enforced_customer_unique_id");
	}
    }
    public void enforcedCustomerUniqueId(String enforced_customer_unique_id)
	throws DeapiException {
	instrumentIdCreate().put("enforced_customer_unique_id",
			    enforced_customer_unique_id);
	parametersCreate().put("enforced_customer_unique_id",
			  enforced_customer_unique_id);
    }

    public String customerUniqueId() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("customer_unique_id");
	} else {
	    return getString("customer_unique_id");
	}
    }
    public void customerUniqueId(String customer_unique_id)
	throws DeapiException {
	instrumentIdCreate().put("customer_unique_id", customer_unique_id);
	parametersCreate().put("customer_unique_id", customer_unique_id);
    }

    public String currency() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("currency");
	} else {
	    return getString("currency");
	}
    }
    public void currency(String currency) throws DeapiException {
	instrumentIdCreate().put("currency", currency);
	instrumentAttributesCreate().put("currency", currency);
    }

    public String isincode() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("isincode");
	} else {
	    return getString("isincode");
	}
    }
    public void isincode(String isincode) throws DeapiException {
	instrumentIdCreate().put("isincode", isincode);
	parametersCreate().put("isincode", isincode);
    }

    public String valoren() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("valoren");
	} else {
	    return getString("valoren");
	}
    }
    public void valoren(String valoren) throws DeapiException {
	instrumentIdCreate().put("valoren", valoren);
	parametersCreate().put("valoren", valoren);
    }

    public String sedol() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("sedol");
	} else {
	    return getString("sedol");
	}
    }
    public void sedol(String sedol) throws DeapiException {
	instrumentIdCreate().put("sedol", sedol);
	parametersCreate().put("sedol", sedol);
    }

    public String kind() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("kind");
	} else {
	    return getString("kind");
	}
    }
    public void kind(String kind) throws DeapiException {
	instrumentIdCreate().put("kind", kind);
	instrumentAttributesCreate().put("kind", kind);
    }

    public String strikeprice() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("strikeprice");
	} else {
	    return getString("strikeprice");
	}
    }
    public void strikeprice(String strikeprice) throws DeapiException {
	instrumentIdCreate().put("strikeprice", strikeprice);
	instrumentAttributesCreate().put("strikeprice", strikeprice);
    }

    public String expirydate() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("expirydate");
	} else {
	    return getString("expirydate");
	}
    }
    public void expirydate(String expirydate) throws DeapiException {
	instrumentIdCreate().put("expirydate", expirydate);
	instrumentAttributesCreate().put("expirydate", expirydate);
    }

    public String expirytype() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("expirytype");
	} else {
	    return getString("expirytype");
	}
    }
    public void expirytype(String expirytype) throws DeapiException {
	instrumentAttributesCreate().put("expirytype", expirytype);
    }

    public String optionKind() throws DeapiException {
	return expirytype() + " " + kind();
    }

    public String assettype() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("assettype");
	} else {
	    return getString("assettype");
	}
    }
    public void assettype(String assettype) throws DeapiException {
	instrumentAttributesCreate().put("assettype", assettype);
    }

    public String multiplier() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("multiplier");
	} else {
	    return getString("multiplier");
	}
    }
    public void multiplier(String multiplier) throws DeapiException {
	instrumentAttributesCreate().put("multiplier", multiplier);
    }

    public String description() throws DeapiException {
	if (get("instrument_id") != null) {
	    return instrumentId().getString("description");
	} else {
	    return getString("description");
	}
    }
    public void description(String description) throws DeapiException {
	parametersCreate().put("description", description);
    }

    // Parameters.... long list

    public Instrument basecontract() throws DeapiException {
	Instrument basecontract;
	
	Message message = parameters().getMessage("basecontract");
	if (message instanceof Instrument) {
	    basecontract = (Instrument) message;
	} else {
	    basecontract = new Instrument(message);
	    basecontract(basecontract);
	}
	return basecontract;
    }
    public void basecontract(Instrument basecontract) throws DeapiException { 
	parameters().put("basecontract", basecontract);
    }
    private Instrument basecontractCreate() {
	Instrument basecontract = null;
	try {
	    basecontract = basecontract();
	} catch (DeapiException e) {
	    basecontract = new Instrument();
	    parametersCreate().put("basecontract", basecontract);
	}
	return basecontract;
    }

    public Instrument dividendcontract() throws DeapiException {
	Instrument dividendcontract;
	
	Message message = parameters().getMessage("dividendcontract");
	if (message instanceof Instrument) {
	    dividendcontract = (Instrument) message;
	} else {
	    dividendcontract = new Instrument(message);
	    dividendcontract(dividendcontract);
	}
	return dividendcontract;
    }
    public void dividendcontract(Instrument dividendcontract)
	throws DeapiException { 
	parameters().put("dividendcontract", dividendcontract);
    }
    private Instrument dividendcontractCreate() {
	Instrument dividendcontract = null;
	try {
	    dividendcontract = dividendcontract();
	} catch (DeapiException e) {
	    dividendcontract = new Instrument();
	    parametersCreate().put("dividendcontract", dividendcontract);
	}
	return dividendcontract;
    }
}

/*
  $log$
*/
