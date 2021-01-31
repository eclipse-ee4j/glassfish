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

import java.util.Properties;

/**
 * Useful services for application loading implementation
 *
 */
public interface ApplicationContext {

    /**
     * Returns the class loader associated with the application. ClassLoader instances are usually obtained by the
     * getClassLoader API on the associated ArchiveHandler for the archive type being deployed.
     *
     * This can return null and the container should allocate a ClassLoader while loading the application.
     *
     * @link {org.jvnet.glassfish.api.deployment.archive.ArchiveHandler.getClassLoader()}
     *
     * @return a class loader capable of loading classes and resources from the source
     */
    ClassLoader getClassLoader();

    /**
     * Returns the application level properties that will be persisted as a key value pair at then end of deployment. That
     * allows individual Deployers implementation to store some information at the application level that should be
     * available upon server restart. Application level propertries are shared by all the modules.
     *
     * @return the application's properties.
     */
    Properties getAppProps();

    /**
     * Returns the module level properties that will be persisted as a key value pair at then end of deployment. That allows
     * individual Deployers implementation to store some information at the module level that should be available upon
     * server restart. Module level properties are only visible to the current module.
     *
     * @return the module's properties.
     */
    Properties getModuleProps();
}
