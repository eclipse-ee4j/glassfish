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

import java.io.Reader;
import java.io.Writer;

/**
 * Defines the creation of {@link Reader} & {@link Writer} for a file on which string substitution has to be performed.
 */
public interface Substitutable {
    /**
     * Gets the processing entity name on which string substitution operation is carrying on.
     *
     * @return Name of the entity.
     */
    String getName();

    /**
     * Gets the character stream from the input.
     * <p>
     * Implementation note: It is a good idea for the input stream to be buffered.
     * </p>
     *
     * @return A Reader.
     */
    Reader getReader();

    /**
     * Gets the {@link Writer} object to write the character stream in to the output.
     * <p>
     * Implementation note: It is a good idea for the output stream to be buffered.
     * </p>
     *
     * @return A Writer.
     */
    Writer getWriter();

    /**
     * Called at the completion of the substitution process to perform post operation. For e.g closing of reader/writer,
     * cleaning of the temporary data... etc.
     */
    void finish();
}
