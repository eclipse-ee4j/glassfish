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

import java.util.Hashtable;

/** Mapping between JDBC Type codes and the ResultSet.getXXX methods
 * This class is used by the CciInteractionSpec.exec() to determine
 * what getXXX method to use for a particular type of parameter.
 * @author Sheetal Vartak
 */

public class Mapping extends Hashtable {

  public Mapping() {
    this.put(new Integer(java.sql.Types.TINYINT), "getByte");
    this.put(new Integer(java.sql.Types.SMALLINT), "getShort");
    this.put(new Integer(java.sql.Types.INTEGER), "getInt");
    this.put(new Integer(java.sql.Types.BIGINT), "getLong");
    this.put(new Integer(java.sql.Types.REAL), "getFloat");
    this.put(new Integer(java.sql.Types.FLOAT), "getDouble");
    this.put(new Integer(java.sql.Types.DOUBLE), "getDouble");
    this.put(new Integer(java.sql.Types.DECIMAL), "getBigDecimal");
    this.put(new Integer(java.sql.Types.NUMERIC), "getBigDecimal");
    this.put(new Integer(java.sql.Types.BIT), "getBoolean");
    this.put(new Integer(java.sql.Types.CHAR), "getString");
    this.put(new Integer(java.sql.Types.VARCHAR), "getString");
    this.put(new Integer(java.sql.Types.LONGVARCHAR), "getString");
    this.put(new Integer(java.sql.Types.BINARY), "getBytes");
    this.put(new Integer(java.sql.Types.VARBINARY), "getBytes");
    this.put(new Integer(java.sql.Types.LONGVARBINARY), "getBinaryStream");
    this.put(new Integer(java.sql.Types.DATE), "getDate");
    this.put(new Integer(java.sql.Types.TIME), "getTime");
    this.put(new Integer(java.sql.Types.TIMESTAMP), "getTimestamp");
    this.put(new Integer(java.sql.Types.OTHER), "getObject");
  }
}
