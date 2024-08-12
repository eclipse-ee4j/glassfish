/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.container;

import com.sun.enterprise.module.HK2Module;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Contract;

/**
 * A sniffer implementation is responsible for identifying a particular application type and/or a particular file type.
 *
 * <p>
 * For clients who want to work with Sniffers, see <tt>SnifferManager</tt> in the kernel.
 *
 * @author Jerome Dochez
 */
@Contract
public interface Sniffer {

    /**
     * Returns true if the passed file or directory is recognized by this sniffer.
     *
     * @param context deployment context
     * @return true if the location is recognized by this sniffer
     */
    boolean handles(DeploymentContext context);

    /**
     * Returns true if the passed file or directory is recognized by this sniffer.
     *
     * @param source the file or directory abstracted as an archive resources from the source archive.
     * @return true if the location is recognized by this sniffer
     */
    boolean handles(ReadableArchive source);

    /**
     * Returns the array of patterns to apply against the request URL If the pattern matches the URL, the service method of
     * the associated container will be invoked
     *
     * @return array of patterns
     */
    String[] getURLPatterns();

    /**
     * Returns the list of annotations types that this sniffer is interested in. If an application bundle contains at least
     * one class annotated with one of the returned annotations, the deployment process will not call the handles method but
     * will invoke the containers deployers as if the handles method had been called and returned true.
     *
     * @return list of annotations this sniffer is interested in or an empty array
     */
    Class<? extends Annotation>[] getAnnotationTypes();

    /**
     * Returns the list of annotation names that this sniffer is interested in. If an application bundle contains at least
     * one class annotated with one of the returned annotations, the deployment process will not call the handles method but
     * will invoke the containers deployers as if the handles method had been called and returned true.
     *
     * @param context deployment context
     * @return list of annotation names this sniffer is interested in or an empty array
     */
    String[] getAnnotationNames(DeploymentContext context);

    /**
     * Returns the container type associated with this sniffer
     *
     * @return the container type
     */
    String getModuleType(); // This method should be renamed to getContainerType

    /**
     * Sets up the container libraries so that any imported bundle from the connector jar file will now be known to the
     * module subsystem
     *
     * This method returns a {@link HK2Module}s for the module containing the core implementation of the container. That
     * means that this module will be locked as long as there is at least one module loaded in the associated container.
     *
     * @param containerHome is where the container implementation resides
     * @param logger the logger to use
     * @return the module definition of the core container implementation.
     *
     * @throws java.io.IOException exception if something goes sour
     */
    HK2Module[] setup(String containerHome, Logger logger) throws IOException;

    /**
     * Tears down a container, remove all imported libraries from the module subsystem.
     *
     */
    void tearDown();

    /**
     * Returns the list of Containers that this Sniffer enables.
     *
     * The runtime will look up each container implementing using the names provided in the habitat.
     *
     * @return list of container names known to the habitat for this sniffer
     */
    String[] getContainersNames();

    /**
     * @return whether this sniffer should be visible to user
     *
     */
    boolean isUserVisible();

    /**
     * @return whether this sniffer represents a Jakarta EE container type
     *
     */
    boolean isJakartaEE();

    /**
     * Returns a map of deployment configurations for this Sniffer from the specific archive source.
     * <p>
     * Many sniffers (esp. Jakarta EE sniffers) will choose to set the key of each map entry to the
     * relative path within the {@link ReadableArchive} of the deployment descriptor and the value
     * of that map entry to the descriptor's contents.
     *
     * @param source the contents of the application's archive
     * @return map of configuration names to configurations for the application
     * @throws java.io.IOException in case of errors searching or reading the archive for the
     *             deployment configuration(s)
     */
    Map<String, String> getDeploymentConfigurations(final ReadableArchive source) throws IOException;

    /**
     * @return the set of the sniffers that should not co-exist for the same module. For example, ejb and appclient sniffers
     * should not be returned in the sniffer list for a certain module. This method will be used to validate and filter the
     * retrieved sniffer lists for a certain module
     *
     */
    String[] getIncompatibleSnifferTypes();

    /**
     *
     * This API is used to help determine if the sniffer should recognize the current archive. If the sniffer does not
     * support the archive type associated with the current deployment, the sniffer should not recognize the archive.
     *
     * @param archiveType the archive type to check
     * @return whether the sniffer supports the archive type
     *
     */
    boolean supportsArchiveType(ArchiveType archiveType);
}
