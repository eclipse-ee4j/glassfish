/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.domain;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.SLogger;
import com.sun.enterprise.admin.servermgmt.pe.PEDomainConfigValidator;
import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutionFactory;
import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutor;
import com.sun.enterprise.admin.servermgmt.stringsubs.impl.AttributePreprocessorImpl;
import com.sun.enterprise.admin.servermgmt.template.TemplateInfoHolder;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Property;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.PropertyType;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.util.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sun.enterprise.admin.servermgmt.SLogger.UNHANDLED_EXCEPTION;
import static com.sun.enterprise.admin.servermgmt.SLogger.getLogger;
import static com.sun.enterprise.admin.servermgmt.domain.DomainConstants.DOMAIN_XML_FILE;
import static java.text.MessageFormat.format;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * Domain builder class.
 */
public class DomainBuilder {

    private static final Logger _logger = SLogger.getLogger();

    /** The default stringsubs configuration file name. */
    private final static String STRINGSUBS_FILE = "stringsubs.xml";
    /** The filename contains basic template information. */
    private final static String TEMPLATE_INFO_XML = "template-info.xml";
    private final static String META_DIR_NAME = "META-INF";
    private final static String DEFUALT_TEMPLATE_RELATIVE_PATH = "common" + File.separator + "templates" + File.separator + "gf";

    private final DomainConfig _domainConfig;
    private JarFile _templateJar;
    private DomainTemplate _domainTempalte;
    private final Properties _defaultPortValues = new Properties();
    private byte[] _keystoreBytes;
    private final Set<String> _extractedEntries = new HashSet<>();

    /**
     * Create's a {@link DomainBuilder} object by initializing and loading the template jar.
     *
     * @param domainConfig An object contains domain creation parameters.
     * @throws DomainException If any error occurs during initialization.
     */
    public DomainBuilder(DomainConfig domainConfig) throws DomainException {
        _domainConfig = domainConfig;
        initialize();
    }

    /**
     * Initialize template by loading template jar.
     *
     * @throws DomainException If exception occurs in initializing the template jar.
     */
    private void initialize() throws DomainException {
        String templateJarPath = (String) _domainConfig.get(DomainConfig.K_TEMPLATE_NAME);
        if (templateJarPath == null || templateJarPath.isEmpty()) {
            String defaultTemplateName = Version.getDomainTemplateDefaultJarFileName();
            if (defaultTemplateName == null || defaultTemplateName.isEmpty()) {
                throw new DomainException("Missing default template information in branding file.");
            }
            Map<String, String> envProperties = new ASenvPropertyReader().getProps();
            templateJarPath = envProperties.get(INSTALL_ROOT.getPropertyName()) + File.separator
                + DEFUALT_TEMPLATE_RELATIVE_PATH + File.separator + defaultTemplateName;
        }
        File template = new File(templateJarPath);
        if (!template.exists() || !template.getName().endsWith(".jar")) {
            throw new DomainException(format("Could not locate template jar {0}", template));
        }
        try {
            _templateJar = new JarFile(new File(templateJarPath));
            JarEntry je = _templateJar.getJarEntry("config/" + DOMAIN_XML_FILE);
            if (je == null) {
                throw new DomainException(format("Missing mandatory file {0}.", DOMAIN_XML_FILE));
            }
            // Loads template-info.xml
            je = _templateJar.getJarEntry(TEMPLATE_INFO_XML);
            if (je == null) {
                throw new DomainException(format("Missing mandatory file {0}.", TEMPLATE_INFO_XML));
            }
            final TemplateInfoHolder templateInfoHolder;
            try (InputStream is = _templateJar.getInputStream(je)) {
                templateInfoHolder = new TemplateInfoHolder(is, templateJarPath);
            }
            _extractedEntries.add(TEMPLATE_INFO_XML);

            // Loads string substitution XML.
            je = _templateJar.getJarEntry(STRINGSUBS_FILE);
            StringSubstitutor stringSubstitutor = null;
            if (je != null) {
                stringSubstitutor = StringSubstitutionFactory.createStringSubstitutor(_templateJar.getInputStream(je));
                List<Property> defaultStringSubsProps = stringSubstitutor.getDefaultProperties(PropertyType.PORT);
                for (Property prop : defaultStringSubsProps) {
                    _defaultPortValues.setProperty(prop.getKey(), prop.getValue());
                }
                _extractedEntries.add(je.getName());
            } else {
                _logger.log(Level.WARNING, SLogger.MISSING_FILE, STRINGSUBS_FILE);
            }
            _domainTempalte = new DomainTemplate(templateInfoHolder, stringSubstitutor, templateJarPath);

            // Loads default self signed certificate.
            je = _templateJar.getJarEntry("config/" + DomainConstants.KEYSTORE_FILE);
            if (je != null) {
                _keystoreBytes = new byte[(int) je.getSize()];
                InputStream in = null;
                int count = 0;
                try {
                    in = _templateJar.getInputStream(je);
                    count = in.read(_keystoreBytes);
                    if (count < _keystoreBytes.length) {
                        throw new DomainException(format("Failure occurred while loading the {0}.", je.getName()));
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                _extractedEntries.add(je.getName());
            }
            File parentDomainDir = FileUtils.safeGetCanonicalFile(new File(_domainConfig.getRepositoryRoot()));
            createDirectory(parentDomainDir);
        } catch (Exception e) {
            throw new DomainException(e);
        }
    }

    /**
     * Validate's the template.
     *
     * @throws DomainException If any exception occurs in validation.
     */
    public void validateTemplate() throws DomainException {
        try {
            // Sanity check on the repository.
            RepositoryManager repoManager = new RepositoryManager();
            repoManager.checkRepository(_domainConfig, false);

            // Validate the port values.
            DomainPortValidator portValidator = new DomainPortValidator(_domainConfig, _defaultPortValues);
            portValidator.validateAndSetPorts();

            // Validate other domain config parameters.
            new PEDomainConfigValidator().validate(_domainConfig);

        } catch (Exception ex) {
            throw new DomainException(ex);
        }
    }

    /**
     * Performs all the domain configurations which includes security, configuration processing, substitution of
     * parameters... etc.
     *
     * @throws DomainException If any exception occurs in configuration.
     */
    public void run() throws RepositoryException, DomainException {

        // Create domain directories.
        File domainDir = FileUtils.safeGetCanonicalFile(new File(_domainConfig.getRepositoryRoot(), _domainConfig.getDomainName()));
        createDirectory(domainDir);
        try {
            // Extract other jar entries
            byte[] buffer = new byte[10000];
            for (Enumeration<JarEntry> entry = _templateJar.entries(); entry.hasMoreElements();) {
                JarEntry jarEntry = entry.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.startsWith(META_DIR_NAME)) {
                    // Skipping the extraction of jar meta data.
                    continue;
                }
                if (_extractedEntries.contains(entryName)) {
                    continue;
                }
                if (jarEntry.isDirectory()) {
                    File dir = new File(domainDir, jarEntry.getName());
                    if (!dir.exists()) {
                        if (!dir.mkdir()) {
                            _logger.log(Level.WARNING, SLogger.DIR_CREATION_ERROR, dir.getName());
                        }
                    }
                    continue;
                }
                InputStream in = null;
                BufferedOutputStream outputStream = null;
                try {
                    in = _templateJar.getInputStream(jarEntry);
                    outputStream = new BufferedOutputStream(
                            new FileOutputStream(new File(domainDir.getAbsolutePath(), jarEntry.getName())));
                    int i = 0;
                    while ((i = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, i);
                    }
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception io) {
                            /** ignore */
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Exception io) {
                            /** ignore */
                        }
                    }
                }
            }

            File configDir = new File(domainDir, DomainConstants.CONFIG_DIR);
            String user = (String) _domainConfig.get(DomainConfig.K_USER);
            String password = (String) _domainConfig.get(DomainConfig.K_PASSWORD);
            String[] adminUserGroups = ((String) _domainConfig.get(DomainConfig.K_INITIAL_ADMIN_USER_GROUPS)).split(",");
            String masterPassword = (String) _domainConfig.get(DomainConfig.K_MASTER_PASSWORD);
            Boolean saveMasterPassword = (Boolean) _domainConfig.get(DomainConfig.K_SAVE_MASTER_PASSWORD);

            // Process domain security.
            File adminKeyFile = new File(configDir, DomainConstants.ADMIN_KEY_FILE);
            DomainSecurity domainSecurity = new DomainSecurity();
            domainSecurity.processAdminKeyFile(adminKeyFile, user, password, adminUserGroups);
            try {
                domainSecurity.createSSLCertificateDatabase(configDir, _domainConfig, masterPassword);
            } catch (Exception e) {
                System.err.println("Domain creation process involves a step that creates primary key and"
                    + "\n self-signed server certificate. This step failed for the reason shown below."
                    + "\n This could be because JDK provided keytool program could not be found (e.g."
                    + "\n you are running with JRE) or for some other reason. No need to panic, as you"
                    + "\n can always use JDK-keytool program to do the needful. A temporary JKS-keystore"
                    + "\n will be created. You should replace it with proper keystore before using it for SSL."
                    + "\n Refer to documentation for details. Actual error is:\n" + e.getMessage());
                File keystoreFile = new File(configDir, DomainConstants.KEYSTORE_FILE);
                try (FileOutputStream fos = new FileOutputStream(keystoreFile)) {
                    fos.write(_keystoreBytes);
                } catch (Exception ex) {
                    getLogger().log(Level.SEVERE, UNHANDLED_EXCEPTION, ex);
                }
            }
            domainSecurity.changeMasterPasswordInMasterPasswordFile(
                new File(domainDir, DomainConstants.MASTERPASSWORD_FILE), masterPassword, saveMasterPassword);
            domainSecurity.createPasswordAliasKeystore(new File(configDir, DomainConstants.DOMAIN_PASSWORD_FILE),
                masterPassword);

            // Add customized tokens in domain.xml.
            CustomTokenClient tokenClient = new CustomTokenClient(_domainConfig);
            Map<String, String> generatedTokens = tokenClient.getSubstitutableTokens();

            // Perform string substitution.
            if (_domainTempalte.hasStringsubs()) {
                StringSubstitutor substitutor = _domainTempalte.getStringSubs();
                Map<String, String> lookUpMap = SubstitutableTokens.getSubstitutableTokens(_domainConfig);
                lookUpMap.putAll(generatedTokens);
                substitutor.setAttributePreprocessor(new AttributePreprocessorImpl(lookUpMap));
                substitutor.substituteAll();
            }

            // Change the permission for bin & config directories.
            try {
                File binDir = new File(domainDir, DomainConstants.BIN_DIR);
                if (binDir.exists() && binDir.isDirectory()) {
                    domainSecurity.changeMode("-R u+x ", binDir);
                }
                domainSecurity.changeMode("-R g-rwx,o-rwx ", configDir);
            } catch (Exception e) {
                throw new DomainException("Error setting permissions.", e);
            }

            // Generate domain-info.xml
            DomainInfoManager domainInfoManager = new DomainInfoManager();
            domainInfoManager.process(_domainTempalte, domainDir);
        } catch (DomainException de) {
            //roll-back
            FileUtils.liquidate(domainDir);
            throw de;
        } catch (Exception ex) {
            //roll-back
            FileUtils.liquidate(domainDir);
            throw new DomainException(ex);
        }
    }

    /**
     * Creates the given directory structure.
     *
     * @param dir The directory.
     * @throws RepositoryException If any error occurs in directory creation.
     */
    private void createDirectory(File dir) throws RepositoryException {
        if (!dir.exists()) {
            try {
                if (!dir.mkdirs()) {
                    throw new RepositoryException(format("Could not create directory {0}", dir));
                }
            } catch (Exception e) {
                throw new RepositoryException(format("Could not create directory {0}", dir), e);
            }
        }
    }
}
