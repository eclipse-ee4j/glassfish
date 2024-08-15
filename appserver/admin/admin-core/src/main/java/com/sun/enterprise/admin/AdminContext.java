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

package com.sun.enterprise.admin;

import com.sun.enterprise.admin.util.proxy.Interceptor;

import java.net.URL;
import java.util.logging.Logger;

import javax.management.MBeanServer;

/**
 * This interface defines the environment for administration.
 */
public interface AdminContext {

    /**
     * Get runtime config context. Runtime config context provides access to
     * the configuration that the server is running with. The server reads
     * the configuration from disk at startup time, the configuration is
     * then updated for every change that is applied dynamically.
     */
    //public ConfigContext getRuntimeConfigContext();

    /**
     * Set runtime config context. If server runtime handles a configuration
     * change dynamically, the context of the runtime is updated with the new
     * changes.
     * @param ctc the config context to use for runtime
     */
    //public void setRuntimeConfigContext(ConfigContext ctx);

    /**
     * Get admin config context. Admin config context provides access to the
     * configuration on the disk. This may be different from runtime context
     * if one or changes have not been applied dynamically to the runtime.
     */
    //public ConfigContext getAdminConfigContext();

    /**
     * Set admin config context. This is the context used for updating
     * configuration on the disk.
     * @param ctx the config context to use for administration
     */
    //public void setAdminConfigContext(ConfigContext ctx);

    /**
     * Get MBeanServer in use for admin, runtime and monitoring MBeans.
     */
    public MBeanServer getMBeanServer();

    /**
     * Set MBeanServer used for admin, runtime and monitoring MBeans.
     * @param mbs the management bean server
     */
    public void setMBeanServer(MBeanServer mbs);

    /**
     * Get domain name
     */
    public String getDomainName();

    /**
     * Set domain name.
     * @param domainName name of the domain
     */
    public void setDomainName(String domainName);

    /**
     * Get server name.
     */
    public String getServerName();

    /**
     * Set server name.
     * @param serverName name of the server
     */
    public void setServerName(String serverName);

    /**
     * Get Admin MBeanRegistry xml file location
     */
    public URL getAdminMBeanRegistryURL();


    /**
     * Get Admin MBeanRegistry xml file location
     * @param url  URL of the Registry file
     */
    public void setAdminMBeanRegistryURL(URL url);

    /**
     * Get Admin MBeanRegistry xml file location
     */
    public URL getRuntimeMBeanRegistryURL();


    /**
     * Get Runtime MBeanRegistry xml file location
     * @param url  URL of the Registry file
     */
    public void setRuntimeMBeanRegistryURL(URL url);

    /**
     * Get admin logger.
     */
    public Logger getAdminLogger();

    /**
     * Set admin logger.
     * @param logger the logger for admin module
     */
    public void setAdminLogger(Logger logger);

    /**
     * Get interceptor for mbean server used. In general, this method will
     * be used only while initializing MBeanServer to setup its interceptor.
     */
    public Interceptor getMBeanServerInterceptor();

    /**
     * Set interceptor. If set prior to creating an MBeanServer, the default
     * implementation of SunOneMBeanServer factory will apply the interceptor
     * to every MBeanServer call.
     */
    public void setMBeanServerInterceptor(Interceptor interceptor);

    /**
     * returns the appropriate dotted name mbean implementation class.
     */
    public String getDottedNameMBeanImplClassName();
}
