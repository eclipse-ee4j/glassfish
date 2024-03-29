/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.server.logging;

import java.io.File;

import org.jvnet.hk2.annotations.Contract;

/**
 * Interface to provide the runtime data in effect for the server logging.
 *
 * @author sandeep.shrivastava
 * @author David Matejcek
 */
@Contract
public interface ServerLogFileManager {

    /**
     * @return the current log file for the server.
     */
    File getCurrentLogFile();

    /**
     * Renames currently used file and creates a new one.
     */
    void roll();
}
