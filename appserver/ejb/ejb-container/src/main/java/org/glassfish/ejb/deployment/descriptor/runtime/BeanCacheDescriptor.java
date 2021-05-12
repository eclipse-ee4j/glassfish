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

package org.glassfish.ejb.deployment.descriptor.runtime;

import com.sun.enterprise.deployment.DescriptorConstants;
import org.glassfish.deployment.common.Descriptor;

/**
 * iAS specific DD Element (see the ias-ejb-jar_2_0.dtd for this element)
 * @author Ludo
 * @since JDK 1.4
 */
public class BeanCacheDescriptor extends Descriptor implements DescriptorConstants{

    private Boolean isCacheOverflowAllowed;
    private String victimSelectionPolicy;

    //initialized default values for class variables
    private int maxCacheSize = MAX_CACHE_SIZE_DEFAULT;
    private int resizeQuantity = RESIZE_QUANTITY_DEFAULT;
    private int cacheIdleTimeoutInSeconds = CACHE_IDLE_TIMEOUT_DEFAULT;
    private int removalTimeoutInSeconds = REMOVAL_TIMEOUT_DEFAULT;

    /** Default constructor. */
    public BeanCacheDescriptor() {
    }

    /**
     * Getter for property cacheIdleTimeoutInSeconds.
     * @return Value of property cacheIdleTimeoutInSeconds.
     */
    public int getCacheIdleTimeoutInSeconds() {
        return cacheIdleTimeoutInSeconds;
    }

    /**
     * Setter for property cacheIdleTimeoutInSeconds.
     * @param cacheIdleTimeoutInSeconds New value of property cacheIdleTimeoutInSeconds.
     */

    public void setCacheIdleTimeoutInSeconds(int cacheIdleTimeoutInSeconds) {
        this.cacheIdleTimeoutInSeconds = cacheIdleTimeoutInSeconds;
    }

    /**
     * Getter for property isCacheOverflowAllowed.
     * @return Value of property isCacheOverflowAllowed.
     */
    public Boolean isIsCacheOverflowAllowed() {
        return isCacheOverflowAllowed;
    }

    /**
     * Setter for property isCacheOverflowAllowed.
     * @param isCacheOverflowAllowed New value of property isCacheOverflowAllowed.
     */
    public void setIsCacheOverflowAllowed(boolean isCacheOverflowAllowed) {
        this.isCacheOverflowAllowed =  Boolean.valueOf(isCacheOverflowAllowed);
    }

    /**
     * Setter for property isCacheOverflowAllowed.
     * @param isCacheOverflowAllowed New value of property isCacheOverflowAllowed.
     */
    public void setIsCacheOverflowAllowed(Boolean isCacheOverflowAllowed) {
        this.isCacheOverflowAllowed =  isCacheOverflowAllowed;
    }

    /**
     * Getter for property maxCacheSize.
     * @return Value of property maxCacheSize.
     */
    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     * Setter for property maxCacheSize.
     * @param maxCacheSize New value of property maxCacheSize.
     */
    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Getter for property resizeQuantity.
     * @return Value of property resizeQuantity.
     */
    public int getResizeQuantity() {
        return resizeQuantity;
    }

    /**
     * Setter for property resizeQuantity.
     * @param resizeQty New value of property resizeQuantity.
     */
    public void setResizeQuantity(int resizeQty) {
        this.resizeQuantity = resizeQty;
    }

    /**
     * Getter for property removalTimeoutInSeconds.
     * @return Value of property removalTimeoutInSeconds.
     */
    public int getRemovalTimeoutInSeconds() {
        return removalTimeoutInSeconds;
    }

    /**
     * Setter for property removalTimeoutInSeconds.
     * @param removalTimeoutInSeconds New value of property removalTimeoutInSeconds.
     */
    public void setRemovalTimeoutInSeconds(int removalTimeoutInSeconds) {
        this.removalTimeoutInSeconds = removalTimeoutInSeconds;
    }

    /**
     * Getter for property victimSelectionPolicy.
     * @return Value of property victimSelectionPolicy.
     */
    public java.lang.String getVictimSelectionPolicy() {
        return victimSelectionPolicy;
    }

    /**
     * Setter for property victimSelectionPolicy.
     * @param victimSelectionPolicy New value of property victimSelectionPolicy.
     */
    public void setVictimSelectionPolicy(java.lang.String victimSelectionPolicy) {
        this.victimSelectionPolicy = victimSelectionPolicy;
    }
}
