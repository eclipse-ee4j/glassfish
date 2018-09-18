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
 * NumericConverter.java
 *
 * Created on March 21, 2003
 */

package com.sun.jdo.spi.persistence.support.sqlstore.utility;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * This is a interface for numeric conversion to BigDecimal or BigInteger.
 *
 * @author Shing Wai Chan
 */
public interface NumericConverter {
     /**
      * The default policy for NumericConverter.
      */
     static final int DEFAULT_POLICY = 0;

     /**
      * To convert BigInteger to BigDecimal.
      * @param bInteger the BigInteger to be converted
      * @return converted BigDecimal
      */
     BigDecimal toBigDecimal(BigInteger bInteger);

     /**
      * To convert Double to BigDecimal.
      * @param d the Double to be converted
      * @return converted BigDecimal
      */
     BigDecimal toBigDecimal(Double d);

     /**
      * To convert Float to BigDecimal.
      * @param f the Float to be converted
      * @return converted BigDecimal
      */
     BigDecimal toBigDecimal(Float f);

     /**
      * To convert Number other than BigInteger, Double and Float to BigDecimal.
      * @param n the Number to be converted
      * @return converted BigDecimal
      */
     BigDecimal toBigDecimal(Number n);

     /**
      * To convert BigDecimal to BigInteger.
      * @param bDecimal the BigDecimal to be converted
      * @return converted BigInteger
      */
     BigInteger toBigInteger(BigDecimal bDecimal);

     /**
      * To convert Double to BigInteger.
      * @param d the Double to be converted
      * @return converted BigInteger
      */
     BigInteger toBigInteger(Double d);

     /**
      * To convert Float to BigInteger.
      * @param f the Float to be converted
      * @return converted BigInteger
      */
     BigInteger toBigInteger(Float f);

     /**
      * To convert Number other than BigDecimal, Double and Float to BigInteger.
      * @param n the Number to be converted
      * @return converted BigInteger
      */
     BigInteger toBigInteger(Number n);
}
