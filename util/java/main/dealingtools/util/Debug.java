/* ----------------------------------------------------------------------
   Debug - used for storing the debug status of the app

   Original Author: John McMullan (2004)

   $Author$
   $Date$
   $Source$
   $Revision$

   ---------------------------------------------------------------------- */

package dealingtools.util;

public class Debug {
    private static boolean debug = false;

    public static boolean isOn() {
	return (debug == true);
    }

    public static void toggleOn() {
	debug = true;
    }

    public static void toggleOff() {
	debug = false;
    }
}

/*
  $log$
*/
