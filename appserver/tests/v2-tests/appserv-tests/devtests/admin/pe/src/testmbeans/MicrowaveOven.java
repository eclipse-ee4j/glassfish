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

package testmbeans;

/**
 * Interface MicrowaveOven
 * MicrowaveOven Description
 * @author kedarm
 */
public interface MicrowaveOven
{
   /**
    * Get Color of the oven
    */
    public String getColor();

   /**
    * Get Number of functions in the oven
    */
    public int getNoFunctions();

   /**
    * Get Make of the oven
    */
    public String getMake();

   /**
    * Get How long it should be heated in seconds
    */
    public int getTimer();

   /**
    * Set How long it should be heated in seconds
    */
    public void setTimer(int value);

   /**
    * Get NewAttribute4 Description
    */
    public boolean getState();

   /**
    * Starts the Oven
    *
    */
    public void start();

   /**
    * Stops the Oven
    *
    */
    public void stop();

}
