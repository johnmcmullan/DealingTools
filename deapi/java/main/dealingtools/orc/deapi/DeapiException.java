/* ----------------------------------------------------------------------
   DeapiException

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 09:03:38 $
   $Source: /usr/export/cvsroot/deapi/java/main/dealingtools/orc/deapi/DeapiException.java,v $
   $Revision: 1.2 $

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
  $Log: DeapiException.java,v $
  Revision 1.2  2004/06/03 09:03:38  john
  Just mucking about... no change that I can think of

*/
