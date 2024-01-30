/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.io;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.ObjectAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * The hierarchy of directories above a running DAS or server instance can get
 * messy to deal with -- thus this class.  This class is a bullet-proof holder of
 * that information.
 *
 * Instances and DAS are arranged differently:
 *
 * examples:
 * DAS
 * domainDir = getServerDir() == C:/glassfish7/glassfish/domains/domain1
 * domainsDir = getServerParentDir() == C:/glassfish7/glassfish/domains
 * grandparent-dir is meaningless
 *
 * Instance
 * instanceDir = getServerDir() == C:/glassfish7/glassfish/nodes/mymachine/instance1
 * agentDir = getServerParentDir() == C:/glassfish7/glassfish/nodes/mymachine
 * agentsDir = getServerGrandParentDir() == C:/glassfish7/glassfish/nodes
 *
 * Currently in all cases the name of the serverDir is the name of the server --
 * by our definition.
 *
 * THIS CLASS IS GUARANTEED THREAD SAFE
 * THIS CLASS IS GUARANTEED IMMUTABLE
 *
 * Contract:  Caller is supposed to NOT call methods on an instance of this class.
 * It's "advanced java" to be able to do that anyway.
 * I don't allow half-baked data out.  It's all or none.  The "valid" flag
 * is checked and if invalid -- all methods return null.  They don't throw an Exception
 * because the caller is not supposed to call the methods - it would just annoy
 * the caller.
 *
 * @author Byron Nevins
 * @since 3.1
 * Created: April 19, 2010
 */
public class ServerDirs {

    private final String serverName;
    private final File serverDir;
    private final File parentDir;
    private final File agentDir;
    private final File grandParentDir;
    private final File configDir;
    private final File domainXml;
    private final File pidFile;
    private final File lastPidFile;
    private final boolean valid;
    private final String localPassword;
    private final File localPasswordFile;
    private final File dasPropertiesFile; // this only makes sense for instances...
    // Can be shared among classes in the package
    static final LocalStringsImpl strings = new LocalStringsImpl(ServerDirs.class);

    /**
     *
     */
    public ServerDirs() {
        serverName = null;
        serverDir = null;
        agentDir = null;
        parentDir = null;
        grandParentDir = null;
        configDir = null;
        domainXml = null;
        pidFile = null;
        lastPidFile = null;
        valid = false;
        localPassword = null;
        localPasswordFile = null;
        dasPropertiesFile = null;
    }

    public ServerDirs(final File serverDir) throws IOException {
        if (serverDir == null) {
            throw new IllegalArgumentException(strings.get("ServerDirs.nullArg", "ServerDirs.ServerDirs()"));
        }

        if (!serverDir.isDirectory()) {
            throw new IOException(strings.get("ServerDirs.badDir", serverDir));
        }

        this.serverDir = SmartFile.sanitize(serverDir);
        serverName = serverDir.getName();

        // note that serverDir has been "smart-filed" so we don't have to worry
        // about getParentFile() which has issues with relative paths...
        parentDir = serverDir.getParentFile();

        if (parentDir == null || !parentDir.isDirectory()) {
            throw new IOException(strings.get("ServerDirs.badParentDir", serverDir));
        }

        // grandparent dir is optional.  It can be null for DAS for instance...
        grandParentDir = parentDir.getParentFile();
        configDir = new File(serverDir, "config");
        domainXml = new File(configDir, "domain.xml");
        pidFile = new File(configDir, "pid");
        lastPidFile = new File(configDir, "pid.prev");
        localPasswordFile = new File(configDir, "local-password");

        if (localPasswordFile.exists()) {
            try (BufferedReader r = new BufferedReader(new FileReader(localPasswordFile))) {
                localPassword = r.readLine();
            }
        } else {
            localPassword = null;
        }
        agentDir = new File(parentDir, "agent");
        dasPropertiesFile = new File(parentDir, "agent/config/das.properties");
        valid = true;
    }

    /**
     * @return domain name or instance name
     */
    public final String getServerName() {
        return valid ? serverName : null;
    }

    public ServerDirs refresh() throws IOException {
        return new ServerDirs(serverDir);
    }

    // getters & setters section below
    public final File getServerDir() {
        return valid ? serverDir : null;
    }

    public final File getAgentDir(){
         return valid ? agentDir : null;
    }

    public final File getServerParentDir() {
        return valid ? parentDir : null;
    }

    public final File getServerGrandParentDir() {
        return valid ? grandParentDir : null;
    }

    public final File getDomainXml() {
        return valid ? domainXml : null;
    }

    public final File getConfigDir() {
        return valid ? configDir : null;
    }

    /**
     * If the server is running, must exist.
     * If the server is stopped, shoud be deleted, but it cannot be guaranteed.
     *
     * @return file containing the process ID.
     */
    public final File getPidFile() {
        return valid ? pidFile : null;
    }

    /**
     * If the server is running, must exist.
     * If the server is stopped, shoud be deleted, but it cannot be guaranteed.
     *
     * @return file containing the process ID.
     */
    public final File getLastPidFile() {
        return lastPidFile;
    }

    public final File getDasPropertiesFile() {
        return dasPropertiesFile;
    }

    public String getLocalPassword() {
        return localPassword;
    }

    public final File getLocalPasswordFile() {
        return valid ? localPasswordFile : null;
    }

    public final boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return ObjectAnalyzer.toString(this);
    }
}
