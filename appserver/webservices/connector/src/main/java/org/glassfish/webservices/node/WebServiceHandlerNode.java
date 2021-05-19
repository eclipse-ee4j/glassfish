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

package org.glassfish.webservices.node;

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.WebServiceHandler;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.webservices.connector.LogUtils;

/**
 * This node does xml marshalling to/from web service handlers.
 *
 * @author Kenneth Saks
 */
public class WebServiceHandlerNode extends DisplayableComponentNode {

    private static final Logger logger = LogUtils.getLogger();

    private final static XMLElement tag =
        new XMLElement(WebServicesTagNames.HANDLER);

    private NameValuePairDescriptor initParam = null;

    public WebServiceHandlerNode() {
        super();
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.SOAP_ROLE, "addSoapRole");
        table.put(WebServicesTagNames.HANDLER_NAME, "setHandlerName");
        table.put(WebServicesTagNames.HANDLER_CLASS, "setHandlerClass");
        table.put(WebServicesTagNames.HANDLER_PORT_NAME, "addPortName");
        return table;
    }

    protected WebServiceHandler createDescriptor() {
       return new WebServiceHandler();
   }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        String qname = element.getQName();
        WebServiceHandler handler = (WebServiceHandler) getDescriptor();
        if (WebServicesTagNames.INIT_PARAM_NAME.equals(qname)) {
            initParam = new NameValuePairDescriptor();
            initParam.setName(value);
        } else if (WebServicesTagNames.INIT_PARAM_VALUE.equals(qname)) {
            initParam.setValue(value);
            handler.addInitParam(initParam);
        } else if (TagNames.DESCRIPTION.equals(qname)) {
            if( initParam != null ) {
                // description for the init-param
                initParam.setDescription(value);
                initParam = null;
            } else {
                // must be the description element of the handler itself.
                super.setElementValue(element, value);
            }
        } else if (WebServicesTagNames.SOAP_HEADER.equals(qname) ) {
            String prefix = getPrefixFromQName(value);
            String localPart = getLocalPartFromQName(value);
            String namespaceUri = resolvePrefix(element, prefix);

            if( namespaceUri == null) {
                logger.log(Level.SEVERE, LogUtils.INVALID_DESC_MAPPING_FAILURE,
                    new Object[] {prefix , handler.getHandlerName()});
            } else {
                QName soapHeader = new QName(namespaceUri, localPart);
                handler.addSoapHeader(soapHeader);
            }

        } else super.setElementValue(element, value);
    }

    /**
     * write the method descriptor class to a query-method DOM tree and
     * return it
     *
     * @param parent node in the DOM tree
     * @param nodeName name for the root element of this xml fragment
     * @param handler the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                WebServiceHandler handler) {
        Node wshNode = super.writeDescriptor(parent, nodeName, handler);

        writeDisplayableComponentInfo(wshNode, handler);
        appendTextChild(wshNode,
                        WebServicesTagNames.HANDLER_NAME,
                        handler.getHandlerName());

        appendTextChild(wshNode,
                        WebServicesTagNames.HANDLER_CLASS,
                        handler.getHandlerClass());

        for(Iterator iter = handler.getInitParams().iterator();iter.hasNext();){
            NameValuePairDescriptor next = (NameValuePairDescriptor)iter.next();
            Node initParamNode =
                appendChild(wshNode, WebServicesTagNames.INIT_PARAM);
            appendTextChild(initParamNode, WebServicesTagNames.INIT_PARAM_NAME,
                            next.getName());
            appendTextChild(initParamNode, WebServicesTagNames.INIT_PARAM_VALUE,
                            next.getValue());
        }

        for(Iterator iter = handler.getSoapHeaders().iterator();
            iter.hasNext();) {
            QName next = (QName) iter.next();
            // Append soap header QName.  NOTE : descriptor does not contain
            // a prefix so always generate one.
            appendQNameChild(WebServicesTagNames.SOAP_HEADER, wshNode,
                             next.getNamespaceURI(), next.getLocalPart(), null);
        }

        for(Iterator iter = handler.getSoapRoles().iterator(); iter.hasNext();){
            String next = (String) iter.next();
            appendTextChild(wshNode, WebServicesTagNames.SOAP_ROLE, next);
        }

        for(Iterator iter = handler.getPortNames().iterator(); iter.hasNext();){
            String next = (String) iter.next();
            appendTextChild(wshNode, WebServicesTagNames.HANDLER_PORT_NAME,
                            next);
        }

        return wshNode;
    }

    public void writeWebServiceHandlers(Node parent, List handlerChain) {
        for(Iterator iter = handlerChain.iterator(); iter.hasNext();) {
            WebServiceHandler next = (WebServiceHandler) iter.next();
            writeDescriptor(parent, WebServicesTagNames.HANDLER, next);
        }
    }

}
