/*
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

import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.LifecycleEventContext;
import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.loader.util.ASClassLoaderUtil;

/**
 * @author Sridatta Viswanath
 */

public final class ServerLifecycleModule {

    private LifecycleListener slcl;
    private String name;
    private String className;
    private String classpath;
    private int loadOrder;
    private boolean isFatal = false;
    private String statusMsg = "OK";

    private ServerContext ctx;
    private LifecycleEventContext leContext;
    private ClassLoader urlClassLoader;
    private Properties props = new Properties();

    private static final Logger _logger = KernelLoggerInfo.getLogger();
    private static boolean _isTraceEnabled = false;

    private final static String LIFECYCLE_PREFIX = "lifecycle_";

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ServerLifecycleModule.class);

    ServerLifecycleModule(ServerContext ctx, String name, String className) {
        this.name = name;
        this.className = className;
        this.ctx = ctx;
        this.leContext = new LifecycleEventContextImpl(ctx);

        _isTraceEnabled = _logger.isLoggable(Level.FINE);
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
        ClassLoader classLoader = ctx.getLifecycleParentClassLoader();

        try {
            if (this.classpath != null) {
                URL[] urls = getURLs();

                if (urls != null) {
                    StringBuffer sb = new StringBuffer(128);
                    for(int i=0;i<urls.length;i++) {
                        sb.append(urls[i].toString());
                    }
                    if (_isTraceEnabled)
                        _logger.fine("Lifecycle module = " + getName() +
                                        " has classpath URLs = " + sb.toString());
                }

                this.urlClassLoader = new URLClassLoader(urls, classLoader);
                classLoader = this.urlClassLoader;
            }

            Class cl = Class.forName(className, true, classLoader);
            slcl = (LifecycleListener) cl.newInstance();
        } catch (Exception ee) {
            _logger.log(Level.SEVERE, KernelLoggerInfo.exceptionLoadingLifecycleModule,
                    new Object[] {this.name, ee}) ;
            if (isFatal) {
                throw new ServerLifecycleException(localStrings.getLocalString("lifecyclemodule.loadExceptionIsFatal", "Treating failure loading the lifecycle module as fatal", this.name));
            }
        }

        return slcl;
    }

    private URL[] getURLs() {
        List<URL> urlList = ASClassLoaderUtil.getURLsFromClasspath(
            this.classpath, File.pathSeparator, "");
        return ASClassLoaderUtil.convertURLListToArray(urlList);
    }

    private void postEvent(int eventType, Object data)
                                    throws ServerLifecycleException {
        if (slcl == null) {
            if (isFatal) {
                throw new ServerLifecycleException(localStrings.getLocalString("lifecyclemodule.loadExceptionIsFatal", "Treating failure loading the lifecycle module as fatal", this.name));
            }

            return;
        }

        if (urlClassLoader != null)
            setClassLoader();

        LifecycleEvent slcEvent= new LifecycleEvent(this, eventType, data, this.leContext);
        try {
            slcl.handleEvent(slcEvent);
        } catch (ServerLifecycleException sle) {
            _logger.log(Level.WARNING, KernelLoggerInfo.serverLifecycleException,
                    new Object[] {this.name, sle});

            if (isFatal)
                throw sle;
        } catch (Exception ee) {
            _logger.log(Level.WARNING, KernelLoggerInfo.lifecycleModuleException,
                    new Object[] {this.name, ee});

            if (isFatal) {
                throw new ServerLifecycleException(localStrings.getLocalString("lifecyclemodule.event_exceptionIsFatal", "Treating the exception from lifecycle module event handler as fatal"), ee);
            }
        }
    }

    public void onInitialization()
                                throws ServerLifecycleException {
        postEvent(LifecycleEvent.INIT_EVENT, props);
    }

    public void onStartup()
                                    throws ServerLifecycleException {
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
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(urlClassLoader);
                    return null;
                }
            }
        );
    }

    /**
     * return status of this lifecycle module as a string
     */
    public String getStatus() {
        return statusMsg;
    }

    public String toString() {
        return "Server LifecycleListener support";
    }
}
