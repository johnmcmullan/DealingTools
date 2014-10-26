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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;


public class Connection {
	private Socket sock;
    private Reader inbuf;
    private Writer outbuf;
    static private int HeaderSize = 10;

    public Connection(String hostname, String service) throws IOException, UnknownHostException {
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

    public Connection(String hostname, int port) throws IOException, UnknownHostException {
    	initialize(hostname, port);
    }

    private void initialize(String hostname, int port) throws IOException, UnknownHostException {
    	sock = new Socket(hostname, port);
    	inbuf = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    	outbuf = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
    }

    public void close() throws IOException {
    	sock.close();
    }

    public synchronized void send(String msg) throws IOException {
    	DecimalFormat formatter = new DecimalFormat("0000000000");
    	byte[] bytes = msg.getBytes("ASCII");
    	String asciiMsg = new String(bytes);
    	int len = asciiMsg.length();

    	outbuf.write(formatter.format(len));
    	outbuf.write(asciiMsg);
    	outbuf.flush();
    }

    public Message recieve() throws IOException, DeapiException {
    	Message reply;

    	inbuf.skip(HeaderSize);
    	reply = new Message(inbuf);
    	// skip over stupid, undocumented \n
    	//inbuf.skip(1);
    	return reply;
    }
}
	
/*
  $log$
*/
