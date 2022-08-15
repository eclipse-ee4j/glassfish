/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.LocaleEncodingMappingDescriptor;
import com.sun.enterprise.deployment.LocaleEncodingMappingListDescriptor;
import com.sun.enterprise.deployment.SecurityRoleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.node.AbstractBundleNode;
import com.sun.enterprise.deployment.node.AdministeredObjectDefinitionNode;
import com.sun.enterprise.deployment.node.ConnectionFactoryDefinitionNode;
import com.sun.enterprise.deployment.node.ContextServiceDefinitionNode;
import com.sun.enterprise.deployment.node.DataSourceDefinitionNode;
import com.sun.enterprise.deployment.node.EjbLocalReferenceNode;
import com.sun.enterprise.deployment.node.EjbReferenceNode;
import com.sun.enterprise.deployment.node.EntityManagerFactoryReferenceNode;
import com.sun.enterprise.deployment.node.EntityManagerReferenceNode;
import com.sun.enterprise.deployment.node.EnvEntryNode;
import com.sun.enterprise.deployment.node.JMSConnectionFactoryDefinitionNode;
import com.sun.enterprise.deployment.node.JMSDestinationDefinitionNode;
import com.sun.enterprise.deployment.node.JndiEnvRefNode;
import com.sun.enterprise.deployment.node.LifecycleCallbackNode;
import com.sun.enterprise.deployment.node.MailSessionNode;
import com.sun.enterprise.deployment.node.ManagedExecutorDefinitionNode;
import com.sun.enterprise.deployment.node.ManagedScheduledExecutorDefinitionNode;
import com.sun.enterprise.deployment.node.ManagedThreadFactoryDefinitionNode;
import com.sun.enterprise.deployment.node.MessageDestinationNode;
import com.sun.enterprise.deployment.node.MessageDestinationRefNode;
import com.sun.enterprise.deployment.node.ResourceEnvRefNode;
import com.sun.enterprise.deployment.node.ResourceRefNode;
import com.sun.enterprise.deployment.node.SecurityRoleNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.web.AppListenerDescriptor;
import com.sun.enterprise.deployment.web.ContextParameter;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.web.MimeMapping;
import com.sun.enterprise.deployment.web.NameValuePair;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.ServletFilterMapping;
import com.sun.enterprise.deployment.web.SessionConfig;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.web.deployment.descriptor.AppListenerDescriptorImpl;
import org.glassfish.web.deployment.descriptor.ErrorPageDescriptor;
import org.glassfish.web.deployment.descriptor.JspConfigDescriptorImpl;
import org.glassfish.web.deployment.descriptor.LoginConfigurationImpl;
import org.glassfish.web.deployment.descriptor.MimeMappingDescriptor;
import org.glassfish.web.deployment.descriptor.SecurityConstraintImpl;
import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;
import org.glassfish.web.deployment.descriptor.ServletFilterMappingDescriptor;
import org.glassfish.web.deployment.descriptor.SessionConfigDescriptor;
import org.glassfish.web.deployment.descriptor.TagLibConfigurationDescriptor;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.TagNames.ADMINISTERED_OBJECT;
import static com.sun.enterprise.deployment.xml.TagNames.CONNECTION_FACTORY;
import static com.sun.enterprise.deployment.xml.TagNames.DATA_SOURCE;
import static com.sun.enterprise.deployment.xml.TagNames.JMS_CONNECTION_FACTORY;
import static com.sun.enterprise.deployment.xml.TagNames.JMS_DESTINATION;
import static com.sun.enterprise.deployment.xml.TagNames.MAIL_SESSION;
import static com.sun.enterprise.deployment.xml.TagNames.MESSAGE_DESTINATION;
import static com.sun.enterprise.deployment.xml.TagNames.POST_CONSTRUCT;
import static com.sun.enterprise.deployment.xml.TagNames.PRE_DESTROY;
import static org.glassfish.web.deployment.xml.WebTagNames.JSPCONFIG;
import static org.glassfish.web.deployment.xml.WebTagNames.LOCALE_ENCODING_MAPPING;
import static org.glassfish.web.deployment.xml.WebTagNames.TAGLIB;
import static org.omnifaces.concurrent.deployment.ConcurrencyConstants.CONTEXT_SERVICE;
import static org.omnifaces.concurrent.deployment.ConcurrencyConstants.MANAGED_EXECUTOR;
import static org.omnifaces.concurrent.deployment.ConcurrencyConstants.MANAGED_SCHEDULED_EXECUTOR;
import static org.omnifaces.concurrent.deployment.ConcurrencyConstants.MANAGED_THREAD_FACTORY;

/**
 * This node is responsible for handling the web-common xml tree
 *
 * @author Shing Wai Chan
 * @version
 */
public abstract class WebCommonNode<T extends WebBundleDescriptorImpl> extends AbstractBundleNode<T> {
    public final static String SPEC_VERSION = "6.0";

    protected T descriptor;
    private Map<String, Vector<String>> servletMappings;

    /** Creates new WebBundleNode */
    protected WebCommonNode() {
        super();

        registerElementHandler(new XMLElement(TagNames.ENVIRONMENT_PROPERTY), EnvEntryNode.class);
        registerElementHandler(new XMLElement(TagNames.EJB_REFERENCE), EjbReferenceNode.class);
        registerElementHandler(new XMLElement(TagNames.EJB_LOCAL_REFERENCE), EjbLocalReferenceNode.class);
        JndiEnvRefNode<?> serviceRefNode = serviceLocator.getService(JndiEnvRefNode.class, WebServicesTagNames.SERVICE_REF);
        if (serviceRefNode != null) {
            registerElementHandler(new XMLElement(WebServicesTagNames.SERVICE_REF), serviceRefNode.getClass(),
                    "addServiceReferenceDescriptor");
        }
        registerElementHandler(new XMLElement(TagNames.RESOURCE_REFERENCE), ResourceRefNode.class, "addResourceReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.RESOURCE_ENV_REFERENCE), ResourceEnvRefNode.class,
                "addResourceEnvReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.MESSAGE_DESTINATION_REFERENCE), MessageDestinationRefNode.class,
                "addMessageDestinationReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.PERSISTENCE_CONTEXT_REF), EntityManagerReferenceNode.class,
                "addEntityManagerReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.PERSISTENCE_UNIT_REF), EntityManagerFactoryReferenceNode.class,
                "addEntityManagerFactoryReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.ROLE), SecurityRoleNode.class, "addRole");
        registerElementHandler(new XMLElement(WebTagNames.SERVLET), ServletNode.class);
        registerElementHandler(new XMLElement(WebTagNames.SERVLET_MAPPING), ServletMappingNode.class);
        registerElementHandler(new XMLElement(WebTagNames.SESSION_CONFIG), SessionConfigNode.class);
        registerElementHandler(new XMLElement(WebTagNames.MIME_MAPPING), MimeMappingNode.class, "addMimeMapping");
        registerElementHandler(new XMLElement(WebTagNames.CONTEXT_PARAM), InitParamNode.class, "addContextParameter");
        registerElementHandler(new XMLElement(WebTagNames.SECURITY_CONSTRAINT), SecurityConstraintNode.class, "addSecurityConstraint");
        registerElementHandler(new XMLElement(WebTagNames.FILTER), FilterNode.class, "addServletFilter");
        registerElementHandler(new XMLElement(WebTagNames.FILTER_MAPPING), FilterMappingNode.class, "addServletFilterMapping");
        registerElementHandler(new XMLElement(WebTagNames.LISTENER), ListenerNode.class, "addAppListenerDescriptor");
        registerElementHandler(new XMLElement(WebTagNames.ERROR_PAGE), ErrorPageNode.class, "addErrorPageDescriptor");
        registerElementHandler(new XMLElement(WebTagNames.LOGIN_CONFIG), LoginConfigNode.class);
        // for backward compatibility, from Servlet 2.4 the taglib element is in jsp-config
        registerElementHandler(new XMLElement(TAGLIB), TagLibNode.class);
        registerElementHandler(new XMLElement(JSPCONFIG), JspConfigNode.class);
        registerElementHandler(new XMLElement(LOCALE_ENCODING_MAPPING), LocaleEncodingMappingNode.class, "addLocaleEncodingMappingDescriptor");
        registerElementHandler(new XMLElement(MESSAGE_DESTINATION), MessageDestinationNode.class, "addMessageDestination");
        registerElementHandler(new XMLElement(POST_CONSTRUCT), LifecycleCallbackNode.class, "addPostConstructDescriptor");
        registerElementHandler(new XMLElement(PRE_DESTROY), LifecycleCallbackNode.class, "addPreDestroyDescriptor");
        registerElementHandler(new XMLElement(DATA_SOURCE), DataSourceDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(CONNECTION_FACTORY), ConnectionFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(JMS_CONNECTION_FACTORY), JMSConnectionFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(JMS_DESTINATION), JMSDestinationDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(MAIL_SESSION), MailSessionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(ADMINISTERED_OBJECT), AdministeredObjectDefinitionNode.class, "addResourceDescriptor");

        registerElementHandler(new XMLElement(MANAGED_EXECUTOR), ManagedExecutorDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(MANAGED_THREAD_FACTORY), ManagedThreadFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(MANAGED_SCHEDULED_EXECUTOR), ManagedScheduledExecutorDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(CONTEXT_SERVICE), ContextServiceDefinitionNode.class, "addResourceDescriptor");
    }

    /**
     * Adds a new DOL descriptor instance to the descriptor instance associated with this XMLNode
     *
     * @param newDescriptor the new descriptor
     */
    @Override
    public void addDescriptor(Object newDescriptor) {
        Logger logger = DOLUtils.getDefaultLogger();
        if (newDescriptor instanceof EjbReferenceDescriptor) {
            descriptor.addEjbReferenceDescriptor((EjbReferenceDescriptor) newDescriptor);
        } else if (newDescriptor instanceof EnvironmentProperty) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding env entry" + newDescriptor);
            }
            descriptor.addEnvironmentProperty((EnvironmentProperty) newDescriptor);
        } else if (newDescriptor instanceof WebComponentDescriptor) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding web component" + newDescriptor);
            }
            descriptor.addWebComponentDescriptor((WebComponentDescriptor) newDescriptor);
        } else if (newDescriptor instanceof TagLibConfigurationDescriptor) {
            // for backward compatibility with 2.2 and 2.3 specs, we need to be able
            // to read tag lib under web-app. Starting with 2.4, the tag moved under jsp-config
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding taglib component " + newDescriptor);
            }
            if (descriptor.getJspConfigDescriptor() == null) {
                descriptor.setJspConfigDescriptor(new JspConfigDescriptorImpl());
            }
            descriptor.getJspConfigDescriptor().addTagLib((TagLibConfigurationDescriptor) newDescriptor);
        } else if (newDescriptor instanceof JspConfigDescriptorImpl) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding JSP Config Descriptor" + newDescriptor);
            }
            if (descriptor.getJspConfigDescriptor() != null) {
                throw new RuntimeException("Has more than one jsp-config element!");
            }
            descriptor.setJspConfigDescriptor((JspConfigDescriptorImpl) newDescriptor);
        } else if (newDescriptor instanceof LoginConfiguration) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding Login Config Descriptor" + newDescriptor);
            }
            if (descriptor.getLoginConfiguration() != null) {
                throw new RuntimeException("Has more than one login-config element!");
            }
            descriptor.setLoginConfiguration((LoginConfiguration) newDescriptor);
        } else if (newDescriptor instanceof SessionConfig) {
            if (descriptor.getSessionConfig() != null) {
                throw new RuntimeException("Has more than one session-config element!");
            }
            descriptor.setSessionConfig((SessionConfig) newDescriptor);
        } else {
            super.addDescriptor(newDescriptor);
        }
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (WebTagNames.WELCOME_FILE.equals(element.getQName())) {
            descriptor.addWelcomeFile(value);
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * add a servelt mapping for one of the servlet of this bundle
     *
     * @param servletName the servlet the mapping applies to
     * @param urlPattern the url pattern mapping
     */
    void addServletMapping(String servletName, String urlPattern) {
        if (servletMappings == null) {
            servletMappings = new HashMap<>();
        }
        if (servletMappings.containsKey(servletName)) {
            servletMappings.get(servletName).add(urlPattern);
        } else {
            Vector<String> mappings = new Vector<>();
            mappings.add(urlPattern);
            servletMappings.put(servletName, mappings);
        }
    }

    /**
     * receives notification of the end of an XML element by the Parser
     *
     * @param element the xml tag identification
     * @return true if this node is done processing the XML sub tree
     */
    @Override
    public boolean endElement(XMLElement element) {
        if (WebTagNames.DISTRIBUTABLE.equals(element.getQName())) {
            descriptor.setDistributable(true);
            return false;
        }
        boolean allDone = super.endElement(element);
        if (allDone && servletMappings != null) {
            for (String servletName : servletMappings.keySet()) {
                Vector<String> mappings = servletMappings.get(servletName);
                WebComponentDescriptor servlet = descriptor.getWebComponentByCanonicalName(servletName);
                if (servlet != null) {
                    for (String mapping2 : mappings) {
                        servlet.addUrlPattern(mapping2);
                    }
                } else {
                    throw new RuntimeException("There is no web component by the name of " + servletName + " here.");
                }
            }
        }
        return allDone;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param webBundleDesc descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, T webBundleDesc) {
        Node jarNode = super.writeDescriptor(parent, webBundleDesc);
        if (webBundleDesc.isDistributable()) {
            appendChild(jarNode, WebTagNames.DISTRIBUTABLE);
        }

        // context-param*
        addInitParam(jarNode, WebTagNames.CONTEXT_PARAM, webBundleDesc.getContextParametersSet());

        // filter*
        FilterNode filterNode = new FilterNode();
        for (Object element : webBundleDesc.getServletFilters()) {
            filterNode.writeDescriptor(jarNode, WebTagNames.FILTER, (ServletFilterDescriptor) element);
        }

        // filter-mapping*
        FilterMappingNode filterMappingNode = new FilterMappingNode();
        for (ServletFilterMapping element : webBundleDesc.getServletFilterMappings()) {
            filterMappingNode.writeDescriptor(jarNode, WebTagNames.FILTER_MAPPING, (ServletFilterMappingDescriptor) element);
        }

        // listener*
        Vector<AppListenerDescriptor> appListeners = webBundleDesc.getAppListenerDescriptors();
        if (!appListeners.isEmpty()) {
            ListenerNode listenerNode = new ListenerNode();
            for (AppListenerDescriptor appListener : appListeners) {
                listenerNode.writeDescriptor(jarNode, WebTagNames.LISTENER, (AppListenerDescriptorImpl) appListener);
            }
        }

        Set<WebComponentDescriptor> servlets = webBundleDesc.getWebComponentDescriptors();
        if (!servlets.isEmpty()) {
            // servlet*
            ServletNode servletNode = new ServletNode();
            for (WebComponentDescriptor aServlet : servlets) {
                servletNode.writeDescriptor(jarNode, aServlet);
            }

            // servlet-mapping*
            for (WebComponentDescriptor aServlet : servlets) {
                for (String pattern : aServlet.getUrlPatternsSet()) {
                    Node mappingNode = appendChild(jarNode, WebTagNames.SERVLET_MAPPING);
                    appendTextChild(mappingNode, WebTagNames.SERVLET_NAME, aServlet.getCanonicalName());

                    // If URL Pattern does not start with "/" then
                    // prepend it (for 1.2 Web apps)
                    if (webBundleDesc.getSpecVersion().equals("2.2")) {
                        if (!pattern.startsWith("/") && !pattern.startsWith("*.")) {
                            pattern = "/" + pattern;
                        }
                    }
                    appendTextChild(mappingNode, WebTagNames.URL_PATTERN, pattern);
                }
            }
        }

        // mime-mapping*
        MimeMappingNode mimeNode = new MimeMappingNode();
        for (Enumeration<MimeMapping> e = webBundleDesc.getMimeMappings(); e.hasMoreElements();) {
            // there's no other implementation
            MimeMappingDescriptor mimeMapping = (MimeMappingDescriptor) e.nextElement();
            mimeNode.writeDescriptor(jarNode, WebTagNames.MIME_MAPPING, mimeMapping);
        }

        // welcome-file-list?
        Enumeration<String> welcomeFiles = webBundleDesc.getWelcomeFiles();
        if (welcomeFiles.hasMoreElements()) {
            Node welcomeList = appendChild(jarNode, WebTagNames.WELCOME_FILE_LIST);
            while (welcomeFiles.hasMoreElements()) {
                appendTextChild(welcomeList, WebTagNames.WELCOME_FILE, welcomeFiles.nextElement());
            }
        }

        // error-page*
        Enumeration<ErrorPageDescriptor> errorPages = webBundleDesc.getErrorPageDescriptors();
        if (errorPages.hasMoreElements()) {
            ErrorPageNode errorPageNode = new ErrorPageNode();
            while (errorPages.hasMoreElements()) {
                errorPageNode.writeDescriptor(jarNode, WebTagNames.ERROR_PAGE, errorPages.nextElement());
            }
        }

        // jsp-config *
        JspConfigDescriptorImpl jspConf = webBundleDesc.getJspConfigDescriptor();
        if (jspConf != null) {
            JspConfigNode ln = new JspConfigNode();
            ln.writeDescriptor(jarNode, JSPCONFIG, jspConf);
        }

        // security-constraint*
        Enumeration<SecurityConstraint> securityConstraints = webBundleDesc.getSecurityConstraints();
        if (securityConstraints.hasMoreElements()) {
            SecurityConstraintNode scNode = new SecurityConstraintNode();
            while (securityConstraints.hasMoreElements()) {
                SecurityConstraintImpl sc = (SecurityConstraintImpl) securityConstraints.nextElement();
                scNode.writeDescriptor(jarNode, WebTagNames.SECURITY_CONSTRAINT, sc);
            }
        }

        // login-config ?
        LoginConfigurationImpl lci = (LoginConfigurationImpl) webBundleDesc.getLoginConfiguration();
        if (lci != null) {
            LoginConfigNode lcn = new LoginConfigNode();
            lcn.writeDescriptor(jarNode, WebTagNames.LOGIN_CONFIG, lci);
        }

        // security-role*
        Enumeration<SecurityRoleDescriptor> roles = webBundleDesc.getSecurityRoles();
        if (roles.hasMoreElements()) {
            SecurityRoleNode srNode = new SecurityRoleNode();
            while (roles.hasMoreElements()) {
                SecurityRoleDescriptor role = roles.nextElement();
                srNode.writeDescriptor(jarNode, TagNames.ROLE, role);
            }
        }

        // env-entry*
        writeEnvEntryDescriptors(jarNode, webBundleDesc.getEnvironmentProperties().iterator());

        // ejb-ref * and ejb-local-ref*
        writeEjbReferenceDescriptors(jarNode, webBundleDesc.getEjbReferenceDescriptors().iterator());

        // service-ref*
        writeServiceReferenceDescriptors(jarNode, webBundleDesc.getServiceReferenceDescriptors().iterator());

        // resource-ref*
        writeResourceRefDescriptors(jarNode, webBundleDesc.getResourceReferenceDescriptors().iterator());

        // resource-env-ref*
        writeResourceEnvRefDescriptors(jarNode, webBundleDesc.getResourceEnvReferenceDescriptors().iterator());

        // message-destination-ref*
        writeMessageDestinationRefDescriptors(jarNode, webBundleDesc.getMessageDestinationReferenceDescriptors().iterator());

        // persistence-context-ref*
        writeEntityManagerReferenceDescriptors(jarNode, webBundleDesc.getEntityManagerReferenceDescriptors().iterator());

        // persistence-unit-ref*
        writeEntityManagerFactoryReferenceDescriptors(jarNode, webBundleDesc.getEntityManagerFactoryReferenceDescriptors().iterator());

        // post-construct
        writeLifeCycleCallbackDescriptors(jarNode, POST_CONSTRUCT, webBundleDesc.getPostConstructDescriptors());

        // pre-destroy
        writeLifeCycleCallbackDescriptors(jarNode, PRE_DESTROY, webBundleDesc.getPreDestroyDescriptors());

        // all descriptors (includes DSD, MSD, JMSCFD, JMSDD,AOD, CFD)*
        writeResourceDescriptors(jarNode, webBundleDesc.getAllResourcesDescriptors().iterator());

        // message-destination*
        writeMessageDestinations(jarNode, webBundleDesc.getMessageDestinations().iterator());

        // locale-encoding-mapping-list
        LocaleEncodingMappingListDescriptor lemListDesc = webBundleDesc.getLocaleEncodingMappingListDescriptor();
        if (lemListDesc != null) {
            Node lemList = appendChild(jarNode, WebTagNames.LOCALE_ENCODING_MAPPING_LIST);
            LocaleEncodingMappingNode lemNode = new LocaleEncodingMappingNode();
            for (LocaleEncodingMappingDescriptor lemDesc : lemListDesc.getLocaleEncodingMappingSet()) {
                lemNode.writeDescriptor(lemList, LOCALE_ENCODING_MAPPING, lemDesc);
            }
        }

        if (webBundleDesc.getSessionConfig() != null) {
            SessionConfigNode scNode = new SessionConfigNode();
            scNode.writeDescriptor(jarNode, WebTagNames.SESSION_CONFIG, (SessionConfigDescriptor) webBundleDesc.getSessionConfig());
        }

        return jarNode;
    }

    static void addInitParam(Node parentNode, String nodeName, Set<ContextParameter> initParams) {
        if (!initParams.isEmpty()) {
            InitParamNode initParamNode = new InitParamNode();
            for (ContextParameter initParam : initParams) {
                EnvironmentProperty ep = (EnvironmentProperty) initParam;
                initParamNode.writeDescriptor(parentNode, nodeName, ep);
            }
        }
    }

    static void addInitParam(Node parentNode, String nodeName, Enumeration<NameValuePair> initParams) {
        InitParamNode initParamNode = new InitParamNode();
        while (initParams.hasMoreElements()) {
            EnvironmentProperty ep = (EnvironmentProperty) initParams.nextElement();
            initParamNode.writeDescriptor(parentNode, nodeName, ep);
        }
    }

    /**
     * @return the default spec version level this node complies to
     */
    @Override
    public String getSpecVersion() {
        return SPEC_VERSION;
    }

}