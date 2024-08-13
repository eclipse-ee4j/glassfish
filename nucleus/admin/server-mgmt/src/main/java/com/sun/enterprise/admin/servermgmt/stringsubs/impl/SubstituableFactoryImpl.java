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

import com.sun.enterprise.admin.servermgmt.SLogger;
import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.admin.servermgmt.stringsubs.SubstitutableFactory;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Archive;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.FileEntry;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default {@link SubstitutableFactory} implementation to retrieve the {@link Substitutable} entries from a
 * {@link FileEntry} or an {@link Archive}.
 */
public class SubstituableFactoryImpl implements SubstitutableFactory {

    private static final Logger _logger = SLogger.getLogger();

    @Override
    public List<? extends Substitutable> getFileEntrySubstituables(FileEntry fileEntry) {
        return new FileEntryFactory().getFileElements(fileEntry);
    }

    @Override
    public List<? extends Substitutable> getArchiveEntrySubstitutable(Archive archive) {
        try {
            return new ArchiveEntryWrapperImpl(archive).getSubstitutables();
        } catch (IOException e) {
            _logger.log(Level.INFO, SLogger.ERR_RETRIEVING_SUBS_ENTRIES, archive.getName());
        }
        return null;
    }
}
