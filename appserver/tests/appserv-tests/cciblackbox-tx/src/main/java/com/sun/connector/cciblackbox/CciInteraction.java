/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.connector.cciblackbox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.List;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.ResourceWarning;

/**
 * This implementation class enables a component to execute EIS functions.
 * @author Sheetal Vartak
 */
public class CciInteraction implements Interaction {

  protected jakarta.resource.cci.Connection connection;

  protected CallableStatement csmt;

  public CciInteraction(jakarta.resource.cci.Connection con) {
    connection = con;
  }

  public jakarta.resource.cci.Connection getConnection() {
    return connection;
  }

  public void close() throws ResourceException {
    connection = null;

  }

  public boolean execute(InteractionSpec ispec, jakarta.resource.cci.Record input, jakarta.resource.cci.Record output)
      throws ResourceException {

    if (ispec == null || (!(ispec instanceof CciInteractionSpec))) {
      throw new ResourceException("Invalid interaction spec");
    }

    String procName = ((CciInteractionSpec) ispec).getFunctionName();
    String schema = ((CciInteractionSpec) ispec).getSchema();
    String catalog = ((CciInteractionSpec) ispec).getCatalog();
    output = exec(procName, schema, catalog, input, output);
    if (output != null) {
      return true;
    } else {
      return false;
    }
  }

  /**
  * This method does the following:
  * 1> using the DatabaseMetadata class, gets the parameters that are IN,OUT
  * or INOUT for the stored procedure.
  * 2>create the callablestatement withthe right JDBC syntax
  * e.g. {? = call proc_name(?,?)}
  * {call proc_name(?)}
  * {? = call proc_name()}
  * 3> execute the statement and return the output in an IndexedRecord object
  */
  jakarta.resource.cci.Record exec(String procName, String schema, String catalog, jakarta.resource.cci.Record input, jakarta.resource.cci.Record output)
      throws ResourceException {
    try {
      java.sql.Connection conn = ((CciConnection) connection).getManagedConnection()
          .getJdbcConnection();
      DatabaseMetaData metadata = conn.getMetaData();
      if (!metadata.supportsCatalogsInProcedureCalls()) {
        catalog = "";
      }
      if (!metadata.supportsSchemasInProcedureCalls()) {
        schema = "";
      }

      ResultSet procNames = metadata.getProcedures(catalog, schema, procName);
      int procFound = 0;
      while (procNames.next()) {
        procFound++;
      }
      procNames.close();
      if (procFound == 0) {
        throw new ResourceException(
            "Cannot find procedure " + procName + ". Please check catalog, schema and function name.");
      }

      ResultSet rs = metadata.getProcedureColumns(catalog, schema, procName, null);
      List parameterList = new ArrayList();
      boolean function = false;
      while (rs.next()) {
        if ((rs.getShort(5) == DatabaseMetaData.procedureColumnReturn) && (!((rs.getString(7))
            .equals("void")))) {
          function = true;
        }
        if (rs.getString(7).equals("void")) {
          continue; // skip extra info from Cloudscape
        }
        parameterList.add(new Parameter(rs.getString(1), rs.getString(2), rs.getString(3), rs
            .getString(4), rs.getShort(5), rs.getShort(6), rs.getShort(10)));
      }
      rs.close();

      int paramCount = parameterList.size();
      if (function) {
        paramCount -= 1;
      }
      //if the procedure is parameterless, paramCount = 0
      procName += "(";
      for (int i = 0; i < paramCount; i++) {
        if (i == 0) {
          procName += "?";
        } else {
          procName += ",?";
        }
      }
      procName += ")";
      String schemaAddOn = "";
      if (schema != null && !schema.equals("")) {
        schemaAddOn = schema + ".";
      }
      if (function) {
        procName = "? = call " + schemaAddOn + procName;
      } else {
        procName = "call " + schemaAddOn + procName;
      }
      //System.out.println("procName.."+procName);
      CallableStatement cstmt = conn.prepareCall("{" + procName + "}");

      //get all IN parameters and register all OUT parameters
      int count = parameterList.size();
      int recCount = 0;
      IndexedRecord iRec = null;

      for (int i = 0; i < count; i++) {
        Parameter parameter = (Parameter) parameterList.get(i);
        if (parameter.isInputColumn()) {
          if (iRec == null) {
            if (input instanceof IndexedRecord) {
              iRec = (IndexedRecord) input;
            } else {
              throw new ResourceException("Invalid input record");
            }
          }
          //get value from input record
          cstmt.setObject(i + 1, iRec.get(recCount));
          recCount++;
        }
      }

      IndexedRecord oRec = null;
      for (int i = 0; i < count; i++) {
        Parameter parameter = (Parameter) parameterList.get(i);
        if (parameter.isOutputColumn()) {
          if (oRec == null) {
            if (output instanceof IndexedRecord) {
              oRec = (IndexedRecord) output;
            } else {
              throw new ResourceException("Invalid output record");
            }
          }
          if (parameter.isDecimalNumeric()) {
            cstmt.registerOutParameter(i + 1, parameter.getDataType(), parameter.getScale());
          } else {
            cstmt.registerOutParameter(i + 1, parameter.getDataType());
          }
        }
      }
      cstmt.execute();

      Class[] parameters = new Class[]
      { int.class };
      //get the right getXXX() from Mapping.java for the output
      Mapping map = new Mapping();
      for (int i = 0; i < count; i++) {
        Parameter parameter = (Parameter) parameterList.get(i);
        if (parameter.isOutputColumn()) {
          String ans = (String) map.get(new Integer(parameter.getDataType()));
          Method method = cstmt.getClass().getMethod(ans, parameters);
          Object[] obj = new Object[]
          { new Integer(i + 1) };
          Object o = method.invoke(cstmt, obj);
          if (output instanceof IndexedRecord) {
            oRec = (IndexedRecord) output;
            oRec.add(o);
            //System.out.println("output..."+o.toString());
          }
        }
      }
      cstmt.close();
      return oRec;
      //  conn.close();
    }
    catch (SQLException ex) {
      throw new ResourceException(ex.getMessage());
    }
    catch (NoSuchMethodException ex) {
      throw new ResourceException(ex.getMessage());
    }
    catch (IllegalAccessException ex) {
      throw new ResourceException(ex.getMessage());
    }
    catch (InvocationTargetException ex) {
      throw new ResourceException(ex.getMessage());
    }
  }

  public jakarta.resource.cci.Record execute(InteractionSpec ispec, jakarta.resource.cci.Record input) throws ResourceException {

    if (ispec == null || (!(ispec instanceof CciInteractionSpec))) {
      throw new ResourceException("Invalid interaction spec");
    }

    String procName = ((CciInteractionSpec) ispec).getFunctionName();
    String schema = ((CciInteractionSpec) ispec).getSchema();
    String catalog = ((CciInteractionSpec) ispec).getCatalog();
    IndexedRecord output = new CciIndexedRecord();
    return exec(procName, schema, catalog, input, output);
  }

  public ResourceWarning getWarnings() throws ResourceException {
    ResourceWarning resWarning = null;
    try {
      java.sql.Connection con = ((CciConnection) connection).getManagedConnection()
          .getJdbcConnection();
      SQLWarning sql = con.getWarnings();
      resWarning = new ResourceWarning(sql.getMessage());
    }
    catch (SQLException e) {
      throw new ResourceException(e.getMessage());
    }
    return resWarning;
  }

  public void clearWarnings() throws ResourceException {
    try {
      java.sql.Connection con = ((CciConnection) connection).getManagedConnection()
          .getJdbcConnection();
      con.clearWarnings();
    }
    catch (SQLException e) {
      throw new ResourceException(e.getMessage());
    }
  }

}
