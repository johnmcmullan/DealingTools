#include "ruby.h"

static int id_push;


/* this is a macro because it gets called so often per message... */
#define add_to_buf(c) if ((buf == 0) || (pos == (bufsize - 1))) {	\
    bufsize += extra;							\
    buf = (char *) realloc(buf, bufsize);				\
    key = (char *) realloc(key, bufsize);				\
  }									\
  buf[pos++] = c


static VALUE cparse(VALUE io) {
  VALUE msg;
  size_t bufsize, pos;
  const size_t extra = 64;
  int looking_for_key = 1;
  char *key, *data, *buf;
  char c;
  pollfd_t server[1];

  bufsize = pos = 0;
  buf = key = data = 0;
  msg = RHASH
  while (1) {
    while ((c = getc(rb_file_fileno(io))) != EOF) {
      switch (c) {
      case '{':
	if (key != 0) {
	  /* not initial opening brace */
	  _msg = _CreateFromFile(fd);
	  err = orcMsg_AddMsg(msg, key, _msg);
	  if (err != ORC_ERR_OK)
	    logit(LOG_INFO, module, "Unable to add [%s] to message\n", key);
	  orcMsg_Destroy(_msg);
	}
	looking_for_key = ORC_TRUE;
	pos = 0;
	break;
      case '|':
	if (pos != 0) {
	  buf[pos] = 0;
	  data = trim(buf, pos);
	  err = orcMsg_AddString(msg, key, data);
	}
	looking_for_key = ORC_TRUE;
	pos = 0;
	break;
      case '=':
	if (looking_for_key == ORC_TRUE) {
	  buf[pos] = 0;
	  memcpy(key, buf, pos + 1);
	  looking_for_key = ORC_FALSE;
	  pos = 0;
	} else {
	  add_to_buf(c);
	}
	break;
      case '}':
	if (pos != 0) {
	  buf[pos] = 0;
	  data = trim(buf, pos);
	  err = orcMsg_AddString(msg, key, data);
	}
	free(key);
	free(buf);
	return msg;
      case ' ':
	if (looking_for_key)
	  /* ignore whitespace */
	  break;
	/* fall through to default */
      default:
	add_to_buf(c);
      }
    }

    logit(LOG_INFO, module, "Waiting for in buffer to fill again\n");
    server[0].fd = fileno(fd);
    server[0].events = POLLIN;
    poll(server, 1, INFTIM);
  }


}
