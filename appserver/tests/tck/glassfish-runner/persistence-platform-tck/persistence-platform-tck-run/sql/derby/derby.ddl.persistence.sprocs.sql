DROP PROCEDURE GetEmpOneFirstNameFromOut;
CREATE PROCEDURE GetEmpOneFirstNameFromOut (out OUT_PARAM VARCHAR(255)) language java external name 'ee.jakarta.tck.persistence.storedprocedures.CS_Procs.GetEmpOneFirstNameFromOut' parameter style java;

DROP PROCEDURE GetEmpFirstNameFromOut;
CREATE PROCEDURE GetEmpFirstNameFromOut (in IN_PARAM INTEGER, out OUT_PARAM VARCHAR(255)) language java external name 'ee.jakarta.tck.persistence.storedprocedures.CS_Procs.GetEmpFirstNameFromOut' parameter style java;

DROP PROCEDURE GetEmpLastNameFromInOut;
CREATE PROCEDURE GetEmpLastNameFromInOut (inout INOUT_PARAM VARCHAR(255)) language java external name 'ee.jakarta.tck.persistence.storedprocedures.CS_Procs.GetEmpLastNameFromInOut' parameter style java;

DROP PROCEDURE GetEmpASCFromRS;
CREATE PROCEDURE GetEmpASCFromRS() language java dynamic result sets 1 external name 'ee.jakarta.tck.persistence.storedprocedures.CS_Procs.GetEmpASCFromRS' parameter style java;

DROP PROCEDURE GetEmpIdFNameLNameFromRS;
CREATE PROCEDURE GetEmpIdFNameLNameFromRS (in IN_PARAM INTEGER) language java dynamic result sets 1 external name 'ee.jakarta.tck.persistence.storedprocedures.CS_Procs.GetEmpIdFNameLNameFromRS' parameter style java;

DROP PROCEDURE GetEmpIdUsingHireDateFromOut;
CREATE PROCEDURE GetEmpIdUsingHireDateFromOut (in IN_PARAM DATE, out OUT_PARAM INTEGER) language java external name 'ee.jakarta.tck.persistence.storedprocedures.CS_Procs.GetEmpIdUsingHireDateFromOut' parameter style java;

DROP PROCEDURE UpdateEmpSalaryColumn;
CREATE PROCEDURE UpdateEmpSalaryColumn() language java external name 'ee.jakarta.tck.persistence.storedprocedures.CS_Procs.UpdateEmpSalaryColumn' parameter style java;

DROP PROCEDURE DeleteAllEmp;
CREATE PROCEDURE DeleteAllEmp() language java external name 'ee.jakarta.tck.persistence.storedprocedures.CS_Procs.DeleteAllEmp' parameter style java;

DROP FUNCTION REPLACE;
CREATE FUNCTION REPLACE (origin_string VARCHAR(255), old_string VARCHAR(255), new_string VARCHAR(255)) RETURNS VARCHAR(255) language java external name 'ee.jakarta.tck.persistence.storedprocedures.CS_Procs.ReplaceString' parameter style java;
