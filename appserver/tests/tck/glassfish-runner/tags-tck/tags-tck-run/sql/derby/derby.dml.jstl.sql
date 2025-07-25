Simple_Select_Query= SELECT * FROM jstl_tab1
Select_NoRows_Query= SELECT * FROM jstl_tab1 WHERE idNum = -9999
Select_Jstl_Tab1_OneRow_Query= SELECT * FROM jstl_tab1 where idNum = 1
Select_Jstl_Tab1_By_Id_Query= SELECT * FROM jstl_tab1 ORDER BY idNum
Select_Jstl_Tab1_Using_Param_Query= SELECT * FROM jstl_tab1 WHERE idNum = ?
Select_Jstl_Tab2_Using_Param_Query= SELECT idNum, lastName FROM jstl_tab2 WHERE idNum = ?

Update_Jstl_Tab2_Using_Param_Query= UPDATE jstl_tab2 SET lastName= ? WHERE idNum = ?

Delete_NoRows_Query= DELETE FROM jstl_tab2 WHERE idNum = -9999
Delete_AllRows_Query= DELETE FROM jstl_tab2 

Insert_Row_Query= INSERT INTO jstl_tab2(idNum, dob, firstName, lastName, rank, rating) VALUES (1, {d '1961-08-30'}, 'Clark', 'Kent', 1, 4.5)
Insert2_Row_Query= INSERT INTO jstl_tab2(idNum, dob, firstName, lastName, rank, rating) VALUES (2, {d '1980-12-30'}, 'Fred', 'Flinstone', 2, 4.5)
Insert3_Row_Query= INSERT INTO jstl_tab2(idNum, dob, firstName, lastName, rank, rating) VALUES (3, {d '1961-01-01'}, 'Scooby', 'Doo', 4, 4.5)
Insert4_Row_Query= INSERT INTO jstl_tab2(idNum, dob, firstName, lastName, rank, rating) VALUES (4, {d '1990-03-15'}, 'Bruce', 'Wayne', 3, 4.5)
Delete_Jstl_Tab2_Using_Param_Query=DELETE FROM jstl_tab2 where idNum = ?
Failed_Insert_Query=INSERT INTO jstl_tab2(idNum, dob, firstName, lastName, rank, rating) VALUES (2, '1992-02-03'}, NULL,NULL, 2, 4.5)
Select_Jstl_Tab2_AllRows_Query=SELECT * from jstl_tab2
Invalid_SQL_Query=This Will Fail on Any RDBMS I Hope!
Select_Jstl_Tab1_MultiParam_Query=SELECT idNum, firstName, lastName from jstl_tab1 where idNum= ? and lastName = ?
Delete_Jstl_Tab2_MultiParam_Query=DELETE FROM jstl_tab2 where idNum = ? and lastName = ?
Select_Jstl_Tab3_Date_Query=SELECT * from jstl_tab3 where aDate= ?
Select_Jstl_Tab3_Time_Query=SELECT * from jstl_tab3 where aTime= ?
Select_Jstl_Tab3_Timestamp_Query=SELECT * from jstl_tab3 where aTimestamp= ?
Insert_Jstl_Tab3_Query=INSERT INTO jstl_tab3(idNum, aDate, aTime, aTimestamp) VALUES(1, {d '2001-08-30'}, {t '20:20:20'}, {ts '2001-08-30 20:20:20'})
Insert_Jstl_Tab3_Date_Query=INSERT INTO jstl_tab3(idNum, aDate, aTime, aTimestamp) VALUES(1, ?, {t '20:20:20'}, {ts '2001-08-30 20:20:20'})
Insert_Jstl_Tab3_Time_Query=INSERT INTO jstl_tab3(idNum, aDate, aTime, aTimestamp) VALUES(1, {d '2001-08-30'}, ?, {ts '2001-08-30 20:20:20'})
Insert_Jstl_Tab3_Timestamp_Query=INSERT INTO jstl_tab3(idNum, aDate, aTime, aTimestamp) VALUES(1, {d '2001-08-30'}, {t '20:20:20'}, ?)
Delete_Jstl_Tab3_AllRows_Query= DELETE FROM jstl_tab3

Insert_Jstl_Tab3_Null_Query=INSERT INTO jstl_tab3(idNum, aDate, aTime, aTimestamp) VALUES(99, null, null, null)
Select_Jstl_Tab2_NullParam_Query= SELECT * FROM jstl_tab2 WHERE rank = ?
Insert_Jstl_Tab2_Null_Query=INSERT INTO jstl_tab2(idNum, dob, firstName, lastName, rank, rating) VALUES (5, {d '1970-07-04'}, 'Peter', 'Parker', 5, null)
Insert_Jstl_Tab2_NullParam_Query=INSERT INTO jstl_tab2(idNum, dob, firstName, lastName, rank, rating) VALUES (5, {d '1970-07-04'}, 'Peter', 'Parker', 5, ?)
