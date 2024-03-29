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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.types.HandlerChainContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.namespace.QName;

import org.glassfish.api.naming.SimpleJndiName;

/**
 * Information about a J2EE web service client.
 *
 * @author Kenneth Saks
 */
public class ServiceReferenceDescriptor extends EnvironmentProperty implements HandlerChainContainer {

    private static final long serialVersionUID = 1L;
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(ServiceReferenceDescriptor.class);

    private String serviceInterface;
    private SimpleJndiName mappedName;
    private String wsdlFileUri;

    /**
     * Derived, non-peristent location of wsdl file.
     * Only used at deployment/runtime.
     */
    private URL wsdlFileUrl;

    private String mappingFileUri;

    /**
     * Derived, non-peristent location of mapping file.
     * Only used at deployment/runtime.
     */
    private File mappingFile;

    // Optional service name.  Only required if service-ref has WSDL and
    // the WSDL defines multiple services.
    private String serviceNamespaceUri;
    private String serviceLocalPart;
    private String serviceNameNamespacePrefix;

    // settings for both container-managed and client-managed ports
    private final Set<ServiceRefPortInfo> portsInfo;

    // module in which this reference is defined.
    private BundleDescriptor bundleDescriptor;

    // List of handlers associated with this service reference.
    // Handler order is important and must be preserved.
    private final LinkedList<WebServiceHandler> handlers;

    // The handler chains defined for this service ref (JAXWS service-ref)
    private final LinkedList<WebServiceHandlerChain> handlerChain;

    private final Set<NameValuePairDescriptor> callProperties;

    // Name of generated service implementation class.
    private String serviceImplClassName;

    // Optional wsdl to be used at deployment instead of the wsdl packaged
    // in module and associated with the service-ref.
    private URL wsdlOverride;

    // interface name of the expected injection recipient.
    // because web service reference are a bit specific (you can inject
    // the Service interface or the Port interface directly), you
    // may need to disambiguate when loading from XML DDs.
    private String injectionTargetType;

    //Support for JAXWS 2.2 features
    //@MTOM for WebserviceRef
    // Boolean instead of boolean for tri-value - true/false/not-set
    private Boolean mtomEnabled ;

    //Support for JAXWS 2.2 features
    //@RespectBinding for WebserviceRef
    private RespectBinding respectBinding;

    //Support for JAXWS 2.2 features
    //@Addressing for WebserviceRef
    private Addressing addressing;

    //Support for JAXWS 2.2 features
    //mtomThreshold
    private int mtomThreshold;

    /**
     * In addition to MTOM,Addressing , RespectBinding
     * pass over other annotations too.
     */
    private Map<Class<? extends Annotation>, Annotation> otherAnnotations = new HashMap<>();

    public ServiceReferenceDescriptor(String name, String description, String service) {
        super(name, "", description);
        handlers = new LinkedList<>();
        handlerChain = new LinkedList<>();
        portsInfo = new HashSet<>();
        callProperties = new HashSet<>();
        serviceInterface = service;
    }


    public ServiceReferenceDescriptor() {
        handlers = new LinkedList<>();
        handlerChain = new LinkedList<>();
        portsInfo = new HashSet<>();
        callProperties = new HashSet<>();
    }


    public Map<Class<? extends Annotation>, Annotation> getOtherAnnotations() {
        return otherAnnotations;
    }


    public void setOtherAnnotations(Map<Class<? extends Annotation>, Annotation> otherAnnotations) {
        this.otherAnnotations = otherAnnotations;
    }


    public boolean isRespectBindingEnabled() {
        return respectBinding.isEnabled();
    }


    public Addressing getAddressing() {
        return addressing;
    }


    public RespectBinding getRespectBinding() {
        return respectBinding;
    }


    public void setRespectBinding(RespectBinding respectBinding) {
        this.respectBinding = respectBinding;
    }


    public boolean hasMtomEnabled() {
        return mtomEnabled != null;
    }


    public boolean isMtomEnabled() {
        return mtomEnabled != null && mtomEnabled.booleanValue();
    }


    public void setMtomEnabled(boolean mtomEnabled) {
        this.mtomEnabled = Boolean.valueOf(mtomEnabled);
    }


    public boolean isAddressingEnabled() {
        return addressing.isEnabled();
    }


    public void setAddressing(Addressing addressing) {
        this.addressing = addressing;
    }


    public boolean isAddressingRequired() {
        return addressing.isRequired();
    }


    public int getMtomThreshold() {
        return mtomThreshold;
    }


    public void setMtomThreshold(int mtomThreshold) {
        this.mtomThreshold = mtomThreshold;
    }


    @Override
    public SimpleJndiName getMappedName() {
        return mappedName;
    }


    @Override
    public void setMappedName(SimpleJndiName value) {
        mappedName = value;
    }


    public void setBundleDescriptor(BundleDescriptor bundle) {
        bundleDescriptor = bundle;
    }


    public BundleDescriptor getBundleDescriptor() {
        return bundleDescriptor;
    }


    public boolean hasGenericServiceInterface() {
        return false;
    }


    public boolean hasGeneratedServiceInterface() {
        return !hasGenericServiceInterface();
    }


    public void setServiceInterface(String service) {
        serviceInterface = service;

    }


    public String getServiceInterface() {
        return serviceInterface;
    }


    public boolean hasWsdlFile() {
        return wsdlFileUri != null && !wsdlFileUri.isEmpty();
    }


    /**
     * Derived, non-peristent location of wsdl file.
     * Only used at deployment/runtime.
     */
    public void setWsdlFileUrl(URL url) {
        wsdlFileUrl = url;

    }


    public URL getWsdlFileUrl() {
        return wsdlFileUrl;
    }


    public void setWsdlFileUri(String uri) {
        if (uri.startsWith("file:")) {
            uri = uri.substring(5);
        }
        wsdlFileUri = uri;
    }


    public String getWsdlFileUri() {
        return wsdlFileUri;
    }


    public boolean hasMappingFile() {
        return mappingFileUri != null;
    }


    /**
     * Derived, non-peristent location of mapping file.
     * Only used at deployment/runtime.
     */
    public void setMappingFile(File file) {
        mappingFile = file;
    }


    public File getMappingFile() {
        return mappingFile;
    }


    public void setMappingFileUri(String uri) {
        mappingFileUri = uri;

    }


    public String getMappingFileUri() {
        return mappingFileUri;
    }


    public void setServiceName(QName serviceName) {
        setServiceName(serviceName, null);
    }


    public void setServiceName(QName serviceName, String prefix) {
        serviceNamespaceUri = serviceName.getNamespaceURI();
        serviceLocalPart = serviceName.getLocalPart();
        serviceNameNamespacePrefix = prefix;
    }


    public void setServiceNamespaceUri(String uri) {
        serviceNamespaceUri = uri;
        serviceNameNamespacePrefix = null;
    }


    public String getServiceNamespaceUri() {
        return serviceNamespaceUri;
    }


    public void setServiceLocalPart(String localpart) {
        serviceLocalPart = localpart;
        serviceNameNamespacePrefix = null;
    }


    public String getServiceLocalPart() {
        return serviceLocalPart;
    }


    public void setServiceNameNamespacePrefix(String prefix) {
        serviceNameNamespacePrefix = prefix;
    }


    public String getServiceNameNamespacePrefix() {
        return serviceNameNamespacePrefix;
    }


    public boolean hasServiceName() {
        return serviceNamespaceUri != null && serviceLocalPart != null;
    }


    /**
     * @return service QName or null if either part of qname is not set
     */
    public QName getServiceName() {
        return hasServiceName() ? new QName(serviceNamespaceUri, serviceLocalPart) : null;
    }


    public Set<ServiceRefPortInfo> getPortsInfo() {
        return portsInfo;
    }


    public void addPortInfo(ServiceRefPortInfo portInfo) {
        portInfo.setServiceReference(this);
        portsInfo.add(portInfo);
    }


    public void removePortInfo(ServiceRefPortInfo portInfo) {
        portsInfo.remove(portInfo);
    }


    /**
     * Special handling of case where runtime port info is added.
     * Ensures that port info is not duplicated when multiple
     * runtime info instances are parsed using same standard descriptor.
     */
    public void addRuntimePortInfo(ServiceRefPortInfo runtimePortInfo) {
        ServiceRefPortInfo existing = null;

        if (runtimePortInfo.hasServiceEndpointInterface()) {
            existing = getPortInfoBySEI(runtimePortInfo.getServiceEndpointInterface());
        }
        if (existing == null && runtimePortInfo.hasWsdlPort()) {
            existing = getPortInfoByPort(runtimePortInfo.getWsdlPort());
        }

        if (existing == null) {
            if (portsInfo != null && !portsInfo.isEmpty()) {
                DOLUtils.getDefaultLogger().warning(
                    I18N.getLocalString("enterprise.deployment.unknownportforruntimeinfo",
                    "Runtime port info SEI {0} is not declared in standard service-ref " +
                    "deployment descriptors (under port-component-ref), is this intended ?",
                    new Object[] {runtimePortInfo.getServiceEndpointInterface()}));
            }
            addPortInfo(runtimePortInfo);
        } else {
            if (!existing.hasServiceEndpointInterface()) {
                existing.setServiceEndpointInterface(runtimePortInfo.getServiceEndpointInterface());
            }
            if (!existing.hasWsdlPort()) {
                existing.setWsdlPort(runtimePortInfo.getWsdlPort());
            }
            for (NameValuePairDescriptor element : runtimePortInfo.getStubProperties()) {
                // adds using name as key
                existing.addStubProperty(element);
            }
            for (NameValuePairDescriptor element : runtimePortInfo.getCallProperties()) {
                // adds using name as key
                existing.addCallProperty(element);
            }
            if (runtimePortInfo.getMessageSecurityBinding() != null) {
                existing.setMessageSecurityBinding(runtimePortInfo.getMessageSecurityBinding());
            }
        }
    }


    public ServiceRefPortInfo addContainerManagedPort(String serviceEndpointInterface) {
        ServiceRefPortInfo info = new ServiceRefPortInfo();
        info.setServiceEndpointInterface(serviceEndpointInterface);
        info.setIsContainerManaged(true);
        info.setServiceReference(this);
        portsInfo.add(info);

        return info;
    }


    public boolean hasContainerManagedPorts() {
        boolean containerManaged = false;
        for (ServiceRefPortInfo element : portsInfo) {
            if (element.isContainerManaged()) {
                containerManaged = true;
                break;
            }
        }
        return containerManaged;
    }


    public boolean hasClientManagedPorts() {
        boolean clientManaged = false;
        for (ServiceRefPortInfo element : portsInfo) {
            if (element.isClientManaged()) {
                clientManaged = true;
                break;
            }
        }
        return clientManaged;
    }


    /**
     * Lookup port info by service endpoint interface.
     */
    public ServiceRefPortInfo getPortInfo(String serviceEndpointInterface) {
        return getPortInfoBySEI(serviceEndpointInterface);
    }


    /**
     * Lookup port info by service endpoint interface.
     */
    public ServiceRefPortInfo getPortInfoBySEI(String serviceEndpointInterface) {
        for (ServiceRefPortInfo element : portsInfo) {
            if (serviceEndpointInterface.equals(element.getServiceEndpointInterface())) {
                return element;
            }
        }
        return null;
    }


    /**
     * Lookup port info by wsdl port.
     */
    public ServiceRefPortInfo getPortInfoByPort(QName wsdlPort) {
        for (ServiceRefPortInfo next : portsInfo) {
            if (next.hasWsdlPort() && wsdlPort.equals(next.getWsdlPort())) {
                return next;
            }
        }
        return null;
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


    public boolean hasHandlers() {
        return !handlers.isEmpty();
    }


    /**
     * Get ordered list of WebServiceHandler handlers for this endpoint.
     */
    public LinkedList<WebServiceHandler> getHandlers() {
        return handlers;
    }


    /**
     * HandlerChain related setters, getters, adders, finders
     */
    @Override
    public void addHandlerChain(WebServiceHandlerChain handler) {
        handlerChain.addLast(handler);

    }


    public void removeHandlerChain(WebServiceHandlerChain handler) {
        handlerChain.remove(handler);

    }


    @Override
    public boolean hasHandlerChain() {
        return !handlerChain.isEmpty();
    }


    @Override
    public LinkedList<WebServiceHandlerChain> getHandlerChain() {
        return handlerChain;
    }


    /**
     * Runtime information.
     */
    public Set<NameValuePairDescriptor> getCallProperties() {
        return callProperties;
    }


    public NameValuePairDescriptor getCallPropertyByName(String name) {
        NameValuePairDescriptor prop = null;
        for (NameValuePairDescriptor element : callProperties) {
            if (element.getName().equals(name)) {
                prop = element;
                break;
            }
        }
        return prop;
    }


    /**
     * Add call property, using property name as a key. This will
     * replace the property value of any existing stub property with
     * the same name.
     */
    public void addCallProperty(NameValuePairDescriptor property) {
        NameValuePairDescriptor prop = getCallPropertyByName(property.getName());
        if (prop == null) {
            callProperties.add(property);
        } else {
            prop.setValue(property.getValue());
        }
    }


    /**
     * Remove call property, using property name as a key. This will
     * remove the property value of an existing stub property with
     * the matching name.
     */
    public void removeCallProperty(NameValuePairDescriptor property) {
        NameValuePairDescriptor prop = getCallPropertyByName(property.getName());
        if (prop != null) {
            callProperties.remove(property);
        }
    }


    public boolean hasServiceImplClassName() {
        return serviceImplClassName != null;
    }


    public void setServiceImplClassName(String className) {
        this.serviceImplClassName = className;
    }


    public String getServiceImplClassName() {
        return serviceImplClassName;
    }


    public boolean hasWsdlOverride() {
        return wsdlOverride != null;
    }


    public void setWsdlOverride(URL override) {
        this.wsdlOverride = override;
    }


    public URL getWsdlOverride() {
        return wsdlOverride;
    }


    public void setInjectionTargetType(String type) {
        this.injectionTargetType = type;
    }


    public String getInjectionTargetType() {
        return injectionTargetType;
    }


    /* Equality on name. */
    @Override
    public boolean equals(Object object) {
        if (object instanceof ServiceReferenceDescriptor) {
            ServiceReferenceDescriptor thatReference = (ServiceReferenceDescriptor) object;
            return thatReference.getName().equals(this.getName());
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }


    public boolean isConflict(ServiceReferenceDescriptor other) {
        return (getName().equals(other.getName())) &&
            (!(
                DOLUtils.equals(getServiceInterface(), other.getServiceInterface()) &&
                DOLUtils.equals(getWsdlFileUri(), other.getWsdlFileUri()) &&
                DOLUtils.equals(getMappingFileUri(), other.getMappingFileUri())
                //XXX need to compare the following
                // handler
                // handle-chains
                // port-component-info
                ) ||
            isConflictResourceGroup(other));
    }
}
