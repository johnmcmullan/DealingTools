/* ----------------------------------------------------------------------
   Log - Dealing Tools logging class definition

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:10:56 $
   $Source: /usr/export/cvsroot/util/c++/main/libdt/Log.cc,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

static char rcsid[] = "$Id: Log.cc,v 1.1.1.1 2004/05/10 14:10:56 john Exp $";

#include <string>
#include <cstdlib>
#include <cstdio>

#include <errno.h>
#include <stdarg.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <pthread.h>
#include <time.h>
#include <unistd.h>
#include "Log.h"

namespace dt {

  static const char *prioritynames[] = { "emerg", "alert", "crit", "err",
					 "warning", "notice", "info",
					 "debug", 0 };

  static const int MaxLogFileSize = 4120; // 10MB

  Log *Log::instance = 0;

  Log::Log() {
    logfile = stdout;
    path = "";
    pthread_mutex_init(&lock, 0);
    linecount = 0;
    logpriority = LOG_NOTICE;
    pid = getpid();
  }

  Log *Log::Instance() {
    if (instance == NULL) {
      instance = new Log();
    }
    return instance;
  }

  void Log::Open() {
    if (path.empty()) {
      return;
    }

    if ((logfile = freopen(path.c_str(), "a", stdout)) == 0) {
      perror("unable to open logfile");
      logfile = stdout;
    }

    setvbuf(logfile, 0, _IONBF, 0);

    Write(LOG_INFO, 0, "NEW LOG ------------------------------\n");
  }

  void Log::Start(const std::string& logname) {
    path = getenv("HOME");
    path += "/log/";
    path += logname + ".log";

    Open();
  }

  void Log::Cycle() {
    if (path.empty())
      return;

    std::string oldpath = path + "-old";

    fprintf(logfile, "Recycling log file to %s\n", oldpath.c_str());
    fclose(logfile);
    rename(path.c_str(), oldpath.c_str());
    Open();
  }

  void Log::Write(priority_t priority, const char *module,
		  const char *fmt, ...) {
    if (priority > logpriority)
      return;
    
    if (linecount == 500) {
      struct stat logstat;
      fstat(fileno(logfile), &logstat);
      if (logstat.st_blocks > MaxLogFileSize) {
	Cycle();
	linecount = 0;
      }
    }

    if (module == NULL) {
      module = "";
    }

    time_t now = time(0);
    struct tm result;
    const int bufsize = 20;
    char timestamp[bufsize]; // "YYYY-MM-DD HH:MM:SS"
    localtime_r(&now, &result);
    strftime(timestamp, bufsize, "%Y-%m-%D %H:%M:%S", &result);

    pthread_mutex_lock(&lock);

    fprintf(logfile, "%s %s[%d:t@%ld]/%s:  ", timestamp, module,
	    (int) pid, (long) pthread_self(), prioritynames[priority]);

    va_list ap;
    va_start(ap, fmt);
    vfprintf(logfile, fmt, ap);
    va_end(ap);
    linecount++;
    pthread_mutex_unlock(&lock);
  }

  void Log::Emerg(const char *module, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    Write(LOG_EMERG, module, fmt, ap);
    va_end(ap);
  }
  void Log::Alert(const char *module, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    Write(LOG_ALERT, module, fmt, ap);
    va_end(ap);
  }
  void Log::Crit(const char *module, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    Write(LOG_CRIT, module, fmt, ap);
    va_end(ap);
  }
  void Log::Err(const char *module, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    Write(LOG_ERR, module, fmt, ap);
    va_end(ap);
  }
  void Log::Warning(const char *module, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    Write(LOG_WARNING, module, fmt, ap);
    va_end(ap);
  }
  void Log::Notice(const char *module, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    Write(LOG_NOTICE, module, fmt, ap);
    va_end(ap);
  }
  void Log::Info(const char *module, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    Write(LOG_INFO, module, fmt, ap);
    va_end(ap);
  }
  void Log::Debug(const char *module, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    Write(LOG_DEBUG, module, fmt, ap);
    va_end(ap);
  }
  
  void Log::IncreaseLogPriority() {
    if (logpriority == LOG_DEBUG)
      return;
	logpriority = static_cast<priority_t>(static_cast<int>(logpriority) + 1);
  }
  void Log::DecreaseLogPriority() {
    if (logpriority == LOG_EMERG)
      return;
	logpriority = static_cast<priority_t>(static_cast<int>(logpriority) - 1);
  }
  priority_t Log::GetLogPriority() {
    return logpriority;
  }

}
/*
  $log$
*/
