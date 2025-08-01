/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.admin.util.LineTokenReplacer;
import com.sun.enterprise.admin.util.TokenValueSet;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
 * The RepositoryManager serves as a common base class for the following PEDomainsManager,
 * PEInstancesManager, AgentManager (the SE Node Agent).
 * It's purpose is to abstract out any shared functionality related to lifecycle management of
 * domains, instances and node agents.
 * This includes creation, deletion, listing, and starting and stopping.
 *
 * @author kebbs
 */
public class RepositoryManager extends MasterPasswordFileManager {
    /**
     * The RepositoryManagerMessages class is used to abstract out ResourceBundle messages that are specific to a domain,
     * node-agent, or server instance.
     */
    protected static class RepositoryManagerMessages {
        private final StringManager _strMgr;
        private final String _badNameMessage;
        private final String _repositoryNameMessage;
        private final String _repositoryRootMessage;
        private final String _existsMessage;
        private final String _noExistsMessage;
        private final String _repositoryNotValidMessage;
        private final String _cannotDeleteMessage;
        private final String _invalidPathMessage;
        private final String _listRepositoryElementMessage;
        private final String _cannotDeleteInstance_invalidState;
        private final String _instanceStartupExceptionMessage;
        private final String _cannotStartInstance_invalidStateMessage;
        private final String _startInstanceTimeOutMessage;
        private final String _portConflictMessage;
        private final String _startupFailedMessage;
        private final String _cannotStopInstance_invalidStateMessage;
        private final String _cannotStopInstanceMessage;
        private final String _timeoutStartingMessage;
        private String _cannotDeleteJmsProviderInstance;

        public RepositoryManagerMessages(StringManager strMgr, String badNameMessage, String repositoryNameMessage,
                String repositoryRootMessage, String existsMessage, String noExistsMessage, String repositoryNotValidMessage,
                String cannotDeleteMessage, String invalidPathMessage, String listRepositoryElementMessage,
                String cannotDeleteInstance_invalidState, String instanceStartupExceptionMessage,
                String cannotStartInstance_invalidStateMessage, String startInstanceTimeOutMessage, String portConflictMessage,
                String startupFailedMessage, String cannotStopInstance_invalidStateMessage, String cannotStopInstanceMessage,
                String timeoutStartingMessage) {
            _strMgr = strMgr;
            _badNameMessage = badNameMessage;
            _repositoryNameMessage = repositoryNameMessage;
            _repositoryRootMessage = repositoryRootMessage;
            _existsMessage = existsMessage;
            _noExistsMessage = noExistsMessage;
            _repositoryNotValidMessage = repositoryNotValidMessage;
            _cannotDeleteMessage = cannotDeleteMessage;
            _invalidPathMessage = invalidPathMessage;
            _listRepositoryElementMessage = listRepositoryElementMessage;
            _cannotDeleteInstance_invalidState = cannotDeleteInstance_invalidState;
            _instanceStartupExceptionMessage = instanceStartupExceptionMessage;
            _cannotStartInstance_invalidStateMessage = cannotStartInstance_invalidStateMessage;
            _startInstanceTimeOutMessage = startInstanceTimeOutMessage;
            _portConflictMessage = portConflictMessage;
            _startupFailedMessage = startupFailedMessage;
            _cannotStopInstance_invalidStateMessage = cannotStopInstance_invalidStateMessage;
            _cannotStopInstanceMessage = cannotStopInstanceMessage;
            _timeoutStartingMessage = timeoutStartingMessage;
        }

        public String getRepositoryNameMessage() {
            return _strMgr.getString(_repositoryNameMessage);
        }

        public String getBadNameMessage(String repositoryName) {
            return _strMgr.getString(_badNameMessage, repositoryName);
        }

        public String getRepositoryRootMessage() {
            return _strMgr.getString(_repositoryRootMessage);
        }

        public String getNoExistsMessage(String repositoryName, String repositoryLocation) {
            return _strMgr.getString(_noExistsMessage, repositoryName, repositoryLocation);
        }

        public String getExistsMessage(String repositoryName, String repositoryLocation) {
            return _strMgr.getString(_existsMessage, repositoryName, repositoryLocation);
        }

        public String getRepositoryNotValidMessage(String path) {
            return _strMgr.getString(_repositoryNotValidMessage, path);
        }

        public String getCannotDeleteMessage(String repositoryName) {
            return _strMgr.getString(_cannotDeleteMessage, repositoryName);
        }

        public String getInvalidPathMessage(String path) {
            return _strMgr.getString(_invalidPathMessage, path);
        }

        public String getListRepositoryElementMessage(String repositoryName, String repositoryStatus) {
            return _strMgr.getString(_listRepositoryElementMessage, repositoryName, repositoryStatus);
        }

        public String getCannotDeleteInstanceInvalidState(String name, String state) {
            return _strMgr.getString(_cannotDeleteInstance_invalidState, name, state);
        }

        public String getInstanceStartupExceptionMessage(String name) {
            return _strMgr.getString(_instanceStartupExceptionMessage, name);
        }

        public String getCannotStartInstanceInvalidStateMessage(String name, String state) {
            return _strMgr.getString(_cannotStartInstance_invalidStateMessage, name, state);
        }

        public String getStartInstanceTimeOutMessage(String name) {
            return _strMgr.getString(_startInstanceTimeOutMessage, name);
        }

        public String getStartupFailedMessage(String name) {
            return _strMgr.getString(_startupFailedMessage, name);
        }

        public String getStartupFailedMessage(String name, int port) {
            if (port != 0) {
                return _strMgr.getString(_portConflictMessage, new Object[] { name, String.valueOf(port) });
            } else {
                return _strMgr.getString(_startupFailedMessage, name);
            }
        }

        public String getCannotStopInstanceInvalidStateMessage(String name, String state) {
            return _strMgr.getString(_cannotStopInstance_invalidStateMessage, name, state);
        }

        public String getCannotStopInstanceMessage(String name) {
            return _strMgr.getString(_cannotStopInstanceMessage, name);
        }

        public String getTimeoutStartingMessage(String name) {
            return _strMgr.getString(_timeoutStartingMessage, name);
        }
    }

    protected static final String CERTUTIL_CMD = System.getProperty(SystemPropertyConstants.NSS_BIN_PROPERTY) + "/certutil";
    protected static final String NEW_LINE = System.getProperty("line.separator");
    private static final StringManager _strMgr = StringManager.getManager(RepositoryManager.class);
    protected RepositoryManagerMessages _messages = null;
    public static final String DEBUG = "Debug";

    /**
     * Creates a new instance of RepositoryManager
     */
    public RepositoryManager() {
        super();
        setMessages(new RepositoryManagerMessages(StringManager.getManager(PEDomainsManager.class), "illegalDomainName", "domainName",
                "domainsRoot", "domainExists", "domainDoesntExist", "domainDirNotValid", "cannotDeleteDomainDir", "invalidDomainDir",
                "listDomainElement", "cannotDeleteInstance_invalidState", "instanceStartupException", "cannotStartInstance_invalidState",
                "startInstanceTimeOut", "portConflict", "startupFailed", "cannotStopInstance_invalidState", "cannotStopInstance",
                "timeoutStarting"));
    }

    protected void setMessages(RepositoryManagerMessages messages) {
        _messages = messages;
    }

    protected RepositoryManagerMessages getMessages() {
        return _messages;
    }

    protected void generateFromTemplate(TokenValueSet tokens, File template, File destinationFile) throws IOException {
        LineTokenReplacer replacer = new LineTokenReplacer(tokens);
        replacer.replace(template, destinationFile);
    }

    protected boolean repositoryExists(RepositoryConfig config) {
        return FileUtils.safeGetCanonicalFile(getRepositoryDir(config)).exists();
    }

    protected boolean isValidRepository(File f) {
        return new File(new File(f, PEFileLayout.CONFIG_DIR), PEFileLayout.DOMAIN_XML_FILE).exists();
    }

    protected boolean isValidRepository(RepositoryConfig config) {
        return getFileLayout(config).getDomainConfigFile().exists();
    }

    protected File getRepositoryDir(RepositoryConfig config) {
        return getFileLayout(config).getRepositoryDir();
    }

    protected File getRepositoryRootDir(RepositoryConfig config) {
        return getFileLayout(config).getRepositoryRootDir();
    }

    protected void checkRepository(RepositoryConfig config) throws RepositoryException {
        checkRepository(config, true, true);
    }

    public void checkRepository(RepositoryConfig config, boolean existingRepository) throws RepositoryException {
        checkRepository(config, existingRepository, true);
    }

    /**
     * Sanity check on the repository. This is executed prior to create/delete/start/stop.
     */
    public void checkRepository(RepositoryConfig config, boolean existingRepository, boolean checkRootDir) throws RepositoryException {
        String repositoryName = config.getDisplayName();

        //check domain name for validity
        new RepositoryNameValidator(getMessages().getRepositoryNameMessage()).validate(repositoryName);

        if (checkRootDir || existingRepository) {
            //check domain root directory is read/writable
            new FileValidator(getMessages().getRepositoryRootMessage(), "drw").validate(config.getRepositoryRoot());
        }

        //check installation root directory is readable
        new FileValidator(_strMgr.getString("installRoot"), "dr").validate(config.getInstallRoot());

        //Ensure that the domain exists or does not exist
        if (existingRepository) {
            if (!repositoryExists(config)) {
                if (Boolean.getBoolean(DEBUG)) {
                    throw new RepositoryException(getMessages().getNoExistsMessage(repositoryName, getBigNoExistsMessage(config)));
                } else {
                    throw new RepositoryException(
                            getMessages().getNoExistsMessage(repositoryName, getRepositoryDir(config).getAbsolutePath()));
                }
            } else if (!isValidRepository(config)) {
                throw new RepositoryException(getMessages().getRepositoryNotValidMessage(getRepositoryDir(config).getAbsolutePath()));
            }
        } else {
            if (repositoryExists(config)) {
                throw new RepositoryException(
                        getMessages().getExistsMessage(repositoryName, getRepositoryRootDir(config).getAbsolutePath()));
            }
        }
    }

    private String getBigNoExistsMessage(RepositoryConfig config) {
        File repdir = getRepositoryDir(config);
        File canrepdir = FileUtils.safeGetCanonicalFile(repdir);
        File canrepdirparent = canrepdir.getParentFile();

        String s = "";
        s += "\nRep. Dir:" + repdir;
        s += "\nDump of RepositoryConfig: " + config.toString();
        s += "\nCanonical File: " + canrepdir;
        s += "\nParent File: " + canrepdirparent;

        boolean regex = repdir.exists();
        boolean canex = canrepdir.exists();
        boolean parentex = canrepdirparent.exists();
        boolean regdir = repdir.isDirectory();
        boolean candir = canrepdir.isDirectory();
        boolean parentdir = canrepdirparent.isDirectory();

        s += "\nrepdir exists: " + regex + ", canon exists: " + canex + ", parent exists: " + parentex + ", reg is dir: " + regdir
                + ", canon isdir: " + candir + ", parent is dir: " + parentdir;
        s += "\nInstance root sys property (";
        s += INSTANCE_ROOT.getSystemPropertyName();
        s += "): ";
        s += System.getProperty(INSTANCE_ROOT.getSystemPropertyName());

        return s;
    }

    /**
     * Sets the permissions for the domain directory, its config directory, startserv/stopserv scripts etc.
     */
    protected void setPermissions(RepositoryConfig repositoryConfig) throws RepositoryException {
        final PEFileLayout layout = getFileLayout(repositoryConfig);
        final File domainDir = layout.getRepositoryDir();
        try {
            chmod("-R 755", domainDir);
        } catch (Exception e) {
            throw new RepositoryException("Error setting permissions.", e);
        }
    }

    /**
     * Deletes the repository (domain, node agent, server instance).
     */
    protected void deleteRepository(RepositoryConfig config) throws RepositoryException {
        deleteRepository(config, true);
    }

    /**
     * Deletes the repository (domain, node agent, server instance). If the deleteJMSProvider flag is set, we delete the jms
     * instance. The jms instance is present in the domain only and not when the repository corresponds to a server instance
     * or node agent.
     */
    protected void deleteRepository(RepositoryConfig config, boolean deleteJMSProvider) throws RepositoryException {
        checkRepository(config, true);

        //Blast the directory
        File repository = getRepositoryDir(config);
        try {
            FileUtils.liquidate(repository);
        } catch (Exception e) {
            throw new RepositoryException(getMessages().getCannotDeleteMessage(repository.getAbsolutePath()), e);
        }

        //Double check to ensure that it was really deleted
        if (repositoryExists(config)) {
            throw new RepositoryException(getMessages().getCannotDeleteMessage(repository.getAbsolutePath()));
        }
    }

    /**
     * Return all repositories (domains, node agents, server instances)
     */
    protected String[] listRepository(RepositoryConfig config) throws RepositoryException {
        File repository = getRepositoryRootDir(config);
        String[] dirs;
        try {
            File f = repository.getCanonicalFile();
            if (!f.isDirectory()) {
                throw new RepositoryException(getMessages().getInvalidPathMessage(f.getAbsolutePath()));
            }
            dirs = f.list(new FilenameFilter() {
                //Only accept directories that are valid (contain the property startserv script)
                @Override
                public boolean accept(File dir, String name) {
                    File f = new File(dir, name);
                    if (!f.isDirectory()) {
                        return false;
                    } else {
                        return isValidRepository(f);
                    }
                }
            });
            if (dirs == null) {
                dirs = new String[0];
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        return dirs;
    }

    protected RepositoryConfig getConfigForRepositoryStatus(RepositoryConfig config, String repository) {
        //The repository here corresponds to either the domain or node agent name
        return new RepositoryConfig(repository, config.getRepositoryRoot());
    }

    /**
     * We validate the master password by trying to open the password alias keystore. This means that the keystore must
     * already exist.
     *
     * @param config
     * @param password
     * @throws RepositoryException
     */
    public void validateMasterPassword(RepositoryConfig config, String password) throws RepositoryException {
        final PEFileLayout layout = getFileLayout(config);
        final File passwordAliases = layout.getPasswordAliasKeystore();
        try {
            // WBN July 2007
            // we are constructing this object ONLY to see if it throws
            // an Exception.  We do not use the object.
            new PasswordAdapter(passwordAliases.getAbsolutePath(), password.toCharArray());
        } catch (IOException ex) {
            throw new RepositoryException(_strMgr.getString("masterPasswordInvalid"));
        } catch (Exception ex) {
            throw new RepositoryException(_strMgr.getString("couldNotValidateMasterPassword", passwordAliases), ex);
        }
    }

    /**
     * retrieve clear password from password alias keystore
     *
     * @param config
     * @param password
     * @param alias for which the clear text password would returns
     * @throws RepositoryException
     */
    public String getClearPasswordForAlias(RepositoryConfig config, String password, String alias) throws RepositoryException {
        final PEFileLayout layout = getFileLayout(config);
        final File passwordAliases = layout.getPasswordAliasKeystore();
        try {
            PasswordAdapter p = new PasswordAdapter(passwordAliases.getAbsolutePath(), password.toCharArray());
            String clearPwd = p.getPasswordForAlias(alias);
            return clearPwd;
        } catch (Exception ex) {
            return null;
        }
    }


    /**
     * Change the password protecting the password alias keystore
     *
     * @param config
     * @param oldPassword old password
     * @param newPassword new password
     * @throws RepositoryException
     */
    protected void changePasswordAliasKeystorePassword(RepositoryConfig config, String oldPassword, String newPassword)
            throws RepositoryException {
        final PEFileLayout layout = getFileLayout(config);
        final File passwordAliases = layout.getPasswordAliasKeystore();

        //Change the password of the keystore alias file
        if (passwordAliases.exists()) {
            try {
                PasswordAdapter p = new PasswordAdapter(passwordAliases.getAbsolutePath(), oldPassword.toCharArray());
                p.changePassword(newPassword.toCharArray());
            } catch (Exception ex) {
                throw new RepositoryException(_strMgr.getString("passwordAliasPasswordNotChanged", passwordAliases), ex);
            }
        }
    }

    /**
     * Create the timer database wal file.
     */
    protected void createTimerWal(RepositoryConfig config) throws RepositoryException {
        final PEFileLayout layout = getFileLayout(config);
        final File src = layout.getTimerWalTemplate();
        final File dest = layout.getTimerWal();
        try {
            FileUtils.copy(src, dest);
        } catch (IOException ioe) {
            throw new RepositoryException(_strMgr.getString("timerWalNotCreated"), ioe);
        }
    }

    /**
     * Create the timer database dbn file.
     */
    protected void createTimerDbn(RepositoryConfig config) throws RepositoryException {
        final PEFileLayout layout = getFileLayout(config);
        final File src = layout.getTimerDbnTemplate();
        final File dest = layout.getTimerDbn();
        try {
            FileUtils.copy(src, dest);
        } catch (IOException ioe) {
            throw new RepositoryException(_strMgr.getString("timerDbnNotCreated"), ioe);
        }
    }


    protected String[] getInteractiveOptions(String user, String password, String masterPassword, HashMap<Object, Object> extraPasswords) {
        int numKeys = extraPasswords == null ? 0 : extraPasswords.size();
        String[] options = new String[3 + numKeys];
        // set interativeOptions for security to hand to starting process from ProcessManager
        options[0] = user;
        options[1] = password;
        options[2] = masterPassword;
        if (extraPasswords != null) {
            int i = 3;
            for (Map.Entry<Object, Object> me : extraPasswords.entrySet()) {
                options[i++] = (String) me.getKey() + "=" + (String) me.getValue();
            }
        }
        return options;
    }


    /**
     * Determines if the NSS support is available in this installation. The check involves availability of the
     * <code> certutil </code> executable.
     *
     * @return true if certutil exists false otherwise
     */
    public static boolean isNSSSupportAvailable() {
        File certUtilFile = null;
        if (OS.isWindows()) {
            certUtilFile = new File(CERTUTIL_CMD + ".exe");
        } else {
            certUtilFile = new File(CERTUTIL_CMD);
        }
        if (certUtilFile.exists()) {
            return (true);
        }
        return (false);
    }
}
