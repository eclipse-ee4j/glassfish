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

package org.glassfish.kernel.javaee;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Used to register MEJB for MEJB lazy initialization
 */
public class MEJBNamingObjectProxy implements NamingObjectProxy {

    private static final String NON_PORTABLE_MEJB_JNDI_NAME = "ejb/mgmt/MEJB";
    private static final String PORTABLE_MEJB_JNDI_NAME_SHORT = "java:global/mejb/MEJBBean";
    private static final String PORTABLE_MEJB_JNDI_NAME_LONG =
            "java:global/mejb/MEJBBean!org.glassfish.admin.mejb.MEJBHome";

    private static String[] jndiNames = new String[]
            {NON_PORTABLE_MEJB_JNDI_NAME,
                    PORTABLE_MEJB_JNDI_NAME_SHORT,
                    PORTABLE_MEJB_JNDI_NAME_LONG};

    private ServiceLocator habitat;

    private static final Logger _logger = LogDomains.getLogger(
            MEJBNamingObjectProxy.class, LogDomains.EJB_LOGGER);


    public MEJBNamingObjectProxy(ServiceLocator habitat) {
        this.habitat = habitat;
    }

    static String[] getJndiNames() {
        return jndiNames;
    }

    public Object create(Context ic) throws NamingException {

        Object mEJBHome = null;
        try {
            unpublishJndiNames();
            deployMEJB();
            mEJBHome = ic.lookup(NON_PORTABLE_MEJB_JNDI_NAME);
        } catch (NamingException ne) {
            throw ne;
        } catch (Exception e) {
            NamingException namingException =
                    new NamingException(e.getMessage());
            namingException.initCause(e);
            throw namingException;
        }
        return mEJBHome;
    }

    private void unpublishJndiNames() throws NamingException {
        GlassfishNamingManager gfNamingManager = habitat.getService(GlassfishNamingManager.class);
        for (String next : getJndiNames()) {
            gfNamingManager.unpublishObject(next);
        }
    }

    private void deployMEJB() throws IOException {
        _logger.info("Loading MEJB app on JNDI look up");
        ServerContext serverContext = habitat.getService(ServerContext.class);
        File mejbArchive = new File(serverContext.getInstallRoot(),
                "lib/install/applications/mejb.jar");
        DeployCommandParameters deployParams =
                new DeployCommandParameters(mejbArchive);
        String targetName = habitat.<Server>getService(Server.class, ServerEnvironment.DEFAULT_INSTANCE_NAME).getName();
        deployParams.target = targetName;
        deployParams.name = "mejb";
        ActionReport report = habitat.getService(ActionReport.class, "plain");
        Deployment deployment = habitat.getService(Deployment.class);
        ExtendedDeploymentContext dc = deployment.getBuilder(_logger, deployParams, report).source(mejbArchive).build();
        deployment.deploy(dc);

        if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
            throw new RuntimeException("Failed to deploy MEJB app: " +
                    report.getFailureCause());
        }
    }
}
