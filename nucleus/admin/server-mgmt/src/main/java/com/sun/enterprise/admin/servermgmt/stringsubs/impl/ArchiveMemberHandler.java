/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import com.sun.enterprise.admin.servermgmt.xml.stringsubs.MemberEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.Writer;

/**
 * Handles the creation of {@link Reader} and {@link Writer} for a {@link MemberEntry} of an archive.
 *
 * @see FileSubstitutionHandler
 */
public class ArchiveMemberHandler implements ArchiveMember {
    // Reference to the parent archive wrapper.
    private ArchiveEntryWrapper _archiveWrapper;
    private FileSubstitutionHandler _handler;

    /**
     * Constructs the {@link ArchiveMemberHandler} for the given input file.
     *
     * @param file Member entry that has to undergo string substitution.
     * @param wrapper Parent archive of the input file.
     * @throws FileNotFoundException If file is not found.
     */
    public ArchiveMemberHandler(File file, ArchiveEntryWrapper wrapper) throws FileNotFoundException {
        _handler = file.length() > SubstitutionFileUtil.getInMemorySubstitutionFileSizeInBytes() ? new LargeFileSubstitutionHandler(file)
                : new SmallFileSubstitutionHandler(file);
        _archiveWrapper = wrapper;
    }

    @Override
    public void finish() {
        _handler.finish();
        getParent().notifyCompletion();
    }

    @Override
    public ArchiveEntryWrapper getParent() {
        return _archiveWrapper;
    }

    @Override
    public String getName() {
        return _handler.getName();
    }

    @Override
    public Reader getReader() {
        return _handler.getReader();
    }

    @Override
    public Writer getWriter() {
        return _handler.getWriter();
    }
}
