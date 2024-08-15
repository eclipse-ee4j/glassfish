/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.server;

import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.Result;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.glassfish.api.FutureProvider;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;

/**
 * Support class to assist in firing LifecycleEvent notifications to
 * registered LifecycleListeners.
 */
@Service
@RunLevel(StartupRunLevel.VAL)
public class LifecycleModuleService implements PreDestroy, PostConstruct, EventListener, FutureProvider<Result<Thread>>{

    @Inject
    ServerContext context;

    @Inject
    Applications apps;

    @Inject
    Events events;

    @Inject @Named( ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    private ConfigBeansUtilities configBeansUtilities;

    /**
     * The set of registered LifecycleListeners for event notifications.
     */
    private ArrayList listeners = new ArrayList();

    List<Future<Result<Thread>>> futures = new ArrayList();

    @Override
    public void postConstruct() {
        events.register(this);
        try {
            onInitialization();
        } catch (Exception e) {
            addExceptionToFuture(e);
        }
    }

    @Override
    public void preDestroy() {
        try {
            onTermination();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Future<Result<Thread>>> getFutures() {
        return futures;
    }

    @Override
    public void event(Event<?> event) {
        try {
            if (event.is(EventTypes.SERVER_STARTUP)) {
                onStartup();
            } else if (event.is(EventTypes.SERVER_READY)) {
                onReady();
            } else if (event.is(EventTypes.PREPARE_SHUTDOWN)) {
                onShutdown();
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private void onInitialization() throws ServerLifecycleException {
        List<Application> applications = apps.getApplications();
        List<Application> lcms = new ArrayList<>();
        for (Application app : applications) {
            if (Boolean.valueOf(app.getDeployProperties().getProperty
                (ServerTags.IS_LIFECYCLE))) {
                lcms.add(app);
            }
        }

        HashSet listenerSet = new HashSet();
        for (Application next : lcms) {
            Properties props = next.getDeployProperties();
            String enabled = next.getEnabled();
            if ( isEnabled(next.getName(), enabled) ) {
                String strOrder = (String)props.remove(
                    ServerTags.LOAD_ORDER);

                int order = Integer.MAX_VALUE;
                if (strOrder != null && strOrder.length() > 0) {
                    try {
                        order = Integer.parseInt(strOrder);
                    } catch(NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                }

                String className = (String)props.remove(
                    ServerTags.CLASS_NAME);
                ServerLifecycleModule slcm =
                    new ServerLifecycleModule(context,
                                next.getName(), className);

                slcm.setLoadOrder(order);

                String classpath = (String)props.remove(
                    ServerTags.CLASSPATH);
                slcm.setClasspath(classpath);

                String isFailureFatal = (String)props.remove(
                    ServerTags.IS_FAILURE_FATAL);
                slcm.setIsFatal(Boolean.valueOf(isFailureFatal));

                props.remove(ServerTags.IS_LIFECYCLE);
                props.remove(ServerTags.OBJECT_TYPE);

                for (String propName : props.stringPropertyNames()) {
                    slcm.setProperty(propName, props.getProperty(propName));
                }

                LifecycleListener listener = slcm.loadServerLifecycle();
                listenerSet.add(slcm);
            }
        }
        sortModules(listenerSet);

        initialize();
    }

    /**
     * Returns true if life cycle module is enabled in the application
     * level and in the application ref level.
     *
     * @return  true if life cycle module is enabled
     */
    private boolean isEnabled(String name, String enabled) {

        // true if enabled in both lifecyle module and in the ref
        return (Boolean.valueOf(enabled) &&
            Boolean.valueOf(configBeansUtilities.getEnabled(
                server.getName(), name)));
    }

    private void resetClassLoader(final ClassLoader c) {
         // set the common class loader as the thread context class loader
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                @Override
                public Object run() {
                    Thread.currentThread().setContextClassLoader(c);
                    return null;
                }
            }
        );
    }

    private void sortModules(HashSet listenerSet) {
        // FIXME: use a better sorting algo
        for(Iterator iter = listenerSet.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            int order = next.getLoadOrder();
            int i=0;
            for(;i<this.listeners.size();i++) {
                if(((ServerLifecycleModule)listeners.get(i)).getLoadOrder() > order) {
                    break;
                }
            }
            this.listeners.add(i,next);
        }
    }

    private void initialize()
                            throws ServerLifecycleException {

        if (listeners.isEmpty()) {
            return;
        }

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onInitialization();
        }
        // set it back
        resetClassLoader(cl);
    }

    private void onStartup() throws ServerLifecycleException {

        if (listeners.isEmpty()) {
            return;
        }

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onStartup();
        }
        // set it back
        resetClassLoader(cl);
    }

    private void onReady() throws ServerLifecycleException {

        if (listeners.isEmpty()) {
            return;
        }

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onReady();
        }
        // set it back
        resetClassLoader(cl);
    }

    private void onShutdown() throws ServerLifecycleException {

        if (listeners.isEmpty()) {
            return;
        }

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onShutdown();
        }
        // set it back
        resetClassLoader(cl);
    }

    private void onTermination() throws ServerLifecycleException {

        if (listeners.isEmpty()) {
            return;
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onTermination();
        }
        // set it back
        resetClassLoader(cl);
    }

    private Future<Result<Thread>> addExceptionToFuture(Throwable t) {
        Future<Result<Thread>> future = new LifecycleModuleFuture();
        ((LifecycleModuleFuture)future).setResult(new Result<Thread>(t));
        futures.add(future);
        return future;
    }

    public static final class LifecycleModuleFuture implements Future<Result<Thread>> {
        Result<Thread> result;
        CountDownLatch latch = new CountDownLatch(1);

        public void setResult(Result<Thread> result) {
            this.result = result;
            latch.countDown();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return latch.getCount() == 0;
        }

        @Override
        public Result<Thread> get() throws InterruptedException {
            latch.await();
            return result;
        }

        @Override
        public Result<Thread> get(long timeout, TimeUnit unit) throws InterruptedException {
            latch.await(timeout, unit);
            return result;
        }
    }
}
