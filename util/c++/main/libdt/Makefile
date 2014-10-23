# ----------------------------------------------------------------------
#   Makefile
#
#   Original author: John McMullan (2004)
#
#   $Author: john $
#   $Date: 2004/06/03 10:33:27 $
#   $Source: /usr/export/cvsroot/util/c++/main/libdt/Makefile,v $
#   $Revision: 1.2 $
#
#   ----------------------------------------------------------------------

OBJS = Log.o Signal.o Socket.o

INCLUDES = Log.h Signal.h Socket.h StrConv.h util.h

LIB = libdt.a

ARFLAGS = crs

DTMAKEFILE = ../../../../util/c++/main/dt.makefile
include $(DTMAKEFILE)

# DEBUG FLAG SET HERE!!!!
DEBUG = -g

all: install

install: includes $(LIB)
	@echo "Installing $(LIB) in $(LIBDIR)"
	@cp -p $(LIB) $(LIBDIR)

includes: $(INCLUDES) $(INCDIR)
	@echo "Installing include files in $(INCDIR)"
	@cp -p $(INCLUDES) $(INCDIR)

$(LIB): $(OBJS) $(LIBDIR)
	$(AR) $(ARFLAGS) $(LIB) $(OBJS)

clean: master.clean
	rm -f *.o $(LIB)
