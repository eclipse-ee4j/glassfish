/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.ObjectAnalyzer;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.File;
import java.util.Date;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * A place to keep platform services info...
 *
 * @author Byron Nevins
 */
public class PlatformServicesInfo {

    // set at construction-time
    final ServerDirs serverDirs;
    final AppserverServiceType type;
    // accessed by classes in this package
    String serviceName;
    boolean dryRun;
    String osUser;
    boolean trace;
    File libDir;
    File asadminScript;
    boolean force;
    String serviceUser;
    Date date;
    File passwordFile;
    String appserverUser;
    // private to this implementation
    private File installRootDir;
    int sPriority;
    int kPriority;

    public PlatformServicesInfo(ServerDirs sDirs, AppserverServiceType theType) {
        serverDirs = sDirs;

        if (serverDirs == null || serverDirs.getServerDir() == null) {
            throw new RuntimeException(Strings.get("bad.server.dirs"));
        }

        type = theType;
        kPriority = 20;
        sPriority = 20;
    }

    public void validate() {
        if (!StringUtils.ok(serviceName)) {
            serviceName = serverDirs.getServerName();
        }

        date = new Date();
        setInstallRootDir();
        setLibDir();
        setAsadmin();
        osUser = System.getProperty("user.name");
    }

    /**
     * @param serviceName the serviceName to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @param dryRun the dryRun to set
     */
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    /**
     * @param passwordFile the passwordFile to set
     */
    public void setPasswordFile(File passwordFile) {
        this.passwordFile = passwordFile;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public void setServiceUser(String serviceUser) {
        this.serviceUser = serviceUser;
    }

    public void setAppServerUser(String user) {
        if (StringUtils.ok(user)) {
            appserverUser = user;
        }
    }

    @Override
    public String toString() {
        return ObjectAnalyzer.toString(this);
    }

    //////////////////////////////////////////////////////////////////////
    //////////////          private         //////////////////////////////
    //////////////////////////////////////////////////////////////////////
    private void setLibDir() {
        libDir = SmartFile.sanitize(new File(installRootDir, "lib"));

        if (!libDir.isDirectory()) {
            throw new RuntimeException(Strings.get("internal.error", "Not a directory: " + libDir));
        }
    }

    private void setInstallRootDir() {
        String ir = System.getProperty(INSTALL_ROOT.getSystemPropertyName());

        if (!StringUtils.ok(ir)) {
            throw new RuntimeException(Strings.get("internal.error",
                "System Property not set: " + INSTALL_ROOT.getSystemPropertyName()));
        }

        installRootDir = SmartFile.sanitize(new File(ir));

        if (!installRootDir.isDirectory()) {
            throw new RuntimeException(Strings.get("internal.error", "Not a directory: " + installRootDir));
        }
    }

    private void setAsadmin() {
        String s = SystemPropertyConstants.getAsAdminScriptLocation();

        if (!StringUtils.ok(s)) {
            throw new RuntimeException(Strings.get("internal.error", "Can't get Asadmin script location"));
        }

        asadminScript = SmartFile.sanitize(new File(s));

        if (!asadminScript.isFile()) {
            throw new RuntimeException(Strings.get("noAsadminScript", asadminScript));
        }
    }
}
