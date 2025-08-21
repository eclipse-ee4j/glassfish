/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.universal.PropertiesDecoder;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.sun.enterprise.admin.servermgmt.services.Constants.AS_ADMIN_PATH_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.CFG_LOCATION_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.CREDENTIALS_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.DATE_CREATED_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.DRYRUN_PREPEND;
import static com.sun.enterprise.admin.servermgmt.services.Constants.ENTITY_NAME_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.LOCATION_ARGS_RESTART_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.LOCATION_ARGS_START_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.LOCATION_ARGS_STOP_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.OS_USER_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.RESTART_COMMAND_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.SERVICE_NAME_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.SERVICE_TYPE_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.START_COMMAND_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.STOP_COMMAND_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.TRACE_PREPEND;

/**
 * @author bnevins
 */
public abstract class ServiceAdapter implements Service {

    private final Map<String, String> tokenMap = new HashMap<>();
    final PlatformServicesInfo info;
    private String flattenedServicePropertes;
    private File templateFile;

    ServiceAdapter(ServerDirs serverDirs, AppserverServiceType type) {
        info = new PlatformServicesInfo(serverDirs, type);
    }

    @Override
    public final String getServiceProperties() {
        return flattenedServicePropertes;
    }

    /*
     * @author Byron Nevins
     * 11/14/11
     * The --serviceproperties option was being completely ignored!
     * The existing structure is brittle, hard to understand, and has wired-in
     * the implementation details to the interface.  I.e. there are tons of problems
     * maintaining the code.
     * What I'm doing here is taking the map with all of the built-in values and
     * overlaying it with name-value pairs that the user specified.
     * I discovered the original problem by trying to change the display name, "ENTITY_NAME"
     * at the command line as a serviceproperty.  It was completely ignored!!
     */
    final Map<String, String> getFinalTokenMap() {
        Map<String, String> map = getTokenMap();
        map.putAll(tokensAndValues());
        return map;
    }

    @Override
    public final void setServiceProperties(String cds) {
        flattenedServicePropertes = cds;
    }

    @Override
    public final Map<String, String> tokensAndValues() {
        return PropertiesDecoder.unflatten(flattenedServicePropertes);
    }

    @Override
    public final void deleteService() {
        info.validate();
        initialize();
        initializeInternal();
        deleteServiceInternal();
    }

    @Override
    public PlatformServicesInfo getInfo() {
        return info;
    }

    @Override
    public final boolean isDomain() {
        return info.type == AppserverServiceType.Domain;
    }

    @Override
    public final boolean isInstance() {
        return info.type == AppserverServiceType.Instance;
    }

    @Override
    public final ServerDirs getServerDirs() {
        return info.serverDirs;
    }

    @Override
    public final void createService() {
        info.validate();
        initialize();
        initializeInternal();
        createServiceInternal();
    }

    @Override
    public String getLocationArgsRestart() {
        return getLocationArgsStart();
    }

    //////////////////////////////////////////////////////////////////////////
    ////////////////   pkg-private     ///////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    void initialize() {
        final String parentPath = info.serverDirs.getServerParentDir().getPath();
        final String serverName = info.serverDirs.getServerName();
        setAsadminCredentials();

        getTokenMap().put(CFG_LOCATION_TN, parentPath);
        getTokenMap().put(ENTITY_NAME_TN, serverName);
        getTokenMap().put(LOCATION_ARGS_START_TN, getLocationArgsStart());
        getTokenMap().put(LOCATION_ARGS_RESTART_TN, getLocationArgsRestart());
        getTokenMap().put(LOCATION_ARGS_STOP_TN, getLocationArgsStop());
        getTokenMap().put(START_COMMAND_TN, info.type.startCommand());
        getTokenMap().put(RESTART_COMMAND_TN, info.type.restartCommand());
        getTokenMap().put(STOP_COMMAND_TN, info.type.stopCommand());
        getTokenMap().put(OS_USER_TN, info.osUser);

        if (OS.isWindowsForSure()) {
            // Windows doesn't respond well to slashes in the name!!
            getTokenMap().put(SERVICE_NAME_TN, info.serviceName);
        }

        getTokenMap().put(AS_ADMIN_PATH_TN, info.asadminScript.getPath().replace('\\', '/'));
        getTokenMap().put(DATE_CREATED_TN, info.date.toString());
        getTokenMap().put(SERVICE_TYPE_TN, info.type.toString());
        getTokenMap().put(CREDENTIALS_TN, getCredentials());

    }

    final String getCredentials() {
        // 1 -- no auth of any kind needed -- by definition when there is no
        // password file
        // note: you do NOT want to give a "--user" arg -- it can only appear
        // if there is a password file too
        if (info.passwordFile == null) {
            return " ";
        }

        // 2. --
        String user = info.appserverUser; // might be null

        StringBuilder sb = new StringBuilder();

        if (StringUtils.ok(user)) {
            sb.append(" --user ").append(user);
        }

        sb.append(" --passwordfile ").append(info.passwordFile.getPath()).append(" ");

        return sb.toString();
    }

    void trace(String s) {
        if (info.trace) {
            System.out.println(TRACE_PREPEND + s);
        }
    }

    void dryRun(String s) {
        if (info.dryRun) {
            System.out.println(DRYRUN_PREPEND + s);
        }
    }

    final Map<String, String> getTokenMap() {
        return tokenMap;
    }

    /**
     * If the user has specified a password file than get the info and convert into a String[] that CLI can use. e.g. {
     * "--user", "harry", "--passwordfile", "/xyz" } authentication artifacts. Parameter may not be null.
     */
    private void setAsadminCredentials() {

        // it is allowed to have no passwordfile specified in V3
        if (info.passwordFile == null) {
            return;
        }

        // But if they DID specify it -- it must be kosher...

        if (!info.passwordFile.isFile()) {
            throw new IllegalArgumentException(Strings.get("windows.services.passwordFileNotA", info.passwordFile));
        }

        if (!info.passwordFile.canRead()) {
            throw new IllegalArgumentException(Strings.get("windows.services.passwordFileNotReadable", info.passwordFile));
        }

        Properties p = getProperties(info.passwordFile);

        // IT 10255
        // the password file may just have master password or just user or just user password
        //

        String userFromPasswordFile = p.getProperty("AS_ADMIN_USER");

        // Byron Nevins sez:
        // unfiled bug -- this was the ONLY check for username.  I changed it
        // in November 2012 -- now the user has been already set to whatever CLICommand's
        // ProgramOptions.getUser() returned.
        // the username in the passwordfile takes precedence if it is in there.
        // In summary - before this change if --user was specified then that username was
        // completely ignored.  Now it is used.
        //
        if (StringUtils.ok(userFromPasswordFile)) {
            info.setAppServerUser(p.getProperty("AS_ADMIN_USER"));
        }
    }

    private Properties getProperties(File f) {
        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(f));
            final Properties p = new Properties();
            p.load(bis);
            return p;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception ee) {
                    // ignore
                }
            }
        }
    }

    File getTemplateFile() {
        return templateFile;
    }

    void setTemplateFile(String name) {
        templateFile = new File(info.libDir, "install/templates/" + name);
    }
}
