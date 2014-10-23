/* ----------------------------------------------------------------------
   Signal - Dealing Tools signal handler implementation

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:10:56 $
   $Source: /usr/export/cvsroot/util/c++/main/libdt/Signal.cc,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#include <string>
#include <stdexcept>

#include <pthread.h>

#include "Signal.h"
#include "Log.h"

namespace dt {

  const char *module = "signal";

  Signal *Signal::instance = 0;

  Signal::Signal() {
    IgnoreSignals();
    pthread_attr_t attr;
    pthread_attr_init(&attr);

    pthread_t tid;
    int ret;
    if ((ret = pthread_create(&tid, &attr, Signal::SignalHandler, 0)) != 0) {
      throw new std::invalid_argument(strerror(ret));
    }
  }

  Signal *Signal::Instance() {
    if (instance == NULL) {
      instance = new Signal();
    }
    return instance;
  }

  void Signal::IgnoreSignals() {
    sigset_t signals;

    SetSignalsToBlock(&signals);
    pthread_sigmask(SIG_BLOCK, &signals, 0);
  }

  void Signal::SetSignalsToBlock(sigset_t *signals) {
    sigemptyset(signals);
    sigaddset(signals, SIGUSR1);
    sigaddset(signals, SIGUSR2);
    sigaddset(signals, SIGPIPE);
    sigaddset(signals, SIGTERM);
  }

  void *Signal::SignalHandler(void *closure) {
    Log *log = Log::Instance();

    log->Info(module, "Signal Handler thread started\n");

    sigset_t signals;
    int sig;
    priority_t;
    while (1) {
      // block until we're signalled
      sigwait(&signals, &sig);

      log->Debug(module, "Recieved signal [%d]\n", sig);

      switch (sig) {
      case SIGUSR1:
	log->IncreaseLogPriority();
	log->Info(module, "Increasing log priority, now [%d]\n",
		  log->GetLogPriority());
	break;
      case SIGUSR2:
	log->DecreaseLogPriority();
	log->Info(module, "Decreasing log priority, now [%d]\n",
		  log->GetLogPriority());
	break;
      case SIGPIPE:
	log->Debug(module, "Ignoring SIGPIPE");
	break;
      case SIGTERM:
	log->Info(module, "Killed, shutting down\n");
	exit(0);
      default:
	log->Err(module, "Unknown signal [%d] recieved, ignoring\n", sig);
	break;
      }
    }
  }
    
}

/*
  $log$
*/
