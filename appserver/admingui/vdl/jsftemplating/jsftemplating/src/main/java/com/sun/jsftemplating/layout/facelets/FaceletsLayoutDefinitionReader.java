/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.facelets;

import com.sun.jsftemplating.layout.LayoutDefinitionException;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.SyntaxException;
import com.sun.jsftemplating.layout.descriptors.ComponentType;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutComposition;
import com.sun.jsftemplating.layout.descriptors.LayoutDefine;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.layout.descriptors.LayoutFacet;
import com.sun.jsftemplating.layout.descriptors.LayoutForEach;
import com.sun.jsftemplating.layout.descriptors.LayoutIf;
import com.sun.jsftemplating.layout.descriptors.LayoutInsert;
import com.sun.jsftemplating.layout.descriptors.LayoutStaticText;
import com.sun.jsftemplating.layout.template.BaseProcessingContext;
import com.sun.jsftemplating.layout.template.EventParserCommand;
import com.sun.jsftemplating.layout.template.ProcessingContextEnvironment;
import com.sun.jsftemplating.layout.template.TemplateParser;
import com.sun.jsftemplating.layout.template.TemplateReader;
import com.sun.jsftemplating.util.IncludeInputStream;
import com.sun.jsftemplating.util.LayoutElementUtil;
import com.sun.jsftemplating.util.Util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jason Lee
 *
 */
public class FaceletsLayoutDefinitionReader {
    private URL url;
    private String key;
    private Document document;
    private int _idNumber;

    public FaceletsLayoutDefinitionReader(String key, URL url) {
        _idNumber = LayoutElementUtil.getStartingIdNumber(null, key);
        InputStream is = null;
        BufferedInputStream bs = null;
        try {
            this.key = key;
            this.url = url;

            DocumentBuilder builder = DbFactory.getInstance();
            builder.setErrorHandler(new ParsingErrorHandler());
            is = this.url.openStream();
            bs = new BufferedInputStream(is);
            document = builder.parse(new IncludeInputStream(bs));
        } catch (Exception e) {
            throw new LayoutDefinitionException(e);
        } finally {
            Util.closeStream(bs);
            Util.closeStream(is);
        }
    }

    public LayoutDefinition read() throws IOException {
        LayoutDefinition layoutDefinition = new LayoutDefinition(key);
        NodeList nodeList = document.getChildNodes();
        boolean abortProcessing = false;
        DocumentType docType = document.getDoctype();
        if (docType != null) {
            LayoutStaticText stDocType = new LayoutStaticText(layoutDefinition, "",
                    "<!DOCTYPE " + docType.getName() + " PUBLIC \"" + docType.getPublicId() + "\" \"" + docType.getSystemId() + "\">");
            layoutDefinition.addChildLayoutElement(stDocType);
        }
        for (int i = 0; i < nodeList.getLength() && !abortProcessing; i++) {
            abortProcessing = process(layoutDefinition, nodeList.item(i), false);
        }
        return layoutDefinition;
    }

    public boolean process(LayoutElement parent, Node node, boolean nested) throws IOException {
        boolean abortProcessing = false;
        LayoutElement element = null;
        LayoutElement newParent = parent;
        boolean endElement = false;

        String value = node.getNodeValue();
//    TODO:  find out what "name" should be in the ctors
        switch (node.getNodeType()) {
        case Node.TEXT_NODE:
            if (!value.trim().equals("")) {
                element = new LayoutStaticText(parent, LayoutElementUtil.getGeneratedId(node.getNodeName(), getNextIdNumber()), value);
            }
            break;
        case Node.ELEMENT_NODE:
            element = createComponent(parent, node, nested);
            if (element instanceof LayoutStaticText) {
                // We have a element node that needs to be static text
                endElement = true;
            } else if ((element instanceof LayoutForEach) || (element instanceof LayoutIf)) {
                newParent = element;
            } else if (element instanceof LayoutComponent) {
                nested = true;
                newParent = element;
            } else if (element instanceof LayoutComposition) {
                abortProcessing = ((LayoutComposition) element).isTrimming();
                newParent = element;
            } else if (element instanceof LayoutDefine) {
                newParent = element;
            } else if (element instanceof LayoutFacet) {
                newParent = element;
            } else if (element instanceof LayoutInsert) {
                newParent = element;
            }
//        FIXME: Jason, this code may need to be refactored.  I think almost
//        FIXME: everything should have newParent = element.  The problem comes when
//        FIXME: you are turning <html> and </html> into 2 separate staticText
//        FIXME: components.  This should be a single component, then it could contain
//        FIXME: children also.  You may want a to create a component like Woodstock's
//        FIXME: "markup" component to do this.
            break;
        default:
            // just because... :P
        }

        if (element != null) {
            parent.addChildLayoutElement(element);

            NodeList nodeList = node.getChildNodes();
            boolean abortChildProcessing = false;
            for (int i = 0; i < nodeList.getLength() && !abortChildProcessing; i++) {
                abortChildProcessing = process(newParent, nodeList.item(i), nested);
            }
            if (abortChildProcessing) {
                abortProcessing = abortChildProcessing;
            } else {

                if (endElement) {
                    String nodeName = node.getNodeName();
                    element = new LayoutStaticText(parent, LayoutElementUtil.getGeneratedId(nodeName, getNextIdNumber()), "</" + nodeName + ">");
                    parent.addChildLayoutElement(element);
                }
            }
        }

        return abortProcessing;
    }

    private LayoutComposition processComposition(LayoutElement parent, String attrName, NamedNodeMap attrs, String id, boolean trimming) {
        LayoutComposition lc = new LayoutComposition(parent, id);
        lc.setTrimming(trimming);
        if (trimming) {
            parent = parent.getLayoutDefinition(); // parent to the LayoutDefinition
            parent.getChildLayoutElements().clear(); // a ui:composition clears everything outside of it
        }
        Node fileNameNode = attrs.getNamedItem(attrName);
        String fileName = fileNameNode != null ? fileNameNode.getNodeValue() : null;
        lc.setTemplate(fileName);

        return lc;
    }

    private LayoutComponent processComponent(LayoutElement parent, Node node, NamedNodeMap attrs, String id, boolean trimming) {
        if (trimming) {
            parent = parent.getLayoutDefinition(); // parent to the LayoutDefinition
            parent.getChildLayoutElements().clear(); // a ui:composition clears everything outside of it
        }
        LayoutComponent lc = new LayoutComponent(parent, id, LayoutDefinitionManager.getGlobalComponentType(null, "event"));
        parent.addChildLayoutElement(lc);
        LayoutComposition comp = processComposition(lc, "template", attrs, id + "_lc", trimming);

        NodeList nodeList = node.getChildNodes();
        boolean abortChildProcessing = false;
        for (int i = 0; i < nodeList.getLength() && !abortChildProcessing; i++) {
            try {
                abortChildProcessing = process(comp, nodeList.item(i), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        lc.addChildLayoutElement(comp);

        return lc;
    }

    /*
     * This code is not used and does not appear to be correct, it should use FacesContext.getApplication() private
     * Application getApplication() { ApplicationFactory appFactory = (ApplicationFactory) FactoryFinder
     * .getFactory(FactoryFinder.APPLICATION_FACTORY); return appFactory.getApplication(); }
     *
     * private Object getElValue(String el) { FacesContext context = FacesContext.getCurrentInstance(); return
     * getApplication() .evaluateExpressionGet(context, el, Object.class); }
     */

    private LayoutElement createComponent(LayoutElement parent, Node node, boolean nested) {
        LayoutElement element = null;
        String nodeName = node.getNodeName();
        NamedNodeMap attrs = node.getAttributes();
        Node nameNode = attrs.getNamedItem("id");
        String id = nameNode != null ? nameNode.getNodeValue() : LayoutElementUtil.getGeneratedId(nodeName, getNextIdNumber());

        if ("ui:composition".equals(nodeName)) {
            element = processComposition(parent, "template", attrs, id, true);
        } else if ("ui:decorate".equals(nodeName)) {
            element = processComposition(parent, "template", attrs, id, false);
        } else if ("ui:define".equals(nodeName)) {
            String name = attrs.getNamedItem("name").getNodeValue();
            element = new LayoutDefine(parent, name);
        } else if ("ui:insert".equals(nodeName)) {
            LayoutInsert li = new LayoutInsert(parent, id);
            Node nameAttr = attrs.getNamedItem("name");
            String name = nameAttr != null ? nameAttr.getNodeValue() : null;
            li.setName(name);
            element = li;
            // Let these be handled by the else below, and let's see what happens :)
        } else if ("ui:component".equals(nodeName)) {
            element = processComponent(parent, node, attrs, id, true);
        } else if ("ui:fragment".equals(nodeName)) {
            element = processComponent(parent, node, attrs, id, false);
            /*
             * Node bindingAttr = attrs.getNamedItem("binding"); if (bindingAttr == null) { throw new
             * LayoutDefinitionException("ui:fragment requires a binding attribute"); } String bindingEl =
             * bindingAttr.getNodeValue(); Object obj = getElValue(bindingEl); if (!(obj instanceof UIComponent)) { throw new
             * LayoutDefinitionException("Binding EL must return a UIComponent"); } UIComponent comp = (UIComponent) obj; String
             * family = comp.getFamily(); // TODO: is the correct?
             * System.out.println(LayoutDefinitionManager.getGlobalComponentTypes(null)); ComponentType componentType =
             * LayoutDefinitionManager.getGlobalComponentType(null, family); LayoutComponent lc = new LayoutComponent(parent, id,
             * componentType); addAttributesToComponent(lc, node); lc.setFacetChild(false); lc.setNested(nested); element = lc;
             */
        } else if ("ui:debug".equals(nodeName)) {
        } else if ("ui:include".equals(nodeName)) {
            element = processComposition(parent, "src", attrs, id, false);
        } else if ("ui:param".equals(nodeName)) {
            // Handle "param"
            Node nameAttNode = attrs.getNamedItem("name");
            if (nameAttNode == null) {
                throw new SyntaxException("The 'name' attribute is required on 'param'.");
            }
            Node valueNode = attrs.getNamedItem("value");
            if (valueNode == null) {
                throw new SyntaxException("The 'value' attribute is required on 'param'.");
            }

            // For now only handle cases where the parent is a LayoutComposition
            if (!(parent instanceof LayoutComposition)) {
                throw new SyntaxException("<" + nodeName + " name='" + nameAttNode.getNodeValue() + "' value='" + valueNode.getNodeValue()
                        + "'> must be child of a 'composition' element!");
            }
            // Set the name=value on the parent LayoutComposition
            ((LayoutComposition) parent).setParameter(nameAttNode.getNodeValue(), valueNode.getNodeValue());
        } else if ("ui:remove".equals(nodeName) || "ui:repeat".equals(nodeName)) {
            // Let the element remain null
        } else if ("ui:event".equals(nodeName)) {
            // per Ken, we need to append "/>" to allow the handler parser code
            // to end correctly
            String body = node.getTextContent();
            body = body == null ? "/>" : body.trim() + "/>";
            Node type = node.getAttributes().getNamedItem("type");
            if (type == null) {
                // Ensure type != null
                throw new SyntaxException("The 'type' attribute is required on 'ui:event'!");
            }
            String eventName = type.getNodeValue();
            InputStream is = new ByteArrayInputStream(body.getBytes());
            EventParserCommand command = new EventParserCommand();
            try {
                TemplateParser parser = new TemplateParser(is);
                parser.open(); // Needed to initialize things.
                // Setup the reader...
                TemplateReader reader = new TemplateReader("foo", parser); // TODO: get a real ID
                reader.pushTag("event"); // The tag will be popped at the end
                // Read the handlers...
                command.process(new BaseProcessingContext(), new ProcessingContextEnvironment(reader, parent, true), eventName);
                // Clean up
                parser.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        } else if ("ui:if".equals(nodeName)) {
            // Handle "if" conditions
            String condition = attrs.getNamedItem("condition").getNodeValue();
            element = new LayoutIf(parent, condition);
        } else if ("ui:foreach".equals(nodeName)) {
            // Handle "foreach" conditions
            Node valueNode = attrs.getNamedItem("value");
            if (valueNode == null) {
                throw new SyntaxException("The 'value' property is required on 'foreach'.");
            }
            Node varNode = attrs.getNamedItem("var");
            if (varNode == null) {
                throw new SyntaxException("The 'var' property is required on 'foreach'.");
            }

            element = new LayoutForEach(parent, valueNode.getNodeValue(), varNode.getNodeValue());
        } else if ("f:facet".equals(nodeName)) {
            // FIXME: Need to take NameSpace into account
            nameNode = attrs.getNamedItem("name");
            if (nameNode == null) {
                throw new IllegalArgumentException(
                        "You must provide a name " + "attribute for all facets!  Parent component is: '" + parent.getUnevaluatedId() + "'.");
            }
            LayoutFacet facetElt = new LayoutFacet(parent, nameNode.getNodeValue());

            // Determine if this is a facet place holder (i.e. we're defining
            // a renderer w/ a facet), or if it is a facet value to set on a
            // containing component.
            boolean isRendered = !LayoutElementUtil.isNestedLayoutComponent(facetElt);
            facetElt.setRendered(isRendered);
            element = facetElt;
        } else {
            LayoutComponent lc = null;
            ComponentType componentType = null;
            String nsURI = node.getNamespaceURI();
            if (nsURI != null) {
                // Do lookup using namespace...
                componentType = LayoutDefinitionManager.getGlobalComponentType(null, nsURI + ':' + node.getLocalName());
            }
            if (componentType == null) {
                // Try w/o using namespace
                componentType = LayoutDefinitionManager.getGlobalComponentType(null, nodeName);
            }
            if (componentType == null) {
                String value = node.getNodeValue();
                if (value == null) {
                    value = "";
                }
//        FIXME: This needs to account for beginning and ending tags....
                lc = new LayoutStaticText(parent, id, "<" + nodeName + buildAttributeList(node) + ">");
            } else {
                lc = new LayoutComponent(parent, id, componentType);
                addAttributesToComponent(lc, node);
            }
            lc.setNested(nested);
//        FIXME: Because of the way pages are composed in facelets, the parent
//        FIXME: LayoutComponent may not exist in this LD.  In that case it is not
//        FIXME: a "facet child", but it appears to be according to the following
//        FIXME: method.  We need a better way to mark children as facets or real
//        FIXME: children.  This may even require diverging the LD into 1 for
//        FIXME: components and 1 for pages. :(
            // LayoutElementUtil.checkForFacetChild(parent, lc);
            // lc.setFacetChild(false); This is done by checkForFacetChild(...)
            element = lc;
        }

        return element;
    }

    private void addAttributesToComponent(LayoutComponent lc, Node node) {
        NamedNodeMap map = node.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Node attr = map.item(i);
            lc.addOption(attr.getNodeName(), attr.getNodeValue());
        }
    }

    private String buildAttributeList(Node node) {
        StringBuilder attrs = new StringBuilder();

        NamedNodeMap map = node.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Node attr = map.item(i);
            attrs.append(" ").append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append("\"");
        }

        return attrs.toString();
    }

    /**
     * <p>
     * This method returns the next ID number. Calling this method will increment the id number.
     * </p>
     */
    public int getNextIdNumber() {
        // Make sure we increment the global counter, if appropriate
        LayoutElementUtil.incHighestId(_idNumber);
        return _idNumber++;
    }
}
