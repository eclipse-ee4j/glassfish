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

package org.glassfish.persistence.jpa;


import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.deployment.common.ModuleDescriptor;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import javax.naming.NamingException;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import javax.sql.DataSource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.DeploymentUtils;

/**
 * This class implements {@link PersistenceUnitInfo} interface.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
    /* This class is public because it is used in verifier */

    private static final String DEFAULT_PROVIDER_NAME = "org.eclipse.persistence.jpa.PersistenceProvider"; // NOI18N

    // We allow the default provider to be specified using -D option.
    private static String defaultProvider;

    private static Logger logger = LogDomains.getLogger(PersistenceUnitInfoImpl.class, LogDomains.PERSISTENCE_LOGGER);

    private static final StringManager localStrings = StringManager.getManager(PersistenceUnitInfoImpl.class);

    private PersistenceUnitDescriptor persistenceUnitDescriptor;

    private ProviderContainerContractInfo providerContainerContractInfo;

    private File absolutePuRootFile;

    private DataSource jtaDataSource;

    private DataSource nonJtaDataSource;

    private List<URL> jarFiles;


    public PersistenceUnitInfoImpl(
            PersistenceUnitDescriptor persistenceUnitDescriptor,
            ProviderContainerContractInfo providerContainerContractInfo) {
        this.persistenceUnitDescriptor = persistenceUnitDescriptor;
        this.providerContainerContractInfo = providerContainerContractInfo;
        jarFiles = _getJarFiles();
        String jtaDataSourceName = persistenceUnitDescriptor.getJtaDataSource();
        String nonJtaDataSourceName = persistenceUnitDescriptor.getNonJtaDataSource();
        try {
            jtaDataSource = jtaDataSourceName == null ? null : providerContainerContractInfo.lookupDataSource(jtaDataSourceName);
            nonJtaDataSource = nonJtaDataSourceName == null ? null : providerContainerContractInfo.lookupNonTxDataSource(nonJtaDataSourceName);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    // Implementation of PersistenceUnitInfo interface

    /**
     * {@inheritDoc}
     */
    public String getPersistenceUnitName() {
        return persistenceUnitDescriptor.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getPersistenceProviderClassName() {
        return getPersistenceProviderClassNameForPuDesc(persistenceUnitDescriptor);
    }

    /**
     * {@inheritDoc}
     */
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.valueOf(
                persistenceUnitDescriptor.getTransactionType());
    }

    /**
     * {@inheritDoc}
     */
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    /**
     * {@inheritDoc}
     */
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public URL getPersistenceUnitRootUrl() {
        try {
            return getAbsolutePuRootFile().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getMappingFileNames() {
        return persistenceUnitDescriptor.getMappingFiles(); // its already unmodifiable
    }

    /**
     * {@inheritDoc}
     */
    public List<URL> getJarFileUrls() {
        return jarFiles;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getManagedClassNames() {
        return persistenceUnitDescriptor.getClasses(); // its already unmodifiable
    }

    public boolean excludeUnlistedClasses() {
        return persistenceUnitDescriptor.isExcludeUnlistedClasses();
    }

    public SharedCacheMode getSharedCacheMode() {
        return persistenceUnitDescriptor.getSharedCacheMode();
    }

    public ValidationMode getValidationMode() {
        return persistenceUnitDescriptor.getValidationMode();
    }

    /**
     * {@inheritDoc}
     */
    public Properties getProperties() {
        return persistenceUnitDescriptor.getProperties(); // its already a clone
    }

    public String getPersistenceXMLSchemaVersion() {
        return persistenceUnitDescriptor.getParent().getSpecVersion();
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getClassLoader() {
        return providerContainerContractInfo.getClassLoader();
    }

    /**
     * {@inheritDoc}
     */
    public void addTransformer(ClassTransformer transformer) {
        providerContainerContractInfo.addTransformer(transformer);
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getNewTempClassLoader() {
        return providerContainerContractInfo.getTempClassloader();
    }

    @Override public String toString() {
        /*
         * This method is used for debugging only.
         */
        StringBuilder result = new StringBuilder("<persistence-unit>"); // NOI18N
        result.append("\n\t<PURoot>").append(getPersistenceUnitRootUrl()).append("</PURoot>"); // NOI18N
        result.append("\n\t<name>").append(getPersistenceUnitName()).append("</name>"); // NOI18N
        result.append("\n\t<provider>").append(getPersistenceProviderClassName()).append("</provider>"); // NOI18N
        result.append("\n\t<transaction-type>").append(getTransactionType()).append("</transaction-type>"); // NOI18N
        result.append("\n\t<jta-data-source>").append(getJtaDataSource()).append("</jta-data-source>"); // NOI18N
        result.append("\n\t<non-jta-data-source>").append(getNonJtaDataSource()).append("</non-jta-data-source>"); // NOI18N
        for (URL jar : getJarFileUrls()) {
            result.append("\n\t<jar-file>").append(jar).append("</jar-file>"); // NOI18N
        }
        for (String mappingFile : getMappingFileNames()) {
            result.append("\n\t<mapping-file>").append(mappingFile).append("</mapping-file>"); // NOI18N
        }
        for (String clsName : getManagedClassNames()) {
            result.append("\n\t<class-name>").append(clsName).append("</class-name>"); // NOI18N
        }
        result.append("\n\t<exclude-unlisted-classes>").append(excludeUnlistedClasses()).append("</exclude-unlisted-classes>"); // NOI18N
        result.append("\n\t<properties>").append(getProperties()).append("</properties>"); // NOI18N
        result.append("\n\t<class-loader>").append(getClassLoader()).append("</class-loader>"); // NOI18N
        result.append("\n</persistence-unit>\n"); // NOI18N
        return result.toString();
    }

    private List<URL> _getJarFiles() {
        List<String> jarFileNames = new ArrayList<String>(
                persistenceUnitDescriptor.getJarFiles());
        List<URL> jarFiles = new ArrayList<URL>(jarFileNames.size() + 1);
        String absolutePuRoot = getAbsolutePuRootFile().getAbsolutePath();
        for (String jarFileName : jarFileNames) {
            String nativeJarFileName = jarFileName.replace('/',
                    File.separatorChar);
            final File parentFile = new File(absolutePuRoot).getParentFile();
            // only components are exploded, hence first look for original archives.
            File jarFile = new File(parentFile, nativeJarFileName);
            if (!jarFile.exists()) {
                // if the referenced jar is itself a component, then
                // it might have been exploded, hence let's see
                // if that is the case.

                // let's calculate the name component and path component from this URI
                // e.g. if URI is ../../foo_bar/my-ejb.jar,
                // name component is foo_bar/my-ejb.jar and
                // path component is ../../
                // These are my own notions used here.
                String pathComponent = "";
                String nameComponent = jarFileName;
                if(jarFileName.lastIndexOf("../") != -1) {
                    final int separatorIndex = jarFileName.lastIndexOf("../")+3;
                    pathComponent = jarFileName.substring(0,separatorIndex);
                    nameComponent = jarFileName.substring(separatorIndex);
                }
                logger.fine("For jar-file="+ jarFileName+ ", " + // NOI18N
                        "pathComponent=" +pathComponent + // NOI18N
                        ", nameComponent=" + nameComponent); // NOI18N
                File parentPath = new File(parentFile, pathComponent);

                jarFile = new File(parentPath, DeploymentUtils.
                        getRelativeEmbeddedModulePath(parentPath.
                        getAbsolutePath(), nameComponent));
            }
            if (jarFile.exists()) {
                try {
                    jarFiles.add(jarFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Should be a caught by verifier. So, just log a message
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "puinfo.referenced_jar_not_found", new Object[]{absolutePuRoot, jarFileName, jarFile});
                }
            }
        }
        return jarFiles;
    }

    private File getAbsolutePuRootFile() {
        // TODO caller of this method are _getJarFiles() and getPersitenceUnitRootUrl(). Both of them can be implemented using helper methods in PersistenceUnitDescriptor to better encapsulate
        if (absolutePuRootFile == null) {
            absolutePuRootFile = new File(providerContainerContractInfo.getApplicationLocation(),
                    getAbsolutePuRootWithinApplication().replace('/', File.separatorChar));
            if (!absolutePuRootFile.exists()) {
                throw new RuntimeException(
                        absolutePuRootFile.getAbsolutePath() + " does not exist!");
            }
        }
        return absolutePuRootFile;
    }

    /**
     * This method calculates the absolute path of the root of a PU.
     * Absolute path is not the path with regards to root of file system.
     * It is the path from the root of the Java EE application this
     * persistence unit belongs to.
     * Returned path always uses '/' as path separator.
     * @return the absolute path of the root of this persistence unit
     */
    private String getAbsolutePuRootWithinApplication() {
        // TODO shift this into PersistenceUnitDescriptor to better encapsulate
        RootDeploymentDescriptor rootDD = persistenceUnitDescriptor.getParent().
                getParent();
        String puRoot = persistenceUnitDescriptor.getPuRoot();
        if(rootDD.isApplication()){
            return puRoot;
        } else {
            ModuleDescriptor module = BundleDescriptor.class.cast(rootDD).
                    getModuleDescriptor();
            if(module.isStandalone()) {
                return puRoot;
            } else {
                // The module is embedded in an ear (an ejb jar or war)
                final String moduleLocation =        // Would point to the directory where module is expanded. For example myejb_jar
                        DeploymentUtils.getRelativeEmbeddedModulePath(
                        providerContainerContractInfo.getApplicationLocation(), module.getArchiveUri());
                return moduleLocation + '/' + puRoot; // see we always '/'
            }
        }
    }


    /**
     * This method first checks if default provider is specified in the
     * environment (e.g. using -D option in domain.xml). If so, we use that.
     * Else we defaults to EclipseLink.
     *
     * @return
     */
    public static String getDefaultprovider() {
        final String DEFAULT_PERSISTENCE_PROVIDER_PROPERTY =
                "com.sun.persistence.defaultProvider"; // NOI18N
        if(defaultProvider == null) {
            defaultProvider =
                    System.getProperty(DEFAULT_PERSISTENCE_PROVIDER_PROPERTY,
                        DEFAULT_PROVIDER_NAME);
        }

        return defaultProvider;
    }

    public static String getPersistenceProviderClassNameForPuDesc(
            PersistenceUnitDescriptor persistenceUnitDescriptor) {
        String provider = persistenceUnitDescriptor.getProvider();
        if (PersistenceUnitLoader.isNullOrEmpty(provider)) {
            provider = getDefaultprovider();
        }
        return provider;
    }

}
