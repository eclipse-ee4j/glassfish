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

package com.sun.enterprise.deployment.deploy.shared;

import com.sun.enterprise.deploy.shared.AbstractReadableArchive;
import com.sun.enterprise.deploy.shared.ArchiveFactory;

import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.jar.Manifest;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Implements ReadableArchive based on multiple underlying ReadableArchives,
 * each of which will be processed in order when looking up entries, finding
 * the manifest, etc.
 *
 * @author tjquinn
 */
@Service
@PerLookup
public class MultiReadableArchive extends AbstractReadableArchive {

    private ReadableArchive parentArchive = null;

    private ReadableArchive[] archives;

    @Inject
    private ArchiveFactory archiveFactory;

    @Override
    public InputStream getEntry(String name) throws IOException {
        for (ReadableArchive ra : archives) {
            final InputStream is = ra.getEntry(name);
            if (is != null) {
                return is;
            }
        }
        return null;
    }

    @Override
    public boolean exists(String name) throws IOException {
        for (ReadableArchive ra : archives) {
            if (ra.exists(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getEntrySize(String name) {
        for (ReadableArchive ra : archives) {
            final long size = ra.getEntrySize(name);
            if (size != 0) {
                return size;
            }
        }
        return 0;
    }

    @Override
    public void open(URI uri) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void open(URI... uris) throws IOException {
        archives = new ReadableArchive[uris.length];
        int slot = 0;
        for (URI uri : uris) {
            archives[slot++] = archiveFactory.openArchive(uri);
        }
    }

    @Override
    public ReadableArchive getSubArchive(String name) throws IOException {
        for (ReadableArchive ra : archives) {
            final ReadableArchive subArchive = ra.getSubArchive(name);
            if (subArchive != null) {
                return subArchive;
            }
        }
        return null;
    }

    @Override
    public boolean exists() {
        boolean result = true;
        for (ReadableArchive ra : archives) {
            result &= ra.exists();
        }
        return result;
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean renameTo(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParentArchive(ReadableArchive parentArchive) {
        this.parentArchive = parentArchive;
    }

    @Override
    public ReadableArchive getParentArchive() {
        return parentArchive;
    }

    @Override
    public void close() throws IOException {
        for (ReadableArchive ra : archives) {
            ra.close();
        }
    }

    @Override
    public Enumeration<String> entries() {
        /*
         * Guard against the same entry appearing in multiple archives.
         * Only one will be returned so only save the name once.
         */
        final LinkedHashSet<String> enums = new LinkedHashSet<String>();
        for (ReadableArchive ra : archives) {
            for (Enumeration<String> e = ra.entries(); e.hasMoreElements(); ) {
                enums.add(e.nextElement());
            }
        }
        return Collections.enumeration(enums);
    }

    @Override
    public Enumeration<String> entries(String prefix) {
        final LinkedHashSet<String> enums = new LinkedHashSet<String>();
        for (ReadableArchive ra : archives) {
            for (Enumeration<String> e = ra.entries(prefix); e.hasMoreElements();) {
                enums.add(e.nextElement());
            }
        }
        return Collections.enumeration(enums);
    }

    @Override
    public Collection<String> getDirectories() throws IOException {
        final Collection<String> result = new LinkedHashSet<String>();
        for (ReadableArchive ra : archives) {
            result.addAll(ra.getDirectories());
        }
        return result;
    }

    @Override
    public boolean isDirectory(String name) {
        for (ReadableArchive ra : archives) {
            if (ra.isDirectory(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Manifest getManifest() throws IOException {
        for (ReadableArchive ra : archives) {
            final Manifest mf = ra.getManifest();
            if (mf != null) {
                return mf;
            }
        }
        return null;
    }

    @Override
    public URI getURI() {
        /*
         * This is not arbitrary.  By convention, the facade ReadableArchive
         * will be listed first when the MultiReadableArchive is created.
         * The URI returned from this method is, for example, added to a
         * class path.  The facade JAR points to the client JAR so adding
         * the facade URI to a class path essentially adds both.
         */
        return archives[0].getURI();
    }

    public URI getURI(final int slot) {
        return archives[slot].getURI();
    }

    @Override
    public long getArchiveSize() throws SecurityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        final StringBuilder name = new StringBuilder();
        for (ReadableArchive a : archives) {
            if (name.length() > 0) {
                name.append(",");
            }
            name.append(a.getName());
        }
        return name.toString();
    }
}
