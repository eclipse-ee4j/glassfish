/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.common.util.GlassfishUrlClassLoader;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.loader.util.ASClassLoaderUtil;

import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.LifecycleEventContext;
import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.logging.LogHelper;

/**
 * @author Sridatta Viswanath
 */
public final class ServerLifecycleModule {

    private static final Logger _logger = KernelLoggerInfo.getLogger();
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ServerLifecycleModule.class);

    private LifecycleListener slcl;
    private final String name;
    private final String className;
    private String classpath;
    private int loadOrder;
    private boolean isFatal;
    private final String statusMsg = "OK";

    private final ServerContext ctx;
    private final LifecycleEventContext leContext;
    private ClassLoader urlClassLoader;
    private final Properties props = new Properties();

    ServerLifecycleModule(ServerContext ctx, String name, String className) {
        this.name = name;
        this.className = className;
        this.ctx = ctx;
        this.leContext = new LifecycleEventContextImpl(ctx);
    }

    void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    void setProperty(String name, String value) {
        props.put(name, value);
    }

    Properties getProperties() {
        return this.props;
    }

    void setLoadOrder(int loadOrder) {
        this.loadOrder = loadOrder;
    }

    void setIsFatal(boolean isFatal) {
        this.isFatal = isFatal;
    }

    String getName() {
        return this.name;
    }

    String getClassName() {
        return this.className;
    }

    String getClasspath() {
        return this.classpath;
    }

    int getLoadOrder() {
        return this.loadOrder;
    }

    boolean isFatal() {
        return isFatal;
    }

    LifecycleListener loadServerLifecycle() throws ServerLifecycleException {
        final ClassLoader classLoader;
        try {
            if (this.classpath == null) {
                classLoader = ctx.getLifecycleParentClassLoader();
            } else {
                URL[] urls = getURLs();
                _logger.log(Level.FINE, "Lifecycle module = {0} has classpath URLs = {1}", new Object[] { getName(), urls });
                this.urlClassLoader = new GlassfishUrlClassLoader(urls, ctx.getLifecycleParentClassLoader());
                classLoader = this.urlClassLoader;
            }
            @SuppressWarnings("unchecked")
            Class<LifecycleListener> clazz = (Class<LifecycleListener>) Class.forName(className, true, classLoader);
            slcl = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ee) {
            LogHelper.log(_logger, Level.SEVERE, KernelLoggerInfo.exceptionLoadingLifecycleModule, ee, new Object[] { this.name, ee });
            if (isFatal) {
                throw new ServerLifecycleException(localStrings.getLocalString("lifecyclemodule.loadExceptionIsFatal",
                        "Treating failure loading the lifecycle module as fatal", this.name));
            }
        }

        return slcl;
    }

    private URL[] getURLs() {
        List<URL> urlList = ASClassLoaderUtil.getURLsFromClasspath(
                this.classpath, File.pathSeparator, "");
        return ASClassLoaderUtil.convertURLListToArray(urlList);
    }

    private void postEvent(int eventType, Object data) throws ServerLifecycleException {
        if (slcl == null) {
            if (isFatal) {
                throw new ServerLifecycleException(localStrings.getLocalString("lifecyclemodule.loadExceptionIsFatal",
                        "Treating failure loading the lifecycle module as fatal", this.name));
            }
            return;
        }

        if (urlClassLoader != null) {
            setClassLoader();
        }

        LifecycleEvent slcEvent = new LifecycleEvent(this, eventType, data, this.leContext);
        try {
            slcl.handleEvent(slcEvent);
        } catch (ServerLifecycleException sle) {
            LogHelper.log(_logger, Level.WARNING, KernelLoggerInfo.serverLifecycleException, sle, new Object[] { this.name, sle });
            if (isFatal) {
                throw sle;
            }
        } catch (Exception ee) {
            LogHelper.log(_logger, Level.WARNING, KernelLoggerInfo.lifecycleModuleException, ee, new Object[] { this.name, ee });
            if (isFatal) {
                throw new ServerLifecycleException(localStrings.getLocalString("lifecyclemodule.event_exceptionIsFatal",
                        "Treating the exception from lifecycle module event handler as fatal"), ee);
            }
        }
    }

    public void onInitialization() throws ServerLifecycleException {
        postEvent(LifecycleEvent.INIT_EVENT, props);
    }

    public void onStartup() throws ServerLifecycleException {
        postEvent(LifecycleEvent.STARTUP_EVENT, props);
    }

    public void onReady() throws ServerLifecycleException {
        postEvent(LifecycleEvent.READY_EVENT, props);
    }

    public void onShutdown() throws ServerLifecycleException {
        postEvent(LifecycleEvent.SHUTDOWN_EVENT, props);
    }

    public void onTermination() throws ServerLifecycleException {
        postEvent(LifecycleEvent.TERMINATION_EVENT, props);
    }

    private void setClassLoader() {
        // set the url class loader as the thread context class loader
        PrivilegedAction<Void> action = () -> {
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            return null;
        };
        AccessController.doPrivileged(action);
    }

    /**
     * @return status of this lifecycle module as a string
     */
    public String getStatus() {
        return statusMsg;
    }

    @Override
    public String toString() {
        return "Server LifecycleListener support";
    }
}
