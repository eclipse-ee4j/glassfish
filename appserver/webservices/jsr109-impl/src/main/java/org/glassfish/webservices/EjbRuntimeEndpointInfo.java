/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.Addressing;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;

import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.soap.MTOMFeature;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.logging.LogHelper;
import org.glassfish.ejb.api.EJBInvocation;
import org.glassfish.ejb.api.EjbEndpointFacade;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_ENV;


/**
 * Runtime dispatch information about one ejb web service
 * endpoint.  This class must support concurrent access,
 * since a single instance will be used for all web
 * service invocations through the same ejb endpoint.
 * <p><b>NOT THREAD SAFE</b>
 * @author Jerome Dochez
 */
public class EjbRuntimeEndpointInfo {

    private static final Logger logger = LogUtils.getLogger();

    protected final WebServiceEndpoint endpoint;

    protected final EjbEndpointFacade container;

    protected final Object webServiceEndpointServant;

    // the variables below are access in non-thread-safe ways
    private volatile ServletAdapter adapter = null;
    private ServletAdapterList adapterList = null;

    private WebServiceContextImpl wsCtxt = null;
    private boolean handlersConfigured = false;

    protected EjbMessageDispatcher messageDispatcher = null;

    public EjbRuntimeEndpointInfo(WebServiceEndpoint webServiceEndpoint,
                                  EjbEndpointFacade ejbContainer,
                                  Object servant) {

        endpoint = webServiceEndpoint;
        container  = ejbContainer;
        webServiceEndpointServant = servant;
    }

    public WebServiceEndpoint getEndpoint() {
        return endpoint;
    }

    public String getEndpointAddressUri() {
        return endpoint.getEndpointAddressUri();
    }

    public synchronized WebServiceContext getWebServiceContext() {
        return wsCtxt;
    }

    public Object prepareInvocation(boolean doPreInvoke)
        throws Exception {
        ComponentInvocation inv = null;
        AdapterInvocationInfo adapterInvInfo = new AdapterInvocationInfo();
        // For proper injection of handlers, we have to configure handler
        // after invManager.preInvoke but the Invocation.contextData has to be set
        // before invManager.preInvoke. So the steps of configuring jaxws handlers and
        // init'ing jaxws is done here - this sequence is important
        if (adapter==null) {
            synchronized(this) {
                if(adapter == null) {
                    try {
                        // Set webservice context here
                        // If the endpoint has a WebServiceContext with @Resource then
                        // that has to be used


                        EjbDescriptor ejbDesc = endpoint.getEjbComponentImpl();
                        for (ResourceReferenceDescriptor r : ejbDesc.getResourceReferenceDescriptors()) {
                            if(r.isWebServiceContext()) {
                                boolean matchingClassFound = false;
                                for (InjectionTarget target : r.getInjectionTargets()) {
                                    if(ejbDesc.getEjbClassName().equals(target.getClassName())) {
                                        matchingClassFound = true;
                                        break;
                                    }
                                }
                                if(!matchingClassFound) {
                                    continue;
                                }
                                try {
                                    javax.naming.InitialContext ic = new javax.naming.InitialContext();
                                    wsCtxt = (WebServiceContextImpl) ic
                                        .lookup(JNDI_CTX_JAVA_COMPONENT_ENV + r.getName());
                                } catch (Throwable t) {
                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.log(Level.FINE, LogUtils.ERROR_EREI, t.getCause());
                                    }
                                }
                            }
                        }
                        if(wsCtxt == null) {
                            wsCtxt = new WebServiceContextImpl();
                        }
                    } catch (Throwable t) {
                        LogHelper.log(logger, Level.SEVERE,
                                LogUtils.CANNOT_INITIALIZE, t, endpoint.getName());
                        return null;
                    }
                }
            }
        }

        if(doPreInvoke) {
            inv =  container.startInvocation();
            adapterInvInfo.setInv(inv);
        }

        // Now process handlers and init jaxws RI
        synchronized(this) {
            if (!handlersConfigured && doPreInvoke) {
                try {
                    WsUtil wsu = new WsUtil();
                    String implClassName = endpoint.getEjbComponentImpl().getEjbClassName();
                    Class clazz = container.getEndpointClassLoader().loadClass(implClassName);

                    // Get the proper binding using BindingID
                    String givenBinding = endpoint.getProtocolBinding();

                    // Get list of all wsdls and schema
                    SDDocumentSource primaryWsdl = null;
                    Collection docs = null;
                    if(endpoint.getWebService().hasWsdlFile()) {

                        WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
                        ApplicationRegistry appRegistry = wscImpl.getApplicationRegistry();
                        ApplicationInfo appInfo = appRegistry.get(endpoint.getBundleDescriptor().getApplication().getRegistrationName());
                        URI deployedDir =appInfo.getSource().getURI();

                        URL pkgedWsdl;
                        if(deployedDir != null) {
                            if(endpoint.getBundleDescriptor().getApplication().isVirtual()) {
                                pkgedWsdl = deployedDir.resolve(endpoint.getWebService().getWsdlFileUri()).toURL();
                            } else {
                                String moduleUri1 = endpoint.getBundleDescriptor().getModuleDescriptor().getArchiveUri();

                                //Fix for issue 7024099
                                //Only replace the last "." with "_" for moduleDescriptor's archive uri

                                String moduleUri = FileUtils.makeFriendlyFilenameExtension(moduleUri1);
                                pkgedWsdl = deployedDir.resolve(moduleUri+"/"+endpoint.getWebService().getWsdlFileUri()).toURL();
                            }
                        } else {
                            pkgedWsdl = endpoint.getWebService().getWsdlFileUrl();
                        }
                        if (pkgedWsdl != null) {
                            primaryWsdl = SDDocumentSource.create(pkgedWsdl);
                            docs = wsu.getWsdlsAndSchemas(pkgedWsdl);
                        }
                    }

                    // Create a Container to pass ServletContext and also inserting the pipe
                    JAXWSContainer container = new JAXWSContainer(null,
                            endpoint);

                    // Get catalog info
                    java.net.URL catalogURL = clazz.getResource('/' + endpoint.getBundleDescriptor().getDeploymentDescriptorDir() + File.separator + "jax-ws-catalog.xml");

                    // Create Binding and set service side handlers on this binding

                    boolean mtomEnabled = wsu.getMtom(endpoint);
                    WSBinding binding = null;

                    ArrayList<WebServiceFeature> wsFeatures = new ArrayList<>();
                    // Only if MTOm is enabled create the Binding with the MTOMFeature
                    if (mtomEnabled) {
                        int mtomThreshold = endpoint.getMtomThreshold() != null ? Integer.parseInt(endpoint.getMtomThreshold()):0;
                        MTOMFeature mtom = new MTOMFeature(true,mtomThreshold);
                        wsFeatures.add(mtom);
                    }

                    Addressing addressing = endpoint.getAddressing();
                    if (endpoint.getAddressing() != null) {
                        AddressingFeature addressingFeature = new AddressingFeature(addressing.isEnabled(),
                            addressing.isRequired(),getResponse(addressing.getResponses()));
                        wsFeatures.add(addressingFeature);
                    }
                    if (wsFeatures.size()>0){
                        binding = BindingID.parse(givenBinding).createBinding(wsFeatures.toArray
                                (new WebServiceFeature[wsFeatures.size()]));
                    } else {
                        binding = BindingID.parse(givenBinding).createBinding();
                    }

                    wsu.configureJAXWSServiceHandlers(endpoint,
                        endpoint.getProtocolBinding(), binding);

                    // See if it is configured with JAX-WS extension InstanceResolver annotation like
                    // @com.sun.xml.ws.developer.servlet.HttpSessionScope or @com.sun.xml.ws.developer.Stateful
                    // #GLASSFISH-21081
                    InstanceResolver ir = InstanceResolver.createFromInstanceResolverAnnotation(clazz);
                    //TODO - Implement 109 StatefulInstanceResolver ??
                    if (ir == null) {
                        //use our own InstanceResolver that does not call @PostConstuct method before
                        //@Resource injections have happened.
                        ir = new InstanceResolverImpl(clazz);
                    }
                    // Create the jaxws2.1 invoker and use this
                    Invoker invoker = ir.createInvoker();
                    WSEndpoint wsep = WSEndpoint.create(
                            clazz, // The endpoint class
                            false, // we do not want JAXWS to process @HandlerChain
                            new EjbInvokerImpl(clazz, invoker, webServiceEndpointServant, wsCtxt), // the invoker
                            endpoint.getServiceName(), // the service QName
                            endpoint.getWsdlPort(), // the port
                            container,
                            binding, // Derive binding
                            primaryWsdl, // primary WSDL
                            docs, // Collection of imported WSDLs and schema
                            catalogURL
                            );

                    String uri = endpoint.getEndpointAddressUri();
                    String urlPattern = uri.startsWith("/") ? uri : "/" + uri;

                    // All set; Create the adapter
                    if(adapterList == null) {
                        adapterList = new ServletAdapterList();
                    }
                    Object obj = adapterList.createAdapter(endpoint.getName(), urlPattern, wsep);
                    if (obj instanceof ServletAdapter) {
                        adapter = ServletAdapter.class.cast(obj);
                    }
                    handlersConfigured=true;
                } catch (Throwable t) {
                        LogHelper.log(logger, Level.SEVERE,
                                LogUtils.CANNOT_INITIALIZE, t, endpoint.getName());
                    adapter = null;
                }
            }
        }

        //Issue 10776 The wsCtxt created using WebServiceReferenceManagerImpl
        //does not have the jaxwsContextDelegate set
        //set it using this method
        synchronized (this) {
            addWSContextInfo(wsCtxt);
            if (inv != null && inv instanceof EJBInvocation) {
                EJBInvocation ejbInv = (EJBInvocation) inv;
                ejbInv.setWebServiceContext(wsCtxt);
            }
        }
        adapterInvInfo.setAdapter(adapter);
        return adapterInvInfo;
    }

    private void addWSContextInfo(WebServiceContextImpl wsCtxt) {
        WebServiceContextImpl wsc = null;

        EjbDescriptor bundle = endpoint.getEjbComponentImpl();
        for (ResourceReferenceDescriptor r : bundle.getResourceReferenceDescriptors()) {
            if(r.isWebServiceContext()) {
                try {
                    javax.naming.InitialContext ic = new javax.naming.InitialContext();
                    wsc = (WebServiceContextImpl) ic.lookup(JNDI_CTX_JAVA_COMPONENT_ENV + r.getName());
                } catch (Throwable t) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, LogUtils.EXCEPTION_THROWN, t);
                    }
                }
                if(wsc != null) {
                    wsc.setContextDelegate(wsCtxt.getContextDelegate());

                }
            }
        }
    }

   /**
     * Force initialization of the endpoint runtime information
     * as well as the handlers injection
     */
    public synchronized void initRuntimeInfo(ServletAdapterList list) throws Exception {
       AdapterInvocationInfo aInfo =null;
        try {
            this.adapterList = list;
            aInfo = (AdapterInvocationInfo)prepareInvocation(true);
        } finally {
            if (aInfo != null) {
                releaseImplementor(aInfo.getInv());
            }
        }

    }


    public InvocationManager getInvocationManager (){
        WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
        return wscImpl.getInvocationManager();
    }

    /**
     * Called after attempt to handle message.  This is coded defensively
     * so we attempt to clean up no matter how much progress we made in
     * getImplementor.  One important thing is to complete the invocation
     * manager preInvoke().
     */
    public void releaseImplementor(ComponentInvocation inv) {
        container.endInvocation(inv);
    }

    public EjbMessageDispatcher getMessageDispatcher() {
        if (messageDispatcher==null) {
            messageDispatcher = new Ejb3MessageDispatcher();
        }
        return messageDispatcher;
    }

    public EjbEndpointFacade getContainer() {
        return container;
    }

    private AddressingFeature.Responses getResponse(String s) {
        if (s != null) {
            return Enum.valueOf(AddressingFeature.Responses.class,s);
        } else {
            return AddressingFeature.Responses.ALL;
        }

    }

}
