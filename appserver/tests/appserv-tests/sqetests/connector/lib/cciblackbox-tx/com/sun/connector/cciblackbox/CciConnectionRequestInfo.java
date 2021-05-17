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

import jakarta.resource.spi.ConnectionRequestInfo;

/**
 * This implementation class enables a resource adapter to pass its own
 * request-specific data structure across connection request flow
 * @author Sheetal Vartak
 */
public class CciConnectionRequestInfo implements ConnectionRequestInfo {

  private String user;

  private String password;

  public CciConnectionRequestInfo(String user, String password) {
    this.user = user;
    this.password = password;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof CciConnectionRequestInfo) {
      CciConnectionRequestInfo other = (CciConnectionRequestInfo) obj;
      return (isEqual(this.user, other.user) && isEqual(this.password, other.password));
    } else {
      return false;
    }
  }

  public int hashCode() {
    String result = "" + user + password;
    return result.hashCode();
  }

  private boolean isEqual(Object o1, Object o2) {
    if (o1 == null) {
      return (o2 == null);
    } else {
      return o1.equals(o2);
    }
  }

}
