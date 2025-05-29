/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.admin.cli.CLIConstants;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.KeystoreManager;
import com.sun.enterprise.admin.servermgmt.domain.DomainBuilder;
import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.admin.util.CommandModelData.ParamModelData;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.net.NetUtils;

import java.io.Console;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandModel.ParamModel;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.glassfish.security.common.FileRealmHelper;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.config.util.PortConstants.DEFAULT_INSTANCE_PORT;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_ADMINPORT_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_DEBUG_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_HTTPSSL_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_IIOPMUTUALAUTH_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_IIOPSSL_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_IIOP_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_INSTANCE_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_JMS_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_JMX_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORTBASE_OSGI_SUFFIX;
import static com.sun.enterprise.config.util.PortConstants.PORT_MAX_VAL;
import static com.sun.enterprise.util.SystemPropertyConstants.DEFAULT_ADMIN_PASSWORD;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_PASSWORD_DEFAULT;
import static org.glassfish.embeddable.GlassFishVariable.DOMAINS_ROOT;

/**
 * This is a local command that creates a domain.
 */
@Service(name = "create-domain")
@PerLookup
public final class CreateDomainCommand extends CLICommand {
    // constants for create-domain options
    private static final String ADMIN_PORT = "adminport";
    private static final String ADMIN_PASSWORD = "password";
    private static final String MASTER_PASSWORD = "masterpassword";
    private static final String SAVE_MASTER_PASSWORD = "savemasterpassword";
    private static final String INSTANCE_PORT = "instanceport";
    private static final String DOMAIN_PROPERTIES = "domainproperties";
    private static final String PORTBASE_OPTION = "portbase";
    private static final LocalStringsImpl strings = new LocalStringsImpl(CreateDomainCommand.class);

    private String adminUser;

    @Param(name = ADMIN_PORT, optional = true)
    private String adminPort;
    @Param(name = PORTBASE_OPTION, optional = true)
    private String portBase;
    @Param(obsolete = true, name = "profile", optional = true)
    private String profile;
    @Param(name = "template", optional = true)
    private String template;
    @Param(name = "domaindir", optional = true)
    private String domainDir;
    @Param(name = INSTANCE_PORT, optional = true)
    private String instancePort;
    @Param(name = SAVE_MASTER_PASSWORD, optional = true, defaultValue = "false")
    private boolean saveMasterPassword = false;
    @Param(name = "usemasterpassword", optional = true, defaultValue = "false")
    private boolean useMasterPassword = false;
    @Param(name = DOMAIN_PROPERTIES, optional = true, separator = ':')
    private Properties domainProperties;
    @Param(name = "keytooloptions", optional = true)
    private String keytoolOptions;
    @Param(name = "savelogin", optional = true, defaultValue = "false")
    private boolean saveLoginOpt = false;
    @Param(name = "nopassword", optional = true, defaultValue = "false")
    private boolean noPassword = false;
    @Param(name = ADMIN_PASSWORD, optional = true, password = true)
    private String adminPassword = null;
    @Param(name = MASTER_PASSWORD, optional = true, password = true)
    private String masterPassword = null;
    @Param(name = "checkports", optional = true, defaultValue = "true")
    private boolean checkPorts = true;
    @Param(name = "domain_name", primary = true)
    private String domainName;

    public CreateDomainCommand() {
    }

    /**
     * Add --adminport and --instanceport options with proper default values. (Can't set default values above because it
     * conflicts with --portbase option processing.)
     */
    @Override
    protected Collection<ParamModel> usageOptions() {
        Collection<ParamModel> opts = commandModel.getParameters();
        Set<ParamModel> uopts = new LinkedHashSet<>();
        ParamModel aPort = new ParamModelData(ADMIN_PORT, String.class, true, Integer.toString(CLIConstants.DEFAULT_ADMIN_PORT));
        ParamModel iPort = new ParamModelData(INSTANCE_PORT, String.class, true, Integer.toString(DEFAULT_INSTANCE_PORT));
        for (ParamModel pm : opts) {
            if (pm.getName().equals(ADMIN_PORT)) {
                uopts.add(aPort);
            } else if (pm.getName().equals(INSTANCE_PORT)) {
                uopts.add(iPort);
            } else {
                uopts.add(pm);
            }
        }
        return uopts;
    }

    /**
     */
    @Override
    protected void validate() throws CommandException, CommandValidationException {
        if (domainDir == null) {
            domainDir = getSystemProperty(DOMAINS_ROOT.getPropertyName());
        }
        if (domainDir == null) {
            throw new CommandValidationException(strings.get("InvalidDomainPath", domainDir));
        }

        /*
         * The only required value is the domain_name operand, which might have
         * been prompted for before we get here.
         *
         * If --user wasn't specified as a program option, we treat it as a
         * required option and prompt for it if possible, unless --nopassword
         * was specified in which case we default the user name.
         *
         * The next prompted-for value will be the admin password, if required.
         */
        if (programOpts.getUser() == null && !noPassword) {
            // prompt for it (if interactive)
            Console cons = System.console();
            if (cons != null && programOpts.isInteractive()) {
                cons.printf("%s", strings.get("AdminUserRequiredPrompt", SystemPropertyConstants.DEFAULT_ADMIN_USER));
                String val = cons.readLine();
                if (ok(val)) {
                    programOpts.setUser(val);
                    if (adminPassword == null) {
                        char[] pwdArr = getAdminPassword();
                        adminPassword = pwdArr != null ? new String(pwdArr) : null;
                    }
                }
            } else {
                //logger.info(strings.get("AdminUserRequired"));
                throw new CommandValidationException(strings.get("AdminUserRequired"));
            }
        }
        if (programOpts.getUser() != null) {
            try {
                FileRealmHelper.validateUserName(programOpts.getUser());
            } catch (IllegalArgumentException ise) {
                throw new CommandValidationException(strings.get("InvalidUserName", programOpts.getUser()));
            }
        }
    }

    public void verifyPortBase() throws CommandValidationException {
        if (usePortBase()) {
            final int portbase = convertPortStr(portBase);
            setOptionsWithPortBase(portbase);
        }
    }

    private void setOptionsWithPortBase(final int portbase) throws CommandValidationException {
        // set the option name and value in the options list
        verifyPortBasePortIsValid(ADMIN_PORT, portbase + PORTBASE_ADMINPORT_SUFFIX);
        adminPort = String.valueOf(portbase + PORTBASE_ADMINPORT_SUFFIX);

        verifyPortBasePortIsValid(INSTANCE_PORT, portbase + PORTBASE_INSTANCE_SUFFIX);
        instancePort = String.valueOf(portbase + PORTBASE_INSTANCE_SUFFIX);

        domainProperties = new Properties();
        verifyPortBasePortIsValid(DomainConfig.K_HTTP_SSL_PORT, portbase + PORTBASE_HTTPSSL_SUFFIX);
        domainProperties.put(DomainConfig.K_HTTP_SSL_PORT, String.valueOf(portbase + PORTBASE_HTTPSSL_SUFFIX));

        verifyPortBasePortIsValid(DomainConfig.K_IIOP_SSL_PORT, portbase + PORTBASE_IIOPSSL_SUFFIX);
        domainProperties.put(DomainConfig.K_IIOP_SSL_PORT, String.valueOf(portbase + PORTBASE_IIOPSSL_SUFFIX));

        verifyPortBasePortIsValid(DomainConfig.K_IIOP_MUTUALAUTH_PORT, portbase + PORTBASE_IIOPMUTUALAUTH_SUFFIX);
        domainProperties.put(DomainConfig.K_IIOP_MUTUALAUTH_PORT, String.valueOf(portbase + PORTBASE_IIOPMUTUALAUTH_SUFFIX));

        verifyPortBasePortIsValid(DomainConfig.K_JMS_PORT, portbase + PORTBASE_JMS_SUFFIX);
        domainProperties.put(DomainConfig.K_JMS_PORT, String.valueOf(portbase + PORTBASE_JMS_SUFFIX));

        verifyPortBasePortIsValid(DomainConfig.K_ORB_LISTENER_PORT, portbase + PORTBASE_IIOP_SUFFIX);
        domainProperties.put(DomainConfig.K_ORB_LISTENER_PORT, String.valueOf(portbase + PORTBASE_IIOP_SUFFIX));

        verifyPortBasePortIsValid(DomainConfig.K_JMX_PORT, portbase + PORTBASE_JMX_SUFFIX);
        domainProperties.put(DomainConfig.K_JMX_PORT, String.valueOf(portbase + PORTBASE_JMX_SUFFIX));

        verifyPortBasePortIsValid(DomainConfig.K_OSGI_SHELL_TELNET_PORT, portbase + PORTBASE_OSGI_SUFFIX);
        domainProperties.put(DomainConfig.K_OSGI_SHELL_TELNET_PORT, String.valueOf(portbase + PORTBASE_OSGI_SUFFIX));

        verifyPortBasePortIsValid(DomainConfig.K_JAVA_DEBUGGER_PORT, portbase + PORTBASE_DEBUG_SUFFIX);
        domainProperties.put(DomainConfig.K_JAVA_DEBUGGER_PORT, String.valueOf(portbase + PORTBASE_DEBUG_SUFFIX));

    }

    /**
     */
    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {

        // domain validation upfront (i.e. before we prompt)
        try {
            DomainsManager manager = new PEDomainsManager();
            DomainConfig config = new DomainConfig(domainName, domainDir);
            manager.validateDomain(config, false);
            verifyPortBase();
        } catch (DomainException e) {
            logger.fine(e.getLocalizedMessage());
            throw new CommandException(strings.get("CouldNotCreateDomain", domainName), e);
        }

        /*
         * The admin user is specified with the --user program option. If not
         * specified (because the user hit Enter at the prompt), we use the
         * default, which allows unauthenticated login.
         */
        adminUser = programOpts.getUser();
        if (!ok(adminUser)) {
            adminUser = SystemPropertyConstants.DEFAULT_ADMIN_USER;
            adminPassword = DEFAULT_ADMIN_PASSWORD;
        } else if (noPassword) {
            adminPassword = DEFAULT_ADMIN_PASSWORD;
        } else {
            char[] pwdArr = getAdminPassword();
            adminPassword = pwdArr != null ? new String(pwdArr) : null;
            boolean haveAdminPwd = true;
        }

        if (saveMasterPassword) {
            useMasterPassword = true;
        }

        if (masterPassword == null) {
            if (useMasterPassword) {
                char[] mpArr = getMasterPassword();
                masterPassword = mpArr == null ? null : new String(mpArr);
            } else {
                masterPassword = KEYSTORE_PASSWORD_DEFAULT;
            }
        }

        try {
            // verify admin port is valid if specified on command line
            if (adminPort != null) {
                verifyPortIsValid(adminPort);
            }
            // instance option is entered then verify instance port is valid
            if (instancePort != null) {
                verifyPortIsValid(instancePort);
            }

            // saving the login information happens inside this method
            createTheDomain(domainDir, domainProperties);
            return 0;
        } catch (CommandException ce) {
            logger.info(ce.getLocalizedMessage());
            throw new CommandException(strings.get("CouldNotCreateDomain", domainName), ce);
        } catch (Exception e) {
            logger.fine(e.getLocalizedMessage());
            throw new CommandException(strings.get("CouldNotCreateDomain", domainName), e);
        }
    }

    /**
     * Get the admin password as a required option.
     */
    private char[] getAdminPassword() throws CommandValidationException {
        // create a required ParamModel for the password
        ParamModelData po = new ParamModelData(ADMIN_PASSWORD, String.class, false, null);
        po.prompt = strings.get("AdminPassword");
        po.promptAgain = strings.get("AdminPasswordAgain");
        po.param._password = true;
        return getPassword(po, DEFAULT_ADMIN_PASSWORD, true);
    }

    /**
     * Get the master password as a required option (by default it is not required)
     */
    private char[] getMasterPassword() throws CommandValidationException {
        // create a required ParamModel for the password
        ParamModelData po = new ParamModelData(MASTER_PASSWORD, String.class, false /* optional */, null);
        po.prompt = strings.get("MasterPassword");
        po.promptAgain = strings.get("MasterPasswordAgain");
        po.param._password = true;
        return getPassword(po, KEYSTORE_PASSWORD_DEFAULT, true);
    }

    /**
     * Verify that the port is valid. Port must be greater than 0 and less than 65535. This method will also check if the
     * port is in use. If checkPorts is false it does not throw an Exception if it is in use.
     *
     * @param portNum - the port number to verify
     * @throws CommandException if Port is not valid
     * @throws CommandValidationException is port number is not a numeric value.
     */
    private void verifyPortIsValid(String portNum) throws CommandException, CommandValidationException {

        final int portToVerify = convertPortStr(portNum);

        if (!NetUtils.isPortValid(portToVerify)) {
            throw new CommandException(strings.get("InvalidPortRange", portNum));
        }

        if (checkPorts == false) {
            // do NOT make any network calls!
            logger.log(Level.FINER, "Port ={0}", portToVerify);
            return;
        }

        NetUtils.PortAvailability avail = NetUtils.checkPort(portToVerify);

        switch (avail) {
        case illegalNumber:
            throw new CommandException(strings.get("InvalidPortRange", portNum));

        case inUse:
            throw new CommandException(strings.get("PortInUseError", domainName, portNum));

        case noPermission:
            throw new CommandException(strings.get("NoPermissionForPortError", portNum, domainName));

        case unknown:
            throw new CommandException(strings.get("UnknownPortMsg", portNum));

        case OK:
            logger.log(Level.FINER, "Port ={0}", portToVerify);
            break;
        default:
            break;
        }
    }

    /**
     * Converts the port string to port int
     *
     * @param port the port number
     * @return the port number as an int
     * @throws CommandValidationException if port string is not numeric
     */
    private int convertPortStr(final String port) throws CommandValidationException {
        try {
            return Integer.parseInt(port);
        } catch (Exception e) {
            throw new CommandValidationException(strings.get("InvalidPortNumber", port));
        }
    }

    /**
     * Verify that the portbase port is valid Port must be greater than 0 and less than 65535. This method will also check
     * if the port is in used.
     *
     * @param portNum the port number to verify
     * @throws CommandException if Port is not valid
     * @throws CommandValidationException is port number is not a numeric value.
     */
    private void verifyPortBasePortIsValid(String portName, int portNum) throws CommandValidationException {
        if (portNum <= 0 || portNum > PORT_MAX_VAL) {
            throw new CommandValidationException(strings.get("InvalidPortBaseRange", portNum, portName));
        }
        if (checkPorts && !NetUtils.isPortFree(portNum)) {
            throw new CommandValidationException(strings.get("PortBasePortInUse", portNum, portName));
        }
        logger.log(Level.FINER, "Port ={0}", portNum);
    }

    /**
     * Create the domain.
     *
     * @param domainPath domain path to insert in domainConfig
     * @param domainProperties properties to insert in domainConfig
     * @throws CommandException if domain cannot be created
     */
    private void createTheDomain(final String domainPath, Properties domainProperties) throws DomainException, CommandValidationException {

        if (FileUtils.safeGetCanonicalFile(new File(domainPath, domainName)).exists()) {
            throw new CommandValidationException(strings.get("DomainExists", domainName));
        }
        DomainConfig domainConfig = null;
        if (template == null || template.endsWith(".jar")) {
            domainConfig = new DomainConfig(domainName, domainPath, adminUser, adminPassword, masterPassword,
                saveMasterPassword, adminPort, instancePort, domainProperties);
            domainConfig.put(DomainConfig.K_VALIDATE_PORTS, Boolean.valueOf(checkPorts));
            domainConfig.put(DomainConfig.KEYTOOLOPTIONS, keytoolOptions);
            domainConfig.put(DomainConfig.K_TEMPLATE_NAME, template);
            domainConfig.put(DomainConfig.K_PORTBASE, portBase);
            domainConfig.put(DomainConfig.K_INITIAL_ADMIN_USER_GROUPS, Version.getDomainDefaultAdminGroups());
            initSecureAdminSettings(domainConfig);
            try {
                DomainBuilder domainBuilder = new DomainBuilder(domainConfig);
                domainBuilder.validateTemplate();
                domainBuilder.run();
            } catch (Exception e) {
                throw new DomainException(e.getMessage());
            }
        } else {
            throw new DomainException(strings.get("InvalidTemplateValue", template));
        }
        logger.info(strings.get("DomainCreated", domainName));
        Integer aPort = (Integer) domainConfig.get(DomainConfig.K_ADMIN_PORT);
        logger.info(strings.get("DomainPort", domainName, Integer.toString(aPort)));
        if (adminPassword != null && adminPassword.equals(DEFAULT_ADMIN_PASSWORD)) {
            logger.info(strings.get("DomainAllowsUnauth", domainName, adminUser));
        } else {
            logger.info(strings.get("DomainAdminUser", domainName, adminUser));
        }
        //checkAsadminPrefsFile();
        if (saveLoginOpt) {
            saveLogin(aPort, adminUser, adminPassword != null ? adminPassword.toCharArray() : null, domainName);
        }
    }

    /**
     * Saves the login information to the login store. Usually this is the file ".asadminpass" in user's home directory.
     */
    private void saveLogin(final int port, final String user, final char[] password, final String dn) {
        try {
            // by definition, the host name will default to "localhost"
            // and entry is overwritten
            final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
            final LoginInfo login = new LoginInfo("localhost", port, user, password);
            if (store.exists(login.getHost(), login.getPort())) {
                // just let the user know that the user has chosen to overwrite
                // the login information. This is non-interactive, on purpose
                logger.info(strings.get("OverwriteLoginMsgCreateDomain", login.getHost(), "" + login.getPort()));
            }
            store.store(login, true);
            logger.info(strings.get("LoginInfoStoredCreateDomain", user, dn, store.getName()));
        } catch (final Throwable e) {
            logger.warning(strings.get("LoginInfoNotStoredCreateDomain", user, dn));
            logger.log(Level.FINER, "Could not save login!", e);
        }
    }

    /**
     * Check if portbase option is specified. Portbase is mutually exclusive to adminport and domainproperties options. If
     * portbase options is specfied and also adminport or domainproperties is specified as well, then throw an exception.
     */
    private boolean usePortBase() throws CommandValidationException {
        if (portBase != null) {
            if (adminPort != null) {
                throw new CommandValidationException(strings.get("MutuallyExclusiveOption", ADMIN_PORT, PORTBASE_OPTION));
            } else if (instancePort != null) {
                throw new CommandValidationException(strings.get("MutuallyExclusiveOption", INSTANCE_PORT, PORTBASE_OPTION));
            } else if (domainProperties != null) {
                throw new CommandValidationException(strings.get("MutuallyExclusiveOption", DOMAIN_PROPERTIES, PORTBASE_OPTION));
            } else {
                return true;
            }
        }
        return false;
    }

    private void initSecureAdminSettings(final DomainConfig config) {
        config.put(DomainConfig.K_ADMIN_CERT_DN, KeystoreManager.getDASCertDN(config));
        config.put(DomainConfig.K_INSTANCE_CERT_DN, KeystoreManager.getInstanceCertDN(config));
        config.put(DomainConfig.K_SECURE_ADMIN_IDENTIFIER, secureAdminIdentifier());
    }

    private String secureAdminIdentifier() {
        final UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
