/* ----------------------------------------------------------------------
   DefaultArgs - basic arguments for all code

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 10:35:23 $
   $Source: /usr/export/cvsroot/util/java/main/dealingtools/util/Args.java,v $
   $Revision: 1.1 $

   ---------------------------------------------------------------------- */

package dealingtools.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import gnu.getopt.*;

public class Args {
    private StringBuffer optionString;
    private ArrayList<LongOpt> options;
    private HashMap<String, String> extras;
    private static String appName = null;
    private String orcServer;
    private String orcPort;
    private String orcLoginId;
    private String orcPassword;
    private String logname;
    private String dbDatabase;
    private String dbHost;
    private String dbUser;
    private String dbPassword;
    private String[] emailAddresses;

    public Args(String argv[]) {
        initializeDefaults();
        extras = null;
        if ((argv != null) && (argv.length > 0))
            parseOptions(argv);
    }
    public Args(String argv[], HashMap<String, String> extraOptions) {
        initializeDefaults();
        extras = extraOptions;
        initializeExtras();
        if ((argv != null) && (argv.length > 0))
            parseOptions(argv);
    }

    private void initializeDefaults() {
    	optionString = new StringBuffer("x:y:u:U:l:dhb:B:e:E:n:z:m:");
    	options = new ArrayList<LongOpt>();

    	orcServer = "localhost";	
    	orcPort = "9010";	
    	orcLoginId = "NO_LOGIN_ID";
    	orcPassword = "";
    	logname = null;
    	dbDatabase = "";
    	dbHost = "";
    	dbUser = "";
    	dbPassword = "";
    	emailAddresses = null;

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
    			null, 'b'));
    	options.add(new LongOpt("database_host", LongOpt.REQUIRED_ARGUMENT,
    			null, 'B'));
    	options.add(new LongOpt("database_user", LongOpt.REQUIRED_ARGUMENT,
    			null, 'e'));
    	options.add(new LongOpt("database_password", LongOpt.REQUIRED_ARGUMENT,
    			null, 'E'));

    	// this option is ignored, we only use it for finding the
    	// correct process instance in the ps output
    	options.add(new LongOpt("procName", LongOpt.REQUIRED_ARGUMENT,
    			null, 'n'));

    	options.add(new LongOpt("email_address", LongOpt.REQUIRED_ARGUMENT,
    			null, 'm'));
    }

    // NB I couldn't be bothered doing extra flag options...
    private void initializeExtras() {
        int optionNumber = 0;
        Iterator<String> i = extras.keySet().iterator();

        while (i.hasNext()) {
            String key = (String) i.next();
            optionString.append(optionNumber + ":");
            options.add(new LongOpt(key, LongOpt.REQUIRED_ARGUMENT,
                null, optionNumber++));
        }
    }

    private void parseOptions(String argv[]) {
    	int c;
    	LongOpt[] longopts = (LongOpt[]) options.toArray(new LongOpt[0]);

    	Getopt g =
    		new Getopt(appName(), argv, optionString.toString(), longopts);
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
    		case 'b':
    			dbDatabase = g.getOptarg();
    			break;
    		case 'B':
    			dbHost = g.getOptarg();
    			break;
    		case 'e':
    			dbUser = g.getOptarg();
    			break;
    		case 'E':
    			dbPassword = g.getOptarg();
    			break;
            case 'm':
    			emailAddresses = g.getOptarg().split(",");
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
    				String key = longopts[g.getLongind()].getName();
    				extras.put(key, g.getOptarg());
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
    	reply.append("-b/--database=<database>[");
    	reply.append(dbDatabase);
    	reply.append("]-B/--database_host=<hostname>[");
    	reply.append(dbHost);
    	reply.append("]\n");
    	reply.append("-e/--database_user=<username>[");
    	reply.append(dbUser);
    	reply.append("]-E/--database_password=<password>[");
    	reply.append(dbPassword);
    	reply.append("]\n");
    	reply.append("]-m/--email_address=<email_address[,email_address]...>[");
    	if (emailAddresses != null) {
            for (String addr : emailAddresses)
                reply.append(addr + ",");
    	}
    	reply.append("]\n");

    	Iterator<String> i = extras.keySet().iterator();
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

    public static String appName() {
    	if (appName == null) {
    		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length > 1)
                appName = stackTrace[1].getClassName();
            else 
                appName = stackTrace[0].getClassName();
            if (appName.contains(".")) {
                String[] appPath = appName.split("\\.");
                appName = appPath[appPath.length -1];
            }
    	}
    	return appName;
    }
    public String orcServer() {
    	return orcServer;
    }
    public String orcPort() {
    	return orcPort;
    }
    public String orcLoginId() {
    	return orcLoginId;
    }
    public String orcPassword() {
    	return orcPassword;
    }
    public String logname() {
    	return logname;
    }
    public String dbDatabase() {
    	return dbDatabase;
    }
    public String dbHost() {
    	return dbHost;
    }
    public String dbUser() {
    	return dbUser;
    }
    public String dbPassword() {
    	return dbPassword;
    }
    public String[] emailAddresses() {
    	return emailAddresses;
    }
}

/*
  $log$
*/
