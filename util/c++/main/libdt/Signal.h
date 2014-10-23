/* ----------------------------------------------------------------------
   Signal - Dealing Tools signal handler definition

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:10:56 $
   $Source: /usr/export/cvsroot/util/c++/main/libdt/Signal.h,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#ifndef __DT_SIGNAL
#define __DT_SIGNAL

#include <signal.h>

namespace dt {

  class Signal {
  private:
    static Signal *instance;
  protected:
    Signal();
    ~Signal() {}
    static void SetSignals(sigset_t *signals);
    static void *SignalHandler(void *closure);
    void IgnoreSignals();
    void SetSignalsToBlock(sigset_t *signals);
  public:
    static Signal *Instance();
  };

}

#endif

/*
  $log$
*/
