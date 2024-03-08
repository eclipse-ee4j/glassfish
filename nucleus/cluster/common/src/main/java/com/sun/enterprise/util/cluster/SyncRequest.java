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

package com.sun.enterprise.util.cluster;

import java.util.*;

import jakarta.xml.bind.annotation.*;

/**
 * Request message to synchronize files.
 */
@XmlRootElement(name = "synchronize-files")
public final class SyncRequest {
    public SyncRequest() {
        files = new ArrayList<ModTime>();
    }

    /**
     * The server instance name.
     */
    @XmlElement(name = "instance")
    public String instance;

    /**
     * The directory to synchronize.
     */
    @XmlElement(name = "directory")
    public String dir;

    /**
     * The list of files the client has.
     */
    @XmlElement(name = "file", type = ModTime.class)
    public List<ModTime> files;

    /**
     * The file name and mod time.
     */
    public static class ModTime {
        public ModTime() {
        }

        public ModTime(String name, long time) {
            this.name = name;
            this.time = time;
        }

        @XmlElement(name = "name")
        public String name;

        @XmlElement(name = "time")
        public long time;
    }
}
