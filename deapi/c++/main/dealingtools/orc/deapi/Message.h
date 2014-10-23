/* ----------------------------------------------------------------------
   Message - Dealing Tools message definition

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 09:08:12 $
   $Source: /usr/export/cvsroot/deapi/c++/main/dealingtools/orc/deapi/Message.h,v $
   $Revision: 1.1 $

   ---------------------------------------------------------------------- */

#ifndef __DEAPI_MESSAGE
#define __DEAPI_MESSAGE

#include <string>
#include <map>
#include <iosfwd>
#include <iterator>

#ifndef __STLPORT
#define istreambuf_iterator istream_iterator
#endif

namespace deapi {

  class Message;

  class Node {
  private:
    std::string str;
    Message *msg;
  public:
    Node(std::string& buf);
    Node(Message* msg);
    inline bool IsMsg();
    inline bool IsString();
    inline Message *GetMessage();
    inline const std::string& GetString();
    friend std::ostream& operator<<(std::ostream& os, const Node& node);
  };

  typedef map<std::string, Node> NodeMap;

  class Message : public NodeMap {
  protected:
    void Parse(std::istreambuf_iterator<char>& inpos);
  public:
    explicit Message(std::istream& in);
    Message(std::string& buf);
    Message();
    ~Message();
    friend std::ostream& operator<<(std::ostream& os, const Message *message);
  };

}

#endif

/*
  $log$
*/
