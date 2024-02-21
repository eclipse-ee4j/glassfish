/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.deployment.archive;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.Manifest;

import org.glassfish.api.deployment.DeploymentContext;

/**
 * This interface is an abstraction for accessing a module archive.
 *
 * @author Jerome Dochez
 */
public interface Archive extends AutoCloseable {

    /**
     * closes this archive and releases all resources
     */
    @Override
    void close() throws IOException;

    /**
     * Returns an enumeration of the module file entries. All elements in the enumeration are of type String. Each String
     * represents a file name relative to the root of the module.
     *
     * @return an enumeration of the archive file entries.
     */
    Enumeration<String> entries();

    /**
     * Returns an enumeration of the module file entries with the specified prefix. All elements in the enumeration are of
     * type String. Each String represents a file name relative to the root of the module.
     *
     * @param prefix the prefix of entries to be included
     * @return an enumeration of the archive file entries.
     */
    Enumeration<String> entries(String prefix);

    /**
     * Returns the enumeration of first level directories in this archive
     *
     * @return enumeration of directories under the root of this archive
     */
    Collection<String> getDirectories() throws IOException;

    /**
     * Returns true if the entry is a directory or a plain file
     *
     * @param name name is one of the entries returned by {@link #entries()}
     * @return true if the entry denoted by the passed name is a directory
     */
    boolean isDirectory(String name);

    /**
     * Returns the manifest information for this archive
     *
     * @return the manifest info or null
     */
    Manifest getManifest() throws IOException;

    /**
     * Returns the path used to create or open the underlying archive
     *
     * <p>
     * TODO: abstraction breakage: Several callers, most notably {@link DeploymentContext#getSourceDir()} implementation,
     * assumes that this URI is an URL, and in fact file URL.
     *
     * <p>
     * If this needs to be URL, use of {@link URI} is misleading. And furthermore, if its needs to be a file URL, this
     * should be {@link File}.
     *
     * @return the path for this archive.
     */
    URI getURI();

    /**
     * Returns the size of the archive.
     *
     * @return long indicating the size of the archive
     */
    long getArchiveSize() throws SecurityException;

    /**
     * Returns the name of the archive.
     * <p>
     * Implementations should not return null.
     *
     * @return the name of the archive
     */
    String getName();
}
