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

package com.sun.enterprise.deployment.runtime;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.DescriptorConstants;

/** iAS specific DD Element (see the ias-ejb-jar_2_0.dtd for this element)
 * @author Ludo
 * @since JDK 1.4
 */
public class BeanPoolDescriptor extends Descriptor implements DescriptorConstants {

    private int maxPoolSize = MAX_POOL_SIZE_DEFAULT;
    private int poolIdleTimeoutInSeconds = POOL_IDLE_TIMEOUT_DEFAULT;
    private int maxWaitTimeInMillis = MAX_WAIT_TIME_DEFAULT;
    private int poolResizeQuantity = POOL_RESIZE_QTY_DEFAULT;
    private int steadyPoolSize = STEADY_POOL_SIZE_DEFAULT;


    /** Default constructor. */
    public BeanPoolDescriptor() {
    }

     /** Getter for property poolIdleTimeoutInSeconds.
     * @return Value of property idleTimeoutInSeconds.
     */
    public int getPoolIdleTimeoutInSeconds() {
        return poolIdleTimeoutInSeconds;
    }

    /** Setter for property poolIdleTimeoutInSeconds.
     * @param poolIdleTimeoutInSeconds New value of property poolIdleTimeoutInSeconds.
     */
    public void setPoolIdleTimeoutInSeconds(int poolIdleTimeoutInSeconds) {
        this.poolIdleTimeoutInSeconds = poolIdleTimeoutInSeconds;
    }

    /** Getter for property maxPoolSize.
     * @return Value of property maxPoolSize.
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /** Setter for property maxPoolSize.
     * @param maxPoolSize New value of property maxPoolSize.
     */
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    /** Getter for property maxWaitTimeInMillis.
     * @return Value of property maxWaitTimeInMillis.
     */
    public int getMaxWaitTimeInMillis() {
        return maxWaitTimeInMillis;
    }

    /** Setter for property maxWaitTimeInMillis.
     * @param maxWaitTimeInMillis New value of property maxWaitTimeInMillis.
     */
    public void setMaxWaitTimeInMillis(int maxWaitTimeInMillis) {
        this.maxWaitTimeInMillis = maxWaitTimeInMillis;
    }

    /** Getter for property poolResizeQuantity
     * @return Value of property poolResizeQuantity.
     */
    public int getPoolResizeQuantity() {
        return poolResizeQuantity;
    }

    /** Setter for property poolResizeQuantity.
     * @param poolResizeQuantity New value of property poolResizeQuantity.
     */
    public void setPoolResizeQuantity(int poolResizeQuantity) {
        this.poolResizeQuantity = poolResizeQuantity;
    }

    /** Getter for property steadyPoolSize
    * @return Value of property steadyPoolSize.
    */
    public int getSteadyPoolSize() {
        return steadyPoolSize;
    }

    /** Setter for property steadyPoolSize.
     * @param steadyPoolSize New value of property steadyPoolSize.
     */
    public void setSteadyPoolSize(int steadyPoolSize) {
        this.steadyPoolSize = steadyPoolSize;
    }
}

