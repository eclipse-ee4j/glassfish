/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import com.sun.logging.LogDomains;

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

import static java.util.logging.Level.WARNING;
import static org.glassfish.deployment.common.DeploymentUtils.getRelativeEmbeddedModulePath;

/**
 * This class implements {@link PersistenceUnitInfo} interface.
 *
 * This class is public because it is used in verifier
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

    private static Logger logger = LogDomains.getLogger(PersistenceUnitInfoImpl.class, LogDomains.PERSISTENCE_LOGGER);

    private static final String DEFAULT_PROVIDER_NAME = "org.eclipse.persistence.jpa.PersistenceProvider";

    // We allow the default provider to be specified using -D option.
    private static String defaultProvider;

    private final PersistenceUnitDescriptor persistenceUnitDescriptor;
    private final ProviderContainerContractInfo providerContainerContractInfo;
    private File absolutePuRootFile;
    private DataSource jtaDataSource;
    private DataSource nonJtaDataSource;
    private final List<URL> jarFiles;

    public PersistenceUnitInfoImpl(PersistenceUnitDescriptor persistenceUnitDescriptor, ProviderContainerContractInfo providerContainerContractInfo) {
        this.persistenceUnitDescriptor = persistenceUnitDescriptor;
        this.providerContainerContractInfo = providerContainerContractInfo;
        jarFiles = _getJarFiles();
        SimpleJndiName jtaDataSourceName = persistenceUnitDescriptor.getJtaDataSource();
        SimpleJndiName nonJtaDataSourceName = persistenceUnitDescriptor.getNonJtaDataSource();
        try {
            jtaDataSource = jtaDataSourceName == null ? null
                : providerContainerContractInfo.lookupDataSource(jtaDataSourceName);
            nonJtaDataSource = nonJtaDataSourceName == null ? null
                : providerContainerContractInfo.lookupNonTxDataSource(nonJtaDataSourceName);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    // Implementation of PersistenceUnitInfo interface

    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitDescriptor.getName();
    }

    @Override
    public String getPersistenceProviderClassName() {
        return getPersistenceProviderClassNameForPuDesc(persistenceUnitDescriptor);
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.valueOf(persistenceUnitDescriptor.getTransactionType());
    }

    @Override
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        try {
            return getAbsolutePuRootFile().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getMappingFileNames() {
        return persistenceUnitDescriptor.getMappingFiles(); // its already unmodifiable
    }

    @Override
    public List<URL> getJarFileUrls() {
        return jarFiles;
    }

    @Override
    public List<String> getManagedClassNames() {
        return persistenceUnitDescriptor.getClasses(); // its already unmodifiable
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return persistenceUnitDescriptor.isExcludeUnlistedClasses();
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return persistenceUnitDescriptor.getSharedCacheMode();
    }

    @Override
    public ValidationMode getValidationMode() {
        return persistenceUnitDescriptor.getValidationMode();
    }

    @Override
    public Properties getProperties() {
        return persistenceUnitDescriptor.getProperties(); // its already a clone
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return persistenceUnitDescriptor.getParent().getSpecVersion();
    }

    @Override
    public ClassLoader getClassLoader() {
        return providerContainerContractInfo.getClassLoader();
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
        providerContainerContractInfo.addTransformer(transformer);
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return providerContainerContractInfo.getTempClassloader();
    }

    @Override
    public String toString() {
        /*
         * This method is used for debugging only.
         */
        StringBuilder result = new StringBuilder("<persistence-unit>");
        result.append("\n\t<PURoot>").append(getPersistenceUnitRootUrl()).append("</PURoot>");
        result.append("\n\t<name>").append(getPersistenceUnitName()).append("</name>");
        result.append("\n\t<provider>").append(getPersistenceProviderClassName()).append("</provider>");
        result.append("\n\t<transaction-type>").append(getTransactionType()).append("</transaction-type>");
        result.append("\n\t<jta-data-source>").append(getJtaDataSource()).append("</jta-data-source>");
        result.append("\n\t<non-jta-data-source>").append(getNonJtaDataSource()).append("</non-jta-data-source>");
        for (URL jar : getJarFileUrls()) {
            result.append("\n\t<jar-file>").append(jar).append("</jar-file>");
        }
        for (String mappingFile : getMappingFileNames()) {
            result.append("\n\t<mapping-file>").append(mappingFile).append("</mapping-file>");
        }
        for (String clsName : getManagedClassNames()) {
            result.append("\n\t<class-name>").append(clsName).append("</class-name>");
        }
        result.append("\n\t<exclude-unlisted-classes>").append(excludeUnlistedClasses()).append("</exclude-unlisted-classes>");
        result.append("\n\t<properties>").append(getProperties()).append("</properties>");
        result.append("\n\t<class-loader>").append(getClassLoader()).append("</class-loader>");
        result.append("\n</persistence-unit>\n");
        return result.toString();
    }

    private List<URL> _getJarFiles() {
        List<String> jarFileNames = new ArrayList<>(persistenceUnitDescriptor.getJarFiles());
        List<URL> jarFiles = new ArrayList<>(jarFileNames.size() + 1);
        String absolutePuRoot = getAbsolutePuRootFile().getAbsolutePath();
        for (String jarFileName : jarFileNames) {
            String nativeJarFileName = jarFileName.replace('/', File.separatorChar);
            final File parentFile = new File(absolutePuRoot).getParentFile();

            // Only components are exploded, hence first look for original archives.
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
                if (jarFileName.lastIndexOf("../") != -1) {
                    final int separatorIndex = jarFileName.lastIndexOf("../") + 3;
                    pathComponent = jarFileName.substring(0, separatorIndex);
                    nameComponent = jarFileName.substring(separatorIndex);
                }
                logger.fine("For jar-file=" + jarFileName + ", " + "pathComponent=" + pathComponent + ", nameComponent=" + nameComponent);
                File parentPath = new File(parentFile, pathComponent);

                jarFile = new File(parentPath, DeploymentUtils.getRelativeEmbeddedModulePath(parentPath.getAbsolutePath(), nameComponent));
            }

            if (jarFile.exists()) {
                try {
                    jarFiles.add(jarFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Should be a caught by verifier. So, just log a message
                if (logger.isLoggable(WARNING)) {
                    logger.log(WARNING, "puinfo.referenced_jar_not_found", new Object[] { absolutePuRoot, jarFileName, jarFile });
                }
            }
        }

        return jarFiles;
    }

    private File getAbsolutePuRootFile() {
        // TODO caller of this method are _getJarFiles() and getPersitenceUnitRootUrl().
        // Both of them can be implemented using helper methods in
        // PersistenceUnitDescriptor to better encapsulate
        if (absolutePuRootFile == null) {
            absolutePuRootFile = new File(providerContainerContractInfo.getApplicationLocation(),
                    getAbsolutePuRootWithinApplication().replace('/', File.separatorChar));
            if (!absolutePuRootFile.exists()) {
                throw new RuntimeException(absolutePuRootFile.getAbsolutePath() + " does not exist!");
            }
        }

        return absolutePuRootFile;
    }

    /**
     * This method calculates the absolute path of the root of a PU. Absolute path
     * is not the path with regards to root of file system. It is the path from the
     * root of the Jakarta EE application this persistence unit belongs to. Returned
     * path always uses '/' as path separator.
     *
     * @return the absolute path of the root of this persistence unit
     */
    private String getAbsolutePuRootWithinApplication() {
        // TODO shift this into PersistenceUnitDescriptor to better encapsulate
        RootDeploymentDescriptor rootDD = persistenceUnitDescriptor.getParent().getParent();
        String persistenceUnitRoot = persistenceUnitDescriptor.getPuRoot();
        if (rootDD.isApplication()) {
            return persistenceUnitRoot;
        }

        ModuleDescriptor<?> module = BundleDescriptor.class.cast(rootDD).getModuleDescriptor();
        if (module.isStandalone()) {
            return persistenceUnitRoot;
        }

        // The module is embedded in an ear (an ejb jar or war)
        final String moduleLocation = // Would point to the directory where module is expanded. For example myejb_jar
                getRelativeEmbeddedModulePath(
                    providerContainerContractInfo.getApplicationLocation(),
                    module.getArchiveUri());

        return moduleLocation + '/' + persistenceUnitRoot; // see we always '/'
    }

    /**
     * This method first checks if default provider is specified in the environment
     * (e.g. using -D option in domain.xml). If so, we use that. Else we defaults to
     * EclipseLink.
     *
     * @return
     */
    public static String getDefaultprovider() {
        if (defaultProvider == null) {
            defaultProvider = System.getProperty("com.sun.persistence.defaultProvider", DEFAULT_PROVIDER_NAME);
        }

        return defaultProvider;
    }

    public static String getPersistenceProviderClassNameForPuDesc(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        String provider = persistenceUnitDescriptor.getProvider();
        if (provider == null || provider.isEmpty()) {
            provider = getDefaultprovider();
        }

        return provider;
    }

    // TODO IMPLEMENT!

    @Override
    public String getScopeAnnotationName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getQualifierAnnotationNames() {
        // TODO Auto-generated method stub
        return null;
    }

}
