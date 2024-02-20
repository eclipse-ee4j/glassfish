/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;

/**
 * Deployer service for GlassFish. Using this service, one can deploy and undeploy applications.
 * It accepts URI as an input for deployment and hence is very easily extensible. User can install their own
 * custom URL handler in Java runtime and create URIs with their custom scheme and pass them to deploy method.
 *
 * @see org.glassfish.embeddable.GlassFish#getDeployer()
 * @see java.net.URL#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory)
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface Deployer {
    /**
     * Deploys an application identified by a URI. URI is used as it is very extensible.
     * GlassFish does not care about what URI scheme is used as long as there is a URL handler installed
     * in the JVM to handle the scheme and a JarInputStream can be obtained from the given URI.
     * This method takes a var-arg argument for the deployment options. Any option that's applicable
     * to "asadmin deploy" command is also applicable here with same semantics. Please refer to GlassFish
     * deployment guide for all available options.
     *
     * <p/>Examples:
     * <pre>
     *
     *           deployer.deploy(new URI("http://acme.com/foo.war"));
     *
     *           deployer.deploy(new URI("http://acme.com/foo.war"),
     *                                    "--name", "bar", "--force", "true", "--create-tables", "true");
     * </pre>
     * @param archive URI identifying the application to be deployed.
     * @param params Optional list of deployment options.
     * @return the name of the deployed application
     */
    String deploy(URI archive, String... params) throws GlassFishException;

    /**
     * Deploys an application identified by a file. Invoking this method is equivalent to invoking
     * {@link #deploy(URI, String...) <tt>deploy(file.toURI, params)</tt>}.
     *
     * @param file File or directory identifying the application to be deployed.
     * @param params Optional list of deployment options.
     * @return the name of the deployed application
     */
    String deploy(File file, String... params) throws GlassFishException;

    /**
     * Deploys an application from the specified <code>InputStream</code> object.
     * The input stream is closed when this method completes, even if an exception is thrown.
     *
     * @param is InputStream used to read the content of the application.
     * @param params Optional list of deployment options.
     * @return the name of the deployed application
     */
    String deploy(InputStream is, String... params) throws GlassFishException;

    /**
     * Undeploys an application from {@link GlassFish}
     * This method takes a var-arg argument for the undeployment options. Any option that's applicable
     * to "asadmin undeploy" command is also applicable here with same semantics. Please refer to GlassFish
     * deployment guide for all available options.
     *
     * <p/>Example:
     * <pre>
     *          deployer.undeploy("foo", "--drop-tables", "true");
     * </pre>
     *
     * @param appName Identifier of the application to be undeployed.
     * @param params Undeployment options.
     */
    void undeploy(String appName, String... params) throws GlassFishException;

    /**
     * Return names of all the deployed applications.
     * @return names of deployed applications.
     */
    Collection<String> getDeployedApplications() throws GlassFishException;
}
