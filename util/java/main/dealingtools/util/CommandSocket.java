/* ----------------------------------------------------------------------
   CommandSocket - socket server for CommandInterpreter

   Original Author: John McMullan (2004)

   $Author$
   $Date$
   $Source$
   $Revision$

   ---------------------------------------------------------------------- */

package dealingtools.util;

import java.io.*;
import java.net.*;


public class CommandSocket extends Thread {
    private ServerSocket listener;

    public CommandSocket(int port) throws IOException {
	initializeConnection(port);
    }

    private void initializeConnection(int port) throws IOException {
	listener = new ServerSocket(port);
	setDaemon(true);
	start();
    }

    public void run() {
	Socket client;
	CommandInterpreter interp;

	while (true) {
	    try {
		client = listener.accept();
		interp = new CommandInterpreter(client.getInputStream(),
						client.getOutputStream());
	    } catch (SocketException se) {
		// someone has closed our socket, this could have been
		// the client closing theirs though... ignore it
	    }
	    catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
    }
}

/*
  $Log$
*/
