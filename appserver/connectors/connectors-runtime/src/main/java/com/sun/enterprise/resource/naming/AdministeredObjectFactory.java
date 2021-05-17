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

package com.sun.enterprise.resource.naming;

import com.sun.enterprise.resource.beans.AdministeredObjectResource;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.logging.LogDomains;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;

import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.naming.*;
import javax.naming.spi.ObjectFactory;

/**
 * An object factory to handle creation of administered object
 *
 * @author    Qingqing Ouyang
 *
 */
public class AdministeredObjectFactory implements ObjectFactory {

    private static Logger logger =
    LogDomains.getLogger(AdministeredObjectFactory.class, LogDomains.RSR_LOGGER);

    //required by ObjectFactory
    public AdministeredObjectFactory() {}

    public Object getObjectInstance(Object obj,
                    Name name,
                    Context nameCtx,
                    Hashtable env) throws Exception {

    Reference ref = (Reference) obj;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("AdministeredObjectFactory: " + ref
                    + " Name:" + name);
        }

        AdministeredObjectResource aor =
            (AdministeredObjectResource) ref.get(0).getContent();
        String moduleName = aor.getResourceAdapter();


        //If call fom application client, start resource adapter lazily.
        //todo: Similar code in ConnectorObjectFactory - to refactor.

        ConnectorRuntime runtime = ConnectorNamingUtils.getRuntime();
        if (runtime.isACCRuntime() || runtime.isNonACCRuntime()) {
            ConnectorDescriptor connectorDescriptor = null;
            try {
                Context ic = new InitialContext();
                String descriptorJNDIName = ConnectorAdminServiceUtils.
                        getReservePrefixedJNDINameForDescriptor(moduleName);
                connectorDescriptor = (ConnectorDescriptor) ic.lookup(descriptorJNDIName);
            } catch (NamingException ne) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Failed to look up ConnectorDescriptor "
                            + "from JNDI", moduleName);
                }
                throw new ConnectorRuntimeException("Failed to look up " +
                        "ConnectorDescriptor from JNDI");
            }
            runtime.createActiveResourceAdapter(connectorDescriptor, moduleName, null);
        }

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (runtime.checkAccessibility(moduleName, loader) == false) {
            throw new NamingException("Only the application that has the embedded resource" +
                                   "adapter can access the resource adapter");

        }

        if(logger.isLoggable(Level.FINE)) {
        logger.fine("[AdministeredObjectFactory] ==> Got AdministeredObjectResource = " + aor);
        }

    // all RARs except system RARs should have been available now.
        if(ConnectorsUtil.belongsToSystemRA(moduleName)) {
            //make sure that system rar is started and hence added to connector classloader chain
            if(ConnectorRegistry.getInstance().getActiveResourceAdapter(moduleName) == null){
                String moduleLocation = ConnectorsUtil.getSystemModuleLocation(moduleName);
                runtime.createActiveResourceAdapter(moduleLocation, moduleName, null);
            }
            loader = ConnectorRegistry.getInstance().getActiveResourceAdapter(moduleName).getClassLoader();
        } else if(runtime.isServer()){
            if(ConnectorsUtil.isStandAloneRA(moduleName) ){
                loader = ConnectorRegistry.getInstance().getActiveResourceAdapter(moduleName).getClassLoader();
            }
        }
    return aor.createAdministeredObject(loader);
    }
}
