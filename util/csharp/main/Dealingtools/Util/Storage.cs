/* ----------------------------------------------------------------------
   Storage - singleton for accessing the Dealingtools part of the registry

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/04/27 11:05:43 $
   $Source: /usr/export/cvsroot/util/csharp/main/Dealingtools/Util/Storage.cs,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

using System;
using System.Runtime.InteropServices;

namespace Dealingtools {
	namespace Util {
		public class Storage {
			private static Storage instance = null;
			private const string rootPath = "Software\\Dealing Tools";
			private string appName = "Default";
			private Microsoft.Win32.RegistryKey root = null;

			private Storage() {
				root = Microsoft.Win32.Registry.CurrentUser.OpenSubKey(rootPath, true);
				if (root == null) {
					root = Microsoft.Win32.Registry.CurrentUser.CreateSubKey(rootPath);
				}
			}

			public static Storage Instance() {
				if (instance == null) {
					instance = new Storage();
				}
				return instance;
			}

			public string AppName {
				get {
					return appName;
				}
				set {
					appName = value;
				}
			}

			// my paths are of the form path\\to\\element:name

			public object Get(string relativePath) {
				string[] path = relativePath.Split(':');
				Microsoft.Win32.RegistryKey subKey = root.OpenSubKey(path[0]);
				if (subKey == null) {
					return null;
				}
				return subKey.GetValue(path[1]);
			}
			public void Set(string relativePath, object data) {
				string[] path = relativePath.Split(':');
				Microsoft.Win32.RegistryKey subKey = root.OpenSubKey(path[0], true);
				if (subKey == null) {
					subKey = root.CreateSubKey(path[0]);
				}
				subKey.SetValue(path[1], data);
			}

			public void Delete(string relativePath) {
				string[] path = relativePath.Split(':');
				Microsoft.Win32.RegistryKey subKey = root.OpenSubKey(path[0], true);
				if (subKey == null) {
					return;
				}
				if (path.Length == 2) {
					// there was a name...
					subKey.DeleteValue(path[1], false);
				} else {
					foreach (object name in GetList(relativePath).Keys) {
						subKey.DeleteValue((string) name, false);
					}
				}
			}

			public object GetWithDefault(string relativePath, object def) {
				object data = Get(relativePath);
				if (data == null) {
					Set(relativePath, def);
					return def;
				}
				return data;
			}

			public System.Collections.Hashtable GetList(string relativePath) {
				Microsoft.Win32.RegistryKey subKey = root.OpenSubKey(relativePath);
				if (subKey == null) {
					return null;
				}
				System.Collections.Hashtable result = new System.Collections.Hashtable();
				foreach (string key in subKey.GetValueNames()) {
					result[key] = subKey.GetValue(key);
				}
				return result;
			}
		}
	}
}

/*
	$log$
*/