# ----------------------------------------------------------------------
#   Makefile
#
#   Original author: John McMullan (2004)
#
#   $Author: john $
#   $Date: 2004/05/10 14:06:02 $
#   $Source: /usr/export/cvsroot/util/deapi/c++/main/dealingtools/orc/deapi/Makefile,v $
#   $Revision: 1.1.1.1 $
#
#   ----------------------------------------------------------------------

OBJS = Message.o Connection.o MessageQueue.o

INCLUDES = Message.h Connection.h MessageQueue.h Exception.h MessageCallback.h

LIB = libdeapi.a

ARFLAGS = crs

DTMAKEFILE = util/c++/main/dt.makefile
include $(DTMAKEFILE)

# DEBUG FLAG SET HERE!!!!
DEBUG = -g

install: $(DTMAKEFILE) includes $(LIB)
	@echo "Installing $(LIB) in $(LIBDIR)"
	@cp -p $(LIB) $(LIBDIR)

includes: $(DTMAKEFILE) $(INCDIR) $(INCLUDES)
	@echo "Installing include files in $(INCDIR)"
	@cp -p $(INCLUDES) $(INCDIR)

$(LIB): $(DTMAKEFILE) $(LIBDIR) $(OBJS)
	$(AR) $(ARFLAGS) $(OBJS)

clean: master.clean
	rm -f $(LIB) util
