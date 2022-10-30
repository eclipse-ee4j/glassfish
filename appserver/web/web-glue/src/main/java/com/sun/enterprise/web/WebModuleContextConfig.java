/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.web.ContextParameter;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.apache.catalina.Authenticator;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.authenticator.DigestAuthenticator;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.startup.ContextConfig;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.valve.GlassFishValve;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Context, and the associated defined servlets.
 *
 * @author Jean-Francois Arcand
 */
public class WebModuleContextConfig extends ContextConfig {

    private static final String DEFAULT_DIGEST_ALGORITHM = "default-digest-algorithm";

    private static final Logger logger = LogFacade.getLogger();

    protected static final ResourceBundle rb = logger.getResourceBundle();


    public final static int CHILDREN = 0;
    public final static int SERVLET_MAPPINGS = 1;
    public final static int LOCAL_EJBS = 2;
    public final static int EJBS = 3;
    public final static int ENVIRONMENTS = 4;
    public final static int ERROR_PAGES = 5;
    public final static int FILTER_DEFS = 6;
    public final static int FILTER_MAPS = 7;
    public final static int APPLICATION_LISTENERS = 8;
    public final static int RESOURCES = 9;
    public final static int APPLICATION_PARAMETERS = 10;
    public final static int MESSAGE_DESTINATIONS = 11;
    public final static int MESSAGE_DESTINATION_REFS = 12;
    public final static int MIME_MAPPINGS = 13;

    protected ServiceLocator services;

    /**
     * The DOL object representing the web.xml content.
     */
    private WebBundleDescriptorImpl webBundleDescriptor;

    /**
     * Resource references from outside the .war
     */
    private final Collection<ResourceReferenceDescriptor> resRefs = new HashSet<>();

    /**
     * Environment properties from outside the .war
     */
    private final Collection<EnvironmentProperty> envProps = new HashSet<>();

    /**
     * Customized <code>ContextConfig</code> which use the DOL for deployment.
     */
    public WebModuleContextConfig(ServiceLocator services) {
        synchronized (this) {
            this.services = services;
        }
    }


    /**
     * Set the DOL object associated with this class.
     */
    public void setDescriptor(WebBundleDescriptorImpl wbd) {
        webBundleDescriptor = wbd;
    }

    /**
     * Return the WebBundleDescriptor
     */
    public WebBundleDescriptorImpl getDescriptor() {
        return webBundleDescriptor;
    }


    protected synchronized void configureResource() throws LifecycleException {
        List<ApplicationParameter> appParams = context.findApplicationParameters();
        ContextParameter contextParam;
        synchronized (appParams) {
            for (ApplicationParameter appParam : appParams) {
                contextParam = new EnvironmentProperty(appParam.getName(), appParam.getValue(),
                    appParam.getDescription());
                webBundleDescriptor.addContextParameter(contextParam);
            }
        }

        ContextEnvironment[] envs = context.findEnvironments();
        EnvironmentProperty envEntry;

        for (ContextEnvironment env : envs) {
            envEntry = new EnvironmentProperty(env.getName(), env.getValue(), env.getDescription(), env.getType());
            if (env.getValue() != null) {
                envEntry.setValue(env.getValue());
            }
            webBundleDescriptor.addEnvironmentProperty(envEntry);
            envProps.add(envEntry);
        }

        ContextResource[] resources = context.findResources();
        Set<ResourceReferenceDescriptor> rrs = webBundleDescriptor.getResourceReferenceDescriptors();
        for (ContextResource resource : resources) {
            ResourceReferenceDescriptor descriptor = new ResourceReferenceDescriptor(resource.getName(),
                resource.getDescription(), resource.getType());
            descriptor.setJndiName(new SimpleJndiName(resource.getName()));
            for (ResourceReferenceDescriptor rr : rrs) {
                if (resource.getName().equals(rr.getName())) {
                    descriptor.setJndiName(rr.getJndiName());
                    ResourcePrincipalDescriptor rp = rr.getResourcePrincipal();
                    if (rp != null) {
                        descriptor.setResourcePrincipal(new ResourcePrincipalDescriptor(rp.getName(), rp.getPassword()));
                    }
                }
            }
            descriptor.setAuthorization(resource.getAuth());
            webBundleDescriptor.addResourceReferenceDescriptor(descriptor);
            resRefs.add(descriptor);
        }
    }


    /**
     * Process a "start" event for this Context - in background
     */
    @Override
    protected synchronized void start() throws LifecycleException {
        configureResource();

        context.setConfigured(false);

        ComponentEnvManager namingMgr = services
            .getService(com.sun.enterprise.container.common.spi.util.ComponentEnvManager.class);
        if (namingMgr != null) {
            try {
                boolean webBundleContainsEjbs
                    = !webBundleDescriptor.getExtensionsDescriptors(EjbBundleDescriptor.class).isEmpty();

                // If .war contains EJBs, .war-defined dependencies have already been bound by
                // EjbDeployer, so just add the dependencies from outside the .war
                if (webBundleContainsEjbs) {
                    namingMgr.addToComponentNamespace(webBundleDescriptor, envProps, resRefs);
                } else {
                    namingMgr.bindToComponentNamespace(webBundleDescriptor);
                }

                String componentId = namingMgr.getComponentEnvId(webBundleDescriptor);
                ((WebModule) context).setComponentId(componentId);
            } catch (NamingException ne) {
                throw new LifecycleException(ne);
            }
        }

        try {
            // When context root = "/"
            if (webBundleDescriptor != null) {
                TomcatDeploymentConfig.configureWebModule((WebModule) context, webBundleDescriptor);
            }
            authenticatorConfig();
            managerConfig();
            context.setConfigured(true);
        } catch (Throwable t) {
            // clean up naming in case of errors
            unbindFromComponentNamespace(namingMgr);

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof LifecycleException) {
                throw (LifecycleException) t;
            } else {
                throw new LifecycleException(t);
            }
        }
    }


    /**
     * Always sets up an Authenticator regardless of any security constraints.
     */
    @Override
    protected synchronized void authenticatorConfig()
            throws LifecycleException {

        LoginConfig loginConfig = context.getLoginConfig();
        if (loginConfig == null) {
            loginConfig = new LoginConfig("NONE", null, null, null);
            context.setLoginConfig(loginConfig);
        }

        // Has an authenticator been configured already?
        if (context instanceof Authenticator) {
            return;
        }
        if (context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                GlassFishValve basic = pipeline.getBasic();
                if ((basic != null) && (basic instanceof Authenticator)) {
                    return;
                }
                GlassFishValve valves[] = pipeline.getValves();
                for (GlassFishValve element : valves) {
                    if (element instanceof Authenticator) {
                        return;
                    }
                }
            }
        } else {
            return;     // Cannot install a Valve even if it would be needed
        }

        // Has a Realm been configured for us to authenticate against?
        /* START IASRI 4856062
        if (context.getRealm() == null) {
        */
        // BEGIN IASRI 4856062
        Realm rlm = context.getRealm();
        if (rlm == null) {
        // END IASRI 4856062
            String realmName = (context.getLoginConfig() != null) ?
                context.getLoginConfig().getRealmName() : null;
            if (realmName != null && !realmName.isEmpty()) {
                String msg = rb.getString(LogFacade.MISSING_REALM);
                throw new LifecycleException(
                        MessageFormat.format(msg, realmName));
            }
            return;
        }

        // BEGIN IASRI 4856062
        // If a realm is available set its name in the Realm(Adapter)
        rlm.setRealmName(loginConfig.getRealmName(),
                         loginConfig.getAuthMethod());

        // END IASRI 4856062

        /*
         * First check to see if there is a custom mapping for the login
         * method. If so, use it. Otherwise, check if there is a mapping in
         * org/apache/catalina/startup/Authenticators.properties.
         */
        GlassFishValve authenticator = null;
        if (customAuthenticators != null) {
            authenticator = (GlassFishValve)
                customAuthenticators.get(loginConfig.getAuthMethod());
        }

        if (authenticator == null) {
            // Identify the class name of the Valve we should configure
            String authenticatorName = null;

            // BEGIN RIMOD 4808402
            // If login-config is given but auth-method is null, use NONE
            // so that NonLoginAuthenticator is picked
            String authMethod = loginConfig.getAuthMethod();
            if (authMethod == null) {
                authMethod = "NONE";
            }
            authenticatorName = authenticators.getProperty(authMethod);
            // END RIMOD 4808402
            /* RIMOD 4808402
            authenticatorName =
                    authenticators.getProperty(loginConfig.getAuthMethod());
            */

            if (authenticatorName == null) {
                String msg = rb.getString(LogFacade.AUTHENTICATOR_MISSING);
                throw new LifecycleException(MessageFormat.format(msg,
                    loginConfig.getAuthMethod()));
            }

            // Instantiate and install an Authenticator of the requested class
            try {
                Class<?> authenticatorClass = Class.forName(authenticatorName);
                authenticator = (GlassFishValve) authenticatorClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                String msg = rb.getString(LogFacade.AUTHENTICATOR_INSTANTIATE_ERROR);
                throw new LifecycleException(
                    MessageFormat.format(msg, authenticatorName),
                    e);
            }
        }

        if (authenticator != null && context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                ((ContainerBase) context).addValve(authenticator);
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST,
                        LogFacade.AUTHENTICATOR_CONFIGURED,
                        loginConfig.getAuthMethod());
                }
            }
        }

        if (authenticator instanceof DigestAuthenticator) {
            Config config = services.getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
            SecurityService securityService = config.getSecurityService();
            String digestAlgorithm = null;
            if (securityService != null) {
                digestAlgorithm = securityService.getPropertyValue(DEFAULT_DIGEST_ALGORITHM);
            }
            if (digestAlgorithm != null) {
                DigestAuthenticator.setAlgorithm(digestAlgorithm);
            }
        }
    }


    /**
     * Process the default configuration file, if it exists.
     * The default config must be read with the container loader - so
     * container servlets can be loaded
     */
    @Override
    protected void defaultConfig() {

    }


    /**
     * Process a "stop" event for this Context.
     */
    @Override
    protected synchronized void stop() {

        super.stop();
        ComponentEnvManager namingMgr = services.getService(
            com.sun.enterprise.container.common.spi.util.ComponentEnvManager.class);
        unbindFromComponentNamespace(namingMgr);

    }

    private void unbindFromComponentNamespace(ComponentEnvManager namingMgr) {
        if (namingMgr != null) {
            try {
                namingMgr.unbindFromComponentNamespace(webBundleDescriptor);
            } catch (javax.naming.NamingException ex) {
                String msg = rb.getString(LogFacade.UNBIND_NAME_SPACE_ERROR);
                msg = MessageFormat.format(msg, context.getName());
                logger.log(Level.WARNING, msg, ex);
            }
        }
    }


}
