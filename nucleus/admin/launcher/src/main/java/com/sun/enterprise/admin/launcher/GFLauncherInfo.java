/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.util.HostAndPort;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.api.admin.RuntimeType;

import static com.sun.enterprise.admin.launcher.ArgumentManager.argsToMap;
import static com.sun.enterprise.universal.glassfish.GFLauncherUtils.ok;
import static com.sun.enterprise.universal.glassfish.GFLauncherUtils.safeExists;
import static com.sun.enterprise.universal.glassfish.GFLauncherUtils.safeIsDirectory;
import static com.sun.enterprise.universal.io.SmartFile.sanitize;

/**
 * @author Byron Nevins
 */
public class GFLauncherInfo {

    // BUG TODO get the def. domains dir from asenv 3/14/2008. Will this ever be done? 24/05/2020
    private final static String DEFAULT_DOMAIN_PARENT_DIR = "domains";
    private final static String CONFIG_DIR = "config";
    private final static String CONFIG_FILENAME = "domain.xml";

    // Set by contructor
    private RuntimeType type;

    /**
     * Incoming arguments from caller
     */
    private List<String> argsRaw = new ArrayList<>();

    /**
     * Intermediate map form of arguments from caller
     */
    private Map<String, String> argsMap;

    // Start set by arguments, final form of arguments from caller

    private boolean debug; // --debug argument, e.g. ./asadmin start-domain --debug
    private boolean suspend; // --suspend
    private boolean verbose; // --verbose argument e.g. ./asadmin start-domain --verbose
    private boolean watchdog;
    private boolean upgrade;

    private File domainParentDir;
    private File instanceRootDir;
    private File domainRootDir;

    private String domainName;
    private String instanceName;

    // End set by arguments

    private File configDir; // default [domainRootDir]/config
    private File configFile; // default [configDir]/domain.xml

    File installDir; // default is [installDir]/modules/common-utils.jar/../..

    private boolean valid;

    private boolean dropInterruptedCommands; // "org.glassfish.job-manager.drop-interrupted-commands" system property
    private List<HostAndPort> adminAddresses; // admin host and port, e.g. localhost:4848
    private RespawnInfo respawnInfo;

    // password tokens -- could be multiple -- launcher should *just* write them onto stdin of server
    final List<String> securityTokens = new ArrayList<>(); // note: it's package private

    GFLauncherInfo(RuntimeType type) {
        this.type = type;
    }

    /**
     * Add the string arguments in the order given.
     *
     * @param args The string arguments
     */
    public void addArgs(String... args) {
        for (String argument : args) {
            argsRaw.add(argument);
        }
    }

    /**
     * Set the (optional) domain name. This can also be sent in as a String arg like so: "-domainname" "theName"
     *
     * @param domainName
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Set the (optional) domain parent directory. This can also be sent in as a String arg like so: "-domaindir"
     * "parentDirPath"
     *
     * @param domainParentName The parent directory of the domain
     */
    public void setDomainParentDir(String domainParentName) {
        this.domainParentDir = new File(domainParentName);
    }

    /**
     * Starts the server in verbose mode
     *
     * @param b
     */
    public void setVerbose(boolean b) {
        verbose = b;
    }

    /**
     * Starts the server in watchdog mode. This is only useful if verbose is false. It does the same thing as verbose --
     * except without the dumping of output to standard out and err streams.
     *
     * @param b
     * @since 3.2
     */
    public void setWatchdog(boolean b) {
        watchdog = b;
    }

    /**
     * Starts the server in debug mode
     *
     * @param b
     */
    public void setDebug(boolean b) {
        debug = b;
    }

    /**
     *
     * @return true if debug mode is on.
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Starts the server in suspended debug mode
     *
     * @param suspend
     */
    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    /**
     *
     * @return true if suspend debug mode is on.
     */
    public boolean isSuspend() {
        return suspend;
    }

    /**
     * Starts the server in upgrade mode
     *
     * @param b
     */
    public void setUpgrade(boolean b) {
        upgrade = b;
    }

    public void setDomainRootDir(File f) {
        domainRootDir = f;
    }

    public void setInstanceName(String name) {
        instanceName = name;
    }

    public void setInstanceRootDir(File f) {
        instanceRootDir = f;
    }

    public void setDropInterruptedCommands(boolean dropInterruptedCommands) {
        this.dropInterruptedCommands = dropInterruptedCommands;
    }

    public final boolean isDomain() {
        return type == RuntimeType.DAS;
    }

    public final boolean isInstance() {
        return type == RuntimeType.INSTANCE;
    }

    /**
     *
     * @return true if verbose mode is on.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     *
     * @return true if watchdog mode is on.
     */
    public boolean isWatchdog() {
        return watchdog;
    }

    /**
     *
     * @return true if upgrade mode is on.
     */
    public boolean isUpgrade() {
        return upgrade;
    }

    /**
     *
     * @return The domain name
     */
    public String getDomainName() {
        return domainName;
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getDomainRootDir() {
        return domainRootDir;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public List<HostAndPort> getAdminAddresses() {
        return adminAddresses;
    }

    public RuntimeType getType() {
        return type;
    }

    public File getConfigDir() {
        return sanitize(configDir);
    }

    void setConfigDir(File f) {
        configDir = sanitize(f);
    }

    public File getInstanceRootDir() throws GFLauncherException {
        if (!valid) {
            throw new GFLauncherException("internalError", "Call to getInstanceRootDir() on an invalid GFLauncherInfo object.");
        }

        if (instanceRootDir != null) {
            return instanceRootDir;
        }

        if (isDomain()) {
            return domainRootDir;
        }

        throw new GFLauncherException("internalError", "Call to getInstanceRootDir() on an invalid GFLauncherInfo object.");
    }

    File getDomainParentDir() {
        return domainParentDir;
    }

    public boolean isDropInterruptedCommands() {
        return dropInterruptedCommands;
    }

    /**
     * TEMPORARY. The guts of HK2 and V3 bootstrapping wants String[] -- this will be changed soon, but it is messy to
     * change it right now. so temporarily we will humor HK2 by sending in String[]
     *
     * @return an array of String arguments
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException
     */
    public String[] getArgsAsStringArray() throws GFLauncherException {
        List<String> list = getArgsAsList();
        String[] ss = new String[list.size()];
        return list.toArray(ss);
    }

    public List<String> getArgsAsList() throws GFLauncherException {
        List<String> argList = new ArrayList<>();
        for (Map.Entry<String, String> entry : getArgs().entrySet()) {
            argList.add(entry.getKey());
            argList.add(entry.getValue());
        }

        return argList;
    }

    /**
     *
     * @return a Map<String,String> of processed and packaged args
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException
     */
    public Map<String, String> getArgs() throws GFLauncherException {
        // args processed and packaged for AppServer

        if (!valid) {
            throw new GFLauncherException("internalError", "Call to getArgs() on an invalid GFLauncherInfo object.");
        }

        Map<String, String> map = new HashMap<>();

        map.put("-type", type.toString());

        if (isDomain()) {
            map.put("-domaindir", sanitize(domainRootDir.getPath()));
            map.put("-domainname", domainName);
        } else if (isInstance()) {
            map.put("-instancedir", sanitize(instanceRootDir.getPath()));
        }

        // no need for watchdog here. It is a client-side phenomenon only!
        map.put("-verbose", Boolean.toString(verbose));
        map.put("-debug", Boolean.toString(debug));
        map.put("-instancename", instanceName);
        map.put("-upgrade", Boolean.toString(upgrade));
        map.put("-read-stdin", "true"); // always make the server read the stdin for master password, at least.

        if (respawnInfo != null) {
            respawnInfo.put(map);
        }

        return map;
    }

    public void setRespawnInfo(String classname, String classpath, String[] args) {
        respawnInfo = new RespawnInfo(classname, classpath, args);
    }

    /**
     * Adds the given name value pair as a security token. This is what will be put on the launched process's stdin to
     * securely pass it on. The value is accepted as a String and it may be insecure. A string formed by concatenating name,
     * '=' and value is written to the stdin as a single line delimited by newline character. To get the value of the token,
     * the server should parse the line knowing this. None of the parameters may be null.
     *
     * @param name String representing name of the token
     * @param value String representing the value (should we call it a password?)
     * @throws NullPointerException if any of the parameters are null
     */
    public void addSecurityToken(String name, String value) {
        if (name == null || value == null) {
            throw new NullPointerException();
        }

        securityTokens.add(name + "=" + value);
    }

    void setAdminAddresses(List<HostAndPort> adminAddresses) {
        this.adminAddresses = adminAddresses;
    }

    /**
     * IMPORTANT: These 2 methods are designed for use only by Unit Tests so we are not dependent on an installation.
     * Normally we figure out installDir from where we are running from.
     */
    void setInstallDir(File f) {
        installDir = f;
    }

    File getInstallDir() {
        return installDir;
    }

    void setup() throws GFLauncherException {
        setupFromArgs();
        finalSetup();
    }

    private void setupFromArgs() {
        argsMap = argsToMap(argsRaw);

        File fileArgument = null;
        String stringArgument = null;

        // pick out file props
        // annoying -- cli uses "domaindir" to represent the parent of the
        // domain root dir. I'm sticking with the same syntax for now...
        if ((fileArgument = getFile("domaindir")) != null) {
            domainParentDir = fileArgument;
        }

        if ((fileArgument = getFile("instanceRootDir")) != null) {
            instanceRootDir = fileArgument;
        }

        if ((fileArgument = getFile("domainroot")) != null) {
            domainRootDir = fileArgument;
        }

        // Now do the same thing with known Strings
        if ((stringArgument = getString("domain")) != null) {
            domainName = stringArgument;
        }

        // the Arg processor may have set the name "default" to the domain name
        // just like in asadmin
        if (!ok(domainName) && (stringArgument = getString("default")) != null) {
            domainName = stringArgument;
        }

        if ((stringArgument = getString("instancename")) != null) {
            instanceName = stringArgument;
        }

        // Finally, do the booleans
        //
        // Getting ugly. Findbugs does not like using regular Boolean object
        // a three-state boolean
        // We do NOT want to disturb the existing values of these variables if the
        // user has not explicitly overridden them.
        debug = getBoolean("debug", debug);
        verbose = getBoolean("verbose", verbose);
        watchdog = getBoolean("watchdog", watchdog);
        upgrade = getBoolean("upgrade", upgrade);
    }

    private void finalSetup() throws GFLauncherException {
        if (installDir == null) {
            installDir = GFLauncherUtils.getInstallDir();
        }

        if (!safeIsDirectory(installDir)) {
            throw new GFLauncherException("noInstallDir", installDir);
        }

        // check user-supplied args
        if (domainParentDir != null) {
            // if the arg was given -- then it MUST point to a real dir
            if (!safeIsDirectory(domainParentDir)) {
                throw new GFLauncherException("noDomainParentDir", domainParentDir);
            }
        }

        setupServerDirs();

        if (!safeIsDirectory(configDir)) {
            throw new GFLauncherException("noConfigDir", configDir);
        }

        configFile = new File(configDir, CONFIG_FILENAME);

        if (!safeExists(configFile)) {
            throw new GFLauncherException("noConfigFile", configFile);
        }

        if (instanceName == null) {
            instanceName = "server";
        }

        // if we made it here -- we're in pretty good shape!
        valid = true;
    }

    private void setupServerDirs() throws GFLauncherException {
        if (isDomain()) {
            setupDomainDirs();
        } else if (isInstance()) {
            setupInstanceDirs();
        }
    }

    private void setupDomainDirs() throws GFLauncherException {
        // if they set domainrootdir -- it takes precedence
        if (domainRootDir != null) {
            domainParentDir = domainRootDir.getParentFile();
            domainName = domainRootDir.getName();
            return;
        }

        // if they set domainParentDir -- use it. o/w use the default dir
        if (domainParentDir == null) {
            domainParentDir = new File(installDir, DEFAULT_DOMAIN_PARENT_DIR);
        }

        // if they specified domain name -- use it. o/w use the one and only dir
        // in the domain parent dir

        if (domainName == null) {
            domainName = getTheOneAndOnlyDomain();
        }

        domainRootDir = new File(domainParentDir, domainName);

        if (!safeIsDirectory(domainRootDir)) {
            throw new GFLauncherException("noDomainRootDir", domainRootDir);
        }

        configDir = new File(domainRootDir, CONFIG_DIR);
    }

    private void setupInstanceDirs() throws GFLauncherException {
        if (instanceRootDir == null) {
            throw new GFLauncherException("Missing instanceRootDir");
        }
        if (instanceName == null) {
            throw new GFLauncherException("Missing instanceName");
        }
        configDir = new File(instanceRootDir, CONFIG_DIR);
    }

    private String getTheOneAndOnlyDomain() throws GFLauncherException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = domainParentDir.listFiles(f -> safeIsDirectory(f));

        if (files == null || files.length == 0) {
            throw new GFLauncherException("noDomainDirs", domainParentDir);
        }

        if (files.length > 1) {
            throw new GFLauncherException("tooManyDomainDirs", domainParentDir);
        }

        return files[0].getName();
    }

    private File getFile(String key) {
        String value = getString(key);

        if (value == null) {
            return null;
        }

        return new File(value);
    }

    private boolean getBoolean(String key, boolean def) {
        ThreeStateBoolean booleanArgument = getBoolean(key);

        if (booleanArgument.isTrue()) {
            return true;
        }

        if (booleanArgument.isFalse()) {
            return false;
        }

        return def;
    }

    private ThreeStateBoolean getBoolean(String key) {
        // 3 return values -- true, false, null
        String value = getValueIgnoreCommandDelimiter(key);

        if (value != null) {
            return new ThreeStateBoolean(Boolean.valueOf(value));
        }

        return new ThreeStateBoolean(null);
    }

    private String getString(String key) {
        return getValueIgnoreCommandDelimiter(key);
    }

    private String getValueIgnoreCommandDelimiter(String key) {
        // it can be confusing trying to remember -- is it "--option"?
        // or "-option" or "option". So look for any such match.

        if (argsMap.containsKey(key)) {
            return argsMap.get(key);
        }

        key = "-" + key;
        if (argsMap.containsKey(key)) {
            return argsMap.get(key);
        }

        key = "-" + key;
        if (argsMap.containsKey(key)) {
            return argsMap.get(key);
        }

        return null;
    }

    boolean isVerboseOrWatchdog() {
        return verbose || watchdog;
    }

    final private static class ThreeStateBoolean {

        final Boolean b;

        ThreeStateBoolean(Boolean b) {
            this.b = b;
        }

        boolean isNull() {
            return b == null;
        }

        boolean isTrue() {
            return !isNull() && b.booleanValue();
        }

        boolean isFalse() {
            return !isNull() && !b.booleanValue();
        }

    }

}
