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

package org.glassfish.admin.amx.base;

import java.util.List;
import java.util.Map;

import javax.management.MBeanOperationInfo;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Param;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
@since GlassFish V3
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@AMXMBeanMetadata(type="runtime",singleton=true, globalSingleton=true)
public interface RuntimeRoot extends AMXProxy, Utility, Singleton
{
    /**
     * The key to store the module name in the deployment descriptor map.
     * @see getDeploymentConfigurations
     */
    public static final String MODULE_NAME_KEY = "module-name";
    /**
     * The key to store the deployment descriptor path in the deployment
     * descriptor map.
     * @see getDeploymentConfigurations
     */
    public static final String DD_PATH_KEY =  "dd-path";
    /**
     * The key to store the deployment descriptor content in the deployment
     * descriptor map.
     * @see getDeploymentConfigurations
     */
    public static final String DD_CONTENT_KEY = "dd-content";

    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    public void stopDomain();

    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    public void restartDomain();

    /**
     * Return a list of deployment descriptor maps for the specified
     * application.
     * In each map:
     * a. The module name is stored by the MODULE_NAME_KEY.
     * b. The path of the deployment descriptor is stored by the DD_PATH_KEY.
     * c. The content of the deployment descriptor is stored by the
     *    DD_CONTENT_KEY.
     * @param the application name
     * @return the list of deployment descriptor maps
     *
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    public List<Map<String, String>> getDeploymentConfigurations(
            @Param(name = "applicationName") String applicationName);

    /**
     * Return the subcomponents (ejb/web) of a specified module.
     * @param applicationName the application name
     * @param moduleName the module name
     * @return a map of the sub components, where the key is the component
     *         name and the value is the component type
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    public Map<String, String> getSubComponentsOfModule(
            @Param(name = "applicationName") String applicationName,
            @Param(name = "moduleName") String moduleName);

    /**
     * Return the context root of a specified module.
     * @param applicationName the application name
     * @param moduleName the module name
     * @return the context root of a specified module
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    public String getContextRoot(
            @Param(name = "applicationName") String applicationName,
            @Param(name = "moduleName") String moduleName);

    /**
    Execute a REST command.  Do not include a leading "/".
     */
    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    public String executeREST(@Param(name = "command") final String command);

    /**
    Return the base URL for use with {@link #executeREST}.  Example:
    http://localhost:4848/__asadmin/

    Example only, the host and port are typically different.  A trailing "/" is
    included; simply append the command string and call {@link #executeREST}.
     */
    @ManagedAttribute
    public String getRESTBaseURL();

    @ManagedAttribute
    public String[] getSupportedCipherSuites();

    @ManagedAttribute
    @Description("Return the available JMXServiceURLs in no particular order")
    public String[] getJMXServiceURLs();


    /** Which: all | summary | memory| class | thread  log */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Return a summary report of the specified type")
    public String getJVMReport( @Param(name = "which")String which);

    @ManagedAttribute
    public Map<String,ServerRuntime>   getServerRuntime();

    @ManagedAttribute
    @Description("Whether the server was started with --debug")
    public boolean isStartedInDebugMode();
}










