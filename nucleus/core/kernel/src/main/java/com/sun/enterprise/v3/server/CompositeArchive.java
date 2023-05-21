/*
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

package com.sun.enterprise.v3.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.Manifest;

import org.glassfish.api.deployment.archive.CompositeHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;

import com.sun.enterprise.deploy.shared.AbstractReadableArchive;

/**
 * A composite archive is a readable archive that hides the sub archives.
 *
 * @author Jerome Dochez
 */
public class CompositeArchive extends AbstractReadableArchive {

    final ReadableArchive delegate;
    final CompositeHandler filter;

    public CompositeArchive(ReadableArchive delegate, CompositeHandler filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public InputStream getEntry(String name) throws IOException {
        if (filter.accept(delegate, name)) {
            return delegate.getEntry(name);
        }
        return null;
    }

    @Override
    public boolean exists(String name) throws IOException {
        if (filter.accept(delegate, name)) {
            return delegate.exists(name);
        }
        return false;
    }

    @Override
    public long getEntrySize(String name) {
        if (filter.accept(delegate, name)) {
            return delegate.getEntrySize(name);
        }
        return 0;
    }

    @Override
    public void open(URI uri) throws IOException {
        delegate.open(uri);
    }

    @Override
    public ReadableArchive getSubArchive(String name) throws IOException {
        if (filter.accept(delegate, name)) {
            return delegate.getSubArchive(name);
        }
        return null;
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public boolean delete() {
        return delegate.delete();
    }

    @Override
    public boolean renameTo(String name) {
        return delegate.renameTo(name);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public Enumeration<String> entries() {

        Enumeration<String> original = delegate.entries();
        Vector<String> results = new Vector<>();
        while (original.hasMoreElements()) {
            String entryName = original.nextElement();
            if (filter.accept(delegate, entryName)) {
                results.add(entryName);
            }
        }
        return results.elements();
    }

    @Override
    public Enumeration<String> entries(String prefix) {

        Enumeration<String> original = delegate.entries(prefix);
        Vector<String> results = new Vector<>();
        while (original.hasMoreElements()) {
            String entryName = original.nextElement();
            if (filter.accept(delegate, entryName)) {
                results.add(entryName);
            }
        }
        return results.elements();
    }

    @Override
    public boolean isDirectory(String name) {
        if (filter.accept(delegate, name)) {
            return delegate.isDirectory(name);
        }
        return false;
    }

    @Override
    public Manifest getManifest() throws IOException {
        return delegate.getManifest();
    }

    @Override
    public URI getURI() {
        return delegate.getURI();
    }

    @Override
    public long getArchiveSize() throws SecurityException {
        return delegate.getArchiveSize();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    // we don't hide the top level directories as we need to use them
    // to figure out whether the EarSniffer can handle it in the
    // case of optional application.xml
    @Override
    public Collection<String> getDirectories() throws IOException {
        return delegate.getDirectories();
    }
}
