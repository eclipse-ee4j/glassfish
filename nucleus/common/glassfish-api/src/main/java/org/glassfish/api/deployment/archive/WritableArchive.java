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

import java.io.IOException;
import java.net.URI;

import org.jvnet.hk2.annotations.Contract;

/**
 * Interface for implementing write access to an underlying archive on a unspecified medium
 *
 * @author Jerome Dochez
 */
@Contract
public interface WritableArchive extends Archive {

    /**
     * Creates a new abstract archive with the given path
     *
     * @param uri the path to create the archive
     */
    void create(URI uri) throws IOException;

    /**
     * Create a new entry in the archive. Caller is responsible for closing the entry.
     *
     * @param name the entry name
     * @return an {@link WritableArchiveEntry} for a new entry in this current abstract archive.
     */
    WritableArchiveEntry putNextEntry(String name) throws IOException;

    /**
     * Returns an instance of this archive abstraction for an embedded archive within this archive.
     *
     * @param name is the entry name relative to the root for the archive
     * @return the Archive instance for this abstraction
     */
    WritableArchive createSubArchive(String name) throws IOException;
}
