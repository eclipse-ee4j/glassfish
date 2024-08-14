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
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.MemberEntry;

import java.io.Reader;
import java.io.Writer;

/**
 * Creates {@link Reader} and {@link Writer} for the {@link MemberEntry} of an archive, that has to undergo string
 * substitution.
 */
public interface ArchiveMember extends Substitutable {
    /**
     * Gets the {@link ArchiveEntryWrapper} of the member entry.
     *
     * @return The parental archive wrapper of the entry.
     */
    ArchiveEntryWrapper getParent();
}
