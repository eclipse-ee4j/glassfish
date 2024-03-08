/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * NumericConverterFactory.java
 *
 * Created on March 21, 2003
 */

package com.sun.jdo.spi.persistence.support.sqlstore.utility;

/**
 * This is a factory class for NumericConverter.
 *
 * @author Shing Wai Chan
 */
public class NumericConverterFactory {
     private static NumericConverter defaultConverter = new NumericConverterImpl();

     /**
      */
     protected NumericConverterFactory() {
     }

     /**
      * This method returns an instance of NumericConverter for a given policy.
      * @param policy for determining mechanism for conversion from
      *        inexact type to exact type.
      * @return NumericConverter corresponds to a policy
      */
     public static NumericConverter getNumericConverter(int policy) {
         return defaultConverter;
     }
}
