/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.data;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.hk2.api.DescriptorFileFinder;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.bootstrap.HK2Populator;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.DeploymentTracing;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.config.TransactionFailure;

import static org.glassfish.internal.deployment.Deployment.APPLICATION_STOPPED;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_UNLOADED;

/**
 * Information about a running application. Applications are composed of modules. Modules run in an individual
 * container.
 *
 * @author Jerome Dochez
 */
public class ApplicationInfo extends ModuleInfo {
    /** The prefix that will be given to the service locator, followed by the name of the application */
    public final static String APP_SERVICE_LOCATOR_PREFIX = "JavaEEApp.";

    private final static String APPLICATION_LOADER_FILES = DescriptorFileFinder.RESOURCE_BASE + "application";
    private final static String WEB_LOADER_FILES = "hk2-locator/application";

    final private Collection<ModuleInfo> modules = new ArrayList<>();

    // The reversed modules contain the same elements as modules but just in
    // reversed order, they are used when stopping/unloading the application.
    // The modules should be stopped/unloaded in the reverse order of what
    // they were originally loaded/started.
    final private Deque<ModuleInfo> reversedModules = new LinkedList<>();

    final private ReadableArchive source;
    final private Map<String, Object> transientAppMetaData = new HashMap<>();

    private String libraries;
    private boolean jakartaEEApp;
    private ClassLoader appClassLoader;
    private boolean isLoaded;

    private ServiceLocator appServiceLocator;
    private DeploymentFailedListener deploymentFailedListener;

    /**
     * Creates a new instance of an ApplicationInfo
     *
     * @param events
     * @param source the archive for this application
     * @param name name of the application
     */
    public ApplicationInfo(final Events events, ReadableArchive source, String name) {
        super(events, name, new LinkedHashSet<>(), null);
        this.source = source;
    }

    private void createServiceLocator() {
        String locatorName = APP_SERVICE_LOCATOR_PREFIX + getName();
        ServiceLocatorFactory slf = ServiceLocatorFactory.getInstance();

        if (slf.find(locatorName) != null) {
            slf.destroy(locatorName);
        }

        appServiceLocator = slf.create(locatorName);
        deploymentFailedListener = new DeploymentFailedListener(source);
        registerEventListener(deploymentFailedListener);
    }

    private void disposeServiceLocator() {
        if (deploymentFailedListener != null) {
            unregisterEventListener(deploymentFailedListener);
            deploymentFailedListener = null;
        }

        if (appServiceLocator != null) {
            ServiceLocatorFactory.getInstance().destroy(appServiceLocator);
            appServiceLocator = null;
        }
    }

    public void add(EngineRef engine) {
        engines.add(engine);
        reversedEngines.addFirst(engine);
    }

    public void addTransientAppMetaData(String metaDataKey, Object metaDataValue) {
        if (metaDataValue != null) {
            transientAppMetaData.put(metaDataKey, metaDataValue);
        }
    }

    public <T> T getTransientAppMetaData(String key, Class<T> expectedType) {
        return expectedType.cast(transientAppMetaData.get(key));
    }

    /**
     * @return the application registration name
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * Returns the deployment time libraries for this application
     *
     * @return the libraries
     */
    public String getLibraries() {
        return libraries;
    }

    /**
     * Sets the deployment time libraries for this application
     *
     * @param libraries the libraries
     */
    public void setLibraries(String libraries) {
        this.libraries = libraries;
    }

    /**
     * Returns the application classloader for this application.
     *
     * @return The application classloader for this application.
     */
    public ClassLoader getAppClassLoader() {
        return appClassLoader;
    }

    /**
     * Sets the application classloader for this application
     *
     * @param cLoader application classloader
     */
    public void setAppClassLoader(ClassLoader cLoader) {
        appClassLoader = cLoader;
    }

    /**
     * Returns the application scoped ServiceLocator
     *
     * @return The application scoped ServiceLocator
     */
    public ServiceLocator getAppServiceLocator() {
        return appServiceLocator;
    }

    /**
     * Returns whether this application is a JavaEE application
     *
     * @return the isJavaEEApp flag
     */
    public boolean isJakartaEEApp() {
        return jakartaEEApp;
    }

    /**
     * Sets whether this application is a Jakarta EE application
     *
     * @param engineInfos the engine info list
     */
    public void detectIfJakartaEEApp(List<EngineInfo<?, ?>> engineInfos) {
        for (EngineInfo<?, ?> engineInfo : engineInfos) {
            Sniffer sniffer = engineInfo.getSniffer();
            if (sniffer.isJakartaEE()) {
                jakartaEEApp = true;
                break;
            }
        }
    }

    /**
     * Returns the directory where the application bits are located
     *
     * @return the application bits directory
     */
    public ReadableArchive getSource() {
        return source;
    }

    /**
     * Returns the modules of this application
     *
     * @return the modules of this application
     */
    public Collection<ModuleInfo> getModuleInfos() {
        return modules;
    }

    /**
     * Returns the list of sniffers that participated in loaded this application
     *
     * @return array of sniffer that loaded the application's module
     */
    @Override
    public Collection<Sniffer> getSniffers() {
        List<Sniffer> sniffers = new ArrayList<>();
        for (EngineRef engine : engines) {
            sniffers.add(engine.getContainerInfo().getSniffer());
        }

        for (ModuleInfo module : modules) {
            sniffers.addAll(module.getSniffers());
        }

        return sniffers;
    }

    /**
     * Returns the EngineRef for a particular container type
     *
     * @param type the container type
     *
     * @return the module info is this application as a module implemented with the passed container type
     */
    public <T extends Container> Collection<EngineRef> getEngineRefsForContainer(Class<T> type) {
        Set<EngineRef> engines = new LinkedHashSet<>();
        for (ModuleInfo module : modules) {
            EngineRef engine = null;
            try {
                engine = module.getEngineRefForContainer(type);
            } catch (Exception e) {
                // ignore, wrong container
            }
            if (engine != null) {
                engines.add(engine);
            }
        }

        return engines;
    }

    protected ExtendedDeploymentContext getSubContext(ModuleInfo info, ExtendedDeploymentContext context) {
        return context;
    }

    @Override
    public void load(ExtendedDeploymentContext context, ProgressTracker tracker) throws Exception {
        Logger logger = context.getLogger();
        if (isLoaded) {
            logger.fine("Application is already loaded.");
            return;
        }

        context.setPhase(ExtendedDeploymentContext.Phase.LOAD);
        DeploymentTracing tracing = context.getModuleMetaData(DeploymentTracing.class);
        if (tracing != null) {
            tracing.addMark(DeploymentTracing.Mark.LOAD);
        }

        super.load(context, tracker);

        appClassLoader = context.getClassLoader();

        for (ModuleInfo module : modules) {
            if (tracing != null) {
                tracing.addModuleMark(DeploymentTracing.ModuleMark.LOAD, module.getName());
            }

            module.load(getSubContext(module, context), tracker);
            if (tracing != null) {
                tracing.addModuleMark(DeploymentTracing.ModuleMark.LOADED, module.getName());
            }
        }

        populateApplicationServiceLocator();

        isLoaded = true;

        if (tracing != null) {
            tracing.addMark(DeploymentTracing.Mark.LOAD_EVENTS);
        }

        sendEvent(new Event<>(Deployment.APPLICATION_LOADED, this), false);

        if (tracing != null) {
            tracing.addMark(DeploymentTracing.Mark.LOADED);
        }
    }

    public void start(ExtendedDeploymentContext context, ProgressTracker tracker) throws Exception {
        DeploymentTracing tracing = context.getModuleMetaData(DeploymentTracing.class);
        if (tracing != null) {
            tracing.addMark(DeploymentTracing.Mark.START);
        }

        super.start(context, tracker);

        // Register all deployed items.
        for (ModuleInfo module : getModuleInfos()) {
            if (tracing != null) {
                tracing.addModuleMark(DeploymentTracing.ModuleMark.START, module.getName());
            }
            module.start(getSubContext(module, context), tracker);
            if (tracing != null) {
                tracing.addModuleMark(DeploymentTracing.ModuleMark.STARTED, module.getName());
            }

        }
        if (tracing != null) {
            tracing.addMark(DeploymentTracing.Mark.START_EVENTS);
        }

        sendEvent(new Event<>(Deployment.APPLICATION_STARTED, this), false);

        if (tracing != null) {
            tracing.addMark(DeploymentTracing.Mark.STARTED);
        }
    }

    @Override
    public void stop(ExtendedDeploymentContext context, Logger logger) {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(appClassLoader);
            context.setClassLoader(appClassLoader);

            super.stop(context, logger);

            for (ModuleInfo module : reversedModules) {
                module.stop(getSubContext(module, context), logger);
            }

            sendEvent(new Event<>(APPLICATION_STOPPED, this), false);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    @Override
    public void unload(ExtendedDeploymentContext context) {
        Logger logger = context.getLogger();
        if (!isLoaded) {
            logger.fine("Application is already unloaded.");
            return;
        }

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(appClassLoader);
            context.setClassLoader(appClassLoader);

            super.unload(context);

            for (ModuleInfo module : reversedModules) {
                module.unload(getSubContext(module, context));
            }

            isLoaded = false;
            sendEvent(new Event<>(APPLICATION_UNLOADED, this), false);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
            context.setClassLoader(null);
        }
    }

    @Override
    public boolean suspend(Logger logger) {
        boolean isSuccess = super.suspend(logger);

        for (ModuleInfo module : reversedModules) {
            if (!module.suspend(logger)) {
                isSuccess = false;
            }
        }

        return isSuccess;
    }

    @Override
    public boolean resume(Logger logger) {
        boolean isSuccess = super.resume(logger);

        for (ModuleInfo module : modules) {
            if (!module.resume(logger)) {
                isSuccess = false;
            }
        }

        return isSuccess;
    }

    @Override
    public void clean(ExtendedDeploymentContext context) throws Exception {
        super.clean(context);

        for (ModuleInfo info : reversedModules) {
            info.clean(getSubContext(info, context));
            info = null;
        }

        // Clean up the app level classloader
        if (appClassLoader != null) {
            try {
                if (appServiceLocator != null) {
                    appServiceLocator.preDestroy(appClassLoader);
                } else {
                    PreDestroy.class.cast(appClassLoader).preDestroy();
                }
            } catch (Exception e) {
                // Ignore, some failure in preDestroy
            }
            appClassLoader = null;
        }

        // clean the module class loaders if they are not already
        // been cleaned
        for (ModuleInfo module : getModuleInfos()) {
            if (module.getClassLoaders() != null) {
                for (ClassLoader cloader : module.getClassLoaders()) {
                    try {
                        PreDestroy.class.cast(cloader).preDestroy();
                    } catch (Exception e) {
                        // ignore, the class loader does not need to be
                        // explicitely stopped or already stopped
                    }
                }
                module.cleanClassLoaders();
            }
        }

        // Will destroy all underlying services
        disposeServiceLocator();

        sendEvent(new EventListener.Event<>(Deployment.APPLICATION_CLEANED, context), false);
    }

    /**
     * Saves its state to the configuration. this method must be called within a transaction to the configured Application
     * instance.
     *
     * @param app the application being persisted
     */
    public void save(Application app) throws TransactionFailure, PropertyVetoException {
        for (EngineRef engineRef : engines) {
            Engine engine = app.createChild(Engine.class);
            app.getEngine().add(engine);
            engineRef.save(engine);
        }

        for (ModuleInfo module : modules) {
            Module modConfig = app.getModule(module.getName());
            if (modConfig == null) {
                // not a JavaEE module, create it here
                modConfig = app.createChild(Module.class);
                modConfig.setName(module.getName());
                app.getModule().add(modConfig);
            }
            module.save(modConfig);
        }
    }

    public void addModule(ModuleInfo info) {
        modules.add(info);
        reversedModules.addFirst(info);
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Populates the ApplicationServiceLocator with services using the current appClassLoader. Services must be described in
     * files named META-INF/hk2-locator/application. {@link PopulationPostProcessor} may be defined in the META-INF/services
     * standard way
     *
     * @throws IOException On failure to read the service files
     */
    private void populateApplicationServiceLocator() throws IOException {
        createServiceLocator();

        ServiceLoader<PopulatorPostProcessor> postProcessors = ServiceLoader.load(PopulatorPostProcessor.class, appClassLoader);

        LinkedList<PopulatorPostProcessor> allProcessors = new LinkedList<>();
        for (PopulatorPostProcessor postProcessor : postProcessors) {
            allProcessors.add(postProcessor);
        }

        // Add this one AFTER all the other processors
        allProcessors.addLast(new ApplicationClassLoadingPostProcessor(appClassLoader));

        HK2Populator.populate(appServiceLocator, new ApplicationDescriptorFileFinder(appClassLoader, APPLICATION_LOADER_FILES),
                allProcessors);

        HashSet<ClassLoader> treatedLoaders = new HashSet<>();
        treatedLoaders.add(appClassLoader);

        for (ModuleInfo module : modules) {
            ClassLoader moduleClassLoader = module.getModuleClassLoader();

            if ((moduleClassLoader == null) || treatedLoaders.contains(moduleClassLoader)) {
                continue;
            }
            treatedLoaders.add(moduleClassLoader);

            allProcessors.removeLast();
            allProcessors.addLast(new ApplicationClassLoadingPostProcessor(moduleClassLoader));

            HK2Populator.populate(
                appServiceLocator,
                new ApplicationDescriptorFileFinder(moduleClassLoader, WEB_LOADER_FILES),
                allProcessors);
        }
    }

    private class DeploymentFailedListener implements EventListener {
        private final ReadableArchive archive;

        private DeploymentFailedListener(ReadableArchive archive) {
            this.archive = archive;
        }

        @Override
        public void event(@RestrictTo(Deployment.DEPLOYMENT_FAILURE_NAME) Event<?> event) {
            if (!event.is(Deployment.DEPLOYMENT_FAILURE)) {
                return;
            }

            DeploymentContext dc = (DeploymentContext) event.hook();

            if (!archive.equals(dc.getSource())) {
                return;
            }

            // Will destroy all underlying services
            disposeServiceLocator();
        }

    }
}
