drop table ctstable2 ;
drop table ctstable1 ;
create table ctstable1 (TYPE_ID int NOT NULL, TYPE_DESC varchar(32), primary key(TYPE_ID)) ;
create table ctstable2 (KEY_ID int NOT NULL, COF_NAME varchar(32), PRICE float, TYPE_ID int, primary key(KEY_ID), foreign key(TYPE_ID) references ctstable1) ;

drop table concurrencetable ;
create table concurrencetable (TYPE_ID int NOT NULL, TYPE_DESC varchar(32), primary key(TYPE_ID)) ;

drop table Numeric_Tab ;
create table Numeric_Tab (MAX_VAL NUMERIC(30,15), MIN_VAL NUMERIC(30,15), NULL_VAL NUMERIC(30,15)) ;

drop table Decimal_Tab ;
 create table Decimal_Tab (MAX_VAL DECIMAL(30,15),MIN_VAL DECIMAL(30,15), NULL_VAL DECIMAL(30,15)) ;

drop table Double_Tab ;
create table Double_Tab (MAX_VAL DOUBLE PRECISION, MIN_VAL DOUBLE PRECISION, NULL_VAL DOUBLE PRECISION) ;

drop table Float_Tab ;
create table Float_Tab (MAX_VAL FLOAT, MIN_VAL FLOAT, NULL_VAL FLOAT) ;

drop table Real_Tab ;
create table Real_Tab (MAX_VAL REAL, MIN_VAL REAL,NULL_VAL REAL) ;

drop table Bit_Tab ;
create table Bit_Tab (MAX_VAL BOOLEAN, MIN_VAL BOOLEAN, NULL_VAL SMALLINT) ;

drop table Smallint_Tab ;
create table Smallint_Tab (MAX_VAL SMALLINT, MIN_VAL SMALLINT, NULL_VAL SMALLINT) ;

drop table Tinyint_Tab ;
create table Tinyint_Tab (MAX_VAL SMALLINT, MIN_VAL SMALLINT, NULL_VAL SMALLINT) ;

drop table Integer_Tab ;
create table Integer_Tab (MAX_VAL INTEGER, MIN_VAL INTEGER, NULL_VAL INTEGER) ;

drop table Bigint_Tab ;
create table Bigint_Tab (MAX_VAL BIGINT, MIN_VAL BIGINT, NULL_VAL BIGINT) ;

drop table Char_Tab ;
create table Char_Tab (COFFEE_NAME CHAR(30), NULL_VAL CHAR(30)) ;

drop table Varchar_Tab ;
create table Varchar_Tab (COFFEE_NAME VARCHAR(30), NULL_VAL VARCHAR(30)) ;

drop table Longvarchar_Tab ;
create table Longvarchar_Tab (COFFEE_NAME LONG VARCHAR) ;

drop table Longvarcharnull_Tab ;
create table Longvarcharnull_Tab (NULL_VAL LONG VARCHAR) ;

drop table Date_Tab ;
create table Date_Tab (MFG_DATE DATE, NULL_VAL DATE) ;

drop table Time_Tab ;
create table Time_Tab (BRK_TIME TIME, NULL_VAL TIME) ;

drop table Timestamp_Tab ;
create table Timestamp_Tab (IN_TIME TIMESTAMP, NULL_VAL TIMESTAMP) ;

drop table Binary_Tab ;
create table Binary_Tab (BINARY_VAL VARCHAR(24) FOR BIT DATA) ;

drop table Varbinary_Tab ;
create table Varbinary_Tab (VARBINARY_VAL VARCHAR(255) FOR BIT DATA) ;

drop table Longvarbinary_Tab ;
create table Longvarbinary_Tab (LONGVARBINARY_VAL VARCHAR(255) FOR BIT DATA) ;

drop table ctstable3 ;
create table ctstable3(STRING1 VARCHAR(20), STRING2 VARCHAR(20), STRING3 VARCHAR(20), NUMCOL INTEGER, FLOATCOL FLOAT, DATECOL DATE, TIMECOL TIME, TSCOL1 TIMESTAMP, TSCOL2 TIMESTAMP) ;

drop table ctstable4 ;
create table ctstable4(STRING4 VARCHAR(20), NUMCOL NUMERIC) ;


drop table TxBean_Tab1 ;
create table TxBean_Tab1 (KEY_ID int, TABONE_NAME varchar(32), PRICE float) ;

drop table TxBean_Tab2 ;
create table TxBean_Tab2 (KEY_ID int, TABTWO_NAME varchar(32), PRICE float) ;
         ;
drop table TxEBean_Tab ;
create table TxEBean_Tab (KEY_ID INTEGER NOT NULL, BRAND_NAME VARCHAR(32), PRICE FLOAT, primary key(KEY_ID)) ;

drop table Integration_Tab ;
create table Integration_Tab (ACCOUNT INTEGER NOT NULL, BALANCE FLOAT, primary key(ACCOUNT)) ;

drop table BB_Tab ;
create table BB_Tab (KEY_ID INTEGER NOT NULL, BRAND_NAME varchar(32), PRICE float, primary key(KEY_ID)) ;

drop table JTA_Tab1 ;
create table JTA_Tab1 (KEY_ID int, COF_NAME varchar(32), PRICE float) ;

drop table JTA_Tab2 ;
create table JTA_Tab2 (KEY_ID int, CHOC_NAME varchar(32), PRICE float) ;

drop table Deploy_Tab1 ;
create table Deploy_Tab1 (KEY_ID INTEGER NOT NULL, BRAND_NAME varchar(32), PRICE float, primary key(KEY_ID)) ;

drop table Deploy_Tab2 ;
create table Deploy_Tab2 (KEY_ID VARCHAR(100) NOT NULL, BRAND_NAME varchar(32), PRICE float, primary key(KEY_ID)) ;

drop table Deploy_Tab3 ;
create table Deploy_Tab3 (KEY_ID BIGINT NOT NULL, BRAND_NAME varchar(32), PRICE float, primary key(KEY_ID)) ;

drop table Deploy_Tab4 ;
create table Deploy_Tab4 (KEY_ID FLOAT NOT NULL, BRAND_NAME varchar(32), PRICE float, primary key(KEY_ID)) ;

drop table Deploy_Tab5 ;
create table Deploy_Tab5 (KEY_ID1 int NOT NULL, KEY_ID2 varchar(100) NOT NULL, KEY_ID3 float NOT NULL, BRAND_NAME varchar(32), PRICE float, primary key(KEY_ID1, KEY_ID2, KEY_ID3)) ;

drop table Xa_Tab1 ;
create table Xa_Tab1 (col1 int NOT NULL, col2 varchar(32), col3 varchar(32), primary key(col1)) ;

drop table Xa_Tab2 ;
create table Xa_Tab2 (col1 int NOT NULL, col2 varchar(32), col3 varchar(32), primary key(col1)) ;

drop table SEC_Tab1 ;
create table SEC_Tab1 (KEY_ID INTEGER NOT NULL, PRICE float, BRAND varchar(32), primary key(KEY_ID)) ;

drop table Connector_Tab ;
create table Connector_Tab (KEY_ID int, PRODUCT_NAME varchar(32), PRICE float) ;

drop table Coffee_Table ;
create table Coffee_Table (KEY_ID INTEGER NOT NULL, BRAND_NAME varchar(32) NOT NULL, PRICE float NOT NULL, primary key(KEY_ID)) ;

drop table Coffee_StringPK_Table ;
create table Coffee_StringPK_Table (KEY_ID VARCHAR(100) NOT NULL, BRAND_NAME varchar(32) NOT NULL, PRICE float NOT NULL, primary key(KEY_ID)) ;

drop table Coffee_LongPK_Table ;
create table Coffee_LongPK_Table (KEY_ID BIGINT NOT NULL, BRAND_NAME varchar(32) NOT NULL, PRICE float NOT NULL, primary key(KEY_ID)) ;

drop table Coffee_FloatPK_Table ;
create table Coffee_FloatPK_Table (KEY_ID FLOAT NOT NULL, BRAND_NAME varchar(32) NOT NULL, PRICE float NOT NULL, primary key(KEY_ID)) ;

drop table Coffee_CompoundPK_Table ;
create table Coffee_CompoundPK_Table (KEY_ID1 int NOT NULL, KEY_ID2 varchar(100) NOT NULL, KEY_ID3 float NOT NULL, BRAND_NAME varchar(32) NOT NULL, PRICE float NOT NULL, primary key(KEY_ID1, KEY_ID2, KEY_ID3)) ;

DROP TABLE COFFEEEJBLITE;
CREATE TABLE COFFEEEJBLITE (ID INT NOT NULL, BRANDNAME VARCHAR(25), PRICE REAL, CONSTRAINT PK_COFFEEEJBLITE PRIMARY KEY (ID));


DROP TABLE EJB_AUTOCLOSE_TAB ;
CREATE TABLE EJB_AUTOCLOSE_TAB (NAME VARCHAR(25) NOT NULL, MESSAGE VARCHAR(25) NOT NULL);


DROP TABLE caller ;
DROP TABLE caller_groups ;

CREATE TABLE caller(name VARCHAR(64) PRIMARY KEY, password VARCHAR(1024)) ;
CREATE TABLE caller_groups(caller_name VARCHAR(64), group_name VARCHAR(64)) ;

INSERT INTO caller VALUES('tom', 'secret1') ;
INSERT INTO caller VALUES('emma', 'secret2') ;
INSERT INTO caller VALUES('bob', 'secret3') ;

INSERT INTO caller_groups VALUES('tom', 'Administrator') ;
INSERT INTO caller_groups VALUES('tom', 'Manager') ;

INSERT INTO caller_groups VALUES('emma', 'Administrator') ;
INSERT INTO caller_groups VALUES('emma', 'Employee') ;

INSERT INTO caller_groups VALUES('bob', 'Administrator') ;

 INSERT INTO caller VALUES('tom_hash512_saltsize16', 'PBKDF2WithHmacSHA512:1024:DbjXqT9p8VhJ7OtU6DrqDw==:p/qihG8IZKkz03JzKd6XXA==') ;
 INSERT INTO caller VALUES('tom_hash256_saltsize32', 'PBKDF2WithHmacSHA256:2048:suVayUIJMQMc6wCgckvAIgKRlo1UkxyFXhXbTxX6C7s=:cvdHkBXVUCN2WL3LRAYodeCdNZxEM4RLlNCCYP68Kmg=') ;
 INSERT INTO caller VALUES('tom_hash512_saltsize32', 'PBKDF2WithHmacSHA512:2048:dPTjUfiklfyg2bas/KOQKqEfdtoXK8YvbBscIxA8tNg=:ixBg0wr3ySBI86y8HP7+Yw==') ;

 INSERT INTO caller_groups VALUES('tom_hash512_saltsize16', 'Administrator') ;
 INSERT INTO caller_groups VALUES('tom_hash512_saltsize16', 'Manager') ;

 INSERT INTO caller_groups VALUES('tom_hash256_saltsize32', 'Administrator') ;
 INSERT INTO caller_groups VALUES('tom_hash256_saltsize32', 'Manager') ;

 INSERT INTO caller_groups VALUES('tom_hash512_saltsize32', 'Administrator') ;
 INSERT INTO caller_groups VALUES('tom_hash512_saltsize32', 'Manager') ;
