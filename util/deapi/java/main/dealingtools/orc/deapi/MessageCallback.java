/* ----------------------------------------------------------------------
   MessageCallback - Definition of DEAPI calback interface

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/main/dealingtools/orc/deapi/MessageCallback.java,v $
   $Revision: 1.1.1.1 $
   

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

public interface MessageCallback {
    void onMessage(Message message);
    void onError(int error, String errorDescription);
}
