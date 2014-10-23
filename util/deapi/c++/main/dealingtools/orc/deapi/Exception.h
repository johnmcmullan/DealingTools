/* ----------------------------------------------------------------------
   Exception - deapi exception interface + implementation

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/c++/main/dealingtools/orc/deapi/Exception.h,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#ifndef __DEAPI_EXCEPTION
#define __DEAPI_EXCEPTION

#include <string>
#include <stdexcept>

namespace deapi {

  typdef enum {
    ERR_LOGIN = 1001,
    ERR_NOCDS = 1002,
    ERR_INVALIDMSGTYPE = 1011,
    ERR_MSGFORMAT = 1012,
    ERR_PARSING = 1013,
    ERR_MISSINGKEY = 1014,
    ERR_ILLEGALVALUE = 1015,
    ERR_UNKNOWN_KEY = 1016,
    ERR_CDS = 1021,
    ERR_NOMATCH = 1022,
    ERR_NOTUNIQUE = 1023,
    ERR_ISBASECONTRACT = 1024,
    ERR_INTERNAL = 1031,
    ERR_SYSERR = 1032,
    ERR_NOOA = 1041,
    ERR_NOORC = 1051,
    ERR_ORC = 1053,
    ERR_NOPERMISSION = 1061,
    ERR_CALC_FAILED = 1071,
    ERR_CALC_NOT_APPLICABLE = 1072,
    ERR_CALC_NOT_IMPLEMENTED = 1073,
    ERR_CALC_NO_BASE_PRICE = 1074,

    WARN_NODATA = 2000,
    WARN_PARTFAILED = 2001
  } error_t;

  class Exception : public std::logic_error {
  private:
    error_t error;
  public:
    Exception(error_t _error, std::string errorDescription) : error(_error),
      logic_error(errorDescription) {};
  };

}

#endif

/*
  $log$
*/
