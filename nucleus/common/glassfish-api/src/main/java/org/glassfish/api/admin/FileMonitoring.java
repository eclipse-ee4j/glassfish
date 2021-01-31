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

package org.glassfish.api.admin;

import java.io.File;

import org.jvnet.hk2.annotations.Contract;

/**
 * Service to monitor changes to files.
 *
 * @author Jerome Dochez
 */
@Contract
public interface FileMonitoring {

    /**
     * Registers a FileChangeListener for a particular file
     *
     * @param file the file of interest
     * @param listener the listener to notify
     */
    void monitors(File file, FileChangeListener listener);

    /**
     * Informs the monitor that a file has been changed. This is a hint to the monitor to prevent missing changes that occur
     * within the granularity of the operating system's file modification time, typically 1 second.
     */
    void fileModified(File file);

    public interface FileChangeListener {
        void changed(File changedFile);

        void deleted(File deletedFile);
    }
}
