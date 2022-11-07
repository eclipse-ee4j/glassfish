/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.container.common.spi.WebServiceReferenceManager;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.xml.ws.api.FeatureConstructor;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.resources.ModelerMessages;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;

import jakarta.inject.Inject;
import jakarta.xml.ws.RespectBinding;
import jakarta.xml.ws.RespectBindingFeature;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.soap.Addressing;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.soap.MTOM;
import jakarta.xml.ws.soap.MTOMFeature;
import jakarta.xml.ws.spi.WebServiceFeatureAnnotation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.deployment.versioning.VersioningUtils;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;


/**
 * This class acts as a service to resolve the
 * </code>jakarta.xml.ws.WebServiceRef</code> references
 * and also <code>jakarta.xml.ws.WebServiceContext</code>
 * Whenever a lookup is done from GlassfishNamingManagerImpl
 * these methods are invoked to resolve the references
 *
 * @author Bhakti Mehta
 */
@Service
public class WebServiceReferenceManagerImpl implements WebServiceReferenceManager {
    private static final Logger LOG = LogUtils.getLogger();

    @Inject
    private ServerEnvironment serverEnv;


    @Override
    public Object getWSContextObject() {
        return new WebServiceContextImpl();
    }

    @Override
    public Object resolveWSReference(ServiceReferenceDescriptor desc, Context context)
            throws NamingException {


        //Taken from NamingManagerImpl.getClientServiceObject
        Class serviceInterfaceClass = null;
        Object returnObj = null;
        WsUtil wsUtil = new WsUtil();

        //Implementation for new lookup element in WebserviceRef
        InitialContext iContext = new InitialContext();
        if (desc.hasLookupName()) {
            return iContext.lookup(desc.getLookupName().toString());
        }

        try {

            WSContainerResolver.set(desc);

            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            serviceInterfaceClass = cl.loadClass(desc.getServiceInterface());

            resolvePortComponentLinks(desc);

            jakarta.xml.ws.Service jaxwsDelegate = null;
            Object injValue = null;

                    // The target is probably a post JAXRPC-1.1- based service;
                    // If Service Interface class is set, check if it is indeed a subclass of Service
                    // initiateInstance should not be called if the user has given jakarta.xml.ws.Service itself
                    // as the interface through DD
                    if(jakarta.xml.ws.Service.class.isAssignableFrom(serviceInterfaceClass) &&
                            !jakarta.xml.ws.Service.class.equals(serviceInterfaceClass) ) {
                        // OK - the interface class is indeed the generated service class; get an instance
                        injValue = initiateInstance(serviceInterfaceClass, desc);
                    } else {
                        // First try failed; Try to get the Service class type from injected field name
                        // and from there try to get an instance of the service class

                        // I assume the at all inejction target are expecting the SAME service
                        // interface, therefore I take the first one.
                        if (desc.isInjectable()) {

                            InjectionTarget target = desc.getInjectionTargets().iterator().next();
                            Class serviceType = null;
                            if (target.isFieldInjectable()) {
                                java.lang.reflect.Field f = target.getField();
                                if(f == null) {
                                    String fName = target.getFieldName();
                                    Class targetClass = cl.loadClass(target.getClassName());
                                    try {
                                        f = targetClass.getDeclaredField(fName);
                                    } catch(java.lang.NoSuchFieldException nsfe) {}// ignoring exception
                                }
                                if(f != null) {
                                    serviceType = f.getType();
                                }
                            }
                            if (target.isMethodInjectable()) {
                                Method m = target.getMethod();
                                if(m == null) {
                                    String mName = target.getMethodName();
                                    Class targetClass = cl.loadClass(target.getClassName());
                                    try {
                                        m = targetClass.getDeclaredMethod(mName);
                                    } catch(java.lang.NoSuchMethodException nsfe) {}// ignoring exception
                                }
                                if (m != null && m.getParameterTypes().length==1) {
                                    serviceType = m.getParameterTypes()[0];
                                }
                            }
                            if (serviceType!=null){
                                Class loadedSvcClass = cl.loadClass(serviceType.getCanonicalName());
                                injValue = initiateInstance(loadedSvcClass, desc);
                            }
                        }
                    }
                    // Unable to get hold of generated service class -> try the Service.create avenue to get a Service
                    if(injValue == null) {
                        // Here create the service with WSDL (overridden wsdl if wsdl-override is present)
                        // so that JAXWS runtime uses this wsdl @ runtime
                        jakarta.xml.ws.Service svc =
                                jakarta.xml.ws.Service.create((new WsUtil()).privilegedGetServiceRefWsdl(desc),
                                        desc.getServiceName());
                        jaxwsDelegate = new JAXWSServiceDelegate(desc, svc, cl);
                    }


                // check if this is a post 1.1 web service
                if (jakarta.xml.ws.Service.class.isAssignableFrom(serviceInterfaceClass)) {
                    // This is a JAXWS based webservice client;
                    // process handlers and mtom setting
                    // moved test for handlers into wsUtil, in case
                    // we have to add system handler

                    jakarta.xml.ws.Service service = injValue == null ? jaxwsDelegate
                        : (jakarta.xml.ws.Service) injValue;

                    if (service != null) {
                        // Now configure client side handlers
                        wsUtil.configureJAXWSClientHandlers(service, desc);
                    }
                    // the requested resource is not the service but one of its port.
                    if (injValue != null && desc.getInjectionTargetType() != null) {
                        Class requestedPortType = service.getClass().getClassLoader().loadClass(desc.getInjectionTargetType());
                        ArrayList<WebServiceFeature> wsFeatures = getWebServiceFeatures(desc);
                        if (wsFeatures.size() >0) {
                             injValue = service.getPort(requestedPortType,wsFeatures.toArray(new WebServiceFeature[wsFeatures.size()]));
                        }   else {
                            injValue = service.getPort(requestedPortType);
                        }
                    }

                }

            if(jaxwsDelegate != null) {
                returnObj = jaxwsDelegate;
            } else if(injValue != null) {
                returnObj = injValue;
            }
        } catch(PrivilegedActionException pae) {
            LOG.log(WARNING, LogUtils.EXCEPTION_THROWN, pae);
            NamingException ne = new NamingException();
            ne.initCause(pae.getCause());
            throw ne;
        } catch(Exception e) {
            LOG.log(WARNING, LogUtils.EXCEPTION_THROWN, e);
            NamingException ne = new NamingException();
            ne.initCause(e);
            throw ne;
        } finally {
            WSContainerResolver.unset();
        }

        return returnObj;
    }


    private Object initiateInstance(Class<?> svcClass, ServiceReferenceDescriptor desc) throws Exception {
        WsUtil wsu = new WsUtil();
        URL wsdlFile = wsu.privilegedGetServiceRefWsdl(desc);
        Constructor<?> cons = svcClass.getConstructor(new Class[] {URL.class, QName.class});
        try {
            return cons.newInstance(wsdlFile, desc.getServiceName());
        } catch (InvocationTargetException e) {
            LOG.log(Level.CONFIG,
                "Service constructor failed, now we will try to generate the WSDL on our own, if it is possible.", e);
            // If WSDL URL is not accessible over http, trying to get an instance via
            // reflection results in InvocationTargetException.
            // If InvocationTargetException is thrown,then catch the exception and generate wsdl
            // in generated xml directory of the application being deployed.
            URL optionalWsdlURL = generateWsdlFile(desc);
            if (optionalWsdlURL == null) {
                throw e;
            }
            return cons.newInstance(optionalWsdlURL, desc.getServiceName());
        }
    }


    /**
     * This method returns the location where optional wsdl file will be generated.
     * The directory will be a directory having same name as WebService name inside
     * application's generated xml directory. The name of the wsdl file will be
     * wsdl.xml e.g. if application name is test and service name is Translator,
     * then the location of wsdl will be
     * $Glassfish_home/domains/domain1/generated/xml/test/Translator/wsdl.xml
     *
     * @param desc ServiceReferenceDescriptor
     * @return optional wsdl file location
     */
    private File getOptionalWsdlLocation(ServiceReferenceDescriptor desc) {
        File generatedXmlDir = serverEnv.getApplicationGeneratedXMLPath();
        return new File(new File(new File(generatedXmlDir,
                VersioningUtils.getRepositoryName(desc.getBundleDescriptor().getApplication()
                        .getRegistrationName())), desc.getServiceLocalPart()), "wsdl.xml");
    }

    private void createParentDirs(File optionalWsdlLocation) throws IOException {
        File parent = optionalWsdlLocation.getParentFile();
        mkDirs(parent);
    }

    private URL generateWsdlFile(ServiceReferenceDescriptor desc) throws IOException {

       /*
        * Following piece of code is basically a copy-paste from JAXWSServlet's
        * doGet method (line 230) and from com.sun.xml.ws.transport.http.servlet.HttpAdapter's
        * publishWSDL method (line 587).This piece of code is not completely clear to me,
        * what I have understood so far is, during WSEndPoint creation on line 267 in
        * WSServletContextListener, com.sun.xml.ws.server.EndPointFactory.create (line 116)
        * method is invoked where ServiceDocumentImpl instance is created, which is later
        * being fetched here to generate wsdl. When serviceDefinition.getPrimary() is
        * invoked, basically it returns the reference to wsdl document marked as primary
        * wsdl inside ServiceDefinition. Probably we can directly fetch this wsdl
        * but for now I will go with the way it has been implemented in HttpAdapter.
        */

        File optionalWsdl = getOptionalWsdlLocation(desc);

        /*
         * Its possible that in a given application there are more than one Filter/Servlet
         * with loadOnStartup=1 having WebServiceRef annotation,or WebServiceRef
         * annotation is used at multiple places within the same Filter/Servlet,
         * in which case, when processing is going on for second filter/servlet
         * or annotation referring to the same web service, then wsdl file has
         * already been generated at this point in time and there is no need to
         * generate it again.
         */

        if (optionalWsdl.exists()) {
            LOG.log(FINEST, "The file already exists, so I am returning it: {0}", optionalWsdl);
            return optionalWsdl.toURI().toURL();
        }

        createParentDirs(optionalWsdl);
        ServletAdapter targetEndpoint = getServletAdapter(desc);
        if (targetEndpoint == null) {
            LOG.log(WARNING,
                "Target endpoint's servlet adapter wasn't found, I cannot generate the wsdl, returning null.");
            return null;
        }
        ServiceDefinition serviceDefinition = targetEndpoint.getServiceDefinition();
        SDDocument wsdlDocument = null;
        for (SDDocument xsdnum : serviceDefinition) {
            if (xsdnum == serviceDefinition.getPrimary()) {
                wsdlDocument = xsdnum;
                break;
            }
        }

        if (wsdlDocument == null) {
            LOG.log(FINEST, "Service definition document wasn't found, I cannot generate the wsdl, returning null.");
            return null;
        }

        LOG.log(INFO, "Generating the WSDL: {0}", optionalWsdl);
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(optionalWsdl))) {
            PortAddressResolver portAddressResolver = targetEndpoint
                .getPortAddressResolver(getBaseAddress(desc.getWsdlFileUrl()));
            DocumentAddressResolver resolver = targetEndpoint.getDocumentAddressResolver(portAddressResolver);
            wsdlDocument.writeTo(portAddressResolver, resolver, outputStream);
        }

        return optionalWsdl.toURI().toURL();
    }

    /**
     * Returns ServletAdapter instance holding wsdl for the WebService being referred
     * in WebServiceRef annotation.
     *
     * @param desc ServiceReferenceDescriptor
     * @return ServletAdapter instance having wsdl contents.
     */
    private ServletAdapter getServletAdapter(ServiceReferenceDescriptor desc) {

        /*
         * If flow has reached to this part of the code,then in all likelihood,
         * the wsdl is available under the context root of the a web application
         * and hence the BundleDescriptor being referred in ServiceReferenceDescriptor
         * is an instance of WebBundleDescriptor.
         */
        if (!(desc.getBundleDescriptor() instanceof WebBundleDescriptor)) {

            /*
             * If above assumption is not true, then make one last attempt to fetch
             * all required params from the wsdl url stored in ServiceReferenceDescriptor.
             */
            return getServletAdapterBasedOnWsdlUrl(desc);
        }

        WebBundleDescriptor webBundle = ((WebBundleDescriptor) desc.getBundleDescriptor());

        /*
         * Get WebServicesDescriptor from WebBundleDescriptorImpl, Since we are
         * dealing with WebServiceRef annotation here, WebServicesDescriptor ought to have
         * reference to WebService in question. WebServicesDescriptor is never null as it
         * is being initialized at class level in BundleDescriptor.
         */
        WebServicesDescriptor wsDesc = webBundle.getWebServices();

        /*
         * WebService name is being fetched by invoking getServiceLocalPart()
         * on ServiceReferenceDescriptor. ServiceLocalPart is set when WebServiceClient
         * annotated class is processed inside
         * org.glassfish.webservices.connector.annotation.handlers.WebServiceRefHandler's
         * processAWsRef call (line 339). WebServiceClient annotation have name param pointing
         * to webservice in question.
         */
        WebService webService = wsDesc.getWebServiceByName(desc.getServiceLocalPart());

        /*
         * If an unlikely event when there is no associated webService or desc.getServiceLocalPart()
         * itself is null, then fall back on fetching ServletAdapter based on wsdl url.
         */
        if (webService == null) {
            return getServletAdapterBasedOnWsdlUrl(desc);
        }

        String contextRoot = webBundle.getContextRoot();
        /*
         * Iterate over all associated WebServiceEndPoints for this WebService
         * and break when condition specified in if block is met. This is the same
         * condition based on which wsdl url is set in first place for this
         * ServiceReferenceDescriptor and hence this must hold true in this context too.
         */

        for (WebServiceEndpoint endpoint : webService.getEndpoints()) {
            if (desc.getServiceName().equals(endpoint.getServiceName())
                    && desc.getServiceNamespaceUri().equals(endpoint.getWsdlService().getNamespaceURI())) {
                String endPointAddressURI = endpoint.getEndpointAddressUri();
                if (endPointAddressURI == null || endPointAddressURI.isEmpty()) {
                    LOG.log(Level.FINEST, "The endpoint address uri is null or empty, returning null.");
                    return null;
                }
                String webSevicePath = endPointAddressURI.startsWith("/") ? endPointAddressURI
                    : ("/" + endPointAddressURI);
                String publishingContext = "/" + endpoint.getPublishingUri() + "/" + webService.getWsdlFileUri();
                Adapter<?> adapter = JAXWSAdapterRegistry.getInstance()
                        .getAdapter(contextRoot, webSevicePath, publishingContext);
                return adapter instanceof ServletAdapter ? (ServletAdapter) adapter : null;

            }
        }
        return null;
    }


    /**
     * This method basically is a fall back mechanism to fetch required
     * parameters from wsdl url stored in ServiceReferenceDescriptor. The flow reaches
     * here only in case where required parameters could not be fetched
     * from WebBundleDescriptor.
     *
     * @param desc ServiceReferenceDescriptor
     * @return ServletAdapter instance having wsdl contents.
     */
    private ServletAdapter getServletAdapterBasedOnWsdlUrl(ServiceReferenceDescriptor desc) {

        if (LOG.isLoggable(INFO)) {
            LOG.log(INFO, LogUtils.SERVLET_ADAPTER_BASED_ON_WSDL_URL,
                    new Object[]{desc.getServiceLocalPart(), desc.getWsdlFileUrl()});
        }

        URL wsdl = desc.getWsdlFileUrl();
        if (wsdl == null) {
            return null;
        }
        String wsdlPath = wsdl.getPath().trim();
        if (!wsdlPath.contains(WebServiceEndpoint.PUBLISHING_SUBCONTEXT)) {
            return null;
        }

         /*
          * WsdlPath indeed contains the WebServiceEndpoint.PUBLISHING_SUBCONTEXT,
          * e.g.assuming that context root is test and Service name is Translator
          * then wsdl url must be in the following format :
          * /test/Translator/__container$publishing$subctx/null?wsdl
          */

        String contextRootAndPath = wsdlPath.substring(1,
                wsdlPath.indexOf(WebServiceEndpoint.PUBLISHING_SUBCONTEXT) - 1); // test/Translator
        if (contextRootAndPath.isEmpty()) {
            return null;
        }
        String[] contextRootAndPathArray = contextRootAndPath.split("/"); // {test, Translator}
        if (contextRootAndPathArray.length != 2) {
            return null;
        }
        if (contextRootAndPathArray[0] == null) {
            return null;
        }
        String contextRoot = "/" + contextRootAndPathArray[0];  // /test
        if (contextRootAndPathArray[1] == null) {
            return null;
        }
        String webSevicePath = "/" + contextRootAndPathArray[1]; // /Translator
        String urlPattern = wsdlPath.substring(contextRoot.length());
        Adapter adapter = JAXWSAdapterRegistry.getInstance()
                .getAdapter(contextRoot, webSevicePath, urlPattern);
        return adapter instanceof ServletAdapter ? (ServletAdapter) adapter : null;

    }

    private static String getBaseAddress(URL wsdlUrl) {
        return wsdlUrl.getProtocol() + "://" + wsdlUrl.getHost() + ":" + wsdlUrl.getPort();
    }

    private void mkDirs(File f) {
        if (!f.mkdirs() && LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, LogUtils.DIR_EXISTS, f);
        }
    }


    /**
     * JAXWS 2.2 enables {@link MTOM}, {@link Addressing}, {@link RespectBinding} on WebServiceRef
     * If these are present use the Service(url,wsdl,features) constructor
     */
    private ArrayList<WebServiceFeature> getWebServiceFeatures(ServiceReferenceDescriptor desc) {
         ArrayList<WebServiceFeature> wsFeatures = new ArrayList<>();
         if (desc.isMtomEnabled()) {
             wsFeatures.add(new MTOMFeature(true, desc.getMtomThreshold()));
         }
         com.sun.enterprise.deployment.Addressing add = desc.getAddressing();
         if (add != null) {
             wsFeatures.add(new AddressingFeature(add.isEnabled(), add.isRequired(), getResponse(add.getResponses())));
         }
         com.sun.enterprise.deployment.RespectBinding rb = desc.getRespectBinding();
         if (rb != null) {
             wsFeatures.add(new RespectBindingFeature(rb.isEnabled()));
         }
         Map<Class<? extends Annotation>, Annotation> otherAnnotations = desc.getOtherAnnotations();
         for (Annotation annotation : otherAnnotations.values()) {
             wsFeatures.add(getWebServiceFeatureBean(annotation));
         }

         return wsFeatures;
    }

    private AddressingFeature.Responses getResponse(String s) {
        if (s == null) {
            return AddressingFeature.Responses.ALL;
        }
        return Enum.valueOf(AddressingFeature.Responses.class, s);
    }

    private void resolvePortComponentLinks(ServiceReferenceDescriptor desc) throws Exception {

        // Resolve port component links to target endpoint address.
        // We can't assume web service client is running in same VM
        // as endpoint in the intra-app case because of app clients.
        //
        // Also set port-qname based on linked port's qname if not
        // already set.
        for (Object element : desc.getPortsInfo()) {
            ServiceRefPortInfo portInfo = (ServiceRefPortInfo) element;

            if( portInfo.isLinkedToPortComponent() ) {
                WebServiceEndpoint linkedPortComponent = portInfo.getPortComponentLink();

                // XXX-JD we could at this point try to figure out the
                // endpoint-address from the ejb wsdl file but it is a
                // little complicated so I will leave it for post Beta2
                if( !(portInfo.hasWsdlPort()) ) {
                    portInfo.setWsdlPort(linkedPortComponent.getWsdlPort());
                }
            }
        }
    }

    private  WebServiceFeature getWebServiceFeatureBean(Annotation a) {
        WebServiceFeatureAnnotation wsfa = a.annotationType().getAnnotation(WebServiceFeatureAnnotation.class);

        Class<? extends WebServiceFeature> beanClass = wsfa.bean();
        WebServiceFeature bean;

        Constructor ftrCtr = null;
        String[] paramNames = null;
        for (Constructor con : beanClass.getConstructors()) {
            FeatureConstructor ftrCtrAnn = (FeatureConstructor) con.getAnnotation(FeatureConstructor.class);
            if (ftrCtrAnn != null) {
                if (ftrCtr == null) {
                    ftrCtr = con;
                    paramNames = ftrCtrAnn.value();
                } else {
                    throw new WebServiceException(ModelerMessages.RUNTIME_MODELER_WSFEATURE_MORETHANONE_FTRCONSTRUCTOR(a, beanClass));
                }
            }
        }
        if (ftrCtr == null) {
            throw new WebServiceException(ModelerMessages.RUNTIME_MODELER_WSFEATURE_NO_FTRCONSTRUCTOR(a, beanClass));
        }
        if (ftrCtr.getParameterTypes().length != paramNames.length) {
            throw new WebServiceException(ModelerMessages.RUNTIME_MODELER_WSFEATURE_ILLEGAL_FTRCONSTRUCTOR(a, beanClass));
        }

        try {
            Object[] params = new Object[paramNames.length];
            for (int i = 0; i < paramNames.length; i++) {
                Method m = a.annotationType().getDeclaredMethod(paramNames[i]);
                params[i] = m.invoke(a);
            }
            bean = (WebServiceFeature) ftrCtr.newInstance(params);
        } catch (Exception e) {
            throw new WebServiceException(e);
        }

        return bean;
    }


}

