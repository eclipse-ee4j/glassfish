drop table jstl_tab1 ;
create table jstl_tab1(idNum INT NOT NULL, firstName VARCHAR(20) NOT NULL, lastName VARCHAR(20) NOT NULL, primary key(idNum)) ;

drop table jstl_tab2 ;
create table jstl_tab2(idNum INT NOT NULL, dob DATE NOT NULL, firstName VARCHAR(20) NOT NULL, lastName VARCHAR(20) NOT NULL, rank INT NOT NULL, rating NUMERIC(10,2)) ;

drop table jstl_tab3 ;
create table jstl_tab3(idNum INTEGER NOT NULL, aDate DATE, aTime TIME, aTimestamp TIMESTAMP) ;

 
INSERT INTO jstl_tab1 VALUES(1, 'Lance', 'Andersen') ;
INSERT INTO jstl_tab1 VALUES(2, 'Ryan', 'Lubke') ;
INSERT INTO jstl_tab1 VALUES(3, 'Sandra', 'Roberts') ;
INSERT INTO jstl_tab1 VALUES(4, 'Hong', 'Zhang') ;
INSERT INTO jstl_tab1 VALUES(5, 'Raja', 'Perumal') ;
INSERT INTO jstl_tab1 VALUES(6, 'Shelly', 'McGowan') ;
INSERT INTO jstl_tab1 VALUES(7, 'Ryan', 'O''Connell') ;
INSERT INTO jstl_tab1 VALUES(8, 'Tonya', 'Andersen') ;
INSERT INTO jstl_tab1 VALUES(9, 'Eric', 'Jendrock') ;
INSERT INTO jstl_tab1 VALUES(10, 'Carla', 'Mott') ;
INSERT INTO jstl_tab2(idNum, dob, firstName, lastName, rank, rating) VALUES (1, {d '1999-05-05'}, 'Lance', 'Andersen', 1, 4.25) ;
INSERT INTO jstl_tab2(idNum, dob, firstName, lastName, rank, rating) VALUES (99, {d '1999-05-05'}, 'Courtney', 'Andersen', 1, NULL) ;
INSERT INTO jstl_tab3(idNum, aDate, aTime, aTimestamp) VALUES(1, {d '2001-08-30'}, {t '20:20:20'}, {ts '2001-08-30 20:20:20'}) ;
INSERT INTO jstl_tab3(idNum, aDate, aTime, aTimestamp) VALUES(2, NULL, NULL, NULL) ;
