/* ----------------------------------------------------------------------
   DeapiErrors

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/14 13:23:30 $
   $Source: /usr/export/cvsroot/deapi/java/main/dealingtools/orc/deapi/DeapiErrors.java,v $
   $Revision: 1.2 $

   ---------------------------------------------------------------------- */

package dealingtools.orc.deapi;

public interface DeapiErrors {
	 int
		OK = 0,
		LOGIN = 1001,
		NO_CDS = 1002,
		NOT_LOGGED_IN = 1003,

		INVALID_MSG_TYPE = 1011,
		MSG_FORMAT = 1012,
		PARSING = 1013,
		MISSING_KEY = 1014,
		ILLEGAL_VALUE = 1015,
		UNKNOWN_KEY = 1016,

		CDS = 1021,
		NO_MATCH = 1022,
		NOT_UNIQUE = 1023,
		IS_BASE_CONTRACT = 1024,
		BAD_CUSTOMER_ID = 1025,
		BAD_PORTFOLIO = 1026,
		

		INTERNAL = 1031,
		SYSERR = 1032,

		NO_OA = 1041,

		NO_ORC = 1051,
		ORC_ERR = 1053,

		NO_PERMISSION = 1061,

		CALC_FAILED = 1071,
		CALC_NOT_APPLICABLE = 1072,
		CALC_NOT_IMPLEMENTED = 1073,
		CALC_NO_BASE_PRICE = 1074,
		CALC_NO_PRICE_FOR_IMPLIED = 1075,
		CALC_NO_CROSS_RATE = 1076,
		CALC_IMPLIED_FAILED = 1077,

		WARN = 1999,
		WARN_NO_DATA = 2000,
		WARN_PART_FAILED = 2001,
	    WARN_PRICE_FEED_RESTARTED = 2002,
	    WARN_INVALID_ARGUMENT = 2003,
	    WARN_BASE_PRICE_NON_UNIQUE = 2004,
	    WARN_NO_BASE_CONTRACT = 2005,
	    WARN_NO_MARKET_CONNECTION = 2006;
}

/*
  $log$
*/
