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

package org.glassfish.webservices.node;

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.WebServiceHandler;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.glassfish.webservices.connector.LogUtils;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER_CLASS;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER_NAME;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER_PORT_NAME;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.INIT_PARAM;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.INIT_PARAM_NAME;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.INIT_PARAM_VALUE;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SOAP_HEADER;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SOAP_ROLE;

/**
 * This node does xml marshalling to/from web service handlers.
 *
 * @author Kenneth Saks
 */
public class WebServiceHandlerNode extends DisplayableComponentNode<WebServiceHandler> {

    private static final Logger LOG = LogUtils.getLogger();
    private static final XMLElement TAG = new XMLElement(HANDLER);

    private NameValuePairDescriptor initParam;

    @Override
    protected XMLElement getXMLRootTag() {
        return TAG;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(SOAP_ROLE, "addSoapRole");
        table.put(HANDLER_NAME, "setHandlerName");
        table.put(HANDLER_CLASS, "setHandlerClass");
        table.put(HANDLER_PORT_NAME, "addPortName");
        return table;
    }


    @Override
    protected WebServiceHandler createDescriptor() {
        return new WebServiceHandler();
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        String qname = element.getQName();
        WebServiceHandler handler = getDescriptor();
        if (INIT_PARAM_NAME.equals(qname)) {
            initParam = new NameValuePairDescriptor();
            initParam.setName(value);
        } else if (INIT_PARAM_VALUE.equals(qname)) {
            initParam.setValue(value);
            handler.addInitParam(initParam);
        } else if (TagNames.DESCRIPTION.equals(qname)) {
            if (initParam != null) {
                // description for the init-param
                initParam.setDescription(value);
                initParam = null;
            } else {
                // must be the description element of the handler itself.
                super.setElementValue(element, value);
            }
        } else if (SOAP_HEADER.equals(qname)) {
            String prefix = getPrefixFromQName(value);
            String localPart = getLocalPartFromQName(value);
            String namespaceUri = resolvePrefix(element, prefix);

            if (namespaceUri == null) {
                LOG.log(Level.SEVERE, LogUtils.INVALID_DESC_MAPPING_FAILURE,
                    new Object[] {prefix, handler.getHandlerName()});
            } else {
                QName soapHeader = new QName(namespaceUri, localPart);
                handler.addSoapHeader(soapHeader);
            }
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, WebServiceHandler handler) {
        Node wshNode = super.writeDescriptor(parent, nodeName, handler);

        writeDisplayableComponentInfo(wshNode, handler);
        appendTextChild(wshNode, HANDLER_NAME, handler.getHandlerName());

        appendTextChild(wshNode, HANDLER_CLASS, handler.getHandlerClass());

        for (NameValuePairDescriptor next : handler.getInitParams()) {
            Node initParamNode = appendChild(wshNode, INIT_PARAM);
            appendTextChild(initParamNode, INIT_PARAM_NAME, next.getName());
            appendTextChild(initParamNode, INIT_PARAM_VALUE, next.getValue());
        }

        for (QName next : handler.getSoapHeaders()) {
            // Append soap header QName. NOTE : descriptor does not contain
            // a prefix so always generate one.
            appendQNameChild(SOAP_HEADER, wshNode, next.getNamespaceURI(), next.getLocalPart(), null);
        }

        for (String next : handler.getSoapRoles()) {
            appendTextChild(wshNode, SOAP_ROLE, next);
        }

        for (String next : handler.getPortNames()) {
            appendTextChild(wshNode, HANDLER_PORT_NAME, next);
        }

        return wshNode;
    }


    public void writeWebServiceHandlers(Node parent, List<WebServiceHandler> handlerChain) {
        for (WebServiceHandler next : handlerChain) {
            writeDescriptor(parent, HANDLER, next);
        }
    }
}
