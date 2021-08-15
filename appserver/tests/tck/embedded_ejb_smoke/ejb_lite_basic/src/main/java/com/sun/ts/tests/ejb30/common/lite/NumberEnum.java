/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */
package com.sun.ts.tests.ejb30.common.lite;

import java.util.EnumSet;

public enum NumberEnum implements NumberIF {

  ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), OTHERS(6);

  private int number;

  private NumberEnum(int n) {
    number = n;
  }

  public int getNumber() {
    return number;
  }

  public static NumberEnum getEnumFor(int n) {
    for (NumberEnum e : EnumSet.allOf(NumberEnum.class)) {
      if (e.number == n) {
        return e;
      }
    }
    return OTHERS;
  }

  public int add(int toAdd) {
    return toAdd + number;
  }

  public NumberIF add(NumberIF toAdd) {
    return getEnumFor(toAdd.add(number));
  }
}
