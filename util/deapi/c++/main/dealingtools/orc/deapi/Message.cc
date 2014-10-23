/* ----------------------------------------------------------------------
   Message - deapi message implementation

   Original author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/c++/main/dealingtools/orc/deapi/Message.cc,v $
   $Revision: 1.1.1.1 $

   ---------------------------------------------------------------------- */

static char rcsid[] = "$Id: Message.cc,v 1.1.1.1 2004/05/10 14:06:02 john Exp $";

#include <dt/util.h>

#include <string>
#include <iostream>
#include <iterator>
#include <sstream>
#include <map>
#include <typeinfo>

#include "Message.h"
#include "Exception.h"

static const char *module = "Message";

namespace deapi {

  // Node

  Node::Node(std::string& buf) : str(&buf), msg(NULL) {
  }
  Node::Node(Message *message) : str(NULL), msg(message) {
  }
  bool Node::IsMsg() {
    return (msg != NULL);
  }
  bool Node::IsString() {
    return (str != NULL);
  }
  Message *Node::GetMessage() {
    return msg;
  }
  Message *Node::GetString() {
    return str;
  }

  std::ostream& operator<<(std::ostream &os, const Node *node) {
    if (node->IsString()) {
      return os << *(node->GetString());
    } else {
      return os << node->GetMessage();
    }
  }

  // Message
      
  Message::Message(std::istream& in) {
    std::istreambuf_iterator<char> inpos(in);
    Parse(inpos);
  }
  Message::Message(std::string& buf) {
    std::istringstream in(buf);
    std::istreambuf_iterator<char> inpos(in);
    Parse(inpos);
  }
  Message::Message(std::istreambuf_iterator<char>& inpos) {
    Parse(inpos);
  }

  Message *Message::Parse(std::istreambuf_iterator<char>& inpos) {
    dt::Log log = dt::Log::Instance();
    std::string buf, key;
    bool lookingForKey = true;
    char c;

    while (1) {
      while (inpos.good())
	c = *inpos;
	switch (c) {
	case '{':
	  if (!key.empty()) {
	    // not initializing the opening brace
	    // work out the object we're actually instantiating as
	    // this method is called by subclasses that expect us
	    // to create the right embedded object, not just Message
	    insert(key, new Node(new this(inpos));
	  }
	  lookingForKey = true;
	  break;
	case '|':
	  // if buf is empty, we just added an embedded message
	  if (!buf.empty()) {
	    insert(key, new Node(new std::string(buf))); // trim buf!
	    lookingForKey = true;
	    buf.clear();
	  }
	  break;
	case '=':
	  if (lookingForKey) {
	    // no need to trim if we were looking for a key
	    key = buf;
	    buf.clear();
	    lookingForKey = false;
	  } else {
	    buf += c;
	  }
	  break;
	case '}':
	  // if buf is empty we just added an embedded message
	  if (!buf.empty()) {
	    insert(key, new Node(new std::string(buf))); // trim buf!
	  }
	  return this;
	case ' ':
	  if (lookingForKey) {
	    // ignore whitespace
	    break;
	  }
	  // fall through
	default:
	  if (c > 0) {
	    buf += c;
	  }
	}
	++inpos;
      }

      // hey.. we ran out of data, wait for the inbuffer to fill again
      log.Info(module, "Waiting for in buffer to fill again\n");
      inpos.clear(std::ios::goodbit);
      pollfd_t server[1];
      server[0].fd = inpos.rdbuf().fd();
      server[0].events = POLLIN;
      poll(server, 1, INFTIM);
    }
  }

  std::ostream& operator<<(std::ostream &os, const Message *message) {
    os << "{";
    for (NodeMap::iterator pos = begin(); pos != end(); ++pos) {
      os << pos->first << "=";
      os << pos->second;
      if (pos != end())
	os << "|";
    }
    return os << "}";
  }    

}

#endif

/*
  $log$
*/
