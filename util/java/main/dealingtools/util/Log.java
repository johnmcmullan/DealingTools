/* ----------------------------------------------------------------------
   Log - process logging

   Original Author: John McMullan (2004)

   $Author$
   $Date$
   $Source$
   $Revision$

   ---------------------------------------------------------------------- */

package dealingtools.util;

import java.io.*;
import java.text.*;
import java.util.*;

public class Log {
    private static File logfile;
    private static String hostname;
    private static PrintWriter logwriter;
    private static int lineCount;
    private static int maxLogSize;
    private static DateFormat formatter;

    private Log() {}

    public static void start(String name, String logname) {
	StringBuffer path;
	// this returns colon...
	//	String sep = System.getProperty("path.separator");
	String sep = "/";
	if (logname.startsWith(sep)) {
	    // absolute
	    path = new StringBuffer(logname);
	} else {
	    // relative
	    String home = System.getProperty("user.home");
	    path = new StringBuffer(home);
	    path.append(sep);
	    path.append("log");
	    path.append(sep);
	    path.append(logname);
	    path.append(".log");
	}

	// open the logfile...
	try {
	    logfile = new File(path.toString());
	    if (!logfile.exists()) {
		new File(logfile.getParent()).mkdirs();
	    }
	    logwriter =
		new PrintWriter(new BufferedWriter(new FileWriter(logfile)));
	} catch (IOException e) {
	    System.err.println(e.toString());
	    start(name);
	}
	
	Log.initialize(name);
    }

    public static void start(String name) {
	logfile = null;
	logwriter = new PrintWriter(System.out, true);
	Log.initialize(name);
    }

    private static void initialize(String name) {
	formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,
						   DateFormat.SHORT);
	hostname = name;
	lineCount = 0;
	maxLogSize = 25000000; // 2.5MB Max
	Log.write("Log", "NEW LOG");
    }

    private static String timestamp() {
	return formatter.format(new Date());
    }
  
    private static void recycle() {
	String path = logfile.getPath();
	String name = logfile.getName();
	String fullname = path + name;
	String oldname = name + "-old";

	logwriter.println("Recycling logfile");
	logfile.renameTo(new File(oldname));
	logwriter.close();
	Log.start(hostname, fullname);
    }
  
    private static void header(String module) {
	// every 100 lines, check the file size is not too big
	if ((logfile != null) && ((lineCount % 100) == 0)) {
	    if (logfile.length() > maxLogSize) {
		Log.recycle();
	    }
	}
	lineCount++;
	logwriter.print(Log.timestamp());
	logwriter.print(" ");
	logwriter.print(hostname);
	logwriter.print(" ");
	logwriter.print(module);
	logwriter.print(": ");
    }

    public static void write(String module, String entry) {
	Log.header(module);
	logwriter.println(entry);
	logwriter.flush();
    }

    public static void write(String module, Object[] entries) {
	Log.header(module);

	for (int i = 0; i < entries.length; i++) {
	    logwriter.print(entries[i]);
	    if (i < entries.length) {
		logwriter.print(", ");
	    }
	}
	logwriter.print("\n");
	logwriter.flush();
    }
    public static void debug(String module, String entry) {
	if (Debug.isOn()) {
	    Log.write(module, entry);
	}
    }
			     
    public static void debug(String module, Object[] entries) {
	if (Debug.isOn()) {
	    Log.write(module, entries);
	}
    }
}

/*
  $Log$
*/









