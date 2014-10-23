/* ----------------------------------------------------------------------
   Connect - base class for connecting to an Orc instance

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/14 13:23:30 $
   $Source: /usr/export/cvsroot/deapi/java/main/dealingtools/orc/deapi/Connection.java,v $
   $Revision: 1.3 $

   low level socket connectivity, needs to be overridden by a class
   that knows about logging into the API and creating messages from
   the stream

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;

public class Connection {
    private Socket deapi;
    private BufferedInputStream inbuf;
    private BufferedOutputStream outbuf;
    static private int HeaderSize = 10;

    public Connection(String hostname, String service)
	throws IOException, UnknownHostException {
	Integer port;

	// was service a port or a servicename?
	try {
	    port = new Integer(service);
	} catch (NumberFormatException e) {
	    // OK, it must be a service name, do this for now
	    port = new Integer(6980);
	}
	initialize(hostname, port.intValue());
    }

    public Connection(String hostname, int port)
	throws IOException, UnknownHostException {
	initialize(hostname, port);
    }

    private void initialize(String hostname, int port)
	throws IOException, UnknownHostException {
	deapi = new Socket(hostname, port);
	inbuf = new BufferedInputStream(deapi.getInputStream());
	outbuf = new BufferedOutputStream(deapi.getOutputStream());
    }

    public void close() throws IOException {
	deapi.close();
    }

    public synchronized void send(String msg) throws IOException {
	DecimalFormat formatter = new DecimalFormat("0000000000");
	int len = msg.length();

	outbuf.write(formatter.format(len).getBytes(), 0, HeaderSize);
	outbuf.write(msg.getBytes(), 0, len);
	outbuf.flush();
    }

    public Message recieve() throws IOException, DeapiException {
	Message reply;

	inbuf.skip(HeaderSize);
	reply = new Message(inbuf);
	// skip over stupid, undocumented \n
	inbuf.skip(1);
	return reply;
    }
}
	
/*
  $log$
*/
