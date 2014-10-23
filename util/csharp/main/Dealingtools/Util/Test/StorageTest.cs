using System;

namespace Dealingtools {
	namespace Util {
		namespace Test {
			using Dealingtools.Util;
			using NUnit.Framework;		

			[TestFixture] public class StorageTest {

				public StorageTest() {
				}

				[Test] public void Use() {
					Storage db = Storage.Instance();
					string testData = "Hello";
					string data = null;
					try {
						db.Set("test:testname", testData);
					} catch (Exception e) {
						Assert.Fail(e.ToString());
					}
					try {
						data = (string) db.Get("test:testname");
					} catch (Exception e) {
						Assert.Fail(e.ToString());
					}
					Assert.AreEqual(data, testData);
					data = null;
					try {
						data = (string) db.GetWithDefault("deftest:testdefname", "Hello");
					} catch (Exception e) {
						Assert.Fail(e.ToString());
					}
					Assert.AreEqual(data, "Hello");
				}
			}
		}
	}
}
