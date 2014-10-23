-- ---------------------------------------------------------------
-- SQL script for creating the database for Yahoo Finance server
--
-- ---------------------------------------------------------------

drop database yahoo;
create database yahoo;
grant dba to public;

create table config (
	yahoo_id char(64),
	password char(32),
	proxy char(128)
);

create table source (
	base_url char(128),
	format_flag char(1),
	stock_flag char(1)
);

create table operations (
	name char(64),
	yahoo_name char(64),
	yahoo_symbol char(2) PRIMARY KEY,
	orc_id int NOT NULL,
	format int NOT NULL,
	realtime int NOT NULL
);

create index operations_rt on operations(realtime);
create index operations_orc on operations(orc_id);

create table level(
  dblevel int not null
);

-- Source map

-- base_url + format_flag + =" + format + "& + stocks_flag + =" + stocks + "
insert into source values("http://quote.yahoo.com/d?", "f", "s");


-- Operation map 

-- name, yahoo_name, yahoo_symbol, orc_id, format, real time flag

-- format values
-- 1 - float number with point
-- 2 - integer number
-- 3 - date
-- 4 - time
-- 5 - string 
-- 6 - percentage
-- 7 - price in display format (prices can contain indicated division eg "a/b")

insert into operations values("", "Symbol", "s", 0, 0, 0); -- old default
insert into operations values("DESCRIPTION", "Name",
	"n", -29, 5, 0); -- old default
insert into operations values("", "Last Trade (With Time)", "l", 0, 0, 0);
insert into operations values("LAST", "Last Trade (Price Only)",
	"l1", -7, 1, 0); -- old default
insert into operations values("TRADE_DATE", "Last Trade Date",
	"d1", 0, 3, 0); -- old default
insert into operations values("TRADE_TIME", "Last Trade Time",
	"t1", 0, 4, 0); -- old default
insert into operations values("LAST_VOLUME", "Last Trade Size",
	"k3", -8, 2, 0);
insert into operations values("", "Change and Percent Change", "c", 0, 0, 0);
insert into operations values("", "Change", "c1", 0, 0, 0); -- old default
insert into operations values("", "Change in Percent",
	"p2", 0, 0, 0); -- old default
insert into operations values("", "Ticker Trend", "t7", 0, 0, 0);
insert into operations values("", "Volume", "v", 0, 0, 0); -- old default
insert into operations values("", "Average Daily Volume",
	"a2", 0, 0, 0); -- old default
insert into operations values("", "More Info", "i", 0, 0, 0);
insert into operations values("", "Trade Links", "t6", 0, 0, 0);
insert into operations values("BID", "Bid", "b", -1, 1, 0); -- old default
insert into operations values("BID_VOLUME", "Bid Size", "b6", -2, 2, 0);
insert into operations values("ASK", "Ask", "a", -4, 1, 0); -- old default
insert into operations values("ASK_VOLUME", "Ask Size", "a5", -5, 2, 0);
insert into operations values("CLOSE", "Previous Close",
	"p", -15, 5, 0); -- old default
insert into operations values("OPEN", "Open", "o", -14, 5, 0); -- old default
insert into operations values("", "Days Range", "m", 0, 0, 0); -- old default
insert into operations values("", "52-week Range",
	"w", 0, 0, 0); -- old default
insert into operations values("", "Change From 52-wk Low", "j5", 0, 0, 0);
insert into operations values("", "Pct Chg From 52-wk Low", "j6", 0, 0, 0);
insert into operations values("", "Change From 52-wk High", "k4", 0, 0, 0);
insert into operations values("", "Pct Chg From 52-wk High", "k5", 0, 0, 0);
insert into operations values("", "Earnings/Share",
	"e", 0, 0, 0); -- old default
insert into operations values("", "P/E Ratio",
	"r", 0, 0, 0); -- old default
insert into operations values("", "Short Ratio", "s7", 0, 0, 0);
insert into operations values("", "Dividend Pay Date",
	"r1", 0, 0, 0); -- old default
insert into operations values("", "Ex-Dividend Date", "q", 0, 0, 0);
insert into operations values("", "Dividend/Share",
	"d", 0, 0, 0); -- old default
insert into operations values("", "Dividend Yield",
	"y", 0, 0, 0); -- old default
insert into operations values("", "Float Shares", "f6", 0, 0, 0);
insert into operations values("", "Market Capitalization",
	"j1", 0, 0, 0); -- old default
insert into operations values("", "1yr Target Price", "t8", 0, 0, 0);
insert into operations values("", "EPS Est. Current Yr", "e7", 0, 0, 0);
insert into operations values("", "EPS Est. Next Year", "e8", 0, 0, 0);
insert into operations values("", "EPS Est. Next Quarter", "e9", 0, 0, 0);
insert into operations values("", "Price/EPS Est. Current Yr", "r6", 0, 0, 0);
insert into operations values("", "Price/EPS Est. Next Yr", "r7", 0, 0, 0);
insert into operations values("", "PEG Ratio", "r5", 0, 0, 0);
insert into operations values("", "Book Value", "b4", 0, 0, 0);
insert into operations values("", "Price/Book", "p6", 0, 0, 0);
insert into operations values("", "Price/Sales", "p5", 0, 0, 0);
insert into operations values("", "EBITDA", "j4", 0, 0, 0);
insert into operations values("", "50-day Moving Avg", "m3", 0, 0, 0);
insert into operations values("", "Change From 50-day Moving Avg",
	"m7", 0, 0, 0);
insert into operations values("", "Pct Chg From 50-day Moving Avg",
	"m8", 0, 0, 0);
insert into operations values("", "200-day Moving Avg", "m4", 0, 0, 0);
insert into operations values("", "Change From 200-day Moving Avg",
	"m5", 0, 0, 0);
insert into operations values("", "Pct Chg From 200-day Moving Avg",
	"m6", 0, 0, 0);
insert into operations values("", "Shares Owned", "s1", 0, 0, 0);
insert into operations values("", "Price Paid", "p1", 0, 0, 0);
insert into operations values("", "Commission", "c3", 0, 0, 0);
insert into operations values("", "Holdings Value", "v1", 0, 0, 0);
insert into operations values("", "Days Value Change", "w1", 0, 0, 0);
insert into operations values("", "Holdings Gain Percent", "g1", 0, 0, 0);
insert into operations values("", "Holdings Gain", "g4", 0, 0, 0);
insert into operations values("", "Trade Date", "d2", 0, 0, 0);
insert into operations values("", "Annualized Gain", "g3", 0, 0, 0);
insert into operations values("HIGH", "High Limit", "l2", -12, 5, 0);
insert into operations values("LOW", "Low Limit", "l3", -13, 5, 0);
insert into operations values("", "Notes", "n4", 0, 0, 0);
insert into operations values("", "Last Trade with Time", "k1", 0, 0, 1);
insert into operations values("BID", "Bid", "b3", -1, 1, 1);
insert into operations values("ASK", "Ask", "b2", -4, 1, 1);
insert into operations values("", "Change Percent", "k2", 0, 0, 1);
insert into operations values("", "Change", "c6", 0, 0, 1);
insert into operations values("", "Holdings Value", "v7", 0, 0, 1);
insert into operations values("", "Days Value Change", "w4", 0, 0, 1);
insert into operations values("", "Holdings Gain Pct", "g5", 0, 0, 1);
insert into operations values("", "Holdings Gain", "g6", 0, 0, 1);
insert into operations values("", "Days Range", "m2", 0, 0, 1);
insert into operations values("", "Market Cap", "j3", 0, 0, 1);
insert into operations values("", "P/E", "r2", 0, 0, 1);
insert into operations values("", "After Hours Change", "c8", 0, 0, 1);
insert into operations values("", "Order Book", "i5", 0, 0, 1);
insert into operations values("", "Stock Exchange",
	"x", 0, 0, 0); -- old default


