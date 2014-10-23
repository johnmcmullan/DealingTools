/* ----------------------------------------------------------------------
   CommandLine - command line server for CommandInterpreter

   Original Author: John McMullan (2004)

   $Author$
   $Date$
   $Source$
   $Revision$

   ---------------------------------------------------------------------- */

package dealingtools.util;

import java.io.*;


public class CommandLine {
    public CommandLine() throws IOException {
	CommandInterpreter interp =
	    new CommandInterpreter(System.in, System.out);
    }
}

/*
  $log$
*/
