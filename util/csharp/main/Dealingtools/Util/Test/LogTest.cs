using System;

namespace Dealingtools {
	namespace Util {
		namespace Test {
			using Dealingtools.Util;
			using NUnit.Framework;		

			[TestFixture] public class LogTest {

				public LogTest() {
				}

				[Test] public void Use() {
					Log log = Log.Instance();
					try {
						log.Write("This is a dealingtools Test log entry");
					} catch (Exception e) {
						Assert.Fail(e.ToString());
					}
					try {
						log.Debug("This is a dealingtools Test log entry");
					} catch (Exception e) {
						Assert.Fail(e.ToString());
					}
				}
			}
		}
	}
}
