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

package com.sun.enterprise.admin.servermgmt.pe;

import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.sun.enterprise.security.store.PasswordAdapter.PASSWORD_ALIAS_KEYSTORE;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_FILENAME_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_FILENAME;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_DEFAULT;
import static java.text.MessageFormat.format;

public class PEFileLayout {
    private static final StringManager _strMgr = StringManager.getManager(PEFileLayout.class);

    public static final String DEFAULT_INSTANCE_NAME = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;
    /* above field is taken from a central place */
    protected final RepositoryConfig _config;

    public PEFileLayout(RepositoryConfig config) {
        _config = config;
    }

    protected RepositoryConfig getConfig() {
        return _config;
    }

    public void createRepositoryRoot() throws RepositoryException {
        createDirectory(getRepositoryRootDir());
    }

    protected void createDirectory(File dir) throws RepositoryException {
        if (!dir.exists()) {
            try {
                if (!dir.mkdirs()) {
                    throw new RepositoryException(format("Could not create directory {0}.", dir));
                }
            } catch (Exception e) {
                throw new RepositoryException(format("Could not create directory {0}.", dir), e);
            }
        }
    }

    public static final String ADDON_DIR = "addons";

    public File getAddonRoot() {
        return new File(getRepositoryDir(), ADDON_DIR);
    }

    public static final String CONFIG_DIR = "config";

    public File getConfigRoot() {
        return new File(getRepositoryDir(), CONFIG_DIR);
    }

    public static final String CONFIG_BACKUP_DIR = "backup";

    public File getRepositoryBackupRoot() {
        return new File(getConfigRoot(), CONFIG_BACKUP_DIR);
    }

    public static final String DOC_ROOT_DIR = "docroot";

    public File getDocRoot() {

        return new File(getRepositoryDir(), DOC_ROOT_DIR);
    }

    public static final String JAVA_WEB_START_DIR = "java-web-start";

    public File getJavaWebStartRoot() {

        return new File(getRepositoryDir(), JAVA_WEB_START_DIR);
    }

    public static final String LIB_DIR = "lib";

    public File getLibDir() {
        return new File(getRepositoryDir(), LIB_DIR);
    }

    public File getBinDir() {
        return new File(getRepositoryDir(), BIN_DIR);
    }

    public static final String CLASSES_DIR = "classes";

    public File getClassesDir() {
        return new File(getLibDir(), CLASSES_DIR);
    }

    public static final String APPLIBS_DIR = "applibs";

    public File getAppLibsDir() {
        return new File(getLibDir(), APPLIBS_DIR);
    }

    public static final String EXTLIB_DIR = "ext";

    public File getExtLibDir() {
        return new File(getLibDir(), EXTLIB_DIR);
    }

    public static final String TIMERDB_DIR = "databases";

    public File getTimerDatabaseDir() {
        return new File(getLibDir(), TIMERDB_DIR);
    }

    public static final String LOGS_DIR = "logs";

    public File getLogsDir() {
        return new File(getRepositoryDir(), LOGS_DIR);
    }

    public static final String APPS_ROOT_DIR = "applications";

    public File getApplicationsRootDir() {
        return new File(getRepositoryDir(), APPS_ROOT_DIR);
    }

    public static final String J2EE_APPS_DIR = "j2ee-apps";

    public File getJ2EEAppsDir() {
        return new File(getApplicationsRootDir(), J2EE_APPS_DIR);
    }

    public static final String J2EE_MODULES_DIR = "j2ee-modules";

    public File getJ2EEModulesDir() {
        return new File(getApplicationsRootDir(), J2EE_MODULES_DIR);
    }

    public static final String LIFECYCLE_MODULES_DIR = "lifecycle-modules";

    public File getLifecycleModulesDir() {
        return new File(getApplicationsRootDir(), LIFECYCLE_MODULES_DIR);
    }

    public static final String MBEAN_FOLDER_NAME = "mbeans";

    public File getMbeansDir() {
        return new File(getApplicationsRootDir(), MBEAN_FOLDER_NAME);
    }

    public static final String GENERATED_DIR = "generated";

    public File getGeneratedDir() {
        return new File(getRepositoryDir(), GENERATED_DIR);
    }

    // Begin EE: 4946914 - cluster deployment support

    public static final String POLICY_DIR = "policy";
    public static final String POLICY_FILE_EXT = "granted.policy";

    public File getPolicyDir() {
        return new File(getGeneratedDir(), POLICY_DIR);
    }

    // End EE: 4946914 - cluster deployment support

    public static final String JSP_DIR = "jsp";

    public File getJspRootDir() {
        return new File(getGeneratedDir(), JSP_DIR);
    }

    public static final String EJB_DIR = "ejb";

    public File getEjbRootDir() {
        return new File(getGeneratedDir(), EJB_DIR);
    }

    public static final String XML_DIR = "xml";

    public File getXmlRootDir() {
        return new File(getGeneratedDir(), XML_DIR);
    }

    public File getRepositoryDir() {
        return new File(getRepositoryRootDir(), getConfig().getRepositoryName());
    }

    public static final String DOMAIN_XML_FILE = "domain.xml";

    public File getDomainConfigFile() {
        return new File(getConfigRoot(), DOMAIN_XML_FILE);
    }

    public File getDomainConfigBackupFile() {
        return new File(getRepositoryBackupRoot(), DOMAIN_XML_FILE);
    }

    public static final String IMQ = "imq";

    public File getImqDir() {
        return new File(getInstallRootDir(), IMQ);
    }

    public static final String IMQ_VAR_DIR = "imq";

    public File getImqVarHome() {
        return new File(getRepositoryDir(), IMQ_VAR_DIR);
    }

    public static final String BIN_DIR = "bin";

    public File getImqBinDir() {
        return new File(getImqDir(), BIN_DIR);
    }

    public File getImqLibDir() {
        return new File(getImqDir(), LIB_DIR);
    }

    public File getInstallRootDir() {
        return getCanonicalFile(new File(getConfig().getInstallRoot()));
    }

    public File getRepositoryRootDir() {
        return getCanonicalFile(new File(getConfig().getRepositoryRoot()));
    }

    public static final String SHARE = "share";

    public File getShareDir() {
        return new File(getInstallRootDir(), SHARE);
    }

    public File getWebServicesLibDir() {
        return new File(getShareDir(), LIB_DIR);
    }

    //$INSTALL_ROOT/lib/install/templates
    public static final String INSTALL_DIR = "install";
    public static final String TEMPLATES_DIR = "templates";
    public static final String COMMON_DIR = "common";
    public static final String PROFILE_PROPERTIES = "profile.properties";
    private static final String TEMPLATE_CONFIG_XML = "default-config.xml";

    public File getTemplatesDir() {
        final File lib = new File(getInstallRootDir(), LIB_DIR);
        //final File install = new File(lib, INSTALL_DIR);
        final File templates = new File(lib, TEMPLATES_DIR);
        return templates;
    }

    public File getProfileFolder(final String profileName) {
        /* Commented out for V3, till things can be more finalized. For
         * now there is only one profile and the template is in the
         * common template directory */

        assert profileName != null : "Name of the profile can't be null";
        final File pf = new File(getTemplatesDir(), profileName);
        return pf;
    }

    public File getProfilePropertiesFile(final String profileName) {
        return (new File(getProfileFolder(profileName), PROFILE_PROPERTIES));
    }

    public File getPreExistingDomainXmlTemplateForProfile(final String profileName) {
        return (new File(getProfileFolder(profileName), DOMAIN_XML_FILE));
    }

    public File getTemplateConfigXml() {
        return (new File(getTemplatesDir(), TEMPLATE_CONFIG_XML));
    }

    //$INSTALL_ROOT/lib/install/applications

    public static final String APPLICATIONS_DIR = "applications";

    public File getInstallApplicationsDir() {
        final File lib = new File(getInstallRootDir(), LIB_DIR);
        final File install = new File(lib, INSTALL_DIR);
        return new File(install, APPLICATIONS_DIR);
    }

    //$INSTALL_ROOT/lib/install/databases

    public static final String DATABASES_DIR = "databases";

    public File getInstallDatabasesDir() {
        final File lib = new File(getInstallRootDir(), LIB_DIR);
        final File install = new File(lib, INSTALL_DIR);
        return new File(install, DATABASES_DIR);
    }

    //$INSTALL_ROOT/lib/dtds

    public static final String DTDS_DIR = "dtds";

    public File getDtdsDir() {
        final File lib = new File(getInstallRootDir(), LIB_DIR);
        return new File(lib, DTDS_DIR);
    }

    public static final String DOMAIN_XML_TEMPLATE = "default-domain.xml.template";

    public File getDomainXmlTemplate() {
        return new File(getTemplatesDir(), DOMAIN_XML_TEMPLATE);
    }

    public File getDomainXmlTemplate(String templateName) {
        // check to see if the user has specified a template file to be used for
        // domain creation. Assumed that the user specified template file
        // exists in the INSTALL_ROOT/lib/install/templates if path is not absolute.
        if (new File(templateName).isAbsolute()) {
            return new File(templateName);
        } else {
            return new File(getTemplatesDir(), templateName);
        }
    }

    public static final String START_SERV_UNIX = "startserv";
    public static final String START_SERV_WIN = "startserv.bat";
    public static final String START_SERV_OS = isWindows() ? START_SERV_WIN : START_SERV_UNIX;

    public File getStartServ() {
        return new File(getBinDir(), START_SERV_OS);
    }

    public static final String START_SERV_TEMPLATE_UNIX = "startserv.tomcat.template";
    public static final String START_SERV_TEMPLATE_WIN = "startserv.tomcat.bat.template";
    public static final String START_SERV_TEMPLATE_OS = isWindows() ? START_SERV_TEMPLATE_WIN : START_SERV_TEMPLATE_UNIX;

    public File getStartServTemplate() {
        return new File(getTemplatesDir(), START_SERV_TEMPLATE_OS);
    }

    public static final String STOP_SERV_UNIX = "stopserv";
    public static final String STOP_SERV_WIN = "stopserv.bat";
    public static final String STOP_SERV_OS = isWindows() ? STOP_SERV_WIN : STOP_SERV_UNIX;

    public File getStopServ() {
        return new File(getBinDir(), STOP_SERV_OS);
    }

    public static final String KILL_SERV_UNIX = "killserv";
    public static final String KILL_SERV_WIN = "killserv.bat";
    public static final String KILL_SERV_OS = isWindows() ? KILL_SERV_WIN : KILL_SERV_UNIX;

    public File getKillServ() {
        return new File(getBinDir(), KILL_SERV_OS);
    }

    public File getKillServTemplate() {
        return new File(getTemplatesDir(), KILL_SERV_OS);
    }

    public static final String STOP_SERV_TEMPLATE_UNIX = "stopserv.tomcat.template";
    public static final String STOP_SERV_TEMPLATE_WIN = "stopserv.tomcat.bat.template";
    public static final String STOP_SERV_TEMPLATE_OS = isWindows() ? STOP_SERV_TEMPLATE_WIN : STOP_SERV_TEMPLATE_UNIX;

    public File getStopServTemplate() {
        return new File(getTemplatesDir(), STOP_SERV_TEMPLATE_OS);
    }

    public static final String POLICY_FILE = "server.policy";

    public File getPolicyFileTemplate() {
        return new File(getTemplatesDir(), POLICY_FILE);
    }

    public File getPolicyFile() {
        return new File(getConfigRoot(), POLICY_FILE);
    }

    public static final String STUB_FILE = "admch";

    public File getStubFile() {
        return new File(getConfigRoot(), STUB_FILE);
    }

    public static final String SEED_FILE = "admsn";

    public File getSeedFile() {
        return new File(getConfigRoot(), SEED_FILE);
    }

    public File getInstallConfigRoot() {
        return new File(getInstallRootDir(), CONFIG_DIR);
    }

    public static final String ACC_XML_TEMPLATE = "glassfish-acc.xml";

    public Map<File, File> getAppClientContainerTemplateAndXml() {
        final Map<File, File> result = new HashMap<>();
        result.put(new File(getTemplatesDir(), ACC_XML_TEMPLATE), new File(getConfigRoot(), ACC_XML));
        return result;
    }

    public static final String ACC_XML = "glassfish-acc.xml";

    public static final String SESSION_STORE = "session-store";

    public File getSessionStore() {
        return new File(getRepositoryDir(), SESSION_STORE);
    }

    public static final String AUTO_DEPLOY = "autodeploy";

    public File getAutoDeployDir() {
        return new File(getRepositoryDir(), AUTO_DEPLOY);
    }

    public static final String AUTO_DEPLOY_STATUS = ".autodeploystatus";

    public File getAutoDeployStatusDir() {
        return new File(getAutoDeployDir(), AUTO_DEPLOY_STATUS);
    }

    private static final String AUTO_DEPLOY_OSGI_BUNDLES_DIR = "bundles";

    public static final String KEY_FILE_TEMPLATE = "keyfile";

    public File getKeyFileTemplate() {
        return new File(getTemplatesDir(), KEY_FILE_TEMPLATE);
    }

    public static final String KEY_FILE = "keyfile";

    public File getKeyFile() {
        return new File(getConfigRoot(), KEY_FILE);
    }

    public static final String ADMIN_KEY_FILE = "admin-keyfile";

    public File getAdminKeyFile() {
        return new File(getConfigRoot(), ADMIN_KEY_FILE);
    }

    public File getBackupKeyFile() {
        return new File(getRepositoryBackupRoot(), KEY_FILE);
    }

    public static final String INDEX_FILE = "index.html";
    public static final String DOC_ROOT = "docroot";

    public File getIndexFileTemplate() {
        final File docRoot = new File(getTemplatesDir(), DOC_ROOT);
        return new File(docRoot, INDEX_FILE);
    }

    private static final String LOCALES = "locales";

    public File getNonEnglishIndexFileTemplate(Locale locale) {
        File locales = new File(getTemplatesDir(), LOCALES);
        File givenLocale = new File(locales, locale.toString());
        return new File(givenLocale, INDEX_FILE);
    }

    public File getIndexFile() {
        return new File(getDocRoot(), INDEX_FILE);
    }

    private static final String ENGLISH_INDEX_FILE = "index_en.html";

    public File getEnglishIndexFile() {
        return new File(getDocRoot(), ENGLISH_INDEX_FILE);
    }

    public static final String DEFAULT_WEB_XML = "default-web.xml";

    public File getDefaultWebXmlTemplate() {
        return new File(getTemplatesDir(), DEFAULT_WEB_XML);
    }

    public File getDefaultWebXml() {
        return new File(getConfigRoot(), DEFAULT_WEB_XML);
    }

    public static final String LOGGING_PROPERTIES_FILE = "logging.properties";

    public File getLoggingPropertiesTemplate() {
        return new File(getTemplatesDir(), LOGGING_PROPERTIES_FILE);
    }

    public File getLoggingProperties() {
        return new File(getConfigRoot(), LOGGING_PROPERTIES_FILE);
    }

    public static final String LOGIN_CONF = "login.conf";

    public File getLoginConfTemplate() {
        return new File(getTemplatesDir(), LOGIN_CONF);
    }

    public File getLoginConf() {
        return new File(getConfigRoot(), LOGIN_CONF);
    }

    public static final String WSSSERVERCONFIGOLD = "wss-server-config-1.0.xml";

    public File getWssServerConfigOldTemplate() {
        return new File(getTemplatesDir(), WSSSERVERCONFIGOLD);
    }

    public File getWssServerConfigOld() {
        return new File(getConfigRoot(), WSSSERVERCONFIGOLD);
    }

    public static final String WSSSERVERCONFIG = "wss-server-config-2.0.xml";

    public File getWssServerConfigTemplate() {
        return new File(getTemplatesDir(), WSSSERVERCONFIG);
    }

    public File getWssServerConfig() {
        return new File(getConfigRoot(), WSSSERVERCONFIG);
    }

    public File getKeyStore() {
        return new File(getConfigRoot(), KEYSTORE_FILENAME_DEFAULT);
    }

    public File getTrustStore() {
        return new File(getConfigRoot(), TRUSTSTORE_FILENAME_DEFAULT);
    }

    public File getMasterPasswordFile() {
        return new File(getRepositoryDir(), MASTER_PASSWORD_FILENAME);
    }

    public File getPasswordAliasKeystore() {
        return new File(getConfigRoot(), PASSWORD_ALIAS_KEYSTORE);
    }

    public static final String TIMERDB_WAL_TEMPLATE = "ejbtimer$1.wal";

    public File getTimerWalTemplate() {
        return new File(getInstallDatabasesDir(), TIMERDB_WAL_TEMPLATE);
    }

    public static final String TIMERDB_WAL = "ejbtimer$1.wal";

    public File getTimerWal() {
        return new File(getTimerDatabaseDir(), TIMERDB_WAL);
    }

    public static final String TIMERDB_DBN_TEMPLATE = "ejbtimer.dbn";

    public File getTimerDbnTemplate() {
        return new File(getInstallDatabasesDir(), TIMERDB_DBN_TEMPLATE);
    }

    public static final String TIMERDB_DBN = "ejbtimer.dbn";

    public File getTimerDbn() {
        return new File(getTimerDatabaseDir(), TIMERDB_DBN);
    }

    public static final String DERBY_SQL_FILE = "ejbtimer_derby.sql";
    public static final String EJB_TIMER_TABLE_NAME = "EJB__TIMER__TBL"; //comes from sql file

    public File getDerbyEjbTimerSqlFile() {
        return new File(getInstallDatabasesDir(), DERBY_SQL_FILE);
    }

    public static final String DERBY_DATABASE_DIRECTORY = "ejbtimer";

    public File getDerbyEjbTimerDatabaseDirectory() {
        return new File(getTimerDatabaseDir(), DERBY_DATABASE_DIRECTORY);
        //this directory must not exist before creating the derby database
    }

    protected static boolean isWindows() {
        return OS.isWindows();
    }

    File getCanonicalFile(File f) {
        return FileUtils.safeGetCanonicalFile(f);
    }
}
