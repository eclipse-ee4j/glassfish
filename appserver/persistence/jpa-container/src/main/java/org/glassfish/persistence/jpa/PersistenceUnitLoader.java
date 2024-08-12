/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.persistence.jpa;

import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.persistence.jpa.schemageneration.SchemaGenerationProcessor;
import org.glassfish.persistence.jpa.schemageneration.SchemaGenerationProcessorFactory;

import static java.util.Collections.unmodifiableMap;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;

/**
 * Loads emf corresponding to a PersistenceUnit. Executes java2db if required.
 *
 * @author Mitesh Meswani
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitLoader {

    private static Logger logger = LogDomains.getLogger(PersistenceUnitLoader.class, LogDomains.PERSISTENCE_LOGGER);

    private static final StringManager localStrings = StringManager.getManager(PersistenceUnitLoader.class);

    private static Map<String, String> integrationProperties;

    /** EclipseLink property name to enable/disable weaving **/
    private static final String ECLIPSELINK_WEAVING_PROPERTY = "eclipselink.weaving";

    /** Name of property used to specify validation mode */
    private static final String VALIDATION_MODE_PROPERTY = "jakarta.persistence.validation.mode";

    /** Name of property used to specify validator factory */
    private static final String VALIDATOR_FACTORY = "jakarta.persistence.validation.factory";

    private static final String DISABLE_UPGRADE_FROM_TOPLINK_ESSENTIALS = "org.glassfish.persistence.jpa.disable.upgrade.from.toplink.essentials";

    static {
        /*
         * We set all the provider specific integration level properties here. It knows
         * about all the integration level properties that are needed to integrate a
         * provider with our container. When we add support for other containers, we
         * should modify this code so that user does not have to specify such properties
         * in their persistence.xml file. These properties can be overriden by
         * persistence.xml as per the spec. Before applying default values for
         * properties, this method first checks if the properties have been set in the
         * system (typically done using -D option in domain.xml).
         *
         */
        // ------------------- The Base -------------------------

        Map<String, String> props = new HashMap<>();

        final String ECLIPSELINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY = "eclipselink.target-server";
        props.put(
            ECLIPSELINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY,
            System.getProperty(ECLIPSELINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY, "Glassfish"));

        // Hibernate specific properties:
        final String HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS_PROPERTY = "hibernate.transaction.manager_lookup_class";
        props.put(HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS_PROPERTY,
            System.getProperty(HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS_PROPERTY,
            "org.hibernate.transaction.SunONETransactionManagerLookup"));

        integrationProperties = unmodifiableMap(props);
    }

    /**
     * Conduit to talk with container
     */
    private final ProviderContainerContractInfo providerContainerContractInfo;

    private EntityManagerFactory entityManagerFactory;

    /**
     * The schemaGenerationProcessor instance for the Java2DB work.
     */
    private SchemaGenerationProcessor schemaGenerationProcessor;

    public PersistenceUnitLoader(PersistenceUnitDescriptor persistenceUnitToInstantiate, ProviderContainerContractInfo providerContainerContractInfo) {
        this.providerContainerContractInfo = providerContainerContractInfo;

        // A hack to work around EclipseLink issue
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=248328 for prelude
        // This should be removed once version of EclipseLink which fixes the issue is
        // integrated.
        // set the system property required by EclipseLink before we load it.
        setSystemPropertyToEnableDoPrivilegedInEclipseLink();

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(providerContainerContractInfo.getClassLoader());
        try {
            entityManagerFactory = loadPU(persistenceUnitToInstantiate);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    /**
     * @return The emf loaded.
     */
    public EntityManagerFactory getEMF() {
        return entityManagerFactory;
    }

    private void setSystemPropertyToEnableDoPrivilegedInEclipseLink() {
        final String PROPERTY_NAME = "eclipselink.security.usedoprivileged";
        // Need not invoke in doPrivileged block as the whole call stack consist of
        // trusted code when this code
        // is invoked
        if (System.getProperty(PROPERTY_NAME) == null) {
            // property not set. Set it to true
            System.setProperty(PROPERTY_NAME, String.valueOf(Boolean.TRUE));
        }
    }

    /**
     * Loads an individual PersistenceUnitDescriptor and registers the
     * EntityManagerFactory in appropriate DOL structure.
     *
     * @param persistenceUnitDescriptor PersistenceUnitDescriptor to be loaded.
     */
    private EntityManagerFactory loadPU(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        checkForUpgradeFromTopLinkEssentials(persistenceUnitDescriptor);
        checkForDataSourceOverride(persistenceUnitDescriptor);
        calculateDefaultDataSource(persistenceUnitDescriptor);

        PersistenceUnitInfo persistenceUnitInfo =
            new PersistenceUnitInfoImpl(persistenceUnitDescriptor, providerContainerContractInfo);

        String applicationLocation = providerContainerContractInfo.getApplicationLocation();
        final boolean fineMsgLoggable = logger.isLoggable(FINE);
        if (fineMsgLoggable) {
            logger.fine("Loading persistence unit for application: \"" + applicationLocation + "\"pu Root is: " + persistenceUnitDescriptor.getPuRoot());
            logger.fine("PersistenceInfo for this pud is :\n" + persistenceUnitInfo);
        }

        PersistenceProvider provider;
        try {
            // We use application CL as opposed to system CL to loadPU
            // provider. This allows user to get hold of provider specific
            // implementation classes in their code.

            // But this also means
            // provider must not use appserver implementation classes directly
            // because once we implement isolation in our class loader hierarchy
            // the only classes available to application class loader would be
            // our appserver interface classes. By Sahoo
            provider = PersistenceProvider.class
                    .cast(
                        providerContainerContractInfo.getClassLoader()
                                                     .loadClass(persistenceUnitInfo.getPersistenceProviderClassName())
                                                     .getDeclaredConstructor()
                                                     .newInstance());

        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> schemaGenerationOverrides;
        schemaGenerationProcessor = SchemaGenerationProcessorFactory.createSchemaGenerationProcessor(persistenceUnitDescriptor);
        if (providerContainerContractInfo.isJava2DBRequired()) {
            schemaGenerationProcessor.init(persistenceUnitDescriptor, providerContainerContractInfo.getDeploymentContext());
            schemaGenerationOverrides = schemaGenerationProcessor.getOverridesForSchemaGeneration();
        } else {
            // schema generation is not required if this EMF is being created for
            // -appserver restarting or,
            // -on an instance or,
            // -appclient
            // Suppress schema generation in this case
            schemaGenerationOverrides = schemaGenerationProcessor.getOverridesForSuppressingSchemaGeneration();
        }

        Map<String, Object> overRides = new HashMap<>(integrationProperties);
        if (schemaGenerationOverrides != null) {
            overRides.putAll(schemaGenerationOverrides);
        }

        // Check if the persistence unit requires Bean Validation
        ValidationMode validationMode = getValidationMode(persistenceUnitDescriptor);
        if (validationMode == ValidationMode.AUTO || validationMode == ValidationMode.CALLBACK) {
            overRides.put(VALIDATOR_FACTORY, providerContainerContractInfo.getValidatorFactory());
        }

        if (!providerContainerContractInfo.isWeavingEnabled()) {
            overRides.put(ECLIPSELINK_WEAVING_PROPERTY, System.getProperty(ECLIPSELINK_WEAVING_PROPERTY, "false"));
        }

        EntityManagerFactory entityManagerFactory = provider.createContainerEntityManagerFactory(persistenceUnitInfo, overRides);

        logger.logp(FINE, "PersistenceUnitLoader", "loadPU", "emf = {0}", entityManagerFactory);

        PersistenceUnitsDescriptor parent = persistenceUnitDescriptor.getParent();
        RootDeploymentDescriptor containingBundle = parent.getParent();
        providerContainerContractInfo.registerEMF(
            persistenceUnitInfo.getPersistenceUnitName(), persistenceUnitDescriptor.getPuRoot(), containingBundle, entityManagerFactory);

        if (fineMsgLoggable) {
            logger.fine("Finished loading persistence unit for application: " +
                    applicationLocation);
        }

        return entityManagerFactory;
    }

    /**
     * If use provided data source is overridden, update PersistenceUnitDescriptor
     * with it
     */
    private void checkForDataSourceOverride(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        SimpleJndiName jtaDataSourceOverride = providerContainerContractInfo.getJTADataSourceOverride();
        if (jtaDataSourceOverride != null) {
            persistenceUnitDescriptor.setJtaDataSource(jtaDataSourceOverride);
        }
    }

    /** Calculate and set the default data source in given <code>pud</code> **/
    private void calculateDefaultDataSource(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        SimpleJndiName jtaDataSourceName =
            calculateJtaDataSourceName(
                persistenceUnitDescriptor.getTransactionType(),
                persistenceUnitDescriptor.getJtaDataSource(),
                persistenceUnitDescriptor.getNonJtaDataSource(),
                persistenceUnitDescriptor.getName());

        SimpleJndiName nonJtaDataSourceName =
            calculateNonJtaDataSourceName(
                persistenceUnitDescriptor.getJtaDataSource(),
                persistenceUnitDescriptor.getNonJtaDataSource());

        persistenceUnitDescriptor.setJtaDataSource(jtaDataSourceName);
        persistenceUnitDescriptor.setNonJtaDataSource(nonJtaDataSourceName);
    }

    /**
     * @return DataSource Name to be used as JTA data source.
     */
    private SimpleJndiName calculateJtaDataSourceName(String transactionType, SimpleJndiName userSuppliedJTADSName,
        SimpleJndiName userSuppliedNonJTADSName, String puName) {
        /*
         * Use DEFAULT_DS_NAME iff user has not specified both jta-ds-name and
         * non-jta-ds-name; and user has specified transaction-type as JTA. See Gf issue
         * #1204 as well.
         */
        if (PersistenceUnitTransactionType.valueOf(transactionType) != PersistenceUnitTransactionType.JTA) {
            logger.logp(FINE,
                "PersistenceUnitInfoImpl",
                 "_getJtaDataSource",
                 "This PU is configured as non-jta, so jta-data-source is null");
            return null; // this is a non-jta-data-source
        }

        SimpleJndiName dataSourceName;
        if (!isNullOrEmpty(userSuppliedJTADSName)) {
            dataSourceName = userSuppliedJTADSName; // use user supplied jta-ds-name
        } else if (isNullOrEmpty(userSuppliedNonJTADSName)) {
            dataSourceName = providerContainerContractInfo.getDefaultDataSourceName();
        } else {
            String msg = localStrings.getString("puinfo.jta-ds-not-configured",
                    new Object[] { puName });
            throw new RuntimeException(msg);
        }

        logger.logp(FINE, "PersistenceUnitLoaderImpl",
                "_getJtaDataSource",
                "JTADSName = {0}",
                dataSourceName);

        return dataSourceName;
    }

    private SimpleJndiName calculateNonJtaDataSourceName(SimpleJndiName userSuppliedJTADSName, SimpleJndiName userSuppliedNonJTADSName) {
        /*
         * If non-JTA name is *not* provided - use the JTA DS name (if supplied) If
         * non-JTA name is provided - use non-JTA DS name (this is done for ease of use,
         * because user does not have to explicitly mark a connection pool as
         * non-transactional. Calling lookupNonTxDataSource() with a resource which is
         * already configured as non-transactional has no side effects.) If neither
         * non-JTA nor JTA name is provided use DEFAULT_DS_NAME.
         */
        SimpleJndiName dataSourceName;
        if (!isNullOrEmpty(userSuppliedNonJTADSName)) {
            dataSourceName = userSuppliedNonJTADSName;
        } else {
            if (!isNullOrEmpty(userSuppliedJTADSName)) {
                dataSourceName = userSuppliedJTADSName;
            } else {
                dataSourceName = providerContainerContractInfo.getDefaultDataSourceName();
            }
        }
        logger.logp(FINE, "PersistenceUnitInfoImpl",
                "_getNonJtaDataSource", "nonJTADSName = {0}",
                dataSourceName);

        return dataSourceName;
    }

    private static boolean isNullOrEmpty(SimpleJndiName s) {
        return s == null || s.isEmpty();
    }

    /**
     * If the app is using Toplink Essentials as the provider and TopLink Essentials
     * is not available in classpath We try to upgrade the app to use EclipseLink.
     * Change the provider to EclipseLink and translate "toplink.*" properties to
     * "eclipselink.*" properties
     */
    private void checkForUpgradeFromTopLinkEssentials(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        if (Boolean.getBoolean(DISABLE_UPGRADE_FROM_TOPLINK_ESSENTIALS)) {
            // Return if instructed by System property
            return;
        }

        boolean upgradeTopLinkEssentialsProperties = false;
        String providerClassName = persistenceUnitDescriptor.getProvider();

        if (providerClassName == null || providerClassName.isEmpty()) {
            // This might be a JavaEE app running against V2 and relying in provider name
            // being defaulted.
            upgradeTopLinkEssentialsProperties = true;
        } else if ("oracle.toplink.essentials.PersistenceProvider".equals(providerClassName)
                || "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider".equals(providerClassName)) {
            try {
                providerContainerContractInfo.getClassLoader().loadClass(providerClassName);
            } catch (ClassNotFoundException e) {
                // Toplink Essentials classes are not available to an application using it as
                // the provider
                // Migrate the application to use EclipseLink

                String defaultProvider = PersistenceUnitInfoImpl.getDefaultprovider();
                if (logger.isLoggable(INFO)) {
                    logger.log(INFO, "puloader.defaulting.provider.on.upgrade", new Object[] { persistenceUnitDescriptor.getName(), defaultProvider });
                }

                // Change the provider name
                persistenceUnitDescriptor.setProvider(defaultProvider);
                upgradeTopLinkEssentialsProperties = true;
            }
        }

        if (upgradeTopLinkEssentialsProperties) {
            // For each "toplink*" property, add a "eclipselink* property
            final String TOPLINK = "toplink";
            final String ECLIPSELINK = "eclipselink";
            Properties properties = persistenceUnitDescriptor.getProperties();
            for (Entry<Object, Object> entry : properties.entrySet()) {
                String key = (String) entry.getKey();
                if (key.startsWith(TOPLINK)) {
                    String translatedKey = ECLIPSELINK + key.substring(TOPLINK.length());
                    persistenceUnitDescriptor.addProperty(translatedKey, entry.getValue());
                }
            }
        }
    }

    /**
     * Called during load when the correct classloader and transformer had been
     * already set. For emf that require Java2DB, call createEntityManager() to
     * populate the DDL files, then iterate over those files and execute each line
     * in them.
     */
    void doJava2DB() {
        if (schemaGenerationProcessor.isContainerDDLExecutionRequired()) {
            logger.fine("<--- To Create Tables");

            schemaGenerationProcessor.executeCreateDDL();

            logger.fine("---> Done Create Tables");
        }
    }

    private ValidationMode getValidationMode(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        // Initialize with value element <validation-mode> in persitence.xml
        ValidationMode validationMode = persistenceUnitDescriptor.getValidationMode();

        // Check is overridden in properties
        String validationModeFromProperty = (String) persistenceUnitDescriptor.getProperties().get(VALIDATION_MODE_PROPERTY);
        if (validationModeFromProperty != null) {
            // User would get IllegalArgumentException if he has specified invalid mode
            validationMode = ValidationMode.valueOf(validationModeFromProperty);
        }

        return validationMode;
    }
}
