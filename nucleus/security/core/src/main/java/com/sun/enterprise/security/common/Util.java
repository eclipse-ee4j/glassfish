/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.embedded.Server;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 *
 * @author venu TODO: need to change this class, it needs to be similar to SecurityServicesUtil
 */
@Service
@Singleton
public class Util {
    private static ServiceLocator habitat = Globals.getDefaultHabitat();

    @Inject
    private ProcessEnvironment penv;

    // stuff required for AppClient
    private CallbackHandler callbackHandler;
    private Object appClientMsgSecConfigs;

    // Note: Will return Non-Null only after Util has been
    // Injected in some Service.
    public static ServiceLocator getDefaultHabitat() {
        return habitat;
    }

    public static Util getInstance() {
        // return my singleton service
        return habitat.getService(Util.class);
    }

    public boolean isACC() {
        return penv.getProcessType().equals(ProcessType.ACC);
    }

    public boolean isServer() {
        return penv.getProcessType().isServer();
    }

    public boolean isNotServerOrACC() {
        return penv.getProcessType().equals(ProcessType.Other);
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAppClientMsgSecConfigs() {
        return (T) appClientMsgSecConfigs;
    }

    public void setAppClientMsgSecConfigs(Object appClientMsgSecConfigs) {
        this.appClientMsgSecConfigs = appClientMsgSecConfigs;
    }

    public static boolean isEmbeddedServer() {
        List<String> servers = Server.getServerNames();
        if (!servers.isEmpty()) {
            return true;
        }

        return false;
    }

    public static File writeConfigFileToTempDir(String fileName) throws IOException {
        File filePath = new File(fileName);

        if (filePath.exists()) {
            // the string provided is a filepath, so return
            return filePath;
        }

        File localFile = null;
        // Parent directories until the fileName exist, so create the file that has been provided
        if (filePath.getParentFile() != null && filePath.getParentFile().exists()) {
            localFile = filePath;
            if (!localFile.createNewFile()) {
                throw new IOException();
            }

        } else {
            /*
             * File parent directory does not exist - so create parent directory as user.home/.glassfish-{embedded}/config
             */
            String userHome = System.getProperty("user.home");

            String embeddedServerName = getCurrentEmbeddedServerName();
            File tempDir = new File(userHome + File.separator + ".glassfish7-" + embeddedServerName + File.separator + "config");
            boolean mkDirSuccess = true;
            if (!tempDir.exists()) {
                mkDirSuccess = tempDir.mkdirs();
            }

            localFile = new File(tempDir.getAbsolutePath() + File.separator + fileName);

            if (mkDirSuccess && !localFile.exists()) {
                localFile.createNewFile();
            }
        }
        FileOutputStream oStream = null;
        InputStream iStream = null;
        try {
            oStream = new FileOutputStream(localFile);
            iStream = Util.class.getResourceAsStream("/config/" + fileName);

            while (iStream != null && iStream.available() > 0) {
                oStream.write(iStream.read());
            }
        } finally {
            if (oStream != null) {
                oStream.close();
            }
            if (iStream != null) {
                iStream.close();
            }

        }

        return localFile;

    }

    public static String getCurrentEmbeddedServerName() {
        List<String> embeddedServerNames = Server.getServerNames();
        return embeddedServerNames.get(0) == null ? "embedded" : embeddedServerNames.get(0);
    }

}
