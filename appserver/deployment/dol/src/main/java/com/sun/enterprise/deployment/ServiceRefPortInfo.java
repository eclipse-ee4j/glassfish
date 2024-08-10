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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.namespace.QName;

import org.glassfish.deployment.common.Descriptor;

/**
 * Information about a single WSDL port or port type in a service reference.
 *
 * @author Kenneth Saks
 */
public class ServiceRefPortInfo extends Descriptor {

    private static final long serialVersionUID = 1L;

    private String serviceEndpointInterface;

    private boolean containerManaged;

    private String portComponentLinkName;
    private WebServiceEndpoint portComponentLink;

    // Service reference with which this port info is associated.
    private ServiceReferenceDescriptor serviceRef;

    //
    // Runtime info
    //

    private QName wsdlPort;

    // Set of name/value pairs corresponding to JAXRPC Stub properties.
    private final Set<NameValuePairDescriptor> stubProperties;

    // Set of name/value pairs corresponding to JAXRPC Call properties.
    private final Set<NameValuePairDescriptor> callProperties;

    // Target endpoint address of linked port component.  This is derived
    // and set at runtime.  There is no element for it in sun-j2ee-ri.xml
    private String targetEndpointAddress;

    // message-security-binding
    private MessageSecurityBindingDescriptor messageSecBindingDesc;

    private String mtomEnabled;

    public ServiceRefPortInfo(ServiceRefPortInfo other) {
        super(other);
        serviceEndpointInterface = other.serviceEndpointInterface;
        containerManaged = other.containerManaged;
        portComponentLinkName = other.portComponentLinkName;
        portComponentLink = other.portComponentLink; // copy as-is
        serviceRef = other.serviceRef; // copy as-is
        wsdlPort = other.wsdlPort; // copy as-is
        mtomEnabled = other.mtomEnabled;

        stubProperties = new HashSet<>();
        for (Object element : other.stubProperties) {
            stubProperties.add(new NameValuePairDescriptor((NameValuePairDescriptor) element));
        }

        callProperties = new HashSet<>(); // NameValuePairDescriptor
        for (NameValuePairDescriptor element : other.callProperties) {
            callProperties.add(new NameValuePairDescriptor(element));
        }
        targetEndpointAddress = other.targetEndpointAddress;
    }


    public ServiceRefPortInfo() {
        stubProperties = new HashSet<>();
        callProperties = new HashSet<>();
        containerManaged = false;
    }


    public void setServiceReference(ServiceReferenceDescriptor desc) {
        serviceRef = desc;
    }


    public ServiceReferenceDescriptor getServiceReference() {
        return serviceRef;
    }


    public boolean hasServiceEndpointInterface() {
        return serviceEndpointInterface != null;
    }


    public void setServiceEndpointInterface(String sei) {
        serviceEndpointInterface = sei;
    }


    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }


    public void setIsContainerManaged(boolean flag) {
        containerManaged = flag;
    }


    public boolean isContainerManaged() {
        return containerManaged;
    }


    public boolean isClientManaged() {
        return !containerManaged;
    }


    /**
     * Sets the name of the port component to which I refer.
     * NOTE : Does *NOT* attempt to resolve link name. Use
     * overloaded version or resolveLink if link resolution
     * is required.
     */
    public void setPortComponentLinkName(String linkName) {
        setPortComponentLinkName(linkName, false);
    }


    public WebServiceEndpoint setPortComponentLinkName(String linkName, boolean resolve) {
        portComponentLinkName = linkName;
        return resolve ? resolveLinkName() : null;
    }


    public boolean hasPortComponentLinkName() {
        return (portComponentLinkName != null);
    }


    public String getPortComponentLinkName() {
        return portComponentLinkName;
    }


    public void setMessageSecurityBinding(MessageSecurityBindingDescriptor messageSecBindingDesc) {
        this.messageSecBindingDesc = messageSecBindingDesc;
    }


    public MessageSecurityBindingDescriptor getMessageSecurityBinding() {
        return messageSecBindingDesc;
    }


    /**
     * @return true only if there is a port component link AND it has been
     *         resolved to a valid port component within the application.
     */
    public boolean isLinkedToPortComponent() {
        return portComponentLinkName != null && portComponentLink != null;
    }


    /**
     * Try to resolve the current link name value to a WebServiceEndpoint object.
     *
     * @return {@link WebServiceEndpoint} to which link was resolved, or null if link name
     *         resolution failed.
     */
    public WebServiceEndpoint resolveLinkName() {
        final String linkName = portComponentLinkName;
        if (linkName == null || linkName.isEmpty()) {
            return null;
        }
        final int hashIndex = linkName.indexOf('#');
        final boolean relativeLink = hashIndex == -1;

        final BundleDescriptor bundleDescriptor = getBundleDescriptor();
        final Application app = bundleDescriptor.getApplication();

        final String portName;
        final BundleDescriptor targetBundle;
        if (app == null || relativeLink) {
            targetBundle = bundleDescriptor;
            portName = linkName;
        } else {
            // Resolve <module>#<port-component-name> style link
            final String relativeModuleUri = linkName.substring(0, hashIndex);
            portName = linkName.substring(hashIndex + 1);
            targetBundle = app.getRelativeBundle(bundleDescriptor, relativeModuleUri);
        }

        // targetBundle will only be null here if module lookup for absolute link failed.
        if (targetBundle == null) {
            return null;
        }
        final LinkedList<BundleDescriptor> bundles = new LinkedList<>();
        bundles.addFirst(targetBundle);
        if (app != null && relativeLink) {
            bundles.addAll(app.getBundleDescriptors());
        }
        for (final BundleDescriptor bundle : bundles) {
            final WebServiceEndpoint port = bundle.getWebServiceEndpointByName(portName);
            if (port != null) {
                setPortComponentLink(port);
                return port;
            }
        }
        return null;
    }


    public WebServiceEndpoint getPortComponentLink() {
        return portComponentLink;
    }


    /**
     * @param newPort the port component to which I refer
     */
    public void setPortComponentLink(WebServiceEndpoint newPort) {
        if (newPort != null) {
            // Keep port component link name in synch with port component
            // object.
            BundleDescriptor bundleDescriptor = getBundleDescriptor();
            BundleDescriptor targetBundleDescriptor = newPort.getBundleDescriptor();
            String linkName = newPort.getEndpointName();
            if (bundleDescriptor != targetBundleDescriptor) {
                Application app = bundleDescriptor.getApplication();
                String relativeUri = app.getRelativeUri(bundleDescriptor, targetBundleDescriptor);
                linkName = relativeUri + "#" + linkName;
            }
            portComponentLinkName = linkName;
        }
        portComponentLink = newPort;
    }


    private BundleDescriptor getBundleDescriptor() {
        return serviceRef.getBundleDescriptor();
    }


    public boolean hasWsdlPort() {
        return wsdlPort != null;
    }


    public void setWsdlPort(QName port) {
        wsdlPort = port;
    }


    public QName getWsdlPort() {
        return wsdlPort;
    }


    /**
     * @return set of {@link NameValuePairDescriptor} objects for each stub property.
     */
    public Set<NameValuePairDescriptor> getStubProperties() {
        return stubProperties;
    }


    public boolean hasStubProperty(String name) {
        return getStubPropertyValue(name) != null;
    }


    public String getStubPropertyValue(String name) {
        for (NameValuePairDescriptor element : stubProperties) {
            if (element.getName().equals(name)) {
                return element.getValue();
            }
        }
        return null;
    }


    public NameValuePairDescriptor getStubPropertyByName(String name) {
        for (NameValuePairDescriptor element : stubProperties) {
            if (element.getName().equals(name)) {
                return element;
            }
        }
        return null;
    }


    /**
     * Add stub property, using property name as a key. This will
     * replace the property value of any existing stub property with
     * the same name.
     */
    public void addStubProperty(NameValuePairDescriptor property) {
        NameValuePairDescriptor prop = getStubPropertyByName(property.getName());
        if (prop == null) {
            stubProperties.add(property);
        } else {
            prop.setValue(property.getValue());
        }
    }


    /**
     * Remove stub property, using property name as a key. This will
     * remove the property value of an existing stub property with
     * the matching name.
     */
    public void removeStubProperty(NameValuePairDescriptor property) {
        NameValuePairDescriptor prop = getStubPropertyByName(property.getName());
        if (prop != null) {
            stubProperties.remove(property);
        }
    }


    /**
     * Add stub property, using property name as a key. This will
     * replace the property value of any existing stub property with
     * the same name.
     */
    public void addStubProperty(String name, String value) {
        NameValuePairDescriptor nvPair = new NameValuePairDescriptor();
        nvPair.setName(name);
        nvPair.setValue(value);
        addStubProperty(nvPair);
    }


    public Set<NameValuePairDescriptor> getCallProperties() {
        return callProperties;
    }


    public boolean hasCallProperty(String name) {
        return getCallPropertyByName(name) != null;
    }


    public NameValuePairDescriptor getCallPropertyByName(String name) {
        for (NameValuePairDescriptor next : callProperties) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        return null;
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


    public boolean hasTargetEndpointAddress() {
        return targetEndpointAddress != null;
    }


    public void setTargetEndpointAddress(String address) {
        targetEndpointAddress = address;
    }


    public String getTargetEndpointAddress() {
        return targetEndpointAddress;
    }


    public void setMtomEnabled(String value) {
        mtomEnabled = value;
    }


    public String getMtomEnabled() {
        return mtomEnabled;
    }
}
