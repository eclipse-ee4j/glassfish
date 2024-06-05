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

package org.glassfish.internal.data;

import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.ServerTags;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.Events;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.DeploymentTracing;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * Each module of an application has an associated module info instance keeping the list of engines in which that module
 * is loaded.
 *
 * @author Jerome Dochez
 */
public class ModuleInfo {

    protected Set<EngineRef> engines = new LinkedHashSet<>();

    // The reversed engines contain the same elements as engines but just in
    // reversed order, they are used when stopping/unloading the module.
    // The engines should be stopped/unloaded in the reverse order of what
    // they were originally loaded/started.
    protected LinkedList<EngineRef> reversedEngines = new LinkedList<>();

    final protected Map<Class<? extends Object>, Object> metaData = new HashMap<>();

    private final String name;
    private final Events events;
    private final Properties moduleProps;
    private boolean started;
    private ClassLoader moduleClassLoader;
    private Set<ClassLoader> classLoaders = new HashSet<>();

    public ModuleInfo(final Events events, String name, Collection<EngineRef> engineRefs, Properties moduleProps) {
        this.name = name;
        this.events = events;
        for (EngineRef engineRef : engineRefs) {
            engines.add(engineRef);
        }

        for (EngineRef ref : engineRefs) {
            reversedEngines.addFirst(ref);
        }

        this.moduleProps = moduleProps;
    }

    protected void sendEvent(Event<?> event, boolean asynchronously) {
        if (events == null) {
            return;
        }
        events.send(event, asynchronously);
    }


    protected void registerEventListener(EventListener listener) {
        events.register(listener);
    }

    protected void unregisterEventListener(EventListener listener) {
        events.unregister(listener);
    }

    public Set<EngineRef> getEngineRefs() {
        Set<EngineRef> copy = new LinkedHashSet<>();
        copy.addAll(_getEngineRefs());
        return copy;
    }

    protected Set<EngineRef> _getEngineRefs() {
        return engines;
    }

    public Set<ClassLoader> getClassLoaders() {
        return classLoaders;
    }

    public ClassLoader getModuleClassLoader() {
        return moduleClassLoader;
    }

    public void cleanClassLoaders() {
        classLoaders = null;
        moduleClassLoader = null;
    }

    public void addMetaData(Object o) {
        metaData.put(o.getClass(), o);
    }

    public <T> T getMetaData(Class<T> c) {
        return c.cast(metaData.get(c));
    }

    public Object getMetaData(String className) {
        for (Class c : metaData.keySet()) {
            if (c.getName().equals(className)) {
                return metaData.get(c);
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public Properties getModuleProps() {
        Properties props = new Properties();
        props.putAll(moduleProps);
        return props;
    }

    /**
     * Returns the list of sniffers that participated in loaded this application
     *
     * @return array of sniffer that loaded the application's module
     */
    public Collection<Sniffer> getSniffers() {
        List<Sniffer> sniffers = new ArrayList<>();
        for (EngineRef engine : _getEngineRefs()) {
            sniffers.add(engine.getContainerInfo().getSniffer());
        }
        return sniffers;
    }

    public void load(ExtendedDeploymentContext context, ProgressTracker tracker) throws Exception {
        Logger logger = context.getLogger();
        context.setPhase(ExtendedDeploymentContext.Phase.LOAD);
        DeploymentTracing tracing = context.getModuleMetaData(DeploymentTracing.class);
        if (tracing != null) {
            tracing.addMark(DeploymentTracing.Mark.LOAD);
        }

        moduleClassLoader = context.getClassLoader();

        Set<EngineRef> filteredEngines = new LinkedHashSet<>();
        LinkedList<EngineRef> filteredReversedEngines = new LinkedList<>();

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(context.getClassLoader());
            for (EngineRef engine : _getEngineRefs()) {

                final EngineInfo engineInfo = engine.getContainerInfo();

                if (tracing != null) {
                    tracing.addContainerMark(DeploymentTracing.ContainerMark.LOAD, engineInfo.getSniffer().getModuleType());
                }

                // get the container.
                Deployer deployer = engineInfo.getDeployer();

                try {
                    ApplicationContainer appCtr = deployer.load(engineInfo.getContainer(), context);
                    if (appCtr == null) {
                        String msg = "Cannot load application in " + engineInfo.getContainer().getName() + " container";
                        logger.fine(msg);
                        continue;
                    }
                    engine.load(context, tracker);
                    engine.setApplicationContainer(appCtr);
                    filteredEngines.add(engine);
                    filteredReversedEngines.addFirst(engine);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception while invoking " + deployer.getClass() + " load method", e);
                    throw e;
                }
                if (tracing != null) {
                    tracing.addContainerMark(DeploymentTracing.ContainerMark.LOADED, engineInfo.getSniffer().getModuleType());
                }

            }
            engines = filteredEngines;
            reversedEngines = filteredReversedEngines;
            if (tracing != null) {
                tracing.addMark(DeploymentTracing.Mark.LOAD_EVENTS);
            }

            sendEvent(new Event<>(Deployment.MODULE_LOADED, this), false);

            if (tracing != null) {
                tracing.addMark(DeploymentTracing.Mark.LOADED);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }

    }

    /*
     * Returns the EngineRef for a particular container type
     *
     * @param type the container type
     *
     * @return the module info is this application as a module implemented with the passed container type
     */
    public <T extends Container> EngineRef getEngineRefForContainer(Class<T> type) {
        for (EngineRef engine : _getEngineRefs()) {
            T container = null;
            try {
                container = type.cast(engine.getContainerInfo().getContainer());
            } catch (Exception e) {
                // ignore, wrong container
            }
            if (container != null) {
                return engine;
            }
        }
        return null;
    }

    public synchronized void start(DeploymentContext context, ProgressTracker tracker) throws Exception {

        Logger logger = context.getLogger();

        if (started) {
            return;
        }

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(context.getClassLoader());
            // registers all deployed items.
            for (EngineRef engine : _getEngineRefs()) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("starting " + engine.getContainerInfo().getSniffer().getModuleType());
                }
                DeploymentTracing tracing = context.getModuleMetaData(DeploymentTracing.class);
                if (tracing != null) {
                    tracing.addContainerMark(DeploymentTracing.ContainerMark.START, engine.getContainerInfo().getSniffer().getModuleType());
                }

                try {
                    if (!engine.start(context, tracker)) {
                        logger.log(Level.SEVERE, "Module not started " + engine.getApplicationContainer().toString());
                        throw new Exception("Module not started " + engine.getApplicationContainer().toString());
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception while invoking " + engine.getApplicationContainer().getClass() + " start method",
                            e);
                    throw e;
                }
                if (tracing != null) {
                    tracing.addContainerMark(DeploymentTracing.ContainerMark.STARTED,
                            engine.getContainerInfo().getSniffer().getModuleType());
                }
            }
            started = true;
            if (events != null) {
                events.send(new Event<>(Deployment.MODULE_STARTED, this), false);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    public synchronized void stop(ExtendedDeploymentContext context, Logger logger) {

        if (!started) {
            return;
        }

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(moduleClassLoader);
            for (EngineRef module : reversedEngines) {
                try {
                    context.setClassLoader(moduleClassLoader);
                    module.stop(context);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Cannot stop module " + module.getContainerInfo().getSniffer().getModuleType(), e);
                }
            }
            started = false;
            sendEvent(new Event<>(Deployment.MODULE_STOPPED, this), false);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    public void unload(ExtendedDeploymentContext context) {

        Logger logger = context.getLogger();
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(moduleClassLoader);
            for (EngineRef engine : reversedEngines) {
                if (engine.getApplicationContainer() != null && engine.getApplicationContainer().getClassLoader() != null) {
                    classLoaders.add(engine.getApplicationContainer().getClassLoader());
                    try {
                        context.setClassLoader(moduleClassLoader);
                        engine.unload(context);
                    } catch (Throwable e) {
                        logger.log(Level.SEVERE,
                                "Failed to unload from container type : " + engine.getContainerInfo().getSniffer().getModuleType(), e);
                    }
                }
            }
            // add the module classloader to the predestroy list if it's not
            // already there
            if (classLoaders != null && moduleClassLoader != null) {
                classLoaders.add(moduleClassLoader);
            }
            sendEvent(new Event<>(Deployment.MODULE_UNLOADED, this), false);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
            context.setClassLoader(null);
        }
    }

    public void clean(ExtendedDeploymentContext context) throws Exception {
        for (EngineRef ref : reversedEngines) {
            ref.clean(context);
        }
        sendEvent(new Event<>(Deployment.MODULE_CLEANED, context), false);
    }

    public boolean suspend(Logger logger) {

        boolean isSuccess = true;

        for (EngineRef engine : reversedEngines) {
            try {
                engine.getApplicationContainer().suspend();
            } catch (Exception e) {
                isSuccess = false;
                logger.log(Level.SEVERE, "Error suspending module " + engine.getContainerInfo().getSniffer().getModuleType(), e);
            }
        }

        return isSuccess;
    }

    public boolean resume(Logger logger) {

        boolean isSuccess = true;

        for (EngineRef module : _getEngineRefs()) {
            try {
                module.getApplicationContainer().resume();
            } catch (Exception e) {
                isSuccess = false;
                logger.log(Level.SEVERE, "Error resuming module " + module.getContainerInfo().getSniffer().getModuleType(), e);
            }
        }

        return isSuccess;
    }

    /**
     * Saves its state to the configuration. this method must be called within a transaction to the configured module
     * instance.
     *
     * @param module the module being persisted
     */
    public void save(Module module) throws TransactionFailure, PropertyVetoException {
        // write out the module properties only for composite app
        if (Boolean.parseBoolean(moduleProps.getProperty(ServerTags.IS_COMPOSITE))) {
            moduleProps.remove(ServerTags.IS_COMPOSITE);
            for (Object element : moduleProps.keySet()) {
                String propName = (String) element;
                Property prop = module.createChild(Property.class);
                module.getProperty().add(prop);
                prop.setName(propName);
                prop.setValue(moduleProps.getProperty(propName));
            }
        }

        for (EngineRef ref : _getEngineRefs()) {
            Engine engine = module.createChild(Engine.class);
            module.getEngines().add(engine);
            ref.save(engine);
        }
    }

    /**
     * Returns simple name of this class and name of the module.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + name + ']';
    }
}
