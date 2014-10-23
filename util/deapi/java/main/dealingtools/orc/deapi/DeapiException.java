/* ----------------------------------------------------------------------
   DeapiException

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/main/dealingtools/orc/deapi/DeapiException.java,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

public class DeapiException extends Exception {
    private int err;

    public DeapiException(int errno, String error_message) {
	super(error_message);
	err = errno;
    }

    public int error() {
	return err;
    }
}

/*
  $log$
*/
