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
 * PrimitiveStandardMBean.java
 *
 * Created on Fri Jul 08 20:26:16 PDT 2005
 */
package testmbeans;

import java.util.Date;
import javax.management.ObjectName;

/**
 * Interface PrimitiveStandardMBean
 * A Standard Test MBean with various attributes that are primitive data types
 */
public interface PrimitiveStandardMBean
{
   /**
    * Get A boolean State Attribute
    */
    public boolean getState();

   /**
    * Set A boolean State Attribute
    */
    public void setState(boolean value);

   /**
    * Get An integer Rank
    */
    public int getRank();

   /**
    * Set An integer Rank
    */
    public void setRank(int value);

   /**
    * Get Time in milliseconds
    */
    public long getTime();

   /**
    * Set Time in milliseconds
    */
    public void setTime(long value);

   /**
    * Get Length in bytes
    */
    public byte getLength();

   /**
    * Set Length in bytes
    */
    public void setLength(byte value);

   /**
    * Get A Color Code as a char
    */
    public char getColorCode();

   /**
    * Set A Color Code as a char
    */
    public void setColorCode(char value);

   /**
    * Get Number of characters
    */
    public short getCharacters();

   /**
    * Set Number of characters
    */
    public void setCharacters(short value);

   /**
    * Get The Annual Percent Rate as a float
    */
    public float getAnnualPercentRate();

   /**
    * Set The Annual Percent Rate as a float
    */
    public void setAnnualPercentRate(float value);

   /**
    * Get Temperature in degrees
    */
    public double getTemperature();

   /**
    * Set Temperature in degrees
    */
    public void setTemperature(double value);

    /**
     * Get Name as the String
     */
    public String getName();
    /**
     * Set the Name
     */
    public void setName(String name);

    /**
     * Get the StartDate attribute
     */
    public Date getStartDate();

    /**
     * Set the StartDate attribute
     */
    public void setStartDate(Date date);

    /**
     * Get the ObjectName of Resource
     */
    public ObjectName getResourceObjectName();

    /**
     * Set the ObjectName of Resource
     */
    public void setResourceObjectName(ObjectName on);
}
