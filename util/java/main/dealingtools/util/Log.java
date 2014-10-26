/* ----------------------------------------------------------------------
   Log - process logging

   Original Author: John McMullan (2004)

   $Author$
   $Date$
   $Source$
   $Revision$

   ---------------------------------------------------------------------- */

package dealingtools.util;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {
    private static Logger log = null;
	private static String hostname;
	private static Level globalLevel = Level.INFO;
	private static final int maxLogSize = 100000000;
	private static final int numLogFiles = 5;

    public static class NSLogStyleFormatter extends Formatter {
    	private SimpleDateFormat nsLogDateFormat;
    	public NSLogStyleFormatter() {
    		super();
    		nsLogDateFormat = new SimpleDateFormat("MMM dd HH:mm:ss.SSS");
    	}

        @Override
    	public String format(LogRecord record) {
    		
    		// Create a StringBuffer to contain the formatted record
    		// start with the date.
    		StringBuffer sb = new StringBuffer();
    		
    		// Get the date from the LogRecord and add it to the buffer
    		Date date = new Date(record.getMillis());
    		sb.append(nsLogDateFormat.format(date));
    		sb.append(" ");
    		
    		// Get the hostname
            sb.append(hostname);
            sb.append(" ");
    		
            // Get the classname
            String className = record.getSourceClassName();
            String methodName = record.getSourceMethodName();
            if (className.length() == 0) {
            	// incomplete Log record... go digging
            	StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            	StackTraceElement e = null;
            	// look for who called us
            	for (int i = 9; i < stackTrace.length; i++) {
            		e = stackTrace[i];
            		if (e.getLineNumber() != -1)
            			break;
            	}
            	if (e != null) {
            		className = e.getClassName();
            		methodName = e.getMethodName();
            	} else {
            		className = "UnknownClass";
            		methodName = "UnknownMethod";
            	}
            }
            if (className.contains(".")) {
            	String[] classPath = className.split("\\.");
            	className = classPath[classPath.length - 1];
            }
            sb.append(className);
            sb.append(String.format("(%s)", methodName));
            
            // And the process/thread ids
            sb.append(String.format("[%d]/", record.getThreadID()));
            
    		// Get the level name and add it to the buffer
    		sb.append(record.getLevel().getName());
    		sb.append(": ");
    		 
    		// Get the formatted message (includes localisation 
    		// and substitution of parameters) and add it to the buffer
    		sb.append(formatMessage(record));
    		sb.append("\n");

    		return sb.toString();
    	}
    }
    
    public static void init(String logName) {
    	String appName = Args.appName();
    	
    	LogManager logManager = LogManager.getLogManager();
    	logManager.reset();
    	
    	log = Logger.getLogger(appName);
    	
    	NSLogStyleFormatter formatter = new NSLogStyleFormatter();
        
    	try {
            hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "unknown";
		}
    	
        if (logName != null) {
            try {
                Handler fileHandler =
                    new FileHandler(logName, maxLogSize, numLogFiles, true);
                log.setUseParentHandlers(false);
                log.addHandler(fileHandler);
                log.info(String.format("Starting log for %s", appName));
            } catch (IOException e) {
                // ignore it... just log to the console
            }
        } else {
        	Handler consoleHandler = new ConsoleHandler();
        	log.addHandler(consoleHandler);
        }

        // redirect stdout and stderr to our logging stream
        LoggingOutputStream los = new LoggingOutputStream(log, globalLevel);
        PrintStream ps = new PrintStream(los, true);
        System.setOut(ps);
        System.setErr(ps);
        
        for (Handler handler : log.getHandlers()) {
			handler.setFormatter(formatter);
			handler.setLevel(globalLevel);
		}
    }
    
    public static Logger logger() {
    	if (log == null)
    		Log.init(null);
    	return log;
    }
    
    public static void setLevel(Level level) {
    	globalLevel = level;
    	if (log != null) {
    		for (Handler handler : log.getHandlers()) {
    			handler.setLevel(globalLevel);
    		}
    	}
    }
}

/*
  $log$
*/
