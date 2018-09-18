/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee.services;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.deployment.ResourceDescriptor;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;
import javax.naming.Context;
import javax.naming.NamingException;

@Service
@PerLookup
public class JMSCFResourcePMProxy extends CommonResourceProxy {

    @Override
    public synchronized Object create(Context ic) throws NamingException {
        if (actualResourceName == null) {
            boolean wasDeployed = false;
            try {
                if (ic.lookup(desc.getName()) != null)
                    wasDeployed = true;
            } catch (NamingException ne) {}

            if (!wasDeployed) {
                try {
                    if (serviceLocator == null) {
                        serviceLocator = Globals.getDefaultHabitat();
                        if (serviceLocator == null) {
                            throw new NamingException("Unable to create resource " +
                                    "[" + desc.getName() + " ] as habitat is null");
                        }
                    }
                    getResourceDeployer(desc).deployResource(desc);
                } catch (Exception e) {
                    NamingException ne = new NamingException("Unable to create resource [" + desc.getName() + " ]");
                    ne.initCause(e);
                    throw ne;
                }
            }
            // append __PM suffix to jndi name
            actualResourceName = ConnectorsUtil.deriveResourceName
                    (desc.getResourceId(), ConnectorsUtil.getPMJndiName(desc.getName()), desc.getResourceType());
        }
        return ic.lookup(actualResourceName);
    }
}
