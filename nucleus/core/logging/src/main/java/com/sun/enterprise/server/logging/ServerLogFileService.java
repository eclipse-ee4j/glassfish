/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Singleton;

import java.io.File;
import java.lang.System.Logger.Level;

import org.glassfish.main.jul.JULHelperFactory;
import org.glassfish.main.jul.JULHelperFactory.JULHelper;
import org.glassfish.main.jul.handler.GlassFishLogHandler;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * This service is used to rotate the server.log file (if it is configured).
 *
 * @author Jerome Dochez
 * @author Carla Mott
 * @author David Matejcek
 */
@Service
@Singleton
@ContractsProvided(ServerLogFileManager.class)
public class ServerLogFileService implements ServerLogFileManager {

    private final JULHelper julHelper = JULHelperFactory.getHelper();


    @Override
    public File getCurrentLogFile() {
        final GlassFishLogHandler logHandler = julHelper.findGlassFishLogHandler();
        if (logHandler == null) {
            julHelper.getSystemLogger(getClass()).log(Level.WARNING, "The GlassFishLogHandler was not found, returning null");
            return null;
        }
        return logHandler.getConfiguration().getLogFile();
    }


    /**
     * Renames the server.log file and starts logging to a new file.
     */
    @Override
    public void roll() {
        final GlassFishLogHandler logHandler = julHelper.findGlassFishLogHandler();
        if (logHandler == null) {
            throw new IllegalStateException("The GlassFishLogHandler was not found, rolling the output file failed.");
        }
        logHandler.roll();
    }
}
