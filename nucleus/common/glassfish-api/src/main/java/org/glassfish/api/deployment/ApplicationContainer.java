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

package org.glassfish.api.deployment;

/**
 * Interface to an application container. mainly used to start and stop the application.
 *
 * @author Jerome Dochez
 */

public interface ApplicationContainer<T> {

    /**
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    T getDescriptor();

    /**
     * Starts an application container. ContractProvider starting should not throw an exception but rather should use their
     * prefered Logger instance to log any issue they encounter while starting. Returning false from a start mean that the
     * container failed to start
     *
     * @param startupContext the start up context
     * @return true if the container startup was successful.
     *
     * @throws Exception if this application container could not be started
     */
    boolean start(ApplicationContext startupContext) throws Exception;

    /**
     * Stop the application container
     *
     * @return true if stopping was successful.
     * @param stopContext
     */
    boolean stop(ApplicationContext stopContext);

    /**
     * Suspends this application container.
     *
     * @return true if suspending was successful, false otherwise.
     */
    boolean suspend();

    /**
     * Resumes this application container.
     *
     * @return true if resumption was successful, false otherwise
     *
     * @throws Exception if this application container could not be resumed
     */
    boolean resume() throws Exception;

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    ClassLoader getClassLoader();

}
