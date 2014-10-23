/* ----------------------------------------------------------------------
   Message - Dealing Tools message definition

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/c++/main/dealingtools/orc/deapi/Message.h,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

#ifndef __DEAPI_MESSAGE
#define __DEAPI_MESSAGE

#include <string>
#include <map>
#include <iosfwd>

namespace deapi {

  class Message;

  class Node {
  private:
    std::string *str;
    Message *msg;
  public:
    Node(std::string& buf);
    Node(Message* msg);
    inline bool Node::IsMsg();
    inline bool Node::IsString();
    inline Message *Node::GetMessage();
    inline Message *Node::GetString();
    friend std::ostream& operator<<(std::ostream& os, const Node* node);
  };

  typedef map<std::string, Node *> NodeMap;

  class Message : public NodeMap {
  protected:
    Parse(std::istreambuf_iterator<char>& inpos);
  public:
    explicit Message(std::istream& in);
    Message(std::string& buf);
    Message();
    ~Message();
    std::ostream& operator<<(std::ostream &os, const Message *message);
  }

}

/*
  $log$
*/
