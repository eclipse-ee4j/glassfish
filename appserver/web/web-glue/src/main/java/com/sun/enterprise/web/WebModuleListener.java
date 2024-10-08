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
package com.sun.enterprise.web;

import com.sun.appserv.web.cache.CacheManager;
import com.sun.enterprise.container.common.spi.CDIService;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.web.InitializationParameter;
import com.sun.enterprise.util.net.JarURIPattern;
import com.sun.enterprise.web.jsp.JspProbeEmitterImpl;
import com.sun.enterprise.web.jsp.ResourceInjectorImpl;

import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Wrapper;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.web.TldProvider;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Populator;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ClasspathDescriptorFileFinder;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.runtime.SunWebAppImpl;
import org.glassfish.web.deployment.runtime.WebProperty;
import org.glassfish.web.deployment.util.WebValidatorWithCL;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.WARNING;
import static org.glassfish.web.LogFacade.CLASS_CAST_EXCEPTION;

/**
 * Startup event listener for a <b>Context</b> that configures the properties of that Jsp Servlet from sun-web.xml
 */
final class WebModuleListener implements LifecycleListener {

    /**
     * The logger used to log messages
     */
    private static final Logger _logger = LogFacade.getLogger();

    /**
     * Descriptor object associated with this web application. Used for loading persistence units.
     */
    private final WebBundleDescriptor webBundleDescriptor;
    private final WebContainer webContainer;
    private boolean includeInitialized;
    private List<String> includeJars;


    /**
     * Constructor.
     *
     * @param webContainer
     * @param wbd descriptor for this module.
     */
    WebModuleListener(WebContainer webContainer, WebBundleDescriptor wbd) {
        this.webContainer = webContainer;
        this.webBundleDescriptor = wbd;
    }

    /**
     * Process the START event for an associated WebModule
     *
     * @param event The lifecycle event that has occurred
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        // Identify the context we are associated with
        WebModule webModule;
        try {
            webModule = (WebModule) event.getLifecycle();
        } catch (ClassCastException e) {
            _logger.log(WARNING, CLASS_CAST_EXCEPTION, event.getLifecycle());
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) {
            // post processing DOL object for standalone web module
            if (webBundleDescriptor != null && webBundleDescriptor.getApplication() != null && webBundleDescriptor.getApplication().isVirtual()) {
                webBundleDescriptor.setClassLoader(webModule.getLoader().getClassLoader());
                webBundleDescriptor.visit(new WebValidatorWithCL());
            }

            // loadPersistenceUnits(webModule);
            configureDefaultServlet(webModule);
            configureJsp(webModule);
            startCacheManager(webModule);
        } else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
            // unloadPersistenceUnits(webModule);
            stopCacheManager(webModule);
        }
    }

    // ------------------------------------------------------- Private Methods
    /**
     * Configure all JSP related aspects of the web module, including any relevant TLDs as well as the jsp config
     * settings of the JspServlet (using the values from sun-web.xml's jsp-config).
     */
    private void configureJsp(WebModule webModule) {

        ServletContext servletContext = webModule.getServletContext();
        servletContext.setAttribute("org.glassfish.jsp.isStandaloneWebapp", Boolean.valueOf(webModule.isStandalone()));

        // Find tld URI and set it to ServletContext attribute
        List<URI> appLibUris = webModule.getDeployAppLibs();
        Map<URI, List<String>> appLibTldMap = new HashMap<>();
        if (appLibUris != null && appLibUris.size() > 0) {
            Pattern pattern = Pattern.compile("META-INF/.*\\.tld");
            for (URI uri : appLibUris) {
                List<String> entries = JarURIPattern.getJarEntries(uri, pattern);
                if (entries != null && entries.size() > 0) {
                    appLibTldMap.put(uri, entries);
                }
            }
        }

        Collection<TldProvider> tldProviders = webContainer.getTldProviders();
        Map<URI, List<String>> tldMap = new HashMap<>();
        for (TldProvider tldProvider : tldProviders) {
            // Skip any JSF related TLDs for non-JSF apps
            if ("jsfTld".equals(tldProvider.getName()) && !webModule.isJsfApplication()) {
                continue;
            }
            Map<URI, List<String>> tmap = tldProvider.getTldMap();
            if (tmap != null) {
                tldMap.putAll(tmap);
            }
        }
        tldMap.putAll(appLibTldMap);
        servletContext.setAttribute("com.sun.appserv.tld.map", tldMap);

        /*
         * Discover all TLDs that are known to contain listener declarations, and store the resulting map as a ServletContext
         * attribute
         */
        Map<URI, List<String>> tldListenerMap = new HashMap<>();
        for (TldProvider tldProvider : tldProviders) {
            // Skip any JSF related TLDs for non-JSF apps
            if ("jsfTld".equals(tldProvider.getName()) && !webModule.isJsfApplication()) {
                continue;
            }
            Map<URI, List<String>> tmap = tldProvider.getTldListenerMap();
            if (tmap != null) {
                tldListenerMap.putAll(tmap);
            }
        }
        tldListenerMap.putAll(appLibTldMap);
        servletContext.setAttribute("com.sun.appserv.tldlistener.map", tldListenerMap);

        ServiceLocator defaultServices = webContainer.getServerContext().getDefaultServices();
        final String servicesName = webModule.getComponentId();
        ServiceLocator webAppServices = ServiceLocatorFactory.getInstance().create(servicesName, defaultServices);
        initializeServicesFromClassLoader(webAppServices, Thread.currentThread().getContextClassLoader());

        // set services for jsf injection
        servletContext.setAttribute(Constants.HABITAT_ATTRIBUTE, webAppServices);

        SunWebAppImpl bean = webModule.getIasWebAppConfigBean();

        // Find the default jsp servlet
        Wrapper wrapper = (Wrapper) webModule.findChild(org.apache.catalina.core.Constants.JSP_SERVLET_NAME);
        if (wrapper == null) {
            return;
        }

        if (webModule.getTldValidation()) {
            wrapper.addInitParameter("enableTldValidation", "true");
        }
        if (bean != null && bean.getJspConfig() != null) {
            WebProperty[] props = bean.getJspConfig().getWebProperty();
            for (WebProperty prop : props) {
                String pname = prop.getValue("name");
                String pvalue = prop.getValue("value");
                if (_logger.isLoggable(FINE)) {
                    _logger.log(FINE, LogFacade.JSP_CONFIG_PROPERTY, "[" + webModule.getID() + "] is [" + pname + "] = [" + pvalue + "]");
                }
                wrapper.addInitParameter(pname, pvalue);
            }
        }

        // Override any log setting with the container wide logging level
        wrapper.addInitParameter("logVerbosityLevel", getWaspLogLevel());

        ResourceInjectorImpl resourceInjector = new ResourceInjectorImpl(webModule);
        servletContext.setAttribute("com.sun.appserv.jsp.resource.injector", resourceInjector);

        String sysClassPath = ASClassLoaderUtil.getModuleClassPath(defaultServices, webModule.getID(), null);

        // If the configuration flag usMyFaces is set, remove jakarta.faces.jar
        // from the system class path
        Boolean useMyFaces = (Boolean) servletContext.getAttribute("com.sun.faces.useMyFaces");
        if (useMyFaces != null && useMyFaces) {
            sysClassPath = sysClassPath.replace("jakarta.faces.jar", "$disabled$.raj");
            // jsf-connector.jar manifest has a Class-Path to jakarta.faces.jar
            sysClassPath = sysClassPath.replace("jsf-connector.jar", "$disabled$.raj");
        }

        // TODO: combine with classpath from
        // servletContext.getAttribute(("org.apache.catalina.jsp_classpath")
        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE, LogFacade.SYS_CLASSPATH, webModule.getID() + " is " + sysClassPath);
        }

        if (sysClassPath.isEmpty()) {
            // In embedded mode, services returns SingleModulesRegistry and
            // it has no modules.
            // Try "java.class.path" system property instead.
            sysClassPath = System.getProperty("java.class.path");
        }
        sysClassPath = trimSysClassPath(sysClassPath);
        wrapper.addInitParameter("com.sun.appserv.jsp.classpath", sysClassPath);

        // Configure Jakarta Pages monitoring
        servletContext.setAttribute("org.glassfish.jsp.monitor.probeEmitter", new JspProbeEmitterImpl(webModule));

        // Pass BeanManager's ELResolver as ServletContext attribute (see IT 11168)
        InvocationManager invocationManager = webContainer.getInvocationManager();
        WebComponentInvocation webComponentInvocation = new WebComponentInvocation(webModule);
        try {
            invocationManager.preInvoke(webComponentInvocation);

            CDIService cdiService = defaultServices.getService(CDIService.class);

            // CDIService can be absent if weld integration is missing in the runtime, so check for null is needed.
            if (cdiService != null && cdiService.isCurrentModuleCDIEnabled()) {
                cdiService.setELResolver(servletContext);
            }
        } catch (NamingException e) {
            _logger.log(Level.CONFIG, "Setting the ELResolver failed. Ignoring the exception.", e);
        } finally {
            invocationManager.postInvoke(webComponentInvocation);
        }

    }

    private static void initializeServicesFromClassLoader(ServiceLocator serviceLocator, ClassLoader classLoader) {
        DynamicConfigurationService dcs =
                    serviceLocator.getService(DynamicConfigurationService.class);
            Populator populator = dcs.getPopulator();
        try {
            populator.populate(new ClasspathDescriptorFileFinder(classLoader));
        } catch (IOException | MultiException ex) {
            _logger.log(Level.SEVERE, ex, ex::getMessage);
        }
    }

    private void initIncludeJars() {
        if (includeInitialized) {
            return;
        }

        String includeJarsString = null;
        for (WebComponentDescriptor wcd : webBundleDescriptor.getWebComponentDescriptors()) {
            if ("jsp".equals(wcd.getCanonicalName())) {
                InitializationParameter initp = wcd.getInitializationParameterByName("system-jar-includes");
                if (initp != null) {
                    includeJarsString = initp.getValue();
                    break;
                }
            }
        }
        includeInitialized = true;
        if (includeJarsString == null) {
            includeJars = null;
            return;
        }
        includeJars = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(includeJarsString);
        while (tokenizer.hasMoreElements()) {
            includeJars.add(tokenizer.nextToken());
        }
    }

    private boolean included(String path) {
        for (String item : includeJars) {
            if (path.contains(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove unnecessary system jars, to improve performance
     */
    private String trimSysClassPath(String sysClassPath) {

        if (sysClassPath == null || sysClassPath.isEmpty()) {
            return "";
        }
        initIncludeJars();
        if (includeJars == null || includeJars.size() == 0) {
            // revert to previous behavior, i.e. no trimming
            return sysClassPath;
        }
        StringBuilder ret = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(sysClassPath, File.pathSeparator);
        String mySep = "";
        while (tokenizer.hasMoreElements()) {
            String path = tokenizer.nextToken();
            if (included(path)) {
                ret.append(mySep);
                ret.append(path);
                mySep = File.pathSeparator;
            }
        }
        return ret.toString();
    }

    /**
     * Determine the debug setting for JspServlet based on the iAS log level.
     */
    private String getWaspLogLevel() {
        Level level = _logger.getLevel();
        if (level == null) {
            return "warning";
        }

        if (level.equals(WARNING)) {
            return "warning";
        }

        if (level.equals(FINE)) {
            return "information";
        }

        if (level.equals(FINER) || level.equals(Level.FINEST)) {
            return "debug";
        }

        return "warning";
    }

    private void startCacheManager(WebModule webModule) {

        SunWebApp bean = webModule.getIasWebAppConfigBean();

        // Configure the cache, cache-mapping and other settings
        if (bean != null) {
            CacheManager cm = null;
            try {
                cm = CacheModule.configureResponseCache(webModule, bean);
            } catch (Exception ee) {
                _logger.log(WARNING, LogFacade.CACHE_MRG_EXCEPTION, ee);
            }

            if (cm != null) {
                try {
                    // first start the CacheManager, if enabled
                    cm.start();
                    _logger.log(FINE, LogFacade.CACHE_MANAGER_STARTED);
                    // set this manager as a context attribute so that
                    // caching filters/tags can find it
                    ServletContext ctxt = webModule.getServletContext();
                    ctxt.setAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME, cm);

                } catch (LifecycleException ee) {
                    _logger.log(WARNING, ee.getMessage(), ee.getCause());
                }
            }
        }
    }

    private void stopCacheManager(WebModule webModule) {
        ServletContext ctxt = webModule.getServletContext();
        CacheManager cm = (CacheManager) ctxt.getAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME);
        if (cm != null) {
            try {
                cm.stop();
                _logger.log(FINE, LogFacade.CACHE_MANAGER_STOPPED);
                ctxt.removeAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME);
            } catch (LifecycleException ee) {
                _logger.log(WARNING, ee.getMessage(), ee.getCause());
            }
        }
    }

    /**
     * Configures the given web module's DefaultServlet with the applicable web properties from sun-web.xml.
     */
    private void configureDefaultServlet(WebModule webModule) {

        // Find the DefaultServlet
        Wrapper wrapper = (Wrapper) webModule.findChild("default");
        if (wrapper == null) {
            return;
        }

        String servletClass = wrapper.getServletClassName();
        if (servletClass == null || !servletClass.equals(Globals.DEFAULT_SERVLET_CLASS_NAME)) {
            return;
        }

        String fileEncoding = webModule.getFileEncoding();
        if (fileEncoding != null) {
            wrapper.addInitParameter("fileEncoding", fileEncoding);
        }
    }
}
