/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.util.Utility;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.resource.spi.ResourceAdapter;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Factory creating Active Resource adapters.
 *
 * @author Binod P.G
 */
@Service
@Singleton
public class ActiveRAFactory {
    private static final Logger LOG = LogDomains.getLogger(ActiveRAFactory.class,LogDomains.RSR_LOGGER);

    @Inject
    private ServiceLocator activeRAHabitat;
    /**
     * Creates an active resource adapter.
     *
     * @param cd         Deployment descriptor object for connectors.
     * @param moduleName Module name of the resource adapter.
     * @param loader     Class Loader,
     * @return An instance of <code> ActiveResourceAdapter </code> object.
     * @throws ConnectorRuntimeException when unable to create the runtime for RA
     */
    public ActiveResourceAdapter createActiveResourceAdapter(
            ConnectorDescriptor cd, String moduleName, ClassLoader loader)
            throws ConnectorRuntimeException {

        ActiveResourceAdapter activeResourceAdapter = null;
        ClassLoader originalContextClassLoader = null;

        ProcessEnvironment.ProcessType processType = ConnectorRuntime.getRuntime().getEnvironment();
        ResourceAdapter ra = null;
        String raClass = cd.getResourceAdapterClass();

        try {

            // If raClass is available, load it...

            if (raClass != null && !raClass.equals("")) {
                if (processType == ProcessEnvironment.ProcessType.Server) {
                    ra = (ResourceAdapter)
                            loader.loadClass(raClass).newInstance();
                } else {
                    //ra = (ResourceAdapter) Class.forName(raClass).newInstance();
                    ra = (ResourceAdapter)
                            Thread.currentThread().getContextClassLoader().loadClass(raClass).newInstance();
                }
            }

            originalContextClassLoader = Utility.setContextClassLoader(loader);
            activeResourceAdapter = instantiateActiveResourceAdapter(cd, moduleName, loader, ra);

        } catch (ClassNotFoundException Ex) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException(
                    "Error in creating active RAR");
            cre.initCause(Ex);
            LOG.log(Level.SEVERE, "rardeployment.class_not_found", raClass);
            LOG.log(Level.SEVERE, "", cre);
            throw cre;
        } catch (InstantiationException Ex) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException("Error in creating active RAR");
            cre.initCause(Ex);
            LOG.log(Level.SEVERE, "rardeployment.class_instantiation_error", raClass);
            LOG.log(Level.SEVERE, "", cre);
            throw cre;
        } catch (IllegalAccessException Ex) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException("Error in creating active RAR");
            cre.initCause(Ex);
            LOG.log(Level.SEVERE, "rardeployment.illegalaccess_error", raClass);
            LOG.log(Level.SEVERE, "", cre);
            throw cre;
        } finally {
            if (originalContextClassLoader != null) {
                Utility.setContextClassLoader(originalContextClassLoader);
            }
        }
        return activeResourceAdapter;
    }

    private  ActiveResourceAdapter instantiateActiveResourceAdapter(ConnectorDescriptor cd,
                                                                    String moduleName, ClassLoader loader,
                                                                    ResourceAdapter ra) throws ConnectorRuntimeException {
        ActiveResourceAdapter activeResourceAdapter = getActiveRA(cd, moduleName);
        activeResourceAdapter.init(ra, cd, moduleName, loader);
        return activeResourceAdapter;
    }

    private ActiveResourceAdapter getActiveRA(ConnectorDescriptor cd, String moduleName)
            throws ConnectorRuntimeException{
        Collection<ActiveResourceAdapter> activeRAs =  activeRAHabitat.getAllServices(ActiveResourceAdapter.class);
        for(ActiveResourceAdapter activeRA : activeRAs){
            if(activeRA.handles(cd, moduleName)){
                if(LOG.isLoggable(Level.FINEST)){
                    LOG.log(Level.FINEST,"found active-RA for the module [ "+moduleName+" ] " +
                        activeRA.getClass().getName());
                }
                return activeRA;
            }
        }

        if(cd.getInBoundDefined()){
            // did not find a suitable Active RA above.
            // [Possibly the profile (eg: WEB profile) does not support it]
            // Let us provide outbound support.
            LOG.log(Level.INFO, "Deployed RAR [ "+moduleName+" ] has inbound artifacts, but the runtime " +
                    "does not support it. Providing only outbound support ");

            return activeRAHabitat.getService(ActiveResourceAdapter.class, ConnectorConstants.AORA);
        }
        //could not fine any impl.
        throw new ConnectorRuntimeException("Unable to get active RA for module " + moduleName);
    }
}
