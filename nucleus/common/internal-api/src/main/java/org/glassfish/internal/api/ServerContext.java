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

package org.glassfish.internal.api;

import java.io.File;

import javax.naming.InitialContext;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Contract;

/**
 * ServerContext interface: the server-wide runtime environment created by
 * ApplicationServer and shared by its subsystems such as the web container
 * or EJB container.
 */
@Contract
public interface ServerContext {

    /**
     * Get the server command-line arguments
     *
     * @return  the server command-line arguments
     */
    public String[] getCmdLineArgs();

    /**
     * Get a factory for supported pluggable features. The server can support
     * many pluggable features in different editions. This factory allows access
     * to specialized implementation of features.
     */
    //public PluggableFeatureFactory getPluggableFeatureFactory();

    /** XXX: begin should move these to Config API */

    /**
     * Get server install root
     *
     * @return  the server install root
     */
    public File getInstallRoot();

    /**
     * Get the server instance name
     *
     * @return  the server instance name
     */
    public String getInstanceName();

    /**
     * Get a URL representation of server configuration
     *
     * @return    the URL to the server configuration
     */
    public String getServerConfigURL();

    /**
     * Get the server configuration bean.
     *
     * @return  the server config bean
     */
    public com.sun.enterprise.config.serverbeans.Server getConfigBean();

    /**
     * Get the initial naming context.
     *
     * @return    the initial naming context
     */
    public InitialContext getInitialContext();

    /**
     * Get the classloader that loads .jars in $instance/lib and classes
     * in $instance/lib/classes.
     *
     * @return  the common class loader for this instance
     */
    public ClassLoader getCommonClassLoader();

    /**
     * Returns the shared class loader for this server instance.
     *
     * @return    the shared class loader
     */
    public ClassLoader getSharedClassLoader();

    /**
     * Get the parent class loader for the life cycle modules.
     *
     * @return  the parent class loader for the life cycle modules
     */
    public ClassLoader getLifecycleParentClassLoader();

    /**
     * Returns the environment object for this instance.
     *
     *  @return    the environment object for this server instance
     */
    //public InstanceEnvironment getInstanceEnvironment();

    /**
     * get the J2EE Server invocation manager
     *
     * @return InvocationManager
     */
    public InvocationManager getInvocationManager();

    /**
     * get the default domain name
     *
     * @return String default domain name
     */
    public String getDefaultDomainName();


    /**
     * Returns the default habitat for this instance
     * @return defa ult habitat
     */
    public ServiceLocator getDefaultServices();

}
