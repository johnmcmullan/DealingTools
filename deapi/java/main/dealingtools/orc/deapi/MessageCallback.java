/* ----------------------------------------------------------------------
   MessageCallback - Definition of DEAPI calback interface

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/14 20:06:17 $
   $Source: /usr/export/cvsroot/deapi/java/main/dealingtools/orc/deapi/MessageCallback.java,v $
   $Revision: 1.2 $
   

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

public interface MessageCallback {
    void onMessage(Message message);
    void onError(int error, String errorDescription);
}
