drop procedure Numeric_Proc ;
create procedure Numeric_Proc(out MAX_PARAM NUMERIC(30,15), out MIN_PARAM NUMERIC(30,15), out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Numeric_Proc' parameter style java;

drop procedure Decimal_Proc ;
create procedure Decimal_Proc(out MAX_PARAM DECIMAL(30,15), out MIN_PARAM DECIMAL(30,15), out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Decimal_Proc' parameter style java;

drop procedure Double_Proc ;
create procedure Double_Proc (out MAX_PARAM DOUBLE PRECISION, out MIN_PARAM DOUBLE PRECISION, out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Double_Proc' parameter style java;

drop procedure Float_Proc ;
create procedure Float_Proc  (out MAX_PARAM FLOAT, out MIN_PARAM FLOAT, out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Float_Proc' parameter style java;

drop procedure Real_Proc ;
create procedure Real_Proc (out MAX_PARAM REAL, out MIN_PARAM REAL, out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Real_Proc' parameter style java;

drop procedure Bit_Proc ;
create procedure Bit_Proc (out MAX_PARAM BOOLEAN, out MIN_PARAM BOOLEAN, out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bit_Proc' parameter style java;

drop procedure Smallint_Proc ;
create procedure Smallint_Proc (out MAX_PARAM SMALLINT, out MIN_PARAM SMALLINT, out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Smallint_Proc' parameter style java;

drop procedure Tinyint_Proc ;
create procedure Tinyint_Proc (out MAX_PARAM INTEGER, out MIN_PARAM INTEGER, out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Tinyint_Proc' parameter style java;

drop procedure Integer_Proc ;
create procedure Integer_Proc (out MAX_PARAM INTEGER, out MIN_PARAM INTEGER, out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Integer_Proc' parameter style java;

drop procedure Bigint_Proc ;
create procedure Bigint_Proc (out MAX_PARAM BIGINT, out MIN_PARAM BIGINT, out NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bigint_Proc' parameter style java;

drop procedure Char_Proc ;
create procedure Char_Proc (out NAME_PARAM CHAR(30), out NULL_PARAM CHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Char_Proc' parameter style java;

drop procedure Varchar_Proc ;
create procedure Varchar_Proc (out NAME_PARAM VARCHAR(30), out NULL_PARAM VARCHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Varchar_Proc' parameter style java;

drop procedure Longvarchar_Proc ;
create procedure Longvarchar_Proc (out NAME_PARAM VARCHAR(448)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Longvarchar_Proc' parameter style java;

drop procedure Lvarcharnull_Proc ;
create procedure Lvarcharnull_Proc (out NULL_PARAM VARCHAR(448)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Longvarcharnull_Proc' parameter style java;

drop procedure Date_Proc ;
create procedure Date_Proc (out MFG_PARAM DATE, out NULL_PARAM DATE) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Date_Proc' parameter style java;

drop procedure Time_Proc ;
create procedure Time_Proc (out BRK_PARAM TIME, out NULL_PARAM TIME) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Time_Proc' parameter style java;

drop procedure Timestamp_Proc ;
create procedure Timestamp_Proc (out IN_PARAM TIMESTAMP, out NULL_PARAM TIMESTAMP) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Timestamp_Proc' parameter style java;

drop procedure Binary_Proc ;
create procedure Binary_Proc (out BINARY_PARAM VARCHAR(24) FOR BIT DATA) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Binary_Proc' parameter style java;

drop procedure Varbinary_Proc ;
create procedure Varbinary_Proc (out VARBINARY_PARAM VARCHAR(255) FOR BIT DATA) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Varbinary_Proc' parameter style java;

drop procedure Longvarbinary_Proc ;
create procedure Longvarbinary_Proc (out LONGVARBINARY_PARAM VARCHAR(255) FOR BIT DATA) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Longvarbinary_Proc' parameter style java;

drop procedure Integer_In_Proc ;
create procedure Integer_In_Proc (IN_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Integer_In_Proc' parameter style java;

drop procedure Integer_InOut_Proc ;
create procedure Integer_InOut_Proc (inout INOUT_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Integer_InOut_Proc' parameter style java;

drop procedure UpdCoffee_Proc ;
create procedure UpdCoffee_Proc (in TYPE_PARAM NUMERIC) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.UpdCoffee_Proc' parameter style java;

drop procedure SelCoffee_Proc ;
create procedure SelCoffee_Proc (out KEYID_PARAM VARCHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.SelCoffee_Proc' parameter style java;

drop procedure IOCoffee_Proc ;
create procedure IOCoffee_Proc (inout PRICE_PARAM REAL) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.IOCoffee_Proc' parameter style java;

drop procedure Coffee_Proc ;
create procedure Coffee_Proc (in TYPE_PARAM Numeric) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Coffee_Proc' parameter style java;

drop procedure Numeric_Io_Max ;
create procedure Numeric_Io_Max (inout MAX_PARAM NUMERIC(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Numeric_Io_Max' parameter style java;

drop procedure Numeric_Io_Min ;
create procedure Numeric_Io_Min (inout MIN_PARAM NUMERIC(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Numeric_Io_Min' parameter style java;

drop procedure Numeric_Io_Null  ;
create procedure Numeric_Io_Null (inout NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Numeric_Io_Null' parameter style java;

drop procedure Decimal_Io_Max  ;
create procedure Decimal_Io_Max (inout MAX_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Decimal_Io_Max' parameter style java;

drop procedure Decimal_Io_Min  ;
create procedure Decimal_Io_Min (inout MIN_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Decimal_Io_Min' parameter style java;

drop procedure Decimal_Io_Null  ;
create procedure Decimal_Io_Null (inout NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Decimal_Io_Null' parameter style java;

drop procedure Double_Io_Max  ;
create procedure Double_Io_Max (inout MAX_PARAM DOUBLE PRECISION) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Double_Io_Max' parameter style java;

drop procedure Double_Io_Min  ;
create procedure Double_Io_Min (inout MIN_PARAM DOUBLE PRECISION) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Double_Io_Min' parameter style java;

drop procedure Double_Io_Null  ;
create procedure Double_Io_Null (inout NULL_PARAM DOUBLE PRECISION) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Double_Io_Null' parameter style java;

drop procedure Float_Io_Max  ;
create procedure Float_Io_Max (inout MAX_PARAM FLOAT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Float_Io_Max' parameter style java;

drop procedure Float_Io_Min  ;
create procedure Float_Io_Min (inout MIN_PARAM FLOAT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Float_Io_Min' parameter style java;

drop procedure Float_Io_Null ;
create procedure Float_Io_Null (inout NULL_PARAM FLOAT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Float_Io_Null' parameter style java;

drop procedure Real_Io_Max  ;
create procedure Real_Io_Max (inout MAX_PARAM REAL) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Real_Io_Max' parameter style java;

drop procedure Real_Io_Min  ;
create procedure Real_Io_Min (inout MIN_PARAM REAL) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Real_Io_Min' parameter style java;

drop procedure Real_Io_Null  ;
create procedure Real_Io_Null (inout NULL_PARAM REAL) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Real_Io_Null' parameter style java;

drop procedure Bit_Io_Max  ;
create procedure Bit_Io_Max (inout MAX_PARAM BOOLEAN) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bit_Io_Max' parameter style java;

drop procedure Bit_Io_Min  ;
create procedure Bit_Io_Min (inout MIN_PARAM BOOLEAN) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bit_Io_Min' parameter style java;

drop procedure Bit_Io_Null  ;
create procedure Bit_Io_Null (inout NULL_PARAM BOOLEAN) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bit_Io_Null' parameter style java;

drop procedure Smallint_Io_Max  ;
create procedure Smallint_Io_Max (inout MAX_PARAM SMALLINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Smallint_Io_Max' parameter style java;

drop procedure Smallint_Io_Min  ;
create procedure Smallint_Io_Min (inout MIN_PARAM SMALLINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Smallint_Io_Min' parameter style java;

drop procedure Smallint_Io_Null  ;
create procedure Smallint_Io_Null (inout NULL_PARAM SMALLINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Smallint_Io_Null' parameter style java;

drop procedure Tinyint_Io_Max  ;
create procedure Tinyint_Io_Max (inout MAX_PARAM SMALLINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Tinyint_Io_Max' parameter style java;

drop procedure Tinyint_Io_Min  ;
create procedure Tinyint_Io_Min (inout MIN_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Tinyint_Io_Min' parameter style java;

drop procedure Tinyint_Io_Null  ;
create procedure Tinyint_Io_Null (inout NULL_PARAM SMALLINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Tinyint_Io_Null' parameter style java;

drop procedure Integer_Io_Max  ;
create procedure Integer_Io_Max (inout MAX_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Integer_Io_Max' parameter style java;

drop procedure Integer_Io_Min  ;
create procedure Integer_Io_Min (inout MIN_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Integer_Io_Min' parameter style java;

drop procedure Integer_Io_Null  ;
create procedure Integer_Io_Null (inout NULL_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Integer_Io_Null' parameter style java;

drop procedure Bigint_Io_Max  ;
create procedure Bigint_Io_Max (inout MAX_PARAM BIGINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bigint_Io_Max' parameter style java;

drop procedure Bigint_Io_Min  ;
create procedure Bigint_Io_Min (inout MIN_PARAM BIGINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bigint_Io_Min' parameter style java;

drop procedure Bigint_Io_Null  ;
create procedure Bigint_Io_Null (inout NULL_PARAM BIGINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bigint_Io_Null' parameter style java;

drop procedure Char_Io_Name ;
create procedure Char_Io_Name (inout NAME_PARAM CHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Char_Io_Name' parameter style java;

drop procedure Char_Io_Null ;
create procedure Char_Io_Null (inout NULL_PARAM VARCHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Char_Io_Null' parameter style java;

drop procedure Varchar_Io_Name ;
create procedure Varchar_Io_Name (inout NAME_PARAM VARCHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Varchar_Io_Name' parameter style java;

drop procedure Varchar_Io_Null ;
create procedure Varchar_Io_Null (inout NULL_PARAM VARCHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Varchar_Io_Null' parameter style java;

drop procedure Lvarchar_Io_Name ;
create procedure Lvarchar_Io_Name (inout NAME_PARAM VARCHAR(448)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Longvarchar_Io_Name' parameter style java;

drop procedure Lvarchar_Io_Null ;
create procedure Lvarchar_Io_Null (inout NULL_PARAM VARCHAR(448)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Longvarchar_Io_Null' parameter style java;

drop procedure Date_Io_Mfg ;
create procedure Date_Io_Mfg (inout MFG_PARAM DATE) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Date_Io_Mfg' parameter style java;

drop procedure Date_Io_Null ;
create procedure Date_Io_Null (inout NULL_PARAM DATE) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Date_Io_Null' parameter style java;

drop procedure Time_Io_Brk ;
create procedure Time_Io_Brk (inout BRK_PARAM TIME) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Time_Io_Brk' parameter style java;

drop procedure Time_Io_Null ;
create procedure Time_Io_Null (inout NULL_PARAM TIME) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Time_Io_Null' parameter style java;

drop procedure Timestamp_Io_Intime ;
create procedure Timestamp_Io_Intime (inout INTIME_PARAM TIMESTAMP) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Timestamp_Io_Intime' parameter style java;

drop procedure Timestamp_Io_Null ;
create procedure Timestamp_Io_Null (inout NULL_PARAM TIMESTAMP) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Timestamp_Io_Null' parameter style java;

drop procedure Binary_Proc_Io ;
create procedure Binary_Proc_Io (inout BINARY_PARAM VARCHAR(24) FOR BIT DATA) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Binary_Proc_Io' parameter style java;

drop procedure Varbinary_Proc_Io ;
create procedure Varbinary_Proc_Io (inout VARBINARY_PARAM VARCHAR(255) FOR BIT DATA) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Varbinary_Proc_Io' parameter style java;

drop procedure Longvarbinary_Io ;
create procedure Longvarbinary_Io (inout LONGVARBINARY_PARAM VARCHAR(255) FOR BIT DATA) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Longvarbinary_Io' parameter style java;

drop procedure Numeric_In_Max  ;
create procedure Numeric_In_Max (in MAX_PARAM NUMERIC(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Numeric_In_Max' parameter style java;

drop procedure Numeric_In_Min  ;
create procedure Numeric_In_Min (in MIN_PARAM NUMERIC(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Numeric_In_Min' parameter style java;

drop procedure Numeric_In_Null  ;
create procedure Numeric_In_Null (in NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Numeric_In_Null' parameter style java;

drop procedure Decimal_In_Max  ;
create procedure Decimal_In_Max (in MAX_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Decimal_In_Max' parameter style java;

drop procedure Decimal_In_Min  ;
create procedure Decimal_In_Min (in MIN_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Decimal_In_Min' parameter style java;

drop procedure Decimal_In_Null  ;
create procedure Decimal_In_Null (in NULL_PARAM DECIMAL(30,15)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Decimal_In_Null' parameter style java;

drop procedure Double_In_Max  ;
create procedure Double_In_Max (in MAX_PARAM DOUBLE PRECISION) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Double_In_Max' parameter style java;

drop procedure Double_In_Min  ;
create procedure Double_In_Min (in MIN_PARAM DOUBLE PRECISION) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Double_In_Min' parameter style java;

drop procedure Double_In_Null  ;
create procedure Double_In_Null (in NULL_PARAM DOUBLE PRECISION) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Double_In_Null' parameter style java;

drop procedure Float_In_Max  ;
create procedure Float_In_Max (in MAX_PARAM FLOAT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Float_In_Max' parameter style java;

drop procedure Float_In_Min  ;
create procedure Float_In_Min (in MIN_PARAM FLOAT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Float_In_Min' parameter style java;

drop procedure Float_In_Null ;
create procedure Float_In_Null (in NULL_PARAM FLOAT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Float_In_Null' parameter style java;

drop procedure Real_In_Max  ;
create procedure Real_In_Max (in MAX_PARAM REAL) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Real_In_Max' parameter style java;

drop procedure Real_In_Min  ;
create procedure Real_In_Min (in MIN_PARAM REAL) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Real_In_Min' parameter style java;

drop procedure Real_In_Null  ;
create procedure Real_In_Null (in NULL_PARAM REAL) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Real_In_Null' parameter style java;

drop procedure Bit_In_Max  ;
create procedure Bit_In_Max (in MAX_PARAM BOOLEAN) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bit_In_Max' parameter style java;

drop procedure Bit_In_Min  ;
create procedure Bit_In_Min (in MIN_PARAM BOOLEAN) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bit_In_Min' parameter style java;

drop procedure Bit_In_Null  ;
create procedure Bit_In_Null (in NULL_PARAM BOOLEAN) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bit_In_Null' parameter style java;

drop procedure Smallint_In_Max  ;
create procedure Smallint_In_Max (in MAX_PARAM SMALLINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Smallint_In_Max' parameter style java;

drop procedure Smallint_In_Min  ;
create procedure Smallint_In_Min (in MIN_PARAM SMALLINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Smallint_In_Min' parameter style java;

drop procedure Smallint_In_Null  ;
create procedure Smallint_In_Null (in NULL_PARAM SMALLINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Smallint_In_Null' parameter style java;

drop procedure Tinyint_In_Max  ;
create procedure Tinyint_In_Max (in MAX_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Tinyint_In_Max' parameter style java;

drop procedure Tinyint_In_Min  ;
create procedure Tinyint_In_Min (in MIN_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Tinyint_In_Min' parameter style java;

drop procedure Tinyint_In_Null  ;
create procedure Tinyint_In_Null (in NULL_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Tinyint_In_Null' parameter style java;

drop procedure Integer_In_Max  ;
create procedure Integer_In_Max (in MAX_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Integer_In_Max' parameter style java;

drop procedure Integer_In_Min  ;
create procedure Integer_In_Min (in MIN_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Integer_In_Min' parameter style java;

drop procedure Integer_In_Null  ;
create procedure Integer_In_Null (in NULL_PARAM INTEGER) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Integer_In_Null' parameter style java;

drop procedure Bigint_In_Max  ;
create procedure Bigint_In_Max (in MAX_PARAM BIGINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bigint_In_Max' parameter style java;

drop procedure Bigint_In_Min  ;
create procedure Bigint_In_Min (in MIN_PARAM BIGINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bigint_In_Min' parameter style java;

drop procedure Bigint_In_Null  ;
create procedure Bigint_In_Null (in NULL_PARAM BIGINT) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Bigint_In_Null' parameter style java;

drop procedure Char_In_Name ;
create procedure Char_In_Name (in NAME_PARAM CHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Char_In_Name' parameter style java;

drop procedure Char_In_Null ;
create procedure Char_In_Null (in NULL_PARAM CHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Char_In_Null' parameter style java;

drop procedure Varchar_In_Name ;
create procedure Varchar_In_Name (in NAME_PARAM VARCHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Varchar_In_Name' parameter style java;

drop procedure Varchar_In_Null ;
create procedure Varchar_In_Null (in NULL_PARAM VARCHAR(30)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Varchar_In_Null' parameter style java;

drop procedure Lvarchar_In_Name ;
create procedure Lvarchar_In_Name (in NAME_PARAM VARCHAR(448)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Longvarchar_In_Name' parameter style java;

drop procedure Lvarchar_In_Null ;
create procedure Lvarchar_In_Null (in NULL_PARAM VARCHAR(448)) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Longvarchar_In_Null' parameter style java;

drop procedure Date_In_Mfg ;
create procedure Date_In_Mfg (in MFG_PARAM DATE) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Date_In_Mfg' parameter style java;

drop procedure Date_In_Null ;
create procedure Date_In_Null (in NULL_PARAM DATE) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Date_In_Null' parameter style java;

drop procedure Time_In_Brk ;
create procedure Time_In_Brk (in BRK_PARAM TIME) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Time_In_Brk' parameter style java;

drop procedure Time_In_Null ;
create procedure Time_In_Null (in NULL_PARAM TIME) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Time_In_Null' parameter style java;

drop procedure Timestamp_In_Intime ;
create procedure Timestamp_In_Intime (in INTIME_PARAM TIMESTAMP) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Timestamp_In_Intime' parameter style java;

drop procedure Timestamp_In_Null ;
create procedure Timestamp_In_Null (in NULL_PARAM TIMESTAMP) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Timestamp_In_Null' parameter style java;

drop procedure Binary_Proc_In ;
create procedure Binary_Proc_In (in BINARY_PARAM VARCHAR(24) FOR BIT DATA) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Binary_Proc_In' parameter style java;

drop procedure Varbinary_Proc_In ;
create procedure Varbinary_Proc_In (in VARBINARY_PARAM VARCHAR(255) FOR BIT DATA) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Varbinary_Proc_In' parameter style java;

drop procedure Longvarbinary_In ;
create procedure Longvarbinary_In (in LONGVARBINARY_PARAM VARCHAR(255) FOR BIT DATA) language java external name 'com.sun.ts.lib.tests.jdbc.CS_Procs.Longvarbinary_Proc_In' parameter style java;
