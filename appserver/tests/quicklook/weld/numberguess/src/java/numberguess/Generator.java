/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package numberguess;


import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class Generator implements Serializable
{

   private static final long serialVersionUID = -7213673465118041882L;

   private java.util.Random random = new java.util.Random( System.currentTimeMillis() );

   private int maxNumber = 100;

   java.util.Random getRandom()
   {
      return random;
   }

   @Produces @Random int next() {
      return getRandom().nextInt(maxNumber);
   }

   @Produces @MaxNumber int getMaxNumber()
   {
      return maxNumber;
   }

}
