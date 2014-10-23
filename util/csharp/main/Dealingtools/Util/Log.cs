/* ----------------------------------------------------------------------
   Log - Error an debug logging

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/04/27 11:05:43 $
   $Source: /usr/export/cvsroot/util/csharp/main/Dealingtools/Util/Log.cs,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

using System;
using System.Runtime.InteropServices;

namespace Dealingtools {
	namespace Util {
		public class Log {
			private static Log instance = null;
			private Storage db = null;
			private const string log = "Application"; // we're always the application log

			private Log() {
				db = Storage.Instance();
			}

			public static Log Instance() {
				if (instance == null) {
					instance = new Log();
				}
				return instance;
			}

			// this is public for COMLog to use
			public void WriteEvent(string source, string msg, params object[] objects) {
				if (!System.Diagnostics.EventLog.SourceExists(source)) {
					System.Diagnostics.EventLog.CreateEventSource(source, log);
				}
				System.Diagnostics.EventLog.WriteEntry(source, msg);
			}

			// as is this
			public string AddDebugInfo(string msg) {
				System.Text.StringBuilder newmsg = new System.Text.StringBuilder();
				// create the stack frame for the function that called Debug, i.e. 2 up
				System.Diagnostics.StackFrame sf = new System.Diagnostics.StackFrame(2, true);
				// save the method name
				string methodName = sf.GetMethod().ToString();
				// save the file name
				string fileName = sf.GetFileName();
				// save the line number
				int lineNumber = sf.GetFileLineNumber();

				// Adjust our format
				newmsg.AppendFormat("DEBUG {0}@{1}:{2} {3}", methodName, fileName, lineNumber, msg);

				return newmsg.ToString();
			}
			
			[System.Diagnostics.Conditional("DEBUG")] public void Debug(string fmt, params object[] objects) {
				string newfmt = AddDebugInfo(fmt);
				WriteEvent(db.AppName, newfmt, objects);
			}

			public void Write(string fmt, params object[] objects) {
				WriteEvent(db.AppName, fmt, objects);
			}
		}

		// singleton wrapper for use by COM
		[ClassInterface(ClassInterfaceType.AutoDual)] public class COMLog {
			private Log log = null;
			private Storage db = null;

			public COMLog() {
				log = Log.Instance();
				db = Storage.Instance();
			}

			// these are reimplemented here instead of being called becuase they rely on
			// being able to look in the stack frame relative to where they are to deterime
			// where we were called from etc

			[System.Diagnostics.Conditional("DEBUG")] public void Debug(string fmt, params object[] objects) {
				string newfmt = log.AddDebugInfo(fmt);
				
				log.WriteEvent(db.AppName, newfmt, objects);
			}

			public void Write(string fmt, params object[] objects) {
				log.WriteEvent(db.AppName, fmt, objects);
			}
		}
	}
}

/*
	$log$
*/