/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.Addressing;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.RespectBinding;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.runtime.ws.ReliabilityConfig;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.developer.SchemaValidationFeature;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeature;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeatureBuilder;
import com.sun.xml.ws.rx.rm.api.RmProtocolVersion;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.xml.ws.RespectBindingFeature;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.soap.MTOMFeature;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static org.glassfish.webservices.LogUtils.DEPLOYMENT_FAILED;

/**
 * This class serves for initialization of JAX-WS WSEndpoints when the context is initialized on deployment.
 *
 * @author Rama Pulavarthi
 */
public class WSServletContextListener implements ServletContextListener {

    private static final Logger logger = LogUtils.getLogger();
    private String contextRoot;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
        ComponentEnvManager compEnvManager = wscImpl.getComponentEnvManager();
        JndiNameEnvironment jndiNameEnv = compEnvManager.getCurrentJndiNameEnvironment();
        WebBundleDescriptor webBundle = null;
        if (jndiNameEnv != null && jndiNameEnv instanceof BundleDescriptor && ((BundleDescriptor) jndiNameEnv).getModuleType().equals(DOLUtils.warType())) {
            webBundle = ((WebBundleDescriptor) jndiNameEnv);
        } else {
            throw new WebServiceException("Cannot intialize the JAXWSServlet for " + jndiNameEnv);
        }

        contextRoot = webBundle.getContextRoot();
        WebServicesDescriptor webServices = webBundle.getWebServices();
        try {
            for (WebServiceEndpoint endpoint : webServices.getEndpoints()) {
                registerEndpoint(endpoint, sce.getServletContext());
            }
        } catch (Throwable t) {
            logger.log(WARNING, DEPLOYMENT_FAILED, t);// TODO Fix Rama
            sce.getServletContext().removeAttribute("ADAPTER_LIST");
            throw new RuntimeException("Servlet web service endpoint '" + "' failure", t);
        }
    }

    private void registerEndpoint(WebServiceEndpoint endpoint, ServletContext servletContext) throws Exception {
        ClassLoader classLoader = servletContext.getClassLoader();
        WsUtil wsu = new WsUtil();
        // Complete all the injections that are required
        Class serviceEndpointClass = Class.forName(endpoint.getServletImplClass(), true, classLoader);

        // Get the proper binding using BindingID
        String givenBinding = endpoint.getProtocolBinding();
        // TODO Rama
//            if(endpoint.getWsdlExposed() != null) {
//                wsdlExposed = Boolean.parseBoolean(endpoint.getWsdlExposed());
//            }
        // Get list of all wsdls and schema
        SDDocumentSource primaryWsdl = null;
        Collection docs = null;
        if (endpoint.getWebService().hasWsdlFile()) {

            URL pkgedWsdl = null;
            try {
                pkgedWsdl = servletContext.getResource('/' + endpoint.getWebService().getWsdlFileUri());
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, LogUtils.CANNOT_LOAD_WSDL_FROM_APPLICATION, e.getMessage());
            }
            if (pkgedWsdl == null) {
                pkgedWsdl = endpoint.getWebService().getWsdlFileUrl();
            }

            if (pkgedWsdl != null) {
                primaryWsdl = SDDocumentSource.create(pkgedWsdl);
                docs = wsu.getWsdlsAndSchemas(pkgedWsdl);

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, LogUtils.CREATING_ENDPOINT_FROM_PACKAGED_WSDL, primaryWsdl.getSystemId().toString());
                    logger.log(Level.FINE, LogUtils.METADATA_DOCS);
                    for (Object source : docs) {
                        logger.log(Level.FINE, ((SDDocumentSource) source).getSystemId().toString());
                    }
                }
            }
        }

        // Create a Container to pass ServletContext and also inserting the pipe
        JAXWSContainer container = new JAXWSContainer(servletContext, endpoint);

        // Get catalog info
        java.net.URL catalogURL = servletContext
                .getResource('/' + endpoint.getBundleDescriptor().getDeploymentDescriptorDir() + File.separator + "jax-ws-catalog.xml");

        // Create Binding and set service side handlers on this binding
        boolean mtomEnabled = wsu.getMtom(endpoint);
        WSBinding binding = null;
        // Only if MTOm is enabled create the Binding with the MTOMFeature
        ArrayList<WebServiceFeature> wsFeatures = new ArrayList<WebServiceFeature>();
        // Only if MTOm is enabled create the Binding with the MTOMFeature
        if (mtomEnabled) {
            int mtomThreshold = endpoint.getMtomThreshold() != null ? Integer.parseInt(endpoint.getMtomThreshold()) : 0;
            MTOMFeature mtom = new MTOMFeature(true, mtomThreshold);
            wsFeatures.add(mtom);
        }

        Addressing addressing = endpoint.getAddressing();
        if (addressing != null) {
            AddressingFeature addressingFeature = new AddressingFeature(addressing.isEnabled(), addressing.isRequired(),
                    getResponse(addressing.getResponses()));
            wsFeatures.add(addressingFeature);
        }
        RespectBinding rb = endpoint.getRespectBinding();
        if (rb != null) {
            RespectBindingFeature rbFeature = new RespectBindingFeature(rb.isEnabled());
            wsFeatures.add(rbFeature);
        }

        if (endpoint.getValidateRequest() != null && Boolean.parseBoolean(endpoint.getValidateRequest())) {
            // enable SchemaValidationFeature
            wsFeatures.add(new SchemaValidationFeature());
        }

        if (endpoint.getStreamAttachments() != null && Boolean.parseBoolean(endpoint.getStreamAttachments())) {
            // enable StreamingAttachmentsFeature
            wsFeatures.add(new StreamingAttachmentFeature());
        }

        if (endpoint.getReliabilityConfig() != null) {
            // TODO Revisit later after Metro provides generic method to pass partial configuration to Metro runtime.
            // Only partial configuration is specified in webservices DD, but the information for creating complete RM feature
            // should be gathered
            // from wsdl policy, annotation or metro configuration file. For ex: RmProtocolVersion would be decided by policy
            // assertion.
            // For now, the feature would be constructed from default values, overriding any configuration specified in wsdl or
            // metro configuration file..

            ReliabilityConfig rxConfig = endpoint.getReliabilityConfig();
            ReliableMessagingFeatureBuilder rmbuilder = new ReliableMessagingFeatureBuilder(RmProtocolVersion.getDefault());

            if (rxConfig.getInactivityTimeout() != null) {
                rmbuilder.sequenceInactivityTimeout(Long.parseLong(rxConfig.getInactivityTimeout().trim()));

            }
            if (endpoint.getHttpResponseBufferSize() != null) {
                rmbuilder.destinationBufferQuota(Long.parseLong(endpoint.getHttpResponseBufferSize().trim()));

            }
            if (rxConfig.getBaseRetransmissionInterval() != null) {
                rmbuilder.messageRetransmissionInterval(Long.parseLong(rxConfig.getBaseRetransmissionInterval().trim()));
            }
            if (rxConfig.getRetransmissionExponentialBackoff() != null) {
                rmbuilder.retransmissionBackoffAlgorithm(
                        Boolean.parseBoolean(rxConfig.getRetransmissionExponentialBackoff()) ? ReliableMessagingFeature.BackoffAlgorithm.EXPONENTIAL
                                : ReliableMessagingFeature.BackoffAlgorithm.getDefault());
            }
            if (rxConfig.getAcknowledgementInterval() != null) {
                rmbuilder.acknowledgementTransmissionInterval(Long.parseLong(rxConfig.getAcknowledgementInterval().trim()));
            }
            if (rxConfig.getSequenceExpiration() != null) {
                logger.log(Level.INFO, LogUtils.CONFIGURATION_IGNORE_IN_WLSWS, new Object[] { endpoint.getEndpointName(), "<sequence-expiration>" });
            }
            if (rxConfig.getBufferRetryCount() != null) {
                rmbuilder.maxMessageRetransmissionCount(Long.parseLong(rxConfig.getBufferRetryCount().trim()));
            }
            if (rxConfig.getBufferRetryDelay() != null) {
                logger.log(Level.INFO, LogUtils.CONFIGURATION_IGNORE_IN_WLSWS, new Object[] { endpoint.getEndpointName(), "<buffer-retry-delay>" });
            }

            wsFeatures.add(rmbuilder.build());
        } else {
            if (endpoint.getHttpResponseBufferSize() != null) {
                logger.log(WARNING, LogUtils.CONFIGURATION_UNSUPPORTED_IN_WLSWS,
                        new Object[] { endpoint.getEndpointName(), "<http-response-buffersize>" });
            }
        }

        if (wsFeatures.size() > 0) {
            binding = BindingID.parse(givenBinding).createBinding(wsFeatures.toArray(new WebServiceFeature[wsFeatures.size()]));
        } else {
            binding = BindingID.parse(givenBinding).createBinding();
        }

        wsu.configureJAXWSServiceHandlers(endpoint, givenBinding, binding);

        // See if it is configured with JAX-WS extension InstanceResolver annotation like
        // @com.sun.xml.ws.developer.servlet.HttpSessionScope or @com.sun.xml.ws.developer.Stateful
        InstanceResolver ir = InstanceResolver.createFromInstanceResolverAnnotation(serviceEndpointClass);
        // TODO - Implement 109 StatefulInstanceResolver ??
        if (ir == null) {
            // use our own InstanceResolver that does not call @PostConstuct method before
            // @Resource injections have happened.
            ir = new InstanceResolverImpl(serviceEndpointClass);
        }
        Invoker inv = ir.createInvoker();

        WSEndpoint wsep = WSEndpoint.create(serviceEndpointClass, // The endpoint class
                false, // we do not want JAXWS to process @HandlerChain
                inv, endpoint.getServiceName(), // the service QName
                endpoint.getWsdlPort(), // the port
                container, // Our container with info on security/monitoring pipe
                binding, // Derive binding
                primaryWsdl, // primary WSDL
                docs, // Collection of imported WSDLs and schema
                catalogURL);

        // Fix for 6852 Add the ServletAdapter which implements the BoundEndpoint
        // container.addEndpoint(wsep);

        // For web components, this will be relative to the web app
        // context root. Make sure there is a leading slash.
        String uri = endpoint.getEndpointAddressUri();
        String urlPattern = uri.startsWith("/") ? uri : "/" + uri;

        // The whole web app should have a single adapter list
        // This is to enable JAXWS publish WSDLs with proper addresses
        ServletAdapter adapter = null;
        synchronized (this) {
            ServletAdapterList list = (ServletAdapterList) servletContext.getAttribute("ADAPTER_LIST");
            if (list == null) {
                list = new ServletAdapterList();
                servletContext.setAttribute("ADAPTER_LIST", list);
            }
            Object obj = list.createAdapter(endpoint.getName(), urlPattern, wsep);
            if (obj instanceof ServletAdapter) {
                adapter = ServletAdapter.class.cast(obj);
                container.addEndpoint(adapter);
            }
        }

        registerEndpointUrlPattern(urlPattern, adapter);
    }

    private AddressingFeature.Responses getResponse(String s) {
        if (s != null) {
            return AddressingFeature.Responses.valueOf(AddressingFeature.Responses.class, s);
        } else {
            return AddressingFeature.Responses.ALL;
        }
    }

    private void registerEndpointUrlPattern(String urlPattern, Adapter info) {
        JAXWSAdapterRegistry.getInstance().addAdapter(contextRoot, urlPattern, info);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        synchronized (this) {
            ServletAdapterList list = (ServletAdapterList) servletContext.getAttribute("ADAPTER_LIST");
            if (list != null) {
                for (ServletAdapter x : list) {
                    x.getEndpoint().dispose();
                    for (Handler handler : x.getEndpoint().getBinding().getHandlerChain()) {
                        try {
                            WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
                            InjectionManager injManager = wscImpl.getInjectionManager();
                            injManager.destroyManagedObject(handler);
                        } catch (InjectionException e) {
                            logger.log(WARNING, LogUtils.DESTORY_ON_HANDLER_FAILED,
                                    new Object[] { handler.getClass(), x.getEndpoint().getServiceName(), e.getMessage() });
                            continue;
                        }
                    }
                }
                servletContext.removeAttribute("ADAPTER_LIST");
            }
            JAXWSAdapterRegistry.getInstance().removeAdapter(contextRoot);
            /*
             * Fix for bug 3932/4052 since the x.getEndpoint().dispose is being called above we do not need to call this explicitly
             * try { (new WsUtil()).doPreDestroy(endpoint, classLoader); } catch (Throwable t) { logger.log(Level.WARNING,
             * "@PreDestroy lifecycle call failed for service" + endpoint.getName(), t); }
             */
        }
        JAXWSServletModule.destroy(contextRoot);
    }
}
