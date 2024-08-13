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

package com.sun.enterprise.admin.servermgmt.stringsubs;

import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Archive;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.FileEntry;

import java.util.List;

/**
 * Factory to retrieve all the {@link Substitutable} entries from a {@link FileEntry} or an {@link Archive}.
 * <p>
 * NOTE: Client can provide the their own implementation to customize the retrieval of substitutable entries from a file
 * entry or an archive.
 * </p>
 */
public interface SubstitutableFactory {
    /**
     * Get's all the {@link Substitutable} entries from a {@link FileEntry}. A file entry can point to a file/directory or
     * can contain pattern or wild card characters.
     *
     * @param fileEntry A file entry.
     * @return All the eligible {@link Substitutable} entries from a file entry.
     */
    List<? extends Substitutable> getFileEntrySubstituables(FileEntry fileEntry);

    /**
     * Get's all the {@link Substitutable} entries from an {@link Archive}. An archive entry can contain one or multiple
     * member entries or can point the entries from nested archives.
     *
     * @param archive An archive.
     * @return All the eligible {@link Substitutable} entries from an archive.
     */
    List<? extends Substitutable> getArchiveEntrySubstitutable(Archive archive);
}
