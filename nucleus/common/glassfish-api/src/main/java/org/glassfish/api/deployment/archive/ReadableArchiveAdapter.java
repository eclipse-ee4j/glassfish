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

import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.glassfish.api.deployment.archive.ReadableArchive;

/**
 * A <strong>lot</strong> of methods need to be written in order to implement ReadableArchive. The no-op methods are
 * implemented here to make ScatteredWar easier to understand.
 * 
 * @author Byron Nevins
 */
abstract public class ReadableArchiveAdapter implements ReadableArchive {

    public long getEntrySize(String arg0) {
        return 0L;
    }

    public void open(URI arg0) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ReadableArchive getSubArchive(String arg0) throws IOException {
        return null;
    }

    public boolean delete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean renameTo(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getArchiveSize() throws SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean exists() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration<String> entries() {
        return null;
    }

    public Enumeration<String> entries(String prefix) {
        return null;
    }

    public Collection<String> getDirectories() throws IOException {
        return null;
    }

    public boolean isDirectory(java.lang.String name) {
        return false;
    }

    public void setParentArchive(ReadableArchive parentArchive) {
    }

    public ReadableArchive getParentArchive() {
        return null;
    }

    public <U> U getExtraData(Class<U> dataType) {
        return null;
    }

    public <U> void setExtraData(Class<U> dataType, U instance) {
    }

    public <U> void removeExtraData(Class<U> dataType) {
    }

    public void addArchiveMetaData(String metaDataKey, Object metaData) {
    }

    public <T> T getArchiveMetaData(String metaDataKey, Class<T> metadataType) {
        return null;
    }

    public void removeArchiveMetaData(String metaDataKey) {
    }

}
