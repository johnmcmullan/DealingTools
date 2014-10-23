/* ----------------------------------------------------------------------
   DefaultArgs - basic arguments for all code

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 10:35:23 $
   $Source: /usr/export/cvsroot/util/java/main/dealingtools/util/Args.java,v $
   $Revision: 1.1 $

   ---------------------------------------------------------------------- */

package dealingtools.util;

import java.util.*;

import gnu.getopt.*;

public class Args {
    private StringBuffer optionString;
    private ArrayList options;
    private HashMap extras;
    private String appName;
    private String orcServer;
    private String orcPort;
    private String orcLoginId;
    private String orcPassword;
    private String logname;
    private String database;
    private String commandPort;

    public Args(String name, String argv[]) {
	appName = name;
	initializeDefaults();
	extras = null;
	if ((argv != null) && (argv.length > 0))
	    parseOptions(argv);;
    }
    public Args(String name, String argv[], HashMap extraOptions) {
	appName = name;
	initializeDefaults();
	extras = extraOptions;
	initializeExtras();
	if ((argv != null) && (argv.length > 0))
	    parseOptions(argv);
    }

    private void initializeDefaults() {
	optionString = new StringBuffer("x:y:u:U:l:dhD:n:z:");
	options = new ArrayList();

	orcServer = new String("localhost");
	orcPort = new String("6980");
	orcLoginId = new String("NO_LOGIN_ID");
	orcPassword = new String("");
	logname = new String(appName);
	database = new String(appName);
	commandPort = new String("3003");

	options.add(new LongOpt("orc_server", LongOpt.REQUIRED_ARGUMENT, 
				null, 'x'));
	options.add(new LongOpt("orc_port", LongOpt.REQUIRED_ARGUMENT,
				null, 'y'));
	options.add(new LongOpt("orc_login_id", LongOpt.REQUIRED_ARGUMENT,
				null, 'u'));
	options.add(new LongOpt("orc_password", LongOpt.REQUIRED_ARGUMENT,
				null, 'U'));
	options.add(new LongOpt("logname", LongOpt.REQUIRED_ARGUMENT,
				null, 'l'));
	options.add(new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'));
	options.add(new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'));
	options.add(new LongOpt("database", LongOpt.REQUIRED_ARGUMENT,
				null, 'D'));

	// this option is ignored, we only use it for finding the
	// correct process instance in the ps output
	options.add(new LongOpt("procName", LongOpt.REQUIRED_ARGUMENT,
				null, 'n'));

	options.add(new LongOpt("command_port", LongOpt.REQUIRED_ARGUMENT,
				null, 'z'));
    }

    // NB I couldn't be bothered doing extra flag options...
    private void initializeExtras() {
	int optionNumber = 0;
	Iterator i = extras.keySet().iterator();
	String key;

	while (i.hasNext()) {
	    key = (String) i.next();
	    optionString.append(optionNumber + ":");
	    options.add(new LongOpt(key, LongOpt.REQUIRED_ARGUMENT,
				    null, optionNumber++));
	}
    }

    private void parseOptions(String argv[]) {
	int c;
	LongOpt[] longopts = (LongOpt[]) options.toArray(new LongOpt[0]);
	String key;
	StringBuffer value;

	Getopt g =
	    new Getopt(appName, argv, optionString.toString(), longopts);
	//	g.setOpterr(false); // We'll do our own error handling

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'x':
		orcServer = g.getOptarg();
		break;
	    case 'y':
		orcPort = g.getOptarg();
		break;
	    case 'u':
		orcLoginId = g.getOptarg();
		break;
	    case 'U':
		orcPassword = g.getOptarg();
		break;
	    case 'l':
		logname = g.getOptarg();
		break;
	    case 'n':
		// we ignore this... it's just for naming the process
		break;
	    case 'D':
		database = g.getOptarg();
		break;
	    case 'z':
		commandPort = g.getOptarg();
		break;

	    case 'd':
		// set the debug to "ON"
		Debug.toggleOn();
		break;

	    case 'h':
		usage();
		System.exit(0);
		break;
	    default:
		if (extras != null) {
		    // extra options...
		    key = longopts[g.getLongind()].getName();
		    value = (StringBuffer) extras.get(key);
		    value.setLength(0);
		    value.append(g.getOptarg());
		}
		break;
	    }
	}
    }

    public String toString() {
	StringBuffer reply = new StringBuffer();
	String key;

	reply.append("-x/--orc_server=<hostname/IP address>[");
	reply.append(orcServer);
	reply.append("]\n");
	reply.append("-y/--orc_port=<IP port>[");
	reply.append(orcPort);
	reply.append("]\n");
	reply.append("-u/--orc_login_id=<username>[");
	reply.append(orcLoginId);
	reply.append("]\n");
	reply.append("-U/--orc_password=<password>[");
	reply.append(orcPassword);
	reply.append("]\n");
	reply.append("-l/--logname=<password>[");
	reply.append(logname);
	reply.append("]\n");
	reply.append("-d/--database=<DB Spec>[");
	reply.append(database);
	reply.append("]\n");
	reply.append("-z/--command_port=<IP port>[");
	reply.append(commandPort);
	reply.append("]\n");


	Iterator i = extras.keySet().iterator();
	while (i.hasNext()) {
	    key = (String) i.next();
	    reply.append(key);
	    reply.append("=<string>[");
	    reply.append(extras.get(key).toString());
	    reply.append("]\n");
	}
	reply.append("-d/--debug\n-h/--help");

	return reply.toString();
    }

    private void usage() {
	System.out.println("Usage:");
	System.out.println(toString());
    }

    public String appName() {
	return appName;
    }
    public String orcServer() {
	return orcServer.toString();
    }
    public String orcPort() {
	return orcPort.toString();
    }
    public String orcLoginId() {
	return orcLoginId.toString();
    }
    public String orcPassword() {
	return orcPassword.toString();
    }
    public String logname() {
	return logname.toString();
    }
    public String database() {
	return database.toString();
    }
    public int commandPort() {
	return new Integer(commandPort.toString()).intValue();
    }
} 

