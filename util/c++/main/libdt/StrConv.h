/* ----------------------------------------------------------------------
   StrConv - Dealing Tools string conversion templates

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:10:56 $
   $Source: /usr/export/cvsroot/util/c++/main/libdt/StrConv.h,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#ifndef _DT_STRINGCONVERSION
#define _DT_STRINGCONVERSION

#include <sstream>
#include <string>

namespace dt {

  template<typename T>
    T fromString(const std::string& s) {
    std::istringstream is(s);
    T t;
    is >> t;
    return t;
  }

  template<typename T>
    std::string toString(const T& t) {
    std::ostringstream os;
    os << t;
    return os.str();
  }

}

#endif
