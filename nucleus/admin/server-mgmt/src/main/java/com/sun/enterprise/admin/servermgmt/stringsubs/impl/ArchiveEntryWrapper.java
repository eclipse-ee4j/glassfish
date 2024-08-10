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

import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Archive;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Group;

import java.util.List;

/**
 * Wraps the processing of an {@link Archive} of a {@link Group}.
 */
public interface ArchiveEntryWrapper {
    /**
     * Get's the parent archive wrapper applicable only for the nested archive.
     *
     * @return Reference to the parent archive or <code>null</code> if no parent archive found.
     */
    ArchiveEntryWrapper getParentArchive();

    /**
     * Get's all the substitutable entries from an archive. List also includes the {@link Substitutable} entries from the
     * nested archive.
     *
     * @return All the archive entries that has to undergo string substitution.
     */
    List<? extends ArchiveMember> getSubstitutables();

    /**
     * An {@link ArchiveMember} call this method to notify the successful completion of string substitution.
     */
    void notifyCompletion();
}
