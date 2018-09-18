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

import java.sql.DatabaseMetaData;
import java.sql.Types;

/**
 * This class stores all the important properties of every parameter of a
 * stored procedure.
 * @author Sheetal Vartak
 */

public class Parameter {

  private String catalog;

  private String schema;

  private String procedureName;

  private String parameterName;

  private short parameterType;

  private short dataType;

  private short scale;

  //the above properties are the only important properties of the parameters

  public Parameter(String catalog, String schema, String procedureName, String parameterName,
      short parameterType, short dataType, short scale) {
    this.catalog = catalog;
    this.schema = schema;
    this.procedureName = procedureName;
    this.parameterName = parameterName;
    this.parameterType = parameterType;
    this.dataType = dataType;
    this.scale = scale;
  }

  public short getScale() {
    return scale;
  }

  public String getCatalog() {
    return catalog;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getProcedureName() {
    return procedureName;
  }

  public void setProcedureName(String procedureName) {
    this.procedureName = procedureName;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public short getParameterType() {
    return parameterType;
  }

  public void setParameterType(short parameterType) {
    this.parameterType = parameterType;
  }

  public short getDataType() {
    return dataType;
  }

  public void setDataType(short dataType) {
    this.dataType = dataType;
  }

  public boolean isOutputColumn() {
    return (parameterType == DatabaseMetaData.procedureColumnOut || parameterType == DatabaseMetaData.procedureColumnInOut || parameterType == DatabaseMetaData.procedureColumnReturn);
  }

  public boolean isInputColumn() {
    return (parameterType == DatabaseMetaData.procedureColumnIn || parameterType == DatabaseMetaData.procedureColumnInOut);
  }

  public boolean isDecimalNumeric() {
    return (dataType == Types.NUMERIC || dataType == Types.DECIMAL);
  }
}
