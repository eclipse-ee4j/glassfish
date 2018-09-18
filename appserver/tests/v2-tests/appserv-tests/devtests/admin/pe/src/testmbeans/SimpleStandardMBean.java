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

/**
 * SimpleStandardMBean.java
 *
 * Created on Sat Jul 02 01:54:54 PDT 2005
 */
package testmbeans;

/**
 * Interface SimpleStandardMBean
 * SimpleStandard Description
 * @author kedarm
 */
public interface SimpleStandardMBean
{
   /**
    * Get This is the Color Attribute.
    */
    public String getColor();

   /**
    * Set This is the Color Attribute.
    */
    public void setColor(String value);

   /**
    * Get This is the State Attribute
    */
    public boolean getState();

   /**
    * Set This is the State Attribute
    */
    public void setState(boolean value);

   /**
    * Greets someone
    *
    * @param name <code>String</code> The person to greet
    */
    public void greet(String name);

}
