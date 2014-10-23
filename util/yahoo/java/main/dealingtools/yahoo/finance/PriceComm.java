/* ----------------------------------------------------------------------
   PriceFeed - class for getting a price feed from Yahoo Finance

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/yahoo/java/main/dealingtools/yahoo/finance/PriceComm.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package dealingtools.yahoo.finance;

import java.util.*;
import java.net.*;
import java.io.*;

public class PriceComm {
    private String baseUrl = null;
    private String subscriptionFormat = null;
    private HashSet rics = null;
    private HashSet items = null;

    public PriceComm(String baseUrl) {
	initialize();
	this.baseUrl = baseUrl;
    }
    public PriceComm() {
	initialize();
    }

    private void initialize() {
	rics = new HashSet();
	items = new HashSet();

	// subscription format
	// b6b3b2a5
	items.add("b6");
	items.add("b3");
	items.add("b2");
	items.add("a5");

	baseUrl = "http://quote.yahoo.com";
    }

    public void addItem(String fmt) {
	items.add(fmt);
    }

    public void subscribe(String ric) {
	rics.add(ric);
    }

    public void getPrices() {
	try {
	    URL yahoo = new URL(toString());
	    BufferedReader in =
		new BufferedReader(new InputStreamReader(yahoo.openStream()));

	    String inputLine;
	    while ((inputLine = in.readLine()) != null) {
		System.out.println(itemsToString());
		System.out.println(inputLine);
	    }
	    in.close();
	} catch (Exception e) {}
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append(baseUrl);
	buf.append("/d?f=\"");
	buf.append(itemsToString());
	buf.append("\"&s=\"");
	buf.append(ricsToString());
	buf.append("\"");
	return buf.toString();
    }

    private String ricsToString() {
	StringBuffer buf = new StringBuffer();
	String ric;
	Iterator p = rics.iterator();
	while (p.hasNext()) {
	    ric = (String) p.next();
	    buf.append(ric);
	    if (p.hasNext()) {
		buf.append(",");
	    }
	}
	return buf.toString();
    }

    private String itemsToString() {
	StringBuffer buf = new StringBuffer();
	String fmt;
	Iterator f = items.iterator();
	while (f.hasNext()) {
	    fmt = (String) f.next();
	    buf.append(fmt);
	}
	return buf.toString();
    }

    private String itemsToCSV() {
	StringBuffer buf = new StringBuffer();
	String fmt;
	Iterator f = items.iterator();
	while (f.hasNext()) {
	    fmt = (String) f.next();
	    buf.append(fmt);
	    if (f.hasNext()) {
		buf.append(",");
	    }
	}
	return buf.toString();
    }
}


/*
  $log$
*/


