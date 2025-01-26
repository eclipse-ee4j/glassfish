/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static com.sun.enterprise.admin.servermgmt.services.Constants.AS_ADMIN_USER_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.CREDENTIALS_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.PRIVILEGES_TN;
import static com.sun.enterprise.admin.servermgmt.services.Constants.README;
import static com.sun.enterprise.admin.servermgmt.services.Constants.TIMEOUT_SECONDS_TN;

/**
 * Represents the SMF Service. Holds the tokens and their values that are consumed by the SMF templates. The recommended
 * way to use this class (or its instances) is to initialize it with default constructor and then apply various mutators
 * to configure the service. Finally, callers should make sure that the configuration is valid, before attempting to
 * create the service in the Solaris platform.
 *
 * @since SJSAS 9.0
 * @see #isConfigValid
 * @see SMFServiceHandler
 * @author Kedar Mhaswade
 */
public final class SMFService extends ServiceAdapter {
    public static final String TIMEOUT_SECONDS_DV = "0";
    public static final String AS_ADMIN_USER_DEF_VAL = "admin";
    public static final String SP_DELIMITER = ":";
    public static final String PRIVILEGES_DEFAULT_VAL = "basic";
    public static final String NETADDR_PRIV_VAL = "net_privaddr";
    public static final String BASIC_NETADDR_PRIV_VAL = PRIVILEGES_DEFAULT_VAL + "," + NETADDR_PRIV_VAL;
    public static final String START_INSTANCES_TN = "START_INSTANCES";
    public static final String START_INSTANCES_DEFAULT_VAL = Boolean.TRUE.toString();
    public static final String NO_START_INSTANCES_PROPERTY = "startinstances=false";
    public static final String SVCCFG = "/usr/sbin/svccfg";
    public static final String SVCADM = "/usr/sbin/svcadm";
    public static final File MANIFEST_HOME = new File("/var/svc/manifest/application/GlassFish");
    private static final String NULL_VALUE = "null";
    private static final StringManager sm = StringManager.getManager(SMFService.class);
    private static final String nullArgMsg = sm.getString("null_arg");
    private static final String MANIFEST_FILE_SUFFIX = "Domain-service-smf.xml";
    private static final String MANIFEST_FILE_TEMPL_SUFFIX = MANIFEST_FILE_SUFFIX + ".template";
    private static final String REL_PATH_TEMPLATES = "lib/install/templates";
    private static final int DEFAULT_SERVICE_TIMEOUT = 600_000;

    /**
     * Creates SMFService instance. All the tokens are initialized to default values. Callers must verify that the tokens
     * are properly token-replaced before using this instance.
     */
    SMFService(ServerDirs dirs, AppserverServiceType type) {
        super(dirs, type);
        if (!apropos()) {
            throw new IllegalArgumentException("Internal Error: SMFService constructor called but SMF is not available.");
        }
        init_old_delete_me();
    }

    static boolean apropos() {
        // suggested by smf-discuss forum on OpenSolaris
        return OS.isSun() && new File(SVCADM).isFile();
    }

    /**
     * Creates the service on the given platform.
     */
    @Override
    public void createServiceInternal() {
        File manifestFile = getManifestFile();
        boolean previousManifestExists = manifestFile.exists();
        try {
            isConfigValid(); //safe, throws exception if not valid
            if (info.trace) {
                printOut(toString());
            }
            validateManifest(manifestFile);
            previousManifestExists = false;
            ServicesUtils.tokenReplaceTemplateAtDestination(tokensAndValues(), getManifestTemplateFile(), getManifestFile());
            validateService();
            importService();
        } catch (final Exception e) {
            if (!previousManifestExists) {
                cleanupManifest();
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteServiceInternal() {
        try {
            String serviceName = info.serviceName;
            if (!ok(serviceName)) {
                throw new RuntimeException(Strings.get("internal.error", "no service name is set"));
            }
            String me = System.getProperty("user.name");
            StringBuilder sb = new StringBuilder();
            if (!isUserSmfAuthorized(me, sb)) {
                throw new RuntimeException(Strings.get("noSmfAuth", me, sb.toString()));
            }
            ProcessManager pm = new ProcessManager(SVCADM, "disable", info.serviceName);
            pm.setEcho(false);
            pm.execute();
            pm = new ProcessManager(SVCCFG, "delete", info.serviceName);
            pm.setEcho(false);
            pm.execute();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void checkServiceName() {
        if (serviceNameExists(info.smfFullServiceName)) {
            final String msg = sm.getString("serviceNameExists", info.smfFullServiceName);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Returns timeout in seconds before the master boot restarter should give up starting this service.
     */
    @Override
    public int getTimeoutSeconds() {
        final int to = Integer.parseInt(getTokenMap().get(TIMEOUT_SECONDS_TN));
        return (to);
    }

    /**
     * Sets timeout in seconds before the master boot restarter should give up starting this service.
     *
     * @param number a non-negative integer representing timeout. A value of zero implies infinite timeout.
     */
    @Override
    public void setTimeoutSeconds(final int number) {
        Integer to = Integer.valueOf(number);
        if (to < 0) {
            final String msg = sm.getString("invalidTO", number);
            throw new IllegalArgumentException(msg);
        }
        getTokenMap().put(TIMEOUT_SECONDS_TN, to.toString());
    }

    /**
     * Sets the OS-level user-id who should start and own the processes started by this service. This user is the same as
     * the value returned by System.getProperty("user.name"). The idea is that the method is called by the user who actually
     * wants to own the service.
     *
     * @throws IllegalArgumentException if the user can not modify MANIFEST_HOME
     * @throws IllegalArgumentException if solaris.smf.modify Authorization is not implied by the authorizations available
     * for the user.
     */
    private void checkOSUser() {
        String msg;
        if (!canCreateManifest()) {
            msg = sm.getString("noPermissionToCreateManifest", info.osUser, MANIFEST_HOME);
            throw new IllegalArgumentException(msg);
        }
        final StringBuilder auths = new StringBuilder();
        if (!isUserSmfAuthorized(info.osUser, auths)) {
            msg = sm.getString("noSmfAuth", info.osUser, auths);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Returns the additional properties of the Service.
     *
     * @return String representing addtional properties of the service. May return default properties as well.
     */
    @Override
    public String getServiceProperties() {
        return (getTokenMap().get(PRIVILEGES_TN));
    }

    /**
     * Sets the additional service properties that are specific to it.
     *
     * @param cds must be a colon separated String, if not null. No effect, if null is passed.
     */
    @Override
    public void setServiceProperties(final String cds) {
        /* For now, we have to take care of only net_privaddr privilege property.
         * Additional properties will result in additional tokens being replaced.
         * A null value for parameter results in setting the basic privilege property.
         */
        if (cds != null) {
            final Set<String> props = ps2Pairs(cds);
            if (props.contains(NETADDR_PRIV_VAL)) {
                getTokenMap().put(PRIVILEGES_TN, BASIC_NETADDR_PRIV_VAL); // you get both basic, netaddr_priv
            }
            if (props.contains(NO_START_INSTANCES_PROPERTY)) {
                getTokenMap().put(START_INSTANCES_TN, Boolean.FALSE.toString());
            }
        }
    }

    /**
     * Determines if the configuration of the method is valid. When this class is constructed, appropriate defaults are
     * used. But before attempting to create the service in the Solaris platform, it is important that the necessary
     * configuration is done by the users via various mutator methods of this class. This method must be called to guard
     * against some abnormal failures before creating the service. It makes sure that the caller has set all the necessary
     * parameters reasonably. Note that it does not validate the actual values.
     *
     * @throws RuntimeException if the configuration is not valid
     * @return true if the configuration is valid, an exception is thrown otherwise
     */
    @Override
    public boolean isConfigValid() {
        final Set<String> keys = getTokenMap().keySet();
        for (final String k : keys) {
            final boolean aNullValue = NULL_VALUE.equals(getTokenMap().get(k));
            if (aNullValue) {
                final String msg = sm.getString("smfTokenNeeded", k, getTokenMap().get(k));
                throw new RuntimeException(msg);
            }
        }
        final File mf = getManifestTemplateFile();
        if (!mf.exists()) {
            final String msg = sm.getString("serviceTemplateNotFound", getManifestTemplateFile());
            throw new RuntimeException(msg);
        }

        // bnevins May 27, 2009
        // passwordfile is now optional for start-domain
        // BEFORE:  --user %%%AS_ADMIN_USER%%% --passwordfile %%%PASSWORD_FILE_PATH%%%
        // AFTER:   %%%CREDENTIALS%%%

        return (true);
    }

    /**
     * Returns the tokens and values of the service as a map. Note that a copy is returned.
     *
     * @return a copy of tokens and values
     */
    @Override
    public Map<String, String> tokensAndValues() {
        return (new HashMap<>(getTokenMap())); //send only copy
    }

    /**
     * Returns the absolute location of the manifest file as SMF understands it. It takes into account the name, type and
     * configuration location of the service. It is expected that these are set before calling this method. If the <b> Fully
     * Qualified Service Name </b> is invalid, a RuntimeException results.
     */
    @Override
    public File getManifestFile() {
        return MANIFEST_HOME.toPath().resolve(Path.of(info.fqsn, MANIFEST_FILE_SUFFIX)).toFile();
    }

    /**
     * Returns the absolute location of the template for the given service. If the file can not be found at its required
     * location then the file will be copied from inside this jar file to the file system. The type of the service must be
     * set before calling this method, otherwise a runtime exception results.
     */
    @Override
    public File getManifestTemplateFile() {
        String ir = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        if (!ok(ir)) {
            throw new RuntimeException("Internal Error - System Property not set: " + SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        }

        File rootDir = SmartFile.sanitize(new File(ir));
        if (!rootDir.isDirectory()) {
            throw new RuntimeException("Internal Error - Not a directory: " + rootDir);
        }

        File templatesDir = new File(rootDir, REL_PATH_TEMPLATES);
        return new File(templatesDir, MANIFEST_FILE_TEMPL_SUFFIX);
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    /**
     * Returns a String representation of the SMFService. It contains a new-line separated "name=value" String that contains
     * the name and value of each of of the tokens that were set in the service.
     *
     * @return a String according to above description, never returns null
     */
    @Override
    public String toString() {
        /* toString method useful for debugging */
        final StringBuilder sb = new StringBuilder();
        final String[] ka = new String[getTokenMap().size()];
        Arrays.sort(getTokenMap().keySet().toArray(ka));
        for (final String n : ka) {
            sb.append(n).append("=").append(getTokenMap().get(n)).append(System.getProperty("line.separator"));
        }
        return (sb.toString());
    }

    /**
     * For safety -- this is similar to the subversion dry-run command. It does everything except create the service.
     */
    @Override
    public String getSuccessMessage() {
        String msg = Strings.get("SMFServiceCreated", info.smfFullServiceName, info.type.toString(),
            info.serverDirs.getServerParentDir(), getManifestFile(), info.serviceName);

        if (info.dryRun) {
            msg += Strings.get("dryrun");
        }

        return msg;
    }

    ////////////////////// PRIVATE METHODS ////////////////////
    @Override
    public void initializeInternal() {
        checkOSUser();
        checkServiceName();
    }

    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
    private void init_old_delete_me() {
        // ?????
        getTokenMap().put(START_INSTANCES_TN, START_INSTANCES_DEFAULT_VAL);
        getTokenMap().put(AS_ADMIN_USER_TN, AS_ADMIN_USER_DEF_VAL);
        getTokenMap().put(TIMEOUT_SECONDS_TN, TIMEOUT_SECONDS_DV);
        getTokenMap().put(PRIVILEGES_TN, BASIC_NETADDR_PRIV_VAL);
        getTokenMap().put(CREDENTIALS_TN, " ");
    }

    private Set<String> ps2Pairs(final String cds) {
        final StringTokenizer p = new StringTokenizer(cds, SP_DELIMITER);
        final Set<String> tokens = new HashSet<>();
        while (p.hasMoreTokens()) {
            tokens.add(p.nextToken());
        }
        return (tokens);
    }

    private boolean canCreateManifest() {
        boolean ok = true;
        if (!MANIFEST_HOME.exists()) {
            ok = MANIFEST_HOME.mkdirs();
        }
        if (ok) {
            if (!MANIFEST_HOME.canWrite()) {
                ok = false;
            }
        }
        return (ok);
    }

    private boolean isUserSmfAuthorized(final String user, final StringBuilder auths) {
        boolean authorized = false;
        String path2Auths = "auths";
        String at = ",";
        final String AUTH1 = "solaris.*";
        final String AUTH2 = "solaris.smf.*";
        final String AUTH3 = "solaris.smf.modify";
        if (System.getProperty("PATH_2_AUTHS") != null) {
            path2Auths = System.getProperty("PATH_2_AUTHS");
        }
        if (System.getProperty("AUTH_TOKEN") != null) {
            at = System.getProperty("AUTH_TOKEN");
        }
        try {
            final String[] cmd = new String[] { path2Auths, user };
            ProcessManager pm = new ProcessManager(cmd);
            pm.setTimeoutMsec(DEFAULT_SERVICE_TIMEOUT);
            pm.execute();
            auths.append(pm.getStdout());
            final StringTokenizer st = new StringTokenizer(pm.getStdout(), at);
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                t = t.trim();
                if (AUTH1.equals(t) || AUTH2.equals(t) || AUTH3.equals(t)) {
                    authorized = true;
                    break;
                }
            }
            return (authorized);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean serviceNameExists(final String sn) {
        boolean exists = false;
        try {
            final String[] cmd = new String[] {"/usr/bin/svcs", sn};
            ProcessManager pm = new ProcessManager(cmd);
            pm.setTimeoutMsec(DEFAULT_SERVICE_TIMEOUT);
            int exitCode = pm.execute();
            if (exitCode == 0) {
                exists = true;
            }
        } catch (final Exception e) {
            // Do nothing, status is already set.
        }
        return exists;
    }

    private void validateManifest(final File manifest) throws Exception {
        final File manifestParent = manifest.getParentFile();
        final String msg = sm.getString("smfLeftoverFiles", manifest.getParentFile().getAbsolutePath());

        if (manifestParent != null && manifestParent.isDirectory()) {

            if (info.force) {
                FileUtils.whack(manifestParent);

                if (manifestParent.isDirectory()) {
                    throw new IllegalArgumentException(msg);
                }
            } else {
                throw new IllegalArgumentException(msg);
            }
        }
        if (!manifest.getParentFile().mkdirs()) {
            if (info.trace) {
                printOut("Failed to create manifest parent file: " + manifest.getParentFile().getAbsolutePath());
            }
        }
        if (info.trace) {
            printOut("Manifest validated: " + manifest);
        }
    }

    private void validateService() throws Exception {
        final String[] cmda = new String[] { SMFService.SVCCFG, "validate", getManifestFile().getAbsolutePath() };
        final ProcessManager pm = new ProcessManager(cmda);
        pm.setTimeoutMsec(DEFAULT_SERVICE_TIMEOUT);
        pm.execute();
        if (info.trace) {
            printOut("Validated the SMF Service: " + info.fqsn + " using: " + SMFService.SVCCFG);
        }
    }

    private boolean importService() throws Exception {
        final String[] cmda = new String[] { SMFService.SVCCFG, "import", getManifestFile().getAbsolutePath() };
        final ProcessManager pm = new ProcessManager(cmda);
        pm.setTimeoutMsec(DEFAULT_SERVICE_TIMEOUT);
        if (info.dryRun) {
            cleanupManifest();
        }
        else {
            pm.execute(); // throws ProcessManagerException in case of an error
        }

        if (info.trace) {
            printOut("Imported the SMF Service: " + info.fqsn);
        }
        return (true);
    }

    private void cleanupManifest() throws RuntimeException {
        final File manifest = getManifestFile();
        if (manifest.exists()) {
            if (!manifest.delete()) {
                manifest.deleteOnExit();
            }
            if (info.trace) {
                printOut("Attempted deleting failed service manifest: " + manifest.getAbsolutePath());
            }
        }
        final File failedServiceNode = manifest.getParentFile();
        if (failedServiceNode.exists()) {
            if (!failedServiceNode.delete()) {
                failedServiceNode.deleteOnExit();
            }
            if (info.trace) {
                printOut("Attempted deleting failed service folder: " + failedServiceNode.getAbsolutePath());
            }
        }
    }

    private void printOut(final String s) {
        System.out.println(s);
    }

    // duplicated
    // todo -- fix the filename!!
    // todo
    // todo
    @Override
    public void writeReadmeFile(String msg) {
        File f = new File(getServerDirs().getServerDir(), README);
        ServicesUtils.appendTextToFile(f, msg);
    }

    @Override
    public String getLocationArgsStart() {
        if (isDomain()) {
            return " --domaindir " + getServerDirs().getServerParentDir().getPath() + " ";
        } else {
            return " --nodedir " + getServerDirs().getServerGrandParentDir().getPath() + " --node "
                    + getServerDirs().getServerParentDir().getName() + " ";
        }
    }

    @Override
    public String getLocationArgsStop() {
        return getLocationArgsStart(); // same with SMF
    }
}
