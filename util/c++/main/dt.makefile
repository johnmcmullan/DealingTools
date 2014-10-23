# ----------------------------------------------------------------------
#   dt.makefile
#
#   Original author: John McMullan (2004)
#
#   $Author: john $
#   $Date: 2004/06/03 10:33:23 $
#   $Source: /usr/export/cvsroot/util/c++/main/dt.makefile,v $
#   $Revision: 1.2 $
#
#   ----------------------------------------------------------------------

# DEBUG FLAG SET HERE!!!!
DEBUG = -O3

LIBDIR = $(HOME)/lib
INCROOT = $(HOME)/include
INCDIR = $(INCROOT)/dt

CFLAGS = -I$(INCROOT) -I. -L$(LIBDIR)

all: install

$(LIBDIR):
	@echo "Creating $(LIBDIR)"
	@mkdir -p $(LIBDIR)
$(INCDIR):
	@echo "Creating $(INCDIR)"
	@mkdir -p $(INCDIR)

master.clean:
	rm -f *.o
