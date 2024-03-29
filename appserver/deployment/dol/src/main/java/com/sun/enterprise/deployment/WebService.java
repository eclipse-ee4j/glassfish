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

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import org.glassfish.deployment.common.Descriptor;


/**
 * Information about a single webservice-description in webservices.xml
 *
 * @author Kenneth Saks
 * @author Jerome Dochez
 */
public class WebService extends Descriptor {

    private static final long serialVersionUID = 1L;

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

    private HashMap<String, WebServiceEndpoint> endpoints;

    // The set of web services to which this service belongs.
    private WebServicesDescriptor webServicesDesc;

    //
    // Runtime info
    //
    // Optional file URL to which final wsdl should be published.
    // This represents a directory on the file system from which deployment
    // is initiated. URL schemes other than file: are legal but ignored.
    private URL publishUrl;

    /** type JAX-WS or JAX-RPC */
    private String type;

    private Boolean isJaxWSBased;

    /**
     * Default constructor.
     */
    public WebService() {
        this("");
    }


    /**
     * copy constructor.
     */
    public WebService(WebService other) {
        super(other);
        wsdlFileUri = other.wsdlFileUri; // String
        wsdlFileUrl = other.wsdlFileUrl;
        mappingFileUri = other.mappingFileUri; // String
        mappingFile = other.mappingFile;
        publishUrl = other.publishUrl;
        webServicesDesc = other.webServicesDesc; // copy as-is
        type = other.type;
        if (other.endpoints == null) {
            endpoints = null;
        } else {
            endpoints = new HashMap<>();
            for (WebServiceEndpoint wsep : other.endpoints.values()) {
                wsep.setWebService(this);
                endpoints.put(wsep.getEndpointName(), wsep);
            }
        }
    }


    public WebService(String name) {
        setName(name);
        endpoints = new HashMap<>();
    }

    // getName is not deprecated here.
    @Override
    @SuppressWarnings("removal")
    public final String getName() {
        return super.getName();
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    /**
     * If this returns non-null value, then it is verified that all the endpoints are of the same
     * type.
     */
    public Boolean isJaxWSBased() {
        return isJaxWSBased;
    }


    /**
     * This is called after verifying that all the endpoints are of the same type,
     * either JAX-WS or JAX-RPC
     *
     * @param isJaxWSBased
     */
    public void setJaxWSBased(boolean isJaxWSBased) {
        this.isJaxWSBased = isJaxWSBased;
    }

    public void setWebServicesDescriptor(WebServicesDescriptor webServices) {
        webServicesDesc = webServices;
    }


    public WebServicesDescriptor getWebServicesDescriptor() {
        return webServicesDesc;
    }


    public BundleDescriptor getBundleDescriptor() {
        return webServicesDesc.getBundleDescriptor();
    }


    public boolean hasWsdlFile() {
        return wsdlFileUri != null;
    }


    public void setWsdlFileUri(String uri) {
        wsdlFileUri = uri;
    }


    public String getWsdlFileUri() {
        return wsdlFileUri;
    }


    public URL getWsdlFileUrl() {
        return wsdlFileUrl;
    }


    public void setWsdlFileUrl(URL url) {
        wsdlFileUrl = url;
    }


    public String getGeneratedWsdlFilePath() {
        if (!hasWsdlFile()) {
            return getWsdlFileUrl().getPath();
        }
        String xmlDir = getBundleDescriptor().getApplication().getGeneratedXMLDirectory();
        if (!getBundleDescriptor().getModuleDescriptor().isStandalone()) {
            String uri = getBundleDescriptor().getModuleDescriptor().getArchiveUri();
            xmlDir = xmlDir + File.separator + uri.replaceAll("\\.", "_");
        }
        if (xmlDir == null) {
            return null;
        }
        return xmlDir + File.separator + wsdlFileUri;
    }


    public boolean hasMappingFile() {
        return mappingFileUri != null;
    }


    public void setMappingFileUri(String uri) {
        mappingFileUri = uri;
    }


    public String getMappingFileUri() {
        return mappingFileUri;
    }


    public File getMappingFile() {
        return mappingFile;
    }


    public void setMappingFile(File file) {
        mappingFile = file;
    }


    public void addEndpoint(WebServiceEndpoint endpoint) {
        endpoint.setWebService(this);
        endpoints.put(endpoint.getEndpointName(), endpoint);
    }


    public void removeEndpointByName(String endpointName) {
        WebServiceEndpoint endpoint = endpoints.remove(endpointName);
        endpoint.setWebService(null);
    }


    public WebServiceEndpoint getEndpointByName(String name) {
        return endpoints.get(name);
    }


    public void removeEndpoint(WebServiceEndpoint endpoint) {
        removeEndpointByName(endpoint.getEndpointName());
    }


    public Collection<WebServiceEndpoint> getEndpoints() {
        HashMap<String, WebServiceEndpoint> shallowCopy = new HashMap<>(endpoints);
        return shallowCopy.values();
    }


    public boolean hasClientPublishUrl() {
        return publishUrl != null;
    }


    public void setClientPublishUrl(URL url) {
        publishUrl = url;
    }


    public URL getClientPublishUrl() {
        return publishUrl;
    }


    public boolean hasUrlPublishing() {
        return !hasFilePublishing();
    }


    public boolean hasFilePublishing() {
        return hasClientPublishUrl() && publishUrl.getProtocol().equals("file");
    }


    /**
     * Select one of this webservice's endpoints to use for converting
     * relative imports.
     */
    public WebServiceEndpoint pickEndpointForRelativeImports() {
        WebServiceEndpoint pick = null;

        // First secure endpoint takes precedence.
        for (WebServiceEndpoint wse : endpoints.values()) {
            if (wse.isSecure()) {
                pick = wse;
                break;
            }
            pick = wse;
        }
        return pick;
    }


    /**
     * Returns a formatted String of the attributes of this object.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append( "\n wsdl file : ").append( wsdlFileUri);
        toStringBuffer.append( "\n mapping file ").append(mappingFileUri);
        toStringBuffer.append( "\n publish url ").append(publishUrl);
        toStringBuffer.append( "\n final wsdl ").append(wsdlFileUrl);
        toStringBuffer.append( "\n endpoints ").append(endpoints);
    }

}
