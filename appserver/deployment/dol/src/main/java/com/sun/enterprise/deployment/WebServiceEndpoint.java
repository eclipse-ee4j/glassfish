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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;
import com.sun.enterprise.deployment.runtime.ws.ReliabilityConfig;
import com.sun.enterprise.deployment.types.HandlerChainContainer;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.UserDataConstraint;

import jakarta.servlet.http.HttpServletRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.internal.api.Globals;


/**
 * This class represents information about a web service endpoint.
 * Represents a single port-component in a webservice in webservices.xml
 *
 * @author Rama Pulavarthi
 * @author Kenneth Saks
 */
public class WebServiceEndpoint extends Descriptor implements HandlerChainContainer {

    private static final long serialVersionUID = 1L;
    private static final String TRANSPORT_INTEGRAL = "INTEGRAL";
    private static final String TRANSPORT_CONFIDENTIAL = "CONFIDENTIAL";
    private static final String CLIENT_CERT = "CLIENT-CERT";

    public static final String PUBLISHING_SUBCONTEXT = "__container$publishing$subctx";

    // Unique endpoint name within all the endpoints in the same module
    private String endpointName;

    private String serviceEndpointInterface;

    private QName wsdlPort;
    private String wsdlPortNamespacePrefix;

    private QName wsdlService;
    private String wsdlServiceNamespacePrefix;

    private String mtomEnabled;
    private String mtomThreshold;

    private String protocolBinding;
    private boolean securePipeline;

    // Linkage to implementing component.  Must be either stateless session
    // ejb OR servlet.
    private String ejbLink;
    private EjbDescriptor ejbComponentImpl;
    private String webComponentLink;
    private WebComponentDescriptor webComponentImpl;

    // List of handlers associated with this endpoint.
    // Handler order is important and must be preserved.
    // This list hols the handlers specified for JAXRPC based service
    private LinkedList<WebServiceHandler> handlers;

    // This is the handlerchain defined for JAXWS based service
    private LinkedList<WebServiceHandlerChain> handlerChains;

    //This is the addressing element in webservices.xml
    private Addressing addressing;

    //This is the addressing element in webservices.xml
    private RespectBinding respectBinding;

    // The web service in which this endpoint lives
    private WebService webService;

    //
    // Runtime information.
    //

    //
    // Endpoint address uri is used to compose the endpoint address url
    // through which the endpoint can be accessed.  It is required for
    // ejb endpoints and optional for servlet endpoints.  This is because
    // web application's have a standard way of mapping servlets to uris.
    //
    // For ejb endpoints, the uri is relative to root of the
    // public web server (i.e. the first portion of the uri is a context root).
    //
    // E.g. if the web server is listening at http://localhost:8000,
    // an endpoint address URI of StockQuoteService/StockQuotePort would result
    // in an endpoint address of :
    //
    //     http://localhost:8000/StockQuoteService/StockQuotePort
    //
    // The context root portion should not conflict with the context root
    // of any web application.
    //
    // For servlet endpoints, the uri is optional.  If the uri is not specified,
    // it will be derived from the url pattern for the servlet that is
    // implementing this endpoint. The syntax of the servlet
    // endpoint address uri is the same as the syntax for the url-pattern
    // element in the web.xml servlet-mapping. Note that this makes it different
    // than the syntax for ejb endpoint uris, since the servlet endpoint uri
    // given here will be relative to the context root of the servlet's
    // web application.
    //
    private String endpointAddressUri;

    // Optional authentication method for EJB endpoints.
    // Auth method holds an authorization type with one of the following
    // values : BASIC, CLIENT-CERT
    // The BASIC constant is defined on HttpServletRequest.
    // If set to null (default), endpoint does not perform authentication
    private String authMethod;

    // Optional, used by ejb endpoint only.
    // The realm info is in web.xml for servlet endpoint.
    private String realm;

    // Optional control over the level of security at the transport level.
    private String transportGuarantee;

    // The Service QName is derived during j2eec and stored as
    // runtime information.
    private String serviceNamespaceUri;
    private String serviceLocalPart;

    // Only used for web components to store the name of the
    // application-written endpoint implementation class.
    // Derived during deployment.
    private String servletImplClass;

    // Derived during tie generation.
    private String tieClassName;

    // message-security-binding
    private MessageSecurityBindingDescriptor messageSecBindingDesc;

    //reliability-config
    private ReliabilityConfig reliabilityConfig;

    // specified in WLS DD, currently has use only in RMFeature
    private String httpResponseBufferSize;

    // should debugging be allowed on this endpoint
    private String debuggingEnabled = "true";

    // jbi related properties
    private List<NameValuePairDescriptor> props;

    /** Should the wsdl be published? */
    private String wsdlExposed;

    /** Validate request messages? */
    private String validateRequest;

    /** Is streaming attachments feature enabled? */
    private String streamAttachments;

    /** copy constructor */
    public WebServiceEndpoint(WebServiceEndpoint other) {
        super(other);
        endpointName = other.endpointName; // String
        serviceEndpointInterface = other.serviceEndpointInterface; // String
        wsdlPort = other.wsdlPort; // QName
        wsdlPortNamespacePrefix = other.wsdlPortNamespacePrefix;
        wsdlService = other.wsdlService;
        wsdlServiceNamespacePrefix = other.wsdlServiceNamespacePrefix;
        mtomEnabled = other.mtomEnabled;
        protocolBinding = other.protocolBinding;
        ejbLink = other.ejbLink; // String
        ejbComponentImpl = other.ejbComponentImpl; // EjbDescriptor copy as-is
        webComponentLink = other.webComponentLink; // String
        webComponentImpl = other.webComponentImpl; // WebComponentDescriptorImpl copy as-is
        handlers = other.handlers; // copy LinkedList(WebServiceHandler)
        addressing = other.addressing;
        respectBinding = other.respectBinding;

        if (other.handlers == null) {
            handlers = null;
        } else {
            handlers = new LinkedList<>();
            for (WebServiceHandler handler : other.handlers) {
                handlers.addLast(new WebServiceHandler(handler));
            }
        }
        if (other.handlerChains == null) {
            handlers = null;
        } else {
            handlerChains = new LinkedList<>();
            for (WebServiceHandlerChain handlerChain : other.handlerChains) {
                handlerChains.addLast(new WebServiceHandlerChain(handlerChain));
            }
        }

        webService = other.webService; // WebService copy as-is
        endpointAddressUri = other.endpointAddressUri; // String
        authMethod = other.authMethod; // String
        transportGuarantee = other.transportGuarantee; // String
        serviceNamespaceUri = other.serviceNamespaceUri; // String
        serviceLocalPart = other.serviceLocalPart; // String
        servletImplClass = other.servletImplClass; // String
        tieClassName = other.tieClassName; // String
        wsdlExposed = other.wsdlExposed;
    }


    public WebServiceEndpoint() {
        handlers = new LinkedList<>();
        handlerChains = new LinkedList<>();
        authMethod = null;
        wsdlPort = null;
    }


    public void setWebService(WebService service) {
        webService = service;
    }

    public String getMtomThreshold() {
        return mtomThreshold;
    }

    public void setMtomThreshold(String mtomThreshold) {
        this.mtomThreshold = mtomThreshold;
    }

    public WebService getWebService() {
        return webService;
    }

    public Addressing getAddressing() {
        return addressing;
    }

    public void setAddressing(Addressing addressing) {
        this.addressing = addressing;
    }

    public RespectBinding getRespectBinding() {
        return respectBinding;
    }

    public void setRespectBinding(RespectBinding respectBinding) {
        this.respectBinding = respectBinding;
    }

    public void setSecurePipeline() {
        securePipeline = true;
    }

    public boolean hasSecurePipeline() {
        return securePipeline;
    }

    public void setEndpointName(String name) {
        endpointName = name;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setServiceEndpointInterface(String endpointInterface) {
        serviceEndpointInterface = endpointInterface;
    }

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public void setProtocolBinding(String value) {
        WSDolSupport dolSupport = Globals.getDefaultHabitat().getService(WSDolSupport.class);
        if (dolSupport == null) {
            protocolBinding = value;
        } else {
            protocolBinding = dolSupport.getProtocolBinding(value);
        }

    }

    public String getProtocolBinding() {
        WSDolSupport dolSupport = Globals.getDefaultHabitat().getService(WSDolSupport.class);
        if (protocolBinding == null && dolSupport != null) {
            protocolBinding = dolSupport.getProtocolBinding(null);
        }
        return protocolBinding;
    }

    public boolean hasUserSpecifiedProtocolBinding() {
        return protocolBinding != null;
    }

    public void setMtomEnabled(String value) {
        mtomEnabled = value;
    }

    public String getMtomEnabled() {
        return mtomEnabled;
    }

    public void setWsdlService(QName svc, String prefix) {
        wsdlService = svc;
        wsdlServiceNamespacePrefix = prefix;
        serviceNamespaceUri = svc.getNamespaceURI();
        serviceLocalPart = svc.getLocalPart();
    }

    public void setWsdlService(QName service) {
        wsdlService = service;
        wsdlServiceNamespacePrefix = service.getPrefix();
        serviceNamespaceUri = service.getNamespaceURI();
        serviceLocalPart = service.getLocalPart();
    }

    public String getWsdlServiceNamespacePrefix() {
        return wsdlServiceNamespacePrefix;
    }

    public boolean hasWsdlServiceNamespacePrefix() {
        return wsdlServiceNamespacePrefix != null;
    }

    public QName getWsdlService() {
        return wsdlService;
    }

    public void setWsdlPort(QName port, String prefix) {
        wsdlPort = port;
        wsdlPortNamespacePrefix = prefix;
    }

    public void setWsdlPort(QName port) {
        wsdlPort = port;
        wsdlPortNamespacePrefix = port.getPrefix();
    }

    public String getWsdlPortNamespacePrefix() {
        return wsdlPortNamespacePrefix;
    }

    public boolean hasWsdlPortNamespacePrefix() {
        return wsdlPortNamespacePrefix != null;
    }

    public boolean hasWsdlPort() {
        return wsdlPort != null;
    }

    public QName getWsdlPort() {
        return wsdlPort;
    }

    public void setMessageSecurityBinding(MessageSecurityBindingDescriptor messageSecBindingDesc) {
        this.messageSecBindingDesc = messageSecBindingDesc;
    }

    public MessageSecurityBindingDescriptor getMessageSecurityBinding() {
        return messageSecBindingDesc;
    }

    public ReliabilityConfig getReliabilityConfig() {
        return reliabilityConfig;
    }

    public void setReliabilityConfig(ReliabilityConfig reliabilityConfig) {
        this.reliabilityConfig = reliabilityConfig;
    }

    public String getHttpResponseBufferSize() {
        return httpResponseBufferSize;
    }

    public void setHttpResponseBufferSize(String httpResponseBufferSize) {
        this.httpResponseBufferSize = httpResponseBufferSize;
    }

    public BundleDescriptor getBundleDescriptor() {
        return getWebService().getBundleDescriptor();
    }

    /**
     * @return true if this endpoint is implemented by any ejb
     */
    public boolean implementedByEjbComponent() {
        return ejbLink != null;
    }

    /**
     * @return true if this endpoint is implemented by a specific ejb
     */
    public boolean implementedByEjbComponent(EjbDescriptor ejb) {
        return ejbLink != null && ejbLink.equals(ejb.getName());
    }

    /**
     * @return true if this endpoint is implemented by any web component
     */
    public boolean implementedByWebComponent() {
        return webComponentLink != null;
    }

    /**
     * @return true if this endpoint is implemented by a specific web component
     */
    public boolean implementedByWebComponent(WebComponentDescriptor webComp) {
        return webComponentLink != null && webComponentLink.equals(webComp.getCanonicalName());
    }

    public String getLinkName() {
        if (implementedByEjbComponent()) {
            return ejbLink;
        } else if (implementedByWebComponent()) {
            return webComponentLink;
        } else {
            return null;
        }
    }

    public void setEjbLink(String link) {
        ejbLink = link;
    }

    public String getEjbLink() {
        return ejbLink;
    }

    public void setEjbComponentImpl(EjbDescriptor ejbComponent) {
        webComponentImpl = null;
        webComponentLink = null;
        ejbLink = ejbComponent.getName();
        ejbComponentImpl = ejbComponent;
    }

    public EjbDescriptor getEjbComponentImpl() {
        return ejbComponentImpl;
    }

    public void setWebComponentLink(String link) {
        webComponentLink = link;
    }

    public String getWebComponentLink() {
        return webComponentLink;
    }

    public void setWebComponentImpl(WebComponentDescriptor webComponent) {
        ejbComponentImpl = null;
        ejbLink = null;
        webComponentLink = webComponent.getCanonicalName();
        webComponentImpl = webComponent;
    }

    public WebComponentDescriptor getWebComponentImpl() {
        return webComponentImpl;
    }

    /**
     * @return true if this endpoint has at least one handler in its handler chain.
     */
    public boolean hasHandlers() {
        return !handlers.isEmpty();
    }

    /**
     * Append handler to end of handler chain for this endpoint.
     */
    public void addHandler(WebServiceHandler handler) {
        handlers.addLast(handler);
    }

    public void removeHandler(WebServiceHandler handler) {
        handlers.remove(handler);
    }

    public void removeHandlerByName(String handlerName) {
        for (Iterator<WebServiceHandler> iter = handlers.iterator(); iter.hasNext();) {
            WebServiceHandler next = iter.next();
            if (next.getHandlerName().equals(handlerName)) {
                iter.remove();
                break;
            }
        }
    }

    /**
     * Get ordered list of WebServiceHandler handler for this endpoint.
     */
    public LinkedList<WebServiceHandler> getHandlers() {
        return handlers;
    }

    /**
     * Get ordered list of WebServiceHandler handler chains for this endpoint.
     */
    @Override
    public LinkedList<WebServiceHandlerChain> getHandlerChain() {
        return handlerChains;
    }

    /**
     * @return true if this endpoint has at least one handler chain
     */
    @Override
    public boolean hasHandlerChain() {
        return !handlerChains.isEmpty();
    }

    /**
     * Append handlerchain to end of handlerchain for this endpoint.
     */
    @Override
    public void addHandlerChain(WebServiceHandlerChain handlerChain) {
        handlerChains.addLast(handlerChain);
    }

    public void removeHandlerChain(WebServiceHandlerChain handlerChain) {
        handlerChains.remove(handlerChain);
    }

    public boolean hasEndpointAddressUri() {
        return endpointAddressUri != null;
    }

    public void setEndpointAddressUri(String uri) {
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        endpointAddressUri = uri;
    }

    public String getEndpointAddressUri() {
        if (implementedByEjbComponent() && !hasEndpointAddressUri()) {
            setEndpointAddressUri("/" + getWebService().getName() + "/" + getEndpointName());
        }
        return endpointAddressUri;
    }

    public boolean isSecure() {
        return hasTransportGuarantee()
            && (transportGuarantee.equals(TRANSPORT_INTEGRAL) || transportGuarantee.equals(TRANSPORT_CONFIDENTIAL));
    }


    // Set to null to indicate "no authentication"
    public void setAuthMethod(String authType) {
        authMethod = authType;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public boolean hasAuthMethod() {
        return authMethod != null;
    }

    public boolean hasBasicAuth() {
        return authMethod != null && authMethod.equals(HttpServletRequest.BASIC_AUTH);
    }

    public boolean hasClientCertAuth() {
        return authMethod != null && authMethod.equals(CLIENT_CERT);
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getRealm() {
        return realm;
    }

    // Set to NONE or null to indicate no transport guarantee
    public void setTransportGuarantee(String guarantee) {
        transportGuarantee = guarantee;
    }

    public String getTransportGuarantee() {
        return transportGuarantee;
    }

    public boolean hasTransportGuarantee() {
        return transportGuarantee != null;
    }

    public void setServiceNamespaceUri(String uri) {
        serviceNamespaceUri = uri;
    }

    public void setServiceLocalPart(String localpart) {
        serviceLocalPart = localpart;
    }

    public boolean hasServiceName() {
        return serviceNamespaceUri != null && serviceLocalPart != null;
    }

    /**
     * @return service QName or null if either part of qname is not set
     */
    public QName getServiceName() {
        // NOTE : Here we do not return getWsdlService to maintain backward
        // compatibility with JAXRPC 1.X
        return hasServiceName() ? new QName(serviceNamespaceUri, serviceLocalPart) : null;
    }

    public boolean hasServletImplClass() {
        return servletImplClass != null;
    }

    public void setServletImplClass(String implClass) {
        servletImplClass = implClass;
    }

    public String getServletImplClass() {
        return servletImplClass;
    }

    public boolean hasTieClassName() {
        return tieClassName != null;
    }

    public void setTieClassName(String tieClass) {
        tieClassName = tieClass;
    }

    public String getTieClassName() {
        return tieClassName;
    }

    public String getDebugging() {
        return debuggingEnabled;
    }

    public void setDebugging(String debuggingEnabled) {
        this.debuggingEnabled = debuggingEnabled;
    }

    public void addProperty(NameValuePairDescriptor newProp) {
        if (props == null) {
            props = new ArrayList<>();
        }
        props.add(newProp);
    }

    public Iterator<NameValuePairDescriptor> getProperties() {
        if (props == null) {
            return null;
        }
        return props.iterator();
    }

    public String getWsdlExposed() {
        return wsdlExposed;
    }

    public void setWsdlExposed(String wsdlExposed) {
        this.wsdlExposed = wsdlExposed;
    }

    public String getValidateRequest() {
        return validateRequest;
    }

    public void setValidateRequest(String validateRequests) {
        this.validateRequest = validateRequests;
    }

    public String getStreamAttachments() {
        return streamAttachments;
    }

    public void setStreamAttachments(String streamAttachments) {
        this.streamAttachments = streamAttachments;
    }

    /**
     * Given the root portion of a URL representing the <protocol>:<host>:<port>
     * of the webserver, return the endpoint address used to make web
     * service invocations on this endpoint.
     */
    public URL composeEndpointAddress(URL root) throws MalformedURLException {
        return composeEndpointAddress(root, null);
    }


    public URL composeEndpointAddress(URL root, String contextRoot) throws MalformedURLException {
        String uri = getEndpointAddressPath(contextRoot);
        return new URL(root.getProtocol(), root.getHost(), root.getPort(), uri);
    }

    public String getEndpointAddressPath() {
        return getEndpointAddressPath(null);
    }


    /**
     * @return the endpoint address path (without http://<host>:<port>)
     * used to make web service invocations on this endpoint .
     */
    private String getEndpointAddressPath(String cr) {
        // Compose file portion of URL depending on endpoint type.
        // The file portion of the URL MUST have a single leading slash.
        // Note that the context root and the endpoint address uri strings
        // from the descriptors may or may not each have a leading slash.
        if (implementedByWebComponent()) {
            if (endpointAddressUri == null) {
                updateServletEndpointRuntime();
            }

            // for servlets, endpoint address uri is relative to
            // web app context root.
            WebBundleDescriptor webBundle = webComponentImpl.getWebBundleDescriptor();
            String contextRoot = cr == null ? webBundle.getContextRoot() : cr;
            if (contextRoot == null) {
                return null;
            }
            if (!contextRoot.startsWith("/")) {
                contextRoot = "/" + contextRoot;
            }
            return contextRoot + (endpointAddressUri.startsWith("/") ? endpointAddressUri : ("/" + endpointAddressUri));
        }
        // If implemented by EJB Component, it will have a default standard endpointAddressUri
        return getEndpointAddressUri();
    }


    /**
     * Convert the contents of the ejb-link or servlet-link element to
     * an object representing the implementation component.
     */
    public boolean resolveComponentLink() {
        boolean resolved = false;
        if (ejbLink == null) {
            if (webComponentLink != null) {
                WebBundleDescriptor webBundle = getWebBundle();
                WebComponentDescriptor webComponent = webBundle.getWebComponentByCanonicalName(webComponentLink);
                if (webComponent != null) {
                    resolved = true;
                    setWebComponentImpl(webComponent);
                }
            }
        } else {
            EjbBundleDescriptor ejbBundle = getEjbBundle();
            if (ejbBundle.hasEjbByName(ejbLink)) {
                resolved = true;
                EjbDescriptor ejb = ejbBundle.getEjbByName(ejbLink);
                setEjbComponentImpl(ejb);
            }
        }
        return resolved;
    }


    /**
     * Generate a URL pointing to the initial wsdl document for this
     * endpoint's web service.
     */
    public URL composeFinalWsdlUrl(URL root) throws MalformedURLException {

        // WSDL for this webservice is published in a subcontext created
        // under the endpoint address uri.  The hierarchy under there mirrors
        // the structure of the module file in which this endpoint's
        // webservice is defined.  This allows easy retrieval of the wsdl
        // content using jar URLs.
        URL context = composeEndpointAddress(root);
        String mainFile = context.getFile() + "/" + PUBLISHING_SUBCONTEXT + "/" + webService.getWsdlFileUri();
        return new URL(context.getProtocol(), context.getHost(), context.getPort(), mainFile);
    }


    /**
     * This is the logical equivalent to endpoint address uri, but for
     * url publishing. Like endpoint address uri, it does not include the
     * context root for servlet endpoints.
     *
     * @return publishing uri without a leading or trailing slash.
     */
    public String getPublishingUri() {
        String uri = endpointAddressUri.startsWith("/") ? endpointAddressUri.substring(1) : endpointAddressUri;
        return uri + "/" + PUBLISHING_SUBCONTEXT;
    }


    /**
     * Checks an ejb request uri to see if it represents a legal request
     * for the wsdl content associated with this endpoint's web service.
     * Equivalent matching for servlets is performed automatically by the
     * web server. Should only be called for HTTP(S) GET.
     */
    public boolean matchesEjbPublishRequest(String requestUriRaw, String query) {
        // Strip off leading slash.
        String requestUri = requestUriRaw.charAt(0) == '/' ? requestUriRaw.substring(1) : requestUriRaw;
        // If request of form http<s>://<host>:<port>/<endpoint-address>?WSDL
        if (query == null) {
            // Add trailing slash to make sure sub context is an exact match.
            String publishingUri = getPublishingUri() + "/";
            return requestUri.startsWith(publishingUri);
        }
        String toMatch = endpointAddressUri.charAt(0) == '/' ? endpointAddressUri.substring(1) : endpointAddressUri;
        return requestUri.equals(toMatch)
            && (query.equalsIgnoreCase("WSDL") || query.startsWith("xsd=") || query.startsWith("wsdl="));
    }


    /**
     * @return the portion of a request uri that represents the location
     *         of wsdl content within a module or null if this request is invalid.
     *         Returned value does not have leading slash.
     */
    public String getWsdlContentPath(String requestUri) {
        // Strip off leading slash.
        String uri = requestUri.charAt(0) == '/' ? requestUri.substring(1) : requestUri;

        // get "raw" internal publishing uri. this value
        // does NOT have a leading slash.
        String publishingUriRaw = getPublishingUri();

        // Construct the publishing root. This should NOT have a
        // leading slash but SHOULD have a trailing slash.
        final String publishingRoot;
        if (implementedByWebComponent()) {
            WebBundleDescriptor webBundle = webComponentImpl.getWebBundleDescriptor();
            String contextRoot = webBundle.getContextRoot();
            if (contextRoot.startsWith("/")) {
                contextRoot = contextRoot.substring(1);
            }
            publishingRoot = contextRoot + "/" + publishingUriRaw + "/";
        } else {
            publishingRoot = publishingUriRaw + "/";
        }
        return uri.startsWith(publishingRoot) ? uri.substring(publishingRoot.length()) : null;
    }


    /**
     * Store current contents of servlet impl class in a runtime descriptor
     * element. The standard deployment descriptor element will be replaced at
     * deployment time with a container-provided servlet impl, so we need
     * to make a copy of this value in the runtime information.
     */
    public void saveServletImplClass() {
        if (implementedByWebComponent()) {
            servletImplClass = webComponentImpl.getWebComponentImplementation();
        } else {
            throw new IllegalStateException("requires ejb");
        }
    }


    private void updateServletEndpointRuntime() {
        // An endpoint might have been loaded off a jar file. In that case the WebFragmentDescriptor
        // can be stale. So patch it
        WebComponentDescriptor wc = ((WebBundleDescriptor) webService.getBundleDescriptor())
            .getWebComponentByCanonicalName(webComponentImpl.getCanonicalName());
        if (wc != webComponentImpl) {
            setWebComponentImpl(wc);
        }

        // Copy the value of the servlet impl bean class into
        // the runtime information. This way, we'll still
        // remember it after the servlet-class element has been
        // replaced with the name of the container's servlet class.
        saveServletImplClass();

        WebBundleDescriptor bundle = webComponentImpl.getWebBundleDescriptor();

        WebServicesDescriptor webServices = bundle.getWebServices();
        Collection<WebServiceEndpoint> endpoints = webServices.getEndpointsImplementedBy(webComponentImpl);

        if (endpoints.size() > 1) {
            String msg = "Servlet " + getWebComponentLink() + " implements " + endpoints.size()
                + " web service endpoints " + " but must only implement 1";
            throw new IllegalStateException(msg);
        }

        if (getEndpointAddressUri() == null) {
            Set<String> urlPatterns = webComponentImpl.getUrlPatternsSet();
            if (urlPatterns.size() == 1) {

                // Set endpoint-address-uri runtime info to uri.
                // Final endpoint address will still be relative to context root
                String uri = urlPatterns.iterator().next();
                setEndpointAddressUri(uri);

                // Set transport guarantee in runtime info if transport
                // guarantee is INTEGRAL or CONDIFIDENTIAL for any
                // security constraint with this url-pattern.
                Collection<SecurityConstraint> constraints = bundle.getSecurityConstraintsForUrlPattern(uri);
                for (SecurityConstraint constraint : constraints) {
                    UserDataConstraint dataConstraint = constraint.getUserDataConstraint();
                    String guarantee = dataConstraint == null ? null : dataConstraint.getTransportGuarantee();

                    if (guarantee != null
                        && (guarantee.equals(UserDataConstraint.INTEGRAL_TRANSPORT)
                            || guarantee.equals(UserDataConstraint.CONFIDENTIAL_TRANSPORT))) {
                        setTransportGuarantee(guarantee);
                        break;
                    }
                }
            } else {
                String msg = "Endpoint " + getEndpointName() + " has not been assigned an endpoint address "
                    + " and is associated with servlet " + webComponentImpl.getCanonicalName() + " , which has "
                    + urlPatterns.size() + " url patterns";
                throw new IllegalStateException(msg);
            }
        }
    }


    public String getSoapAddressPrefix() {
        WSDolSupport dolSupport = Globals.getDefaultHabitat().getService(WSDolSupport.class);
        if (dolSupport == null) {
            // anything else should be soap11
            return "so`ap";
        }
        return dolSupport.getSoapAddressPrefix(protocolBinding);
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n endpoint name = ").append(endpointName);
        toStringBuffer.append("\n endpoint intf = ").append(serviceEndpointInterface);
        toStringBuffer.append("\n wsdl Port = ").append(wsdlPort);
        toStringBuffer.append("\n wsdl Addressing = ").append(addressing);
        toStringBuffer.append("\n wsdl RespectBinding = ").append(respectBinding);
        toStringBuffer.append("\n ejb Link = ").append(ejbLink);
        toStringBuffer.append("\n web Link = ").append(webComponentLink);
    }


    private EjbBundleDescriptor getEjbBundle() {
        return (EjbBundleDescriptor) getBundleDescriptor();
    }


    private WebBundleDescriptor getWebBundle() {
        return (WebBundleDescriptor) getBundleDescriptor();
    }
}
