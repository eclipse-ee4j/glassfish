/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.webservices;

import static com.sun.xml.wss.provider.wsit.PipeConstants.BINDING;
import static com.sun.xml.wss.provider.wsit.PipeConstants.CLIENT_SUBJECT;
import static com.sun.xml.wss.provider.wsit.PipeConstants.ENDPOINT;
import static com.sun.xml.wss.provider.wsit.PipeConstants.ENDPOINT_ADDRESS;
import static com.sun.xml.wss.provider.wsit.PipeConstants.POLICY;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SEI_MODEL;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SERVICE_ENDPOINT;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SERVICE_REF;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SOAP_LAYER;
import static com.sun.xml.wss.provider.wsit.PipeConstants.WSDL_MODEL;
import static jakarta.xml.ws.handler.MessageContext.SERVLET_REQUEST;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.epicyro.config.module.configprovider.GFServerConfigProvider;
import org.glassfish.epicyro.services.BaseAuthenticationService;
import org.glassfish.internal.api.Globals;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.appclient.ConfigXMLParser;
import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.common.AppservAccessController;
import com.sun.enterprise.security.common.ClientSecurityContext;
import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
import com.sun.enterprise.security.ee.authorize.EJBPolicyContextDelegate;
import com.sun.enterprise.security.ee.jmac.AuthMessagePolicy;
import com.sun.enterprise.security.ee.jmac.ConfigDomainParser;
import com.sun.enterprise.security.ee.jmac.WebServicesDelegate;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.wss.provider.wsit.PipeConstants;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.config.ClientAuthConfig;
import jakarta.security.auth.message.config.ClientAuthContext;
import jakarta.security.auth.message.config.ServerAuthConfig;
import jakarta.security.auth.message.config.ServerAuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.ws.WebServiceException;

public class SoapAuthenticationService extends BaseAuthenticationService {

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(PipeConstants.class);

    private AppServerAuditManager auditManager;
    private boolean isEjbEndpoint;
    private SEIModel seiModel;
    private SOAPVersion soapVersion;
    private InvocationManager invocationManager;
    private EJBPolicyContextDelegate ejbDelegate;

    public SoapAuthenticationService(String layer, Map<String, Object> map, CallbackHandler callbackHandler) {
        init(layer, getAppCtxt(map), map, callbackHandler, null);

        this.isEjbEndpoint = processSunDeploymentDescriptor();
        this.seiModel = (SEIModel) map.get(SEI_MODEL);

        WSBinding binding = (WSBinding) map.get(BINDING);
        if (binding == null) {
            WSEndpoint<?> endPoint = (WSEndpoint<?>) map.get(ENDPOINT);
            if (endPoint != null) {
                binding = endPoint.getBinding();
            }
        }

        this.soapVersion = binding != null
                ? binding.getSOAPVersion()
                : SOAPVersion.SOAP_11;

        AuditManager auditManager = SecurityServicesUtil.getInstance() != null
                ? SecurityServicesUtil.getInstance().getAuditManager()
                : null;

        this.auditManager = auditManager != null && auditManager instanceof AppServerAuditManager
                ? (AppServerAuditManager) auditManager
                : new AppServerAuditManager();// workaround for standalone clients where no habitat

        this.invocationManager = SecurityServicesUtil.getInstance() != null
                ? SecurityServicesUtil.getInstance().getHabitat().getService(InvocationManager.class)
                : null;

        this.ejbDelegate = new EJBPolicyContextDelegate();
    }

    @Override
    public ClientAuthContext getClientAuthContext(MessageInfo messageInfo, Subject subject) throws AuthException {
        ClientAuthConfig clientAuthConfig = (ClientAuthConfig) getAuthConfig(false);
        if (clientAuthConfig == null) {
            return null;
        }

        addModel(messageInfo, map);

        return clientAuthConfig.getAuthContext(clientAuthConfig.getAuthContextID(messageInfo), subject, map);
    }

    @Override
    public ServerAuthContext getServerAuthContext(MessageInfo messageInfo, Subject subject) throws AuthException {
        ServerAuthConfig serverAuthConfig = (ServerAuthConfig) getAuthConfig(true);
        if (serverAuthConfig == null) {
            return null;
        }

        addModel(messageInfo, map);
        addPolicy(messageInfo, map);

        return serverAuthConfig.getAuthContext(serverAuthConfig.getAuthContextID(messageInfo), subject, map);
    }

    public static Subject getClientSubject() {
        Subject subject = null;

        if (isACC()) {
            ClientSecurityContext clientSecurityContext = ClientSecurityContext.getCurrent();
            if (clientSecurityContext != null) {
                subject = clientSecurityContext.getSubject();
            }
            if (subject == null) {
                subject = Subject.getSubject(AccessController.getContext());
            }
        } else {
            SecurityContext securityContext = SecurityContext.getCurrent();
            if (securityContext != null && !securityContext.didServerGenerateCredentials()) {
                // Make sure we don't use default unauthenticated subject,
                // so that module cannot change this important (constant)
                // subject.
                subject = securityContext.getSubject();
            }
        }

        if (subject == null) {
            subject = new Subject();
        }

        return subject;
    }

    public void getSessionToken(Map<String, Object> properties, MessageInfo messageInfo, Subject subject) throws AuthException {
        ClientAuthConfig clientAuthConfig = (ClientAuthConfig) getAuthConfig(false);
        if (clientAuthConfig != null) {
            properties.putAll(map);
            addModel(messageInfo, map);
            clientAuthConfig.getAuthContext(clientAuthConfig.getAuthContextID(messageInfo), subject, properties);
        }
    }

    public void authorize(Packet request) throws Exception {
        // SecurityContext constructor should set initiator to unathenticated if Subject is null or empty
        Subject subject = (Subject) request.invocationProperties.get(CLIENT_SUBJECT);

        if (subject == null || (subject.getPrincipals().isEmpty() && subject.getPublicCredentials().isEmpty())) {
            SecurityContext.setUnauthenticatedContext();
        } else {
            SecurityContext.setCurrent(new SecurityContext(subject));
        }

        // We should try to replace this endpoint specific authorisation check with a generic web service
        // message check and move the endpoint specific check down stream

        if (isEjbEndpoint) {
            if (invocationManager == null) {
                throw new RuntimeException(localStrings.getLocalString("enterprise.webservice.noEjbInvocationManager",
                        "Cannot validate request : invocation manager null for EJB WebService"));
            }

            ComponentInvocation currentInvocation = invocationManager.getCurrentInvocation();

            // One need to copy message here, otherwise the message may be consumed
            if (ejbDelegate != null) {
                ejbDelegate.setSOAPMessage(request.getMessage(), currentInvocation);
            }

            Method targetMethod = null;
            if (seiModel != null) {
                JavaMethod jm = request.getMessage().getMethod(seiModel);
                targetMethod = (jm != null) ? jm.getMethod() : null;
            } else { // WebServiceProvider

                WebServiceEndpoint endpoint = (WebServiceEndpoint) map.get(SERVICE_ENDPOINT);
                EjbDescriptor ejbDescriptor = endpoint.getEjbComponentImpl();
                if (ejbDescriptor != null) {
                    final String ejbImplClassName = ejbDescriptor.getEjbImplClassName();
                    if (ejbImplClassName != null) {
                        try {
                            targetMethod = (Method) AppservAccessController.doPrivileged(new PrivilegedExceptionAction<>() {
                                @Override
                                public Object run() throws Exception {
                                    return Class.forName(ejbImplClassName, true, Thread.currentThread().getContextClassLoader())
                                                .getMethod("invoke", new Class[] { Object.class });
                                }
                            });
                        } catch (PrivilegedActionException pae) {
                            throw new RuntimeException(pae.getException());
                        }
                    }
                }
            }

            if (targetMethod != null) {
                if (ejbDelegate != null) {
                    try {
                        if (!ejbDelegate.authorize(currentInvocation, targetMethod)) {
                            throw new Exception(localStrings.getLocalString("enterprise.webservice.methodNotAuth",
                                    "Client not authorized for invocation of {0}", new Object[] { targetMethod }));
                        }
                    } catch (UnmarshalException e) {
                        throw new UnmarshalException(localStrings.getLocalString("enterprise.webservice.errorUnMarshalMethod",
                                "Error unmarshalling method for ejb {0}", new Object[] { ejbName() }), e);
                    } catch (Exception e) {
                        throw new Exception(localStrings.getLocalString("enterprise.webservice.methodNotAuth",
                                "Client not authorized for invocation of {0}", new Object[] { targetMethod }), e);
                    }
                }
            }
        }
    }

    public void auditInvocation(Packet request, AuthStatus status) {
        if (auditManager.isAuditOn()) {
            String uri = null;
            if (!isEjbEndpoint && request != null && request.supports(SERVLET_REQUEST)) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request.get(SERVLET_REQUEST);
                uri = httpServletRequest.getRequestURI();
            }

            String endpointName = null;
            if (map != null) {
                WebServiceEndpoint endpoint = (WebServiceEndpoint) map.get(SERVICE_ENDPOINT);
                if (endpoint != null) {
                    endpointName = endpoint.getEndpointName();
                }
            }

            if (endpointName == null) {
                endpointName = "(no endpoint)";
            }

            if (isEjbEndpoint) {
                auditManager.ejbAsWebServiceInvocation(endpointName, AuthStatus.SUCCESS.equals(status));
            } else {
                auditManager.webServiceInvocation(((uri == null) ? "(no uri)" : uri), endpointName, AuthStatus.SUCCESS.equals(status));
            }
        }
    }

    public Object getModelName() {
        WSDLPort wsdlModel = (WSDLPort) getProperty(WSDL_MODEL);
        return (wsdlModel == null ? "unknown" : wsdlModel.getName());
    }

    // Always returns response with embedded fault
    public Packet makeFaultResponse(Packet response, Throwable t) {
        // wrap throwable in WebServiceException, if necessary
        if (!(t instanceof WebServiceException)) {
            t = new WebServiceException(t);
        }
        if (response == null) {
            response = new Packet();
        }
        // try to create fault in provided response packet, if an exception
        // is thrown, create new packet, and create fault in it.
        try {
            return response.createResponse(Messages.create(t, this.soapVersion));
        } catch (Exception e) {
            response = new Packet();
        }

        return response.createResponse(Messages.create(t, this.soapVersion));
    }

    public boolean isTwoWay(boolean twoWayIsDefault, Packet request) {
        boolean twoWay = twoWayIsDefault;
        Message requestMessage = request.getMessage();
        if (requestMessage != null) {
            WSDLPort wsdlModel = (WSDLPort) getProperty(WSDL_MODEL);
            if (wsdlModel != null) {
                twoWay = requestMessage.isOneWay(wsdlModel) ? false : true;
            }
        }

        return twoWay;
    }

    // Returns empty response if request is determined to be one-way
    public Packet getFaultResponse(Packet request, Packet response, Throwable t) {
        boolean twoWay = true;
        try {
            twoWay = isTwoWay(true, request);
        } catch (Exception e) {
            // exception is consumed, and twoWay is assumed
        }

        if (twoWay) {
            return makeFaultResponse(response, t);
        }

        return new Packet();
    }

    @Override
    public void disable() {
        listenerWrapper.disableWithRefCount();
    }

    private static boolean isACC() {
        return SecurityServicesUtil.getInstance() == null || SecurityServicesUtil.getInstance().isACC();
    }

    private boolean processSunDeploymentDescriptor() {
        if (authConfigFactory == null) {
            return false;
        }

        MessageSecurityBindingDescriptor binding = AuthMessagePolicy.getMessageSecurityBinding(SOAP_LAYER, map);

        Function<MessageInfo, String> authContextIdGenerator =
            e -> Globals.get(WebServicesDelegate.class).getAuthContextID(e);

        BiFunction<String, Map<String, Object>, MessagePolicy[]> soapPolicyGenerator =
                (authContextId, properties) -> AuthMessagePolicy.getSOAPPolicies(
                       AuthMessagePolicy.getMessageSecurityBinding("SOAP", properties),
                       authContextId, true);

        String authModuleId = AuthMessagePolicy.getProviderID(binding);

        map.put("authContextIdGenerator", authContextIdGenerator);
        map.put("soapPolicyGenerator", soapPolicyGenerator);

        if (authModuleId != null) {
            map.put("authModuleId", authModuleId);
        }

        if (binding != null) {
            if (!hasExactMatchAuthProvider()) {
                String jmacProviderRegisID = authConfigFactory.registerConfigProvider(
                        new GFServerConfigProvider(
                            map,
                            isACC()? new ConfigXMLParser() : new ConfigDomainParser(),
                            authConfigFactory),
                        messageLayer, appContextId,
                        "GF AuthConfigProvider bound by Sun Specific Descriptor");

                setRegistrationId(jmacProviderRegisID);
            }
        }

        WebServiceEndpoint webServiceEndpoint = (WebServiceEndpoint) map.get(SERVICE_ENDPOINT);
        return webServiceEndpoint == null ? false : webServiceEndpoint.implementedByEjbComponent();
    }

    private static String getAppCtxt(Map map) {
        WebServiceEndpoint webServiceEndpoint = (WebServiceEndpoint) map.get(SERVICE_ENDPOINT);

        // Endpoint
        if (webServiceEndpoint != null) {
            return getServerName(webServiceEndpoint) + " " + getEndpointURI(webServiceEndpoint);
        }

        // Client reference
        ServiceReferenceDescriptor serviceReferenceDescriptor = (ServiceReferenceDescriptor) map.get(SERVICE_REF);

        return getClientModuleID(serviceReferenceDescriptor) + " " + getRefName(serviceReferenceDescriptor, map);
    }

    private static String getServerName(WebServiceEndpoint wse) {
        // XXX FIX ME: need to lookup real hostname
        String hostname = "localhost";
        return hostname;
    }

    private static String getRefName(ServiceReferenceDescriptor serviceReferenceDescriptor, Map map) {
        String name = null;
        if (serviceReferenceDescriptor != null) {
            name = serviceReferenceDescriptor.getName();
        }

        if (name == null) {
            EndpointAddress endpointAddress = (EndpointAddress) map.get(ENDPOINT_ADDRESS);
            if (endpointAddress != null) {
                URL url = endpointAddress.getURL();
                if (url != null) {
                    name = url.toString();
                }
            }
        }

        if (name == null) {
            name = "#default-ref-name#";
        }

        return name;
    }

    private static String getEndpointURI(WebServiceEndpoint webServiceEndpoint) {
        String uri = "#default-endpoint-context#";

        if (webServiceEndpoint != null) {
            uri = webServiceEndpoint.getEndpointAddressUri();
            if (uri != null && (!uri.startsWith("/"))) {
                uri = "/" + uri;
            }

            if (webServiceEndpoint.implementedByWebComponent()) {
                WebBundleDescriptor webBundleDescriptor = (WebBundleDescriptor) webServiceEndpoint.getBundleDescriptor();
                if (webBundleDescriptor != null) {
                    String contextRoot = webBundleDescriptor.getContextRoot();
                    if (contextRoot != null) {
                        if (!contextRoot.startsWith("/")) {
                            contextRoot = "/" + contextRoot;
                        }
                        uri = contextRoot + uri;
                    }
                }
            }
        }

        return uri;
    }

    private static String getClientModuleID(ServiceReferenceDescriptor serviceReferenceDescriptor) {
        String clientModuleID = "#default-client-context#";

        if (serviceReferenceDescriptor != null) {
            ModuleDescriptor<?> moduleDescriptor = null;
            BundleDescriptor bundleDescriptor = serviceReferenceDescriptor.getBundleDescriptor();

            if (bundleDescriptor != null) {
                moduleDescriptor = bundleDescriptor.getModuleDescriptor();
            }

            Application application = bundleDescriptor == null ? null : bundleDescriptor.getApplication();
            if (application != null) {
                if (application.isVirtual()) {
                    clientModuleID = application.getRegistrationName();
                } else if (moduleDescriptor != null) {
                    clientModuleID = FileUtils.makeFriendlyFilename(moduleDescriptor.getArchiveUri());
                }
            } else if (moduleDescriptor != null) {
                clientModuleID = FileUtils.makeFriendlyFilename(moduleDescriptor.getArchiveUri());
            }
        }

        return clientModuleID;
    }

    private static void addModel(MessageInfo messageInfo, Map<String, Object> map) {
        Object model = map.get(WSDL_MODEL);
        if (model != null) {
            messageInfo.getMap().put(WSDL_MODEL, model);
        }
    }

    private static void addPolicy(MessageInfo messageInfo, Map<String, Object> map) {
        Object policy = map.get(POLICY);
        if (policy != null) {
            messageInfo.getMap().put(POLICY, policy);
        }
    }

    private String ejbName() {
        WebServiceEndpoint webServiceEndpoint = (WebServiceEndpoint) getProperty(SERVICE_ENDPOINT);
        return webServiceEndpoint == null ? "unknown" : webServiceEndpoint.getEjbComponentImpl().getName();
    }
}
