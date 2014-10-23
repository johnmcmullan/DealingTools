/* ----------------------------------------------------------------------
   Log - Dealing Tools logging class definition

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:10:56 $
   $Source: /usr/export/cvsroot/util/c++/main/libdt/Log.h,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#ifndef __DT_LOG
#define __DT_LOG

#include <cstdio>
#include <string>
#include <pthread.h>
#include <unistd.h>
#include <stdarg.h>

namespace dt {

  // shamelessly borrowed from /usr/include/syslog.h
  typedef enum {	LOG_EMERG,	/* system is unusable */
			LOG_ALERT,	/* action must be taken immediately */
			LOG_CRIT,	/* critical conditions */
			LOG_ERR,	/* error conditions */
			LOG_WARNING,	/* warning conditions */
			LOG_NOTICE,	/* normal but significant condition */
			LOG_INFO,	/* informational */
			LOG_DEBUG	/* debug-level messages */
  } priority_t;

  class Log {
  private:
    FILE *logfile;
    std::string path;
    pthread_mutex_t lock;
    int linecount;
    priority_t logpriority;
    pid_t pid;
    static Log *instance;
  protected:
    Log();
    ~Log() {}
    void Open();
    void Cycle();
    inline void Write(priority_t priority, const char *module,
		      const char *fmt, ...);
  public:
    static Log *Instance();
    void Emerg(const char *module, const char *fmt, ...);
    void Alert(const char *module, const char *fmt, ...);
    void Crit(const char *module, const char *fmt, ...);
    void Err(const char *module, const char *fmt, ...);
    void Warning(const char *module, const char *fmt, ...);
    void Notice(const char *module, const char *fmt, ...);
    void Info(const char *module, const char *fmt, ...);
    void Debug(const char *module, const char *fmt, ...);
    void IncreaseLogPriority();
    void DecreaseLogPriority();
    priority_t GetLogPriority();
    void Start(const std::string& logname);
  };

}


#endif

/*
  $log$
*/
