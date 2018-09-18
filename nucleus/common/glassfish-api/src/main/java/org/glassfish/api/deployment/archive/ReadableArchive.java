/*
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

import org.jvnet.hk2.annotations.Contract;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Interface for implementing read access to an underlying archive on a unspecified medium
 *
 * @author Jerome Dochez
 */
@Contract
public interface ReadableArchive extends Archive {

    /**
     * Returns the InputStream for the given entry name
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.
     * @return the InputStream for the given entry name or null if not found.
     */
    public InputStream getEntry(String name) throws IOException;

    /**
     * Returns the existence of the given entry name
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.
     * @return the existence the given entry name.
     */
    public boolean exists(String name) throws IOException;

    /**
     * Returns the entry size for a given entry name or 0 if not known
     *
     * @param name the entry name
     * @return the entry size
     */
    public long getEntrySize(String name);

    /**
     * Open an abstract archive
     *
     * @param uri path to the archive
     */
    public void open(URI uri) throws IOException;

    /**
     * Returns an instance of this archive abstraction for an embedded
     * archive within this archive.
     *
     * @param name is the entry name relative to the root for the archive
     * @return
     *      the Archive instance for this abstraction,
     *      or null if no such entry exists.
     */
    public ReadableArchive getSubArchive(String name) throws IOException;

    /**
     * @return true if this archive exists
     */
    public boolean exists();

    /**
     * deletes the archive
     */
    public boolean delete();

    /**
     * rename the archive
     *
     * @param name the archive name
     */
    public boolean renameTo(String name);

    /**
     * set the parent archive for this archive
     *
     * @param parentArchive the parent archive
     */
    public void setParentArchive(ReadableArchive parentArchive);

    /**
     * get the parent archive of this archive
     *
     * @return the parent archive
     */
    public ReadableArchive getParentArchive();

    /**
     * Returns any data that could have been calculated as part of
     * the descriptor loading.
     *
     * @param dataType the type of the extra data
     * @return the extra data or null if there are not an instance of
     * type dataType registered.
     */
    public <U> U getExtraData(Class<U> dataType);

    public <U> void setExtraData(Class<U> dataType, U instance);

    public <U> void removeExtraData(Class<U> dataType);

    public void addArchiveMetaData(String metaDataKey, Object metaData);

    public <T> T getArchiveMetaData(String metaDataKey, Class<T> metadataType);

    public void removeArchiveMetaData(String metaDataKey);
}
