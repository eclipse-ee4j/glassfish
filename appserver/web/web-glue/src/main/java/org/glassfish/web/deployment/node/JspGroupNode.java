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

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.LocalizedInfoNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;

import org.glassfish.web.deployment.descriptor.JspGroupDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling jsp-group xml tag
 */
public class JspGroupNode  extends DeploymentDescriptorNode<JspGroupDescriptor> {
    private JspGroupDescriptor descriptor;

    public JspGroupNode() {
        super();
        registerElementHandler(new XMLElement(TagNames.DISPLAY_NAME), LocalizedInfoNode.class);
    }


    /**
     * all sub-implementation of this class can use a dispatch table to map
     * xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a
     *         value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(WebTagNames.URL_PATTERN, "addUrlPattern");
        table.put(TagNames.DISPLAY_NAME, "setDisplayName");
        table.put(WebTagNames.EL_IGNORED, "setElIgnored");
        table.put(WebTagNames.PAGE_ENCODING, "setPageEncoding");
        table.put(WebTagNames.SCRIPTING_INVALID, "setScriptingInvalid");
        table.put(WebTagNames.INCLUDE_PRELUDE, "addIncludePrelude");
        table.put(WebTagNames.INCLUDE_CODA, "addIncludeCoda");
        table.put(WebTagNames.IS_XML, "setIsXml");
        table.put(WebTagNames.DEFERRED_SYNTAX_ALLOWED_AS_LITERAL, "setDeferredSyntaxAllowedAsLiteral");
        table.put(WebTagNames.TRIM_DIRECTIVE_WHITESPACES, "setTrimDirectiveWhitespaces");
        table.put(WebTagNames.DEFAULT_CONTENT_TYPE, "setDefaultContentType");
        table.put(WebTagNames.BUFFER, "setBuffer");
        table.put(WebTagNames.ERROR_ON_UNDECLARED_NAMESPACE, "setErrorOnUndeclaredNamespace");
        return table;
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public JspGroupDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new JspGroupDescriptor();
        }
        return descriptor;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName node name for the root element of this xml fragment
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, JspGroupDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);

        LocalizedInfoNode localizedNode = new LocalizedInfoNode();
        writeLocalizedDescriptions(myNode, descriptor);
        localizedNode.writeLocalizedMap(myNode, TagNames.DISPLAY_NAME, descriptor.getLocalizedDisplayNames());

        // url-pattern*
        for (String urlPattern : descriptor.getUrlPatterns()) {
            appendTextChild(myNode, WebTagNames.URL_PATTERN, urlPattern);
        }
        appendTextChild(myNode, WebTagNames.EL_IGNORED, descriptor.getElIgnored());
        appendTextChild(myNode, WebTagNames.PAGE_ENCODING, descriptor.getPageEncoding());
        appendTextChild(myNode, WebTagNames.SCRIPTING_INVALID, descriptor.getScriptingInvalid());

        appendTextChild(myNode, WebTagNames.IS_XML, descriptor.getIsXml());

        // include-prelude*
        for (String includePrelude : descriptor.getIncludePreludes()) {
            appendTextChild(myNode, WebTagNames.INCLUDE_PRELUDE, includePrelude);
        }
        // include-coda*
        for (String includeCoda : descriptor.getIncludeCodas()) {
            appendTextChild(myNode, WebTagNames.INCLUDE_CODA, includeCoda);
        }
        appendTextChild(myNode, WebTagNames.DEFERRED_SYNTAX_ALLOWED_AS_LITERAL,
            descriptor.getDeferredSyntaxAllowedAsLiteral());
        appendTextChild(myNode, WebTagNames.TRIM_DIRECTIVE_WHITESPACES, descriptor.getTrimDirectiveWhitespaces());
        appendTextChild(myNode, WebTagNames.DEFAULT_CONTENT_TYPE, descriptor.getDefaultContentType());
        appendTextChild(myNode, WebTagNames.BUFFER, descriptor.getBuffer());
        appendTextChild(myNode, WebTagNames.ERROR_ON_UNDECLARED_NAMESPACE, descriptor.getErrorOnUndeclaredNamespace());

        return myNode;
    }
}
