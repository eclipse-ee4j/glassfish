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

package com.sun.enterprise.deployment;

/**
 * Contains all deployment descriptor constants.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public interface DescriptorConstants {

    /**
     * Bean Pool - maximum size, a pool of slsb can grow to.
     */ 
    int MAX_POOL_SIZE_DEFAULT = -1;
    
    /**
     * Bean Pool - maximum time a caller will have to wait when pool 
     * has reached maximum configured size and an instance is not 
     * available to process the incoming request.
     */ 
    int MAX_WAIT_TIME_DEFAULT = -1;
    
    /**
     * Bean Pool - size of slsb pool grows in increments specified by 
     * resize-quantity, within the configured upper limit max-pool-size. 
     */
    int POOL_RESIZE_QTY_DEFAULT = -1;
    
    /**
     * Bean Pool - minimum number of slsb instances maintained in a pool.
     */ 
    int STEADY_POOL_SIZE_DEFAULT = -1;   
    
    /**
     * Bean Pool - idle bean instance in a pool becomes a candidate for 
     * passivation (sfsb/eb) or deletion (slsb), when this timeout expires.
     */ 
    int POOL_IDLE_TIMEOUT_DEFAULT = -1;
    
    /**
     * Bean Cache - sfsb and eb are created and cached, on demand.
     */     
    int MAX_CACHE_SIZE_DEFAULT = -1;
    
    /**
     * Bean Cache - resize quantity
     */     
    int RESIZE_QUANTITY_DEFAULT = -1;
    
    /**
     * Bean Cache - Passivated bean (sfsb/eb) instance is removed if it 
     * is not accesed within  this time, after passivation
     */
    int REMOVAL_TIMEOUT_DEFAULT = -1;   
    
    /**
     * Bean Cache - idle bean instance in a pool becomes a candidate for 
     * passivation (sfsb/eb) or deletion (slsb), when this timeout expires.
     */
    int CACHE_IDLE_TIMEOUT_DEFAULT = -1;
    
    /**
     * ejb - refresh period in seconds
     */
    int REFRESH_PERIOD_IN_SECONDS_DEFAULT = -1;
}
