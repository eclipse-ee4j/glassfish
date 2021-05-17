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

/**
 * This implementation class is used by an application component to pass
 * connection-specific info/properties to the getConnection method in
 * CciConnectionFactory class
 *
 * @author Sheetal Vartak
 */
public class CciConnectionSpec implements jakarta.resource.cci.ConnectionSpec {

  private String user;

  private String password;

  public CciConnectionSpec(String user, String password) {
    this.user = user;
    this.password = password;
  }

  public CciConnectionSpec() {
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
