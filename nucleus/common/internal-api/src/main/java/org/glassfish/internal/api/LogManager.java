/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.logging.Handler;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface LogManager {
    Map<String, String> getLoggingProperties() throws IOException;

    File getLoggingPropertiesFile() throws IOException;

    /**
     * Adds a new handler to the root logger
     *
     * @param handler handler to be iadded.
     */
    void addHandler(Handler handler);

    PrintStream getErrStream();

    PrintStream getOutStream();
}
