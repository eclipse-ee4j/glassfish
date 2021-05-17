/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.adapter;



import com.sun.enterprise.config.serverbeans.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;


@Service(name = "AdminConsoleStartupService")
@RunLevel(PostStartupRunLevel.VAL)
public class AdminConsoleStartupService implements  PostConstruct {

    @Inject
    private AdminService adminService;

    @Inject @Optional
    private AdminConsoleAdapter adminConsoleAdapter = null;

    @Inject
    private ServerEnvironmentImpl env;

    @Inject
    private Domain domain;

    private static final Logger logger = KernelLoggerInfo.getLogger();
    private final long ONE_DAY = 24 * 60 * 60 * 1000;

    @Override
    public void postConstruct() {

        if (adminConsoleAdapter == null) { // there may be no console in this environment.
            return;
        }

        /* This service must run only on the server where the console should run. Currently, that server is DAS. If and when
         *  the console becomes dis-associated with DAS, this logic will need to be modified.
         */
        if (!env.isDas())
            return;

        // FIXME : Use ServerTags, when this is finalized.
        Property initProp = adminService.getProperty("adminConsoleStartup");
        String initPropVal = "DEFAULT";
        if (initProp != null) {
            initPropVal = initProp.getValue();
            if ( !(initPropVal.equals("ALWAYS") || initPropVal.equals("NEVER") || initPropVal.equals("DEFAULT"))){
                initPropVal="DEFAULT";
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "AdminConsoleStartupService, console loading option is {0}", initPropVal);
        }

        if (initPropVal.equalsIgnoreCase("DEFAULT")) {
            handleDefault();
        } else if (initPropVal.equalsIgnoreCase("ALWAYS")) {
            handleHigh();
        }
    }

    private void handleDefault() {
        /* if there are servers other than DAS */
        if ((domain.getServers().getServer().size() > 1)) {
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "AdminConsoleStartup DAS usecase");
            }
            handleHigh();
            return;
        }
        // if last access was within a day
        long currentTime = System.currentTimeMillis();
        try {
            long lastTime = getTimeStamp();
            if (currentTime  - lastTime < ONE_DAY) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "AdminConsoleStartup frequent user, lastTime =  ", lastTime);
                }
                handleHigh();
            }
        } catch (IOException ex) {
                logger.fine(ex.getMessage());
        }
    }

    private void handleLow() {
        adminConsoleAdapter.initRest();
    }


    private void handleHigh() {
        handleLow();
        synchronized(this) {
            if (!adminConsoleAdapter.isInstalling() && !adminConsoleAdapter.isApplicationLoaded()) {
                adminConsoleAdapter.loadConsole();
            }
        }
    }

    private long getTimeStamp() throws IOException {
        File f = new File(env.getConfigDirPath(), ".consolestate");
        if (!f.exists())
            return 0L;
        return f.lastModified();
    }


}
