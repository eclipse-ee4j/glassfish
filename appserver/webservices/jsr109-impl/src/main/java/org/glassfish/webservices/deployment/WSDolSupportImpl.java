/*
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

package org.glassfish.webservices.deployment;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WSDolSupport;

import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.http.HTTPBinding;
import jakarta.xml.ws.soap.SOAPBinding;

import javax.xml.namespace.QName;

import org.jvnet.hk2.annotations.Service;

/**
 *Implementation of jaxws dependent services for the DOL
 *
 * @author Jerome Dochez
 */
@Service
public class WSDolSupportImpl implements WSDolSupport {

    private static final String SOAP11_TOKEN = "##SOAP11_HTTP";
    private static final String SOAP12_TOKEN = "##SOAP12_HTTP";
    private static final String SOAP11_MTOM_TOKEN = "##SOAP11_HTTP_MTOM";
    private static final String SOAP12_MTOM_TOKEN = "##SOAP12_HTTP_MTOM";
    private static final String XML_TOKEN = "##XML_HTTP";

    @Override
    public String getProtocolBinding(String value) {
        if (value == null) {
            return SOAPBinding.SOAP11HTTP_BINDING;
        } else if (SOAP11_TOKEN.equals(value)) {
            return SOAPBinding.SOAP11HTTP_BINDING;
        } else if (SOAP11_MTOM_TOKEN.equals(value)) {
            return SOAPBinding.SOAP11HTTP_MTOM_BINDING;
        } else if (SOAP12_TOKEN.equals(value)) {
            return SOAPBinding.SOAP12HTTP_BINDING;
        } else if (SOAP12_MTOM_TOKEN.equals(value)) {
            return SOAPBinding.SOAP12HTTP_MTOM_BINDING;
        } else if (XML_TOKEN.equals(value)) {
            return HTTPBinding.HTTP_BINDING;
        } else {
            return value;
        }
    }


    @Override
    public String getSoapAddressPrefix(String protocolBinding) {
        if ((SOAPBinding.SOAP12HTTP_BINDING.equals(protocolBinding))
            || (SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(protocolBinding)) || (SOAP12_TOKEN.equals(protocolBinding))
            || (SOAP12_MTOM_TOKEN.equals(protocolBinding))) {
            return "soap12";
        }
        // anything else should be soap11
        return "soap";
    }


    @Override
    public void setServiceRef(Class annotatedClass, ServiceReferenceDescriptor ref) {
        WebServiceClient wsc = (WebServiceClient) annotatedClass.getAnnotation(WebServiceClient.class);
        if (wsc != null) {
            ref.setWsdlFileUri(wsc.wsdlLocation());
            // we set the service QName too from the @WebServiceClient annotation
            ref.setServiceName(new QName(wsc.targetNamespace(), wsc.name()));
        }
    }


    @Override
    public Class<?> getType(String className) throws ClassNotFoundException {
        return this.getClass().getClassLoader().loadClass(className);
    }
}
