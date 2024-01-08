/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.internal.deployment;

import java.io.File;
import java.io.IOException;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.api.ClassLoaderHierarchy;

import java.util.Map;
import java.util.Properties;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

/**
 * semi-private interface to the deployment context
 *
 * @author Jerome Dochez
 */
public interface ExtendedDeploymentContext extends DeploymentContext {

    public enum Phase { UNKNOWN, PREPARE, PREPARED, LOAD, START, STOP, UNLOAD, CLEAN, REPLICATION }

    public static final String IS_TEMP_CLASSLOADER = "isTempClassLoader";
    public static final String TRACKER = "tracker";


    /**
     * Sets the phase of the deployment activity.
     *
     * @param newPhase
     */
    public void setPhase(Phase newPhase);

    public Phase getPhase();

    /**
     * Create the deployment class loader. It will be used for sniffer
     * retrieval, metadata parsing and deployer prepare.
     *
     * @param clh the hierarchy of class loader for the parent
     * @param handler the archive handler for the source archive
     */
    public void createDeploymentClassLoader(ClassLoaderHierarchy clh, ArchiveHandler handler)
            throws URISyntaxException, MalformedURLException;

    /**
     * Create the final class loader. It will be used to load and start
     * application.
     *
     * @param clh the hierarchy of class loader for the parent
     * @param handler the archive handler for the source archive
     */
    public void createApplicationClassLoader(ClassLoaderHierarchy clh, ArchiveHandler handler)
            throws URISyntaxException, MalformedURLException;

    public void clean();

    /**
     * Sets the archive handler that's associated with this context
     *
     * @param archiveHandler
     */
    public void setArchiveHandler(ArchiveHandler archiveHandler);

    /**
     * Sets the source archive
     *
     * @param props
     */
    public void setSource(ReadableArchive source);

    /**
     * Sets the module properties for modules
     *
     * @param modulePropsMap
     */
    public void setModulePropsMap(Map<String, Properties> modulePropsMap);

    /**
     * Gets the deployment context for modules
     *
     * @return a map containing module deployment contexts
     */
    public Map<String, ExtendedDeploymentContext> getModuleDeploymentContexts();

   /**
     * Sets the classloader
     *
     * @param cloader
     */
    public void setClassLoader(ClassLoader cloader);

   /**
     * Sets the parent context
     *
     * @param parentContext
     */
    public void setParentContext(ExtendedDeploymentContext parentContext);

    /**
     * Gets the module uri for this module context
     *
     * @return the module uri
     */
    public String getModuleUri();

   /**
     * Sets the module uri for this module context
     *
     * @param moduleUri
     */
    public void setModuleUri(String moduleUri);

    /**
     * Gets the parent context for this context
     *
     * @return the parent context
     */
    public ExtendedDeploymentContext getParentContext();


    /**
     * Returns the internal directory for the application (used for holding
     * the uploaded archive, for example).
     *
     * @return location of the internal directory for the application
     */
    public File getAppInternalDir();

    /**
     * Returns the alternate deployment descriptor directory for the
     * application (used for holding the external alternate deployment
     * descriptors).
     *
     * @return location of the alternate deployment descriptor directory for
     *  the application
     */
    public File getAppAltDDDir();


    /**
     * Returns the tenant, if one is valid for this DeploymentContext.
     * @return tenant name if applicable, null if no tenant is set for this DC
     */
    public String getTenant();

    /**
     * Sets the tenant to which this deployment context applies. Also initializes
     * the tenant directory.
     *
     * @param tenantName the name of the tenant
     * @param appName the name of the application
     */
    public void setTenant(String tenant, String appName);

    /**
     * Returns the directory containing the expanded tenant customization archive,
     * if this DC is for a tenant and if a customization archive was specified
     * when the tenant was provisioned.
     * @return directory containing the expanded customization archive; null if none
     */
    public File getTenantDir();

    /**
     * Performs any clean-up of the deployment context after deployment has
     * finished.
     * <p>
     * This method can be invoked either with "true", meaning that this is the
     * final clean-up for the DC, or with "false," meaning that the DC
     * implementation should be selective.  (Some data is used, for instance,
     * in the DeployCommand logic after ApplicationLifeCycle.deploy has
     * completed.)
     *
     * @param isFinalClean whether this clean is the final clean or a selective one.
     */
    public void postDeployClean(boolean isFinalClean);

    /**
     * Prepare the scratch directories, creating the directories
     * if they do not exist
     */
    public void prepareScratchDirs() throws IOException;
}
