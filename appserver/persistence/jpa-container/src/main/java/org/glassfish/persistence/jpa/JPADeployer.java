/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.persistence.common.Java2DBProcessorHelper;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;

import static java.util.Collections.emptyList;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_DISABLED;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_PREPARED;

/**
 * Deployer for JPA applications
 *
 * @author Mitesh Meswani
 */
@Service
public class JPADeployer extends SimpleDeployer<JPAContainer, JPApplicationContainer> implements PostConstruct, EventListener {

    private static Logger logger = LogDomains.getLogger(PersistenceUnitLoader.class, LogDomains.PERSISTENCE_LOGGER + ".jpadeployer");

    /** Key used to get/put emflists in transientAppMetadata */
    private static final String ENTITY_MANAGER_FACTORY_KEY = EntityManagerFactory.class.toString();

    @Inject
    private ConnectorRuntime connectorRuntime;

    @Inject
    private ServerEnvironmentImpl serverEnvironment;

    @Inject
    private volatile StartupContext startupContext;

    @Inject
    private Events events;

    @Inject
    private ApplicationRegistry applicationRegistry;

    @Override
    public void postConstruct() {
        events.register(this);
    }

    @Override
    public MetaData getMetaData() {
        return new MetaData(
            true /* invalidateCL */,
            null /* provides */,
            new Class[] { Application.class } /* requires Application from dol */);
    }

    /**
     * EntityManagerFactories for persistence units are created and stored in JPAApplication instance. The
     * JPAApplication instance is stored in given DeploymentContext to be retrieved
     * by load
     */
    @Override
    public boolean prepare(DeploymentContext deploymentContext) {
        boolean prepared = super.prepare(deploymentContext);

        if (prepared && isEntityManagerFactoryCreationRequired(deploymentContext)) {
            createEntityManagerFactories(deploymentContext);
        }

        return prepared;
    }

    @Override
    public JPApplicationContainer load(JPAContainer container, DeploymentContext context) {
        return new JPApplicationContainer();
    }

    @Override
    protected void cleanArtifacts(DeploymentContext deploymentContext) throws DeploymentException {
        // Drop tables if needed on undeploy.
        OpsParams params = deploymentContext.getCommandParameters(OpsParams.class);
        if (params.origin.isUndeploy() && isDas()) {

            Application application = applicationRegistry.get(params.name()).getMetaData(Application.class);

            boolean hasScopedResource = hasScopedResource(application);

            // If there are scoped resources, deploy them so that they are accessible for
            // Java2DB to delete tables.
            if (hasScopedResource) {
                connectorRuntime.registerDataSourceDefinitions(application);
            }

            Java2DBProcessorHelper java2DBProcessor = new Java2DBProcessorHelper(deploymentContext);
            java2DBProcessor.init();
            java2DBProcessor.createOrDropTablesInDB(false, "JPA");

            // If there are scoped resources, undeploy them.
            if (hasScopedResource) {
                connectorRuntime.unRegisterDataSourceDefinitions(application);
            }
        }
    }

    @Override
    public void event(Event<?> event) {
        logger.log(FINE, () -> "JpaDeployer.event():" + event.name());

        if (event.is(APPLICATION_PREPARED)) {
            ExtendedDeploymentContext context = (ExtendedDeploymentContext) event.hook();
            DeployCommandParameters deployCommandParameters = context.getCommandParameters(DeployCommandParameters.class);

            logger.log(FINE, () -> "JpaDeployer.event(): Handling APPLICATION_PREPARED origin is:" + deployCommandParameters.origin);

            // When create-application-ref is called for an already deployed application,
            // APPLICATION_PREPARED will be sent on DAS.
            //
            // Obviously there is no new EntityManagerFactory created for this event and we need not do
            // java2db also. Ignore the event.
            //
            // However, if target for create-application-ref is DAS => the app was deployed
            // on other instance but now/ an application-ref is being created on DAS. Process the app.
            if (!deployCommandParameters.origin.isCreateAppRef() || isTargetDas(deployCommandParameters)) {

                // Bundle level persistence units
                for (DeploymentContext deploymentContext : context.getModuleDeploymentContexts().values()) {
                    iterateInitializedPUsAtApplicationPrepare(deploymentContext);
                }

                // Application level persistence unit
                iterateInitializedPUsAtApplicationPrepare(context);
            }
        } else if (event.is(APPLICATION_DISABLED)) {
            logger.fine("JpaDeployer.event(): APPLICATION_DISABLED");

            // APPLICATION_DISABLED will be generated when an application is
            // disabled or undeployed, or the appserver goes down.
            //
            // Close all the EntityManagerFactories created for this application.
            closeEntityManagerFactories((ApplicationInfo) event.hook());
        }
    }



    // ### Private methods



    /**
     * @param context
     * @return true if EntityManagerFactory creation is required false otherwise
     */
    private boolean isEntityManagerFactoryCreationRequired(DeploymentContext context) {
        /*
         * Here are various use cases that needs to be handled. This method handles
         * EntityManagerFactory creation part, APPLICATION_PREPARED event handle handles
         * java2db and closing of EntityManagerFactory
         *
         * To summarize, -Unconditionally create EMFs on DAS for java2db if it is
         * deploy. We will close this EMF in APPLICATION_PREPARED after java2db if
         * (target!= DAS || enable=false) -We will not create EMFs on instance if
         * application is not enabled
         *
         * -----------------------------------------------------------------------------
         * ------- Scenario Expected Behavior
         * -----------------------------------------------------------------------------
         * ------- deploy --target=server --enabled=true. DAS(EMF created, java2db, EMF
         * remains open) -restart DAS(EMF created, EMF remains open) -undeploy DAS(EMF
         * closed. Drop tables) -create-application-ref instance1 DAS(No action)
         * INSTANCE1(EMF created)
         *
         * deploy --target=server --enabled=false. DAS(EMF created,java2db, EMF closed
         * in APPLICATION_PREPARED) -restart DAS(No EMF created) -undeploy DAS(No EMF to
         * close, Drop tables)
         *
         * -enable DAS(EMF created) -undelpoy DAS(EMF closed, Drop tables)
         *
         * -create-application-ref instance1 DAS(No action) INSTANCE1(EMF created)
         *
         * deploy --target=instance1 --enabled=true DAS(EMF created, java2db, EMF closed
         * in APPLICATION_PREPARED) INSTANCE1(EMF created) -create-application-ref
         * instance2 INSTANCE2(EMF created) -restart DAS(No EMF created) INSTANCE1(EMF
         * created) INSTANCE2(EMF created) -undeploy DAS(No EMF to close, Drop tables)
         * INSTANCE1(EMF closed)
         *
         * -create-application-ref server DAS(EMF created) -delete-application-ref
         * server DAS(EMF closed) undeploy INSTANCE1(EMF closed)
         *
         *
         * deploy --target=instance --enabled=false. DAS(EMF created, java2db, EMF
         * closed in APPLICATION_PREPARED) INSTANCE1(No EMF created)
         * -create-application-ref instance2 DAS(No action) INSTANCE2(No Action)
         * -restart DAS(No EMF created) INSTANCE1(No EMF created) INSTANCE2(No EMF
         * created) -undeploy DAS(No EMF to close, Drop tables) INSTANCE1(No EMF to
         * close) INSTANCE2(No EMF to close)
         *
         * -enable --target=instance1 DAS(No EMF created) INSTANCE1(EMF created)
         *
         */

        boolean createEntityManagerFactories = false;
        DeployCommandParameters deployCommandParameters = context.getCommandParameters(DeployCommandParameters.class);
        boolean deploy = deployCommandParameters.origin.isDeploy();
        boolean enabled = deployCommandParameters.enabled;
        boolean isDas = isDas();

        logger.log(FINER, () -> "isEMFCreationRequired(): deploy: " + deploy + " enabled: " + enabled + " isDas: " + isDas);

        if (isDas) {
            if (deploy) {
                // Always create emfs on DAS while deploying to take care of java2db and PU
                // validation on deploy
                createEntityManagerFactories = true;
            } else {
                // We reach here for (!deploy && das) => server restart or enabling a disabled
                // app on DAS
                boolean isTargetDas = isTargetDas(deployCommandParameters);
                logger.log(FINER, () -> "isEMFCreationRequired(): isTargetDas: " + isTargetDas);

                if (enabled && isTargetDas) {
                    createEntityManagerFactories = true;
                }
            }
        } else { // !das => on an instance
            if (enabled) {
                createEntityManagerFactories = true;
            }
        }

        if (logger.isLoggable(FINER)) {
            logger.finer("isEMFCreationRequired(): returning createEMFs:" + createEntityManagerFactories);
        }

        return createEntityManagerFactories;
    }

    /**
     * Returns unique identifier for this persistence unit within the application.
     *
     * @param persistenceUnitDescriptor The given persistence unit
     * @return Absolute persistence unit root + persistence unit name
     */
    private static String getUniquePersistenceUnitIdentifier(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        return
            persistenceUnitDescriptor.getAbsolutePuRoot() +
            persistenceUnitDescriptor.getName();
    }

    private static boolean isTargetDas(DeployCommandParameters deployCommandParameters) {
        return "server".equals(deployCommandParameters.target);
    }

    private boolean isDas() {
        return serverEnvironment.isDas() || serverEnvironment.isEmbedded();
    }

    /**
     * Create Entity Manager Factories and save them in persistence.
     *
     * @param deploymentContext
     */
    private void createEntityManagerFactories(DeploymentContext deploymentContext) {

        // EntityManager etc can be injected using CDI as well. Since CDI has the ability to programmatically
        // look up beans, we can't check whether the persistence unit is actually referenced as a previous
        // optimisation did.
        Application application = deploymentContext.getModuleMetaData(Application.class);
        if (hasScopedResource(application)) {
            // Scoped resources are registered by connector runtime after prepare().
            // That is too late for Jakarta Persistence.
            //
            // This is a hack to initialize connectorRuntime for scoped resources
            connectorRuntime.registerDataSourceDefinitions(application);
        }

        // Iterate through all the processPersistenceUnitDescriptors for this bundle and load the corresponding persistence unit
        for (var persistenceUnitDescriptor : getPersistenceUnitDescriptors(deploymentContext)) {
            loadPersistenceUnit(persistenceUnitDescriptor, deploymentContext);
        }
    }

    private boolean hasScopedResource(Application application) {
        for (var bundleDescriptor : application.getBundleDescriptors()) {

            Set<PersistenceUnitDescriptor> allPersistenceUnitDescriptors = new HashSet<>();

            // Add persistenceUnitDescriptors from the current bundle
            allPersistenceUnitDescriptors.addAll(getPersistenceUnitDescriptors(bundleDescriptor));

            // Find (obscure) persistence units referenced by @PersistenceContext annotations that reside
            // in places like ear/lib.
            //
            // E.g.
            // @PersistenceContext(
            //     unitName="lib/ejb-ejb30-persistence-tx_propagation-par1.jar#em",
            //     type=EXTENDED);
            // EntityManager extendedEM;
            //
            // The above calls to getPersistenceUnitDescriptors() won't find these.
            //
            // Alternatively we can use:
            //
            // application.getExtensionsDescriptors(PersistenceUnitsDescriptor.class)
            allPersistenceUnitDescriptors.addAll(bundleDescriptor.findReferencedPUs());

            for (var persistenceUnitDescriptor : allPersistenceUnitDescriptors) {
                if (hasScopedResource(persistenceUnitDescriptor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return true if given <code>persistenceUnitDescriptor</code> is using scoped resource
     */
    private boolean hasScopedResource(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        SimpleJndiName jtaDataSource = persistenceUnitDescriptor.getJtaDataSource();

        return jtaDataSource != null && jtaDataSource.hasJavaPrefix();
    }

    private static List<PersistenceUnitDescriptor> getPersistenceUnitDescriptors(DeploymentContext context) {
        BundleDescriptor bundle = DOLUtils.getCurrentBundleForContext(context);
        if (bundle == null) {
            // It can be null for non-Jakarta EE type of application deployment. e.g., issue 15869
            return emptyList();
        }

        return getPersistenceUnitDescriptors(bundle);
    }

    private static List<PersistenceUnitDescriptor> getPersistenceUnitDescriptors(BundleDescriptor bundle) {
        List<PersistenceUnitDescriptor> persistenceUnitDescriptors = new ArrayList<>();

        for (PersistenceUnitsDescriptor persistenceUnitsDescriptor : bundle.getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
            persistenceUnitDescriptors.addAll(persistenceUnitsDescriptor.getPersistenceUnitDescriptors());
        }

        return persistenceUnitDescriptors;
    }

    private void loadPersistenceUnit(PersistenceUnitDescriptor persistenceUnitDescriptor, DeploymentContext deploymentContext) {
        boolean isDas = isDas();

        // While running in embedded mode, it is not possible to guarantee that entity
        // classes are not loaded by the app classloader before transformers are
        // installed.
        //
        // If that happens, weaving will not take place and EclipseLink will throw up.
        // Provide users an option to disable weaving by passing the flag.
        // Note that we enable weaving if not explicitly disabled by user
        boolean weavingEnabled = Boolean
                .parseBoolean(startupContext.getArguments().getProperty("org.glassfish.persistence.embedded.weaving.enabled", "true"));

        ProviderContainerContractInfo providerContainerContractInfo = weavingEnabled
                ? new ServerProviderContainerContractInfo(deploymentContext, connectorRuntime, isDas)
                : new EmbeddedProviderContainerContractInfo(deploymentContext, connectorRuntime, isDas);

        try {
            ((ExtendedDeploymentContext) deploymentContext).prepareScratchDirs();
        } catch (IOException e) {
            // There is no way to recover if we are not able to create the scratch dirs.
            // Just rethrow the exception.
            throw new RuntimeException(e);
        }

        // Instantiating the PersistenceUnitLoader instance has the side-effect of actually loading the persistence unit
        PersistenceUnitLoader persistenceUnitLoader = new PersistenceUnitLoader(persistenceUnitDescriptor, providerContainerContractInfo);

        // Store the persistenceUnitLoader in context.
        //
        // It is retrieved to execute java2db and to store the loaded entity manager factories in a
        // JPAApplicationContainer object for cleanup
        deploymentContext.addTransientAppMetaData(
            getUniquePersistenceUnitIdentifier(persistenceUnitDescriptor),
            persistenceUnitLoader);
    }

    /**
     * Does java2db (schema generation) on DAS and saves entity manager factories created during prepare to ApplicationInfo
     * maintained by DOL.
     *
     * <p>
     * ApplicationInfo is not available during prepare() so we can not directly use it there.
     *
     * @param context
     */
    private void iterateInitializedPUsAtApplicationPrepare(final DeploymentContext context) {

        // PersistenceUnitsDescriptor corresponds to persistence.xml.
        // A bundle can only have one persistence.xml except/ when the bundle is an application
        // which can have multiple persitence.xml under jars in root of ear and lib.

        for (var descriptor : getPersistenceUnitDescriptors(context)) {
            doPersistenceSchemaGeneration(descriptor, context);
        }
    }

    private void doPersistenceSchemaGeneration(PersistenceUnitDescriptor persistenceUnitDescriptor, DeploymentContext deploymentContext) {

        // Get back the loader that we stored in loadPersistenceUnit()
        PersistenceUnitLoader persistenceUnitLoader =
            deploymentContext.getTransientAppMetaData(
                                getUniquePersistenceUnitIdentifier(persistenceUnitDescriptor),
                                PersistenceUnitLoader.class);

        if (persistenceUnitLoader == null) {
            return;
        }

        // We have initialized persistence unit

        boolean saveEntityManagerFactory = true;

        DeployCommandParameters deployCommandParameters = deploymentContext.getCommandParameters(DeployCommandParameters.class);

        // We do validation and execute Java2DB only on DAS
        if (isDas()) {

            // APPLICATION_PREPARED will be called for create-application-ref
            // also. We should perform java2db only on first deploy
            if (deployCommandParameters.origin.isDeploy()) {

                EntityManagerFactory entityManagerFactory = persistenceUnitLoader.getEntityManagerFactory();

                // Create EntityManager to trigger validation on persistence unit which is lazily performed by the
                // provider.
                //
                // Entity manager creation also triggers DDL generation by provider.
                try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {

                } catch (PersistenceException e) {
                    // Exception indicates something went wrong while performing validation. Clean
                    // up and rethrow to fail deployment
                    entityManagerFactory.close();

                    // Need to wrap exception in DeploymentException else deployment will not fail.
                    throw new DeploymentException(e);
                }

                persistenceUnitLoader.doJava2DB();

                boolean enabled = deployCommandParameters.enabled;
                boolean isTargetDas = isTargetDas(deployCommandParameters);

                logger.log(FINER, () ->
                    "iterateInitializedPUsAtApplicationPrepare(): enabled: " + enabled + " isTargetDas: " + isTargetDas);

                if (!isTargetDas || !enabled) {
                    // We are on DAS but target != das or app is not enabled on das => The entityManagerFactory was
                    // only created for Java2Db. Close it.
                    entityManagerFactory.close();
                    saveEntityManagerFactory = false; // Do not save EMF. We have already closed it
                }
            }
        }

        if (saveEntityManagerFactory) {
            saveEntityManagerFactory(
                applicationRegistry.get(deployCommandParameters.name),
                persistenceUnitLoader.getEntityManagerFactory());
        }
    }

    /**
     * Save EntityManagerFactory in ApplicationInfo so that it can be retrieved and closed for cleanup.
     *
     * @param applicationInfo
     * @param entityManagerFactory
     */
    private void saveEntityManagerFactory(ApplicationInfo applicationInfo, EntityManagerFactory entityManagerFactory) {
        @SuppressWarnings("unchecked")
        List<EntityManagerFactory> emfsCreatedForThisApp = applicationInfo.getTransientAppMetaData(ENTITY_MANAGER_FACTORY_KEY, List.class);

        if (emfsCreatedForThisApp == null) {
            // First EntityManagerFactory for this app, initialize
            emfsCreatedForThisApp = new ArrayList<>();
            applicationInfo.addTransientAppMetaData(ENTITY_MANAGER_FACTORY_KEY, emfsCreatedForThisApp);
        }

        emfsCreatedForThisApp.add(entityManagerFactory);
    }

    private void closeEntityManagerFactories(ApplicationInfo appInfo) {
        @SuppressWarnings("unchecked")
        List<EntityManagerFactory> entityManagerFactoriesCreatedForThisApp =
            appInfo.getTransientAppMetaData(ENTITY_MANAGER_FACTORY_KEY, List.class);

        if (entityManagerFactoriesCreatedForThisApp != null) {

            // Events are always dispatched to all registered listeners.
            // emfsCreatedForThisApp will be null for an app that does not have PUs.

            for (EntityManagerFactory entityManagerFactory : entityManagerFactoriesCreatedForThisApp) {
                entityManagerFactory.close();
            }

            // We no longer have the entity manager factories in open state clear the list.
            //
            // On app enable(after a disable), for a cluster, the deployment framework calls
            // prepare() for instances but not for DAS.
            // So on DAS, at a disable, the entity manager factories will be closed and we will not attempt to
            // close entity manager factories when appserver goes down even if the app is re-enabled.
            entityManagerFactoriesCreatedForThisApp.clear();
        }
    }

}
