require 'socket'

module Constants
  USER = ENV["USER"] || "apps"
  PASSWORD = ""
  HOSTNAME = Socket.gethostname.split(".")[0]

  DB_HOST = "dbhost"
  DB_SERVICE = "postgresql"
  DB_PORT = Socket.getservbyname(DB_SERVICE) || 5432

  DB_SERVER_PORT = "25555"

  OP_PORT = "6980"    # A string because of how it is used
  OP_HOST = "ophost"

  # having these defined here stops us running into difficulty
  STRIKEPRICE_MULTIPLICATION_FACTOR = 1000
  DT_INSTRUMENT_ID_STRING = "%s%s%s%08.0f%08.0f"

  # comparison of floating point numbers done to this many decimal places
  FLOAT_SIGNIFICANT_FIGURES = 5

  # Assumption that all contracts are expired by 18:01.... bad I know
  EXPIRY_HOUR = 18
  EXPIRY_MINUTE = 1
end
