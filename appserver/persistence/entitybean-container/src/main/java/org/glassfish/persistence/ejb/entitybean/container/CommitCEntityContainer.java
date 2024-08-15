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

package org.glassfish.persistence.ejb.entitybean.container;

import com.sun.ejb.EjbInvocation;
import com.sun.enterprise.security.SecurityManager;

import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/*
* This class implements the Commit-Option C as described in
* the EJB Specification.
*
* The CommitOptionC Container extends Entity Container and
* hence all the life cycle management is still in Entitycontainer
*
* @author Mahesh Kannan
*/

public class CommitCEntityContainer
    extends EntityContainer
{
    /**
     * This constructor is called from the JarManager when a Jar is deployed.
     * @exception Exception on error
     */
    protected CommitCEntityContainer(EjbDescriptor desc, ClassLoader loader, SecurityManager sm)
        throws Exception
    {
        super(desc, loader, sm);
    }

    protected EntityContextImpl getReadyEJB(EjbInvocation inv) {
        Object primaryKey = getInvocationKey(inv);
        return activateEJBFromPool(primaryKey, inv);
    }

    protected void createReadyStore(int cacheSize, int numberOfVictimsToSelect,
            float loadFactor, long idleTimeout)
    {
        readyStore = null;
    }

    protected void createEJBObjectStores(int cacheSize,
            int numberOfVictimsToSelect, long idleTimeout) throws Exception
    {
        super.defaultCacheEJBO = false;
        super.createEJBObjectStores(cacheSize, numberOfVictimsToSelect, idleTimeout);
    }

    // called from releaseContext, afterCompletion
    protected void addReadyEJB(EntityContextImpl context) {
        passivateAndPoolEJB(context);
    }

    protected void destroyReadyStoreOnUndeploy() {
        readyStore = null;
    }

    protected void removeContextFromReadyStore(Object primaryKey,
            EntityContextImpl context)
    {
        // There is nothing to remove as we don't have a readyStore
    }

}

