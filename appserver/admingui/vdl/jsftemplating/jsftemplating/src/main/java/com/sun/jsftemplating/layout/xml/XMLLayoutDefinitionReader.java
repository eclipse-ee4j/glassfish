/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.xml;

import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.SyntaxException;
import com.sun.jsftemplating.layout.descriptors.ComponentType;
import com.sun.jsftemplating.layout.descriptors.LayoutAttribute;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.layout.descriptors.LayoutFacet;
import com.sun.jsftemplating.layout.descriptors.LayoutForEach;
import com.sun.jsftemplating.layout.descriptors.LayoutIf;
import com.sun.jsftemplating.layout.descriptors.LayoutMarkup;
import com.sun.jsftemplating.layout.descriptors.LayoutStaticText;
import com.sun.jsftemplating.layout.descriptors.LayoutWhile;
import com.sun.jsftemplating.layout.descriptors.Resource;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerDefinition;
import com.sun.jsftemplating.layout.descriptors.handler.IODescriptor;
import com.sun.jsftemplating.util.IncludeInputStream;
import com.sun.jsftemplating.util.LayoutElementUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>
 * This class is responsible for doing the actual parsing of an XML document following the <code>layout.dtd</code>. It
 * produces a {@link LayoutElement} tree with a {@link LayoutDefinition} object at the root of the tree.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class XMLLayoutDefinitionReader {

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param url A URL pointing to the {@link LayoutDefinition}
     * @param entityResolver EntityResolver to use, may be (null)
     * @param errorHandler ErrorHandler to use, may be (null)
     * @param baseURI The base URI passed to DocumentBuilder.parse()
     */
    public XMLLayoutDefinitionReader(URL url, EntityResolver entityResolver, ErrorHandler errorHandler, String baseURI) {
        _url = url;
        _entityResolver = entityResolver;
        _errorHandler = errorHandler;
        _baseURI = baseURI;
    }

    /**
     * <p>
     * Accessor for the URL.
     * </p>
     */
    public URL getURL() {
        return _url;
    }

    /**
     * <p>
     * Accessor for the entityResolver.
     * </p>
     */
    public EntityResolver getEntityResolver() {
        return _entityResolver;
    }

    /**
     * <p>
     * Accessor for the ErrorHandler.
     * </p>
     */
    public ErrorHandler getErrorHandler() {
        return _errorHandler;
    }

    /**
     * <p>
     * Accessor for the base URI.
     * </p>
     */
    public String getBaseURI() {
        return _baseURI;
    }

    /**
     * <p>
     * The read method opens the given URL and parses the XML document that it points to. It then walks the DOM and
     * populates a {@link LayoutDefinition} structure, which is returned.
     * </p>
     *
     * @return The {@link LayoutDefinition}
     *
     * @throws IOException
     */
    public LayoutDefinition read() throws IOException {
        // Open the URL
        InputStream inputStream = new IncludeInputStream(new BufferedInputStream(getURL().openStream()));
        Document doc = null;

        try {
            // Get a DocumentBuilderFactory and set it up
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            String FEATURE = null;
            try {
                FEATURE = "http://xml.org/sax/features/external-parameter-entities";
                dbf.setFeature(FEATURE, false);

                FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
                dbf.setFeature(FEATURE, false);

                FEATURE = "http://xml.org/sax/features/external-general-entities";
                dbf.setFeature(FEATURE, false);

                dbf.setXIncludeAware(false);
                dbf.setExpandEntityReferences(false);

                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            } catch (ParserConfigurationException e) {
                throw new IllegalStateException("The feature '"
                + FEATURE + "' is not supported by your XML processor.", e);
            }
            dbf.setNamespaceAware(true);
            dbf.setValidating(true);
            dbf.setIgnoringComments(true);
            dbf.setIgnoringElementContentWhitespace(false);
            dbf.setCoalescing(false);
            // The opposite of creating entity ref nodes is expanding inline
            dbf.setExpandEntityReferences(true);

            // Get a DocumentBuilder...
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                throw new RuntimeException(ex);
            }
            if (getEntityResolver() != null) {
                db.setEntityResolver(getEntityResolver());
            }
            if (getErrorHandler() != null) {
                db.setErrorHandler(getErrorHandler());
            }

            // Parse the XML file
            try {
                doc = db.parse(inputStream, getBaseURI());
            } catch (IOException ex) {
                throw new SyntaxException("Unable to parse XML file!", ex);
            } catch (SAXException ex) {
                throw new SyntaxException(ex);
            }
        } finally {
            try {
                inputStream.close();
            } catch (Exception ex) {
                // Ignore...
            }
        }

        // Populate the LayoutDefinition from the Document
        return createLayoutDefinition(doc);
    }

    /**
     * <p>
     * This method is responsible for extracting all the information out of the supplied document and filling the
     * {@link LayoutDefinition} structure.
     * </p>
     *
     * @param doc The <code>Document</code> object to read.
     *
     * @return The new {@link LayoutDefinition} Object.
     */
    private LayoutDefinition createLayoutDefinition(Document doc) {
        // Get the document element (LAYOUT_DEFINITION_ELEMENT)
        Node node = doc.getDocumentElement();
        if (!node.getNodeName().equalsIgnoreCase(LAYOUT_DEFINITION_ELEMENT)) {
            throw new RuntimeException("Document Element must be '" + LAYOUT_DEFINITION_ELEMENT + "'");
        }

        // Create a new LayoutDefinition (the id is not propagated here)
        LayoutDefinition ld = new LayoutDefinition("");

        // Do "resources" first, they are defined at the top of the document
        List<Node> childElements = getChildElements(node, RESOURCES_ELEMENT);
        Iterator<Node> it = childElements.iterator();
        if (it.hasNext()) {
            // Found the RESOURCES_ELEMENT, there is at most 1
            addResources(ld, it.next());
        }

        // Do "types", they need to be defined before parsing the layout
        childElements = getChildElements(node, TYPES_ELEMENT);
        it = childElements.iterator();
        if (it.hasNext()) {
            // Found the TYPES_ELEMENT, there is at most 1
            addTypes(ld, it.next());
        }

        // Do "handlers" next, they need to be defined before parsing the layout
        childElements = getChildElements(node, HANDLERS_ELEMENT);
        it = childElements.iterator();
        if (it.hasNext()) {
            // Found the HANDLERS_ELEMENT, there is at most 1
            cacheHandlerDefs(it.next());
        }

        // Look to see if there is an EVENT_ELEMENT defined
        childElements = getChildElements(node, EVENT_ELEMENT);
        it = childElements.iterator();
        if (it.hasNext()) {
            // Found the EVENT_ELEMENT, there is at most 1
            // Get the event type
            Node eventNode = it.next();
            String type = getAttributes(eventNode).get(TYPE_ATTRIBUTE);

            // Set the Handlers for the given event type (name)
            List<Handler> handlers = ld.getHandlers(type);
            ld.setHandlers(type, getHandlers(eventNode, handlers));
        }

        // Next look for "layout", there is exactly 1
        childElements = getChildElements(node, LAYOUT_ELEMENT);
        it = childElements.iterator();
        if (it.hasNext()) {
            // Found the LAYOUT_ELEMENT, there is only 1
            addChildLayoutElements(ld, it.next());
        } else {
            throw new RuntimeException("A '" + LAYOUT_ELEMENT + "' element is required in the XML document!");
        }

        // Return the LayoutDefinition
        return ld;
    }

    /**
     * <p>
     * This method iterates throught the child RESOURCE_ELEMENT nodes and adds new resource objects to the
     * {@link LayoutDefinition}.
     * </p>
     *
     * @param ld The {@link LayoutDefinition}
     * @param node Parent <code>Node</code> containing the {@link #RESOURCE_ELEMENT} nodes.
     */
    private void addResources(LayoutDefinition ld, Node node) {
        // Get the child nodes
        Iterator<Node> it = getChildElements(node, RESOURCE_ELEMENT).iterator();

        // Walk children (we only care about RESOURCE_ELEMENT)
        while (it.hasNext()) {
            // Found a RESOURCE_ELEMENT
            ld.addResource(createResource(it.next()));
        }
    }

    /**
     * <p>
     * This method takes the given Resource Element node and reads the {@link #ID_ATTRIBUTE}, {@link #EXTRA_INFO_ATTRIBUTE}
     * and {@link #FACTORY_CLASS_ATTRIBUTE} attributes. It then instantiates a new {@link Resource} with the values of these
     * two attributes.
     * </p>
     *
     * @param node The {@link Resource} node to extract information from when creating the {@link Resource}.
     */
    private Resource createResource(Node node) {
        // Pull off the attributes
        Map<String, String> attributes = getAttributes(node);
        String id = attributes.get(ID_ATTRIBUTE);
        String extraInfo = attributes.get(EXTRA_INFO_ATTRIBUTE);
        String factoryClass = attributes.get(FACTORY_CLASS_ATTRIBUTE);

        // Make sure required values are present
        if (factoryClass == null || id == null || extraInfo == null || factoryClass.trim().equals("") || id.trim().equals("") || extraInfo.trim().equals("")) {
            throw new RuntimeException("'" + ID_ATTRIBUTE + "', '" + EXTRA_INFO_ATTRIBUTE + "', and '" + FACTORY_CLASS_ATTRIBUTE
                    + "' are required attributes of '" + RESOURCE_ELEMENT + "' Element!");
        }

        // Create the new Resource
        return new Resource(id, extraInfo, factoryClass);
    }

    /**
     * <p>
     * This method iterates through the child {@link #COMPONENT_TYPE_ELEMENT} nodes and adds new {@link ComponentTypes} to
     * the {@link LayoutDefinition}.
     * </p>
     *
     * @param ld The {@link LayoutDefinition}
     * @param node Parent <code>Node</code> containing the {@link #COMPONENT_TYPE_ELEMENT} nodes
     */
    private void addTypes(LayoutDefinition ld, Node node) {
        // Get the child nodes
        Iterator<Node> it = getChildElements(node, COMPONENT_TYPE_ELEMENT).iterator();

        // Walk the COMPONENT_TYPE_ELEMENT elements
        while (it.hasNext()) {
            ld.addComponentType(createComponentType(it.next()));
        }
    }

    /**
     * <p>
     * This method takes the given {@link ComponentType} Element node and reads the {@link #ID_ATTRIBUTE} and
     * {@link #FACTORY_CLASS_ATTRIBUTE} attributes. It then instantiates a new {@link ComponentType} with the values of
     * these two attributes.
     * </p>
     *
     * @param node The {@link ComponentType} node to extract information from when creating the {@link ComponentType}.
     */
    private ComponentType createComponentType(Node node) {
        // Pull off the attributes
        Map<String, String> attributes = getAttributes(node);
        String id = attributes.get(ID_ATTRIBUTE);
        String factoryClass = attributes.get(FACTORY_CLASS_ATTRIBUTE);

        // Make sure required values are present
        if (factoryClass == null || id == null || factoryClass.trim().equals("") || id.trim().equals("")) {
            throw new RuntimeException(
                    "Both '" + ID_ATTRIBUTE + "' and '" + FACTORY_CLASS_ATTRIBUTE + "' are required attributes of '" + COMPONENT_TYPE_ELEMENT + "' Element!");
        }

        // Create the new ComponentType
        return new ComponentType(id, factoryClass);
    }

    /**
     * <p>
     * This method iterates through the child {@link #HANDLER_DEFINITION_ELEMENT} nodes and caches them, so they may be
     * retrieved later by {@link Handlers} referring to them.
     * </p>
     *
     * @param node Parent <code>Node</code> containing {@link #HANDLER_DEFINITION_ELEMENT} nodes.
     */
    private void cacheHandlerDefs(Node node) {
        HandlerDefinition def = null;

        // Get the child nodes
        Iterator<Node> it = getChildElements(node, HANDLER_DEFINITION_ELEMENT).iterator();
        while (it.hasNext()) {
            // Found a HANDLER_DEFINITION_ELEMENT, cache it
            def = createHandlerDefinition(it.next());
            _handlerDefs.put(def.getId(), def);
        }
    }

    /**
     * <p>
     * This method takes the given {@link #HANDLER_DEFINITION_ELEMENT} node and reads the {@link #ID_ATTRIBUTE},
     * {@link #CLASS_NAME_ATTRIBUTE}, and {@link #METHOD_NAME_ATTRIBUTE} attributes. It then instantiates a new
     * {@link HandlerDefinition} object.
     * </p>
     *
     * <p>
     * Next it looks to see if the {@link HandlerDefinition} has child inputDef, outputDef, and/or nested handler elements.
     * If so it processes them.
     * </p>
     *
     * @param node The {@link #HANDLER_DEFINITION_ELEMENT} node to extract information from when creating the
     * {@link HandlerDefinition}.
     *
     * @return The newly created {@link HandlerDefinition}.
     */
    public HandlerDefinition createHandlerDefinition(Node node) {

        // Create he HandlerDefinition
        Map<String, String> attributes = getAttributes(node);
        String value = attributes.get(ID_ATTRIBUTE);
        HandlerDefinition hd = new HandlerDefinition(value);

// hd.setDescription(_description)

        // Check for a className
        value = attributes.get(CLASS_NAME_ATTRIBUTE);
        if (value != null && !value.equals("")) {
            // Found a className, now get the methodName
            String tmpStr = attributes.get(METHOD_NAME_ATTRIBUTE);
            if (tmpStr == null || tmpStr.equals("")) {
                throw new IllegalArgumentException("You must provide a '" + METHOD_NAME_ATTRIBUTE + "' attribute on the '" + HANDLER_DEFINITION_ELEMENT
                        + "' element with " + CLASS_NAME_ATTRIBUTE + " atttribute equal to '" + value + "'.");
            }
            hd.setHandlerMethod(value, tmpStr);
        }

        // Add child handlers to this HandlerDefinition. This allows a
        // HandlerDefinition to define handlers that should be invoked before
        // the method defined by this handler definition is invoked.
        List<Handler> handlers = new ArrayList(hd.getChildHandlers());
        hd.setChildHandlers(getHandlers(node, handlers));

        // Add InputDef objects to the HandlerDefinition
        addInputDefs(hd, node);

        // Add OutputDef objects to the HandlerDefinition
        addOutputDefs(hd, node);

        // Return the newly created HandlerDefinition object
        return hd;
    }

    /**
     * <p>
     * This method creates a <code>List</code> of {@link Handler}s from the provided <code>Node</code>. It will look at the
     * child <code>Element</code>s for {@link #HANDLER_ELEMENT} elements. When found, it will create a new {@link Handler}
     * object and add it to a <code>List</code> that is created internally. This <code>List</code> is returned.
     * </p>
     *
     * @param node <code>Node</code> containing {@link #HANDLER_ELEMENT} elements.
     * @param handlers <code>List</code> of existing {@link Handler}s.
     *
     * @return A <code>List</code> of {@link Handler} objects, empty <code>List</code> if no {@link Handler}s found
     */
    private List<Handler> getHandlers(Node node, List<Handler> handlers) {
        // Get the child nodes
        Iterator<Node> it = getChildElements(node, HANDLER_ELEMENT).iterator();

        // Walk children (we only care about HANDLER_ELEMENT)
        if (handlers == null) {
            handlers = new ArrayList<>();
        }
        while (it.hasNext()) {
            // Found a HANDLER_ELEMENT
            handlers.add(createHandler(it.next()));
        }

        // Return the handlers
        return handlers;
    }

    /**
     * <p>
     * This method creates a {@link Handler} from the given handler <code>Node</code>. It will add input and/or output
     * mappings specified by any child Elements named {@link #INPUT_ELEMENT} or {@link #OUTPUT_MAPPING_ELEMENT}.
     * </p>
     *
     * @param handlerNode The <code>Node</code> describing the {@link Handler} to be created.
     *
     * @return The newly created {@link Handler}.
     */
    private Handler createHandler(Node handlerNode) {
        // Pull off attributes...
        String id = getAttributes(handlerNode).get(ID_ATTRIBUTE);
        if (id == null || id.trim().equals("")) {
            throw new RuntimeException("'" + ID_ATTRIBUTE + "' attribute not found on '" + HANDLER_ELEMENT + "' Element!");
        }

        // Find the HandlerDefinition associated with this Handler
        HandlerDefinition handlerDef = getHandlerDef(id);
        if (handlerDef == null) {
            throw new IllegalArgumentException(HANDLER_ELEMENT + " elements " + ID_ATTRIBUTE + " attribute must match the " + ID_ATTRIBUTE + " attribute of a "
                    + HANDLER_DEFINITION_ELEMENT + ".  A HANDLER_ELEMENT with '" + id + "' was specified, however there is no cooresponding "
                    + HANDLER_DEFINITION_ELEMENT + " with a matching " + ID_ATTRIBUTE + " attribute.");
        }

        // Create new Handler
        Handler handler = new Handler(handlerDef);

        // Add the inputs
        Map<String, String> attributes = null;
        Node inputNode = null;
        Iterator<Node> it = getChildElements(handlerNode, INPUT_ELEMENT).iterator();
        while (it.hasNext()) {
            // Processing an INPUT_ELEMENT
            inputNode = it.next();
            attributes = getAttributes(inputNode);
            handler.setInputValue(attributes.get(NAME_ATTRIBUTE), getValueFromNode(inputNode, attributes));
        }

        // Add the OutputMapping objects
        it = getChildElements(handlerNode, OUTPUT_MAPPING_ELEMENT).iterator();
        while (it.hasNext()) {
            // Processing an OUTPUT_MAPPING_ELEMENT
            attributes = getAttributes(it.next());
            handler.setOutputMapping(attributes.get(OUTPUT_NAME_ATTRIBUTE), attributes.get(TARGET_KEY_ATTRIBUTE), attributes.get(TARGET_TYPE_ATTRIBUTE));
        }

        // Return the newly created handler
        return handler;
    }

    /**
     * <p>
     * This method first attempts to find a locally defined {@link HandlerDefinition} with the given <code>id</code>. If
     * found, it will be returned, otherwise it attempts to return a globally defined {@link HandlerDefinition} via
     * {@link LayoutDefinitionManager#getGlobalHandlerDefinition(String)}.
     * </p>
     *
     * @param id The desired {@link HandlerDefinition}'s id.
     *
     * @return The desired {@link HandlerDefinition} or <code>null</code>.
     */
    private HandlerDefinition getHandlerDef(String id) {
        // Try local...
        HandlerDefinition def = _handlerDefs.get(id);
        if (def == null) {
            // Try global...
            def = LayoutDefinitionManager.getGlobalHandlerDefinition(id);
        }
        return def;
    }

    /**
     * <p>
     * This method adds InputDefs to the given {@link HandlerDefinition} object. It will look at the child elements for
     * those named {@link #INPUT_DEF_ELEMENT}. It will create an {@link #IODescriptor} for each and add it to the
     * {@link HandlerDefinition}.
     * </p>
     *
     * @param hd {@link HandlerDefinition}.
     * @param hdNode {@link HandlerDefinition} <code>Node</code>, its children will be searched
     */
    private void addInputDefs(HandlerDefinition hd, Node hdNode) {
        // Get the child nodes
        Iterator<Node> it = getChildElements(hdNode, INPUT_DEF_ELEMENT).iterator();

        // Walk children (we only care about INPUT_DEF_ELEMENT)
        while (it.hasNext()) {
            // Found a INPUT_DEF_ELEMENT
            hd.addInputDef(createIODescriptor(it.next()));
        }
    }

    /**
     * <p>
     * This method adds OutputDefs to the given {@link HandlerDefinition} object. It will look at the child elements for
     * those named {@link #OUTPUT_DEF_ELEMENT}. It will create an {@link #IODescriptor} for each and add it to the
     * {@link HandlerDefinition}.
     * </p>
     *
     * @param hd {@link HandlerDefinition}.
     * @param hdNode {@link HandlerDefinition} <code>Node</code>, its children will be searched.
     */
    private void addOutputDefs(HandlerDefinition hd, Node hdNode) {
        // Get the child nodes
        Iterator<Node> it = getChildElements(hdNode, OUTPUT_DEF_ELEMENT).iterator();

        // Walk children (we only care about OUTPUT_DEF_ELEMENT)
        while (it.hasNext()) {
            // Found a OUTPUT_DEF_ELEMENT
            hd.addOutputDef(createIODescriptor(it.next()));
        }
    }

    /**
     * <p>
     * This method will create an {@link #IODescriptor} from the given node. The node must contain atleast a
     * {@link #NAME_ATTRIBUTE} and a {@link #TYPE_ATTRIBUTE} attribute. It may also contain a {@link #DEFAULT_ATTRIBUTE} and
     * a {@link #REQUIRED_ATTRIBUTE}. These are only meaningful for input {@link IODescriptors}, however -- this method does
     * not know the difference between input and output descriptors.
     * </p>
     *
     * @param node The <code>Node</code> holding info used to create an {@link IODescriptor}.
     *
     * @return A newly created {@link IODescriptor}.
     */
    private IODescriptor createIODescriptor(Node node) {
        // Get the attributes
        Map<String, String> attributes = getAttributes(node);
        String name = attributes.get(NAME_ATTRIBUTE);
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("Name must be provided!");
        }
        String type = attributes.get(TYPE_ATTRIBUTE);
        if (type == null || type.equals("")) {
            throw new IllegalArgumentException("Type must be provided!");
        }
        Object def = attributes.get(DEFAULT_ATTRIBUTE);
        String req = attributes.get(REQUIRED_ATTRIBUTE);

        // Create the IODescriptor
        IODescriptor ioDesc = new IODescriptor(name, type);
        ioDesc.setDefault(def);
        if (req != null) {
            ioDesc.setRequired(Boolean.valueOf(req).booleanValue());
        }
// ioDesc.setDescription(attributes.get(DESCRIPTION_ATTRIBUTE))

        // Return the new IODescriptor
        return ioDesc;
    }

    /**
     * <p>
     * This method adds child LayoutElements.
     * </p>
     *
     * @param layoutDefinition
     * @param node
     */
    private void addChildLayoutElements(LayoutElement layElt, Node node) {
        // Get the child nodes
        Iterator<Node> it = getChildElements(node).iterator();

        // Walk children (we care about IF_ELEMENT, ATTRIBUTE_ELEMENT,
        // MARKUP_ELEMENT, FACET_ELEMENT, STATIC_TEXT_ELEMENT,
        // COMPONENT_ELEMENT, EVENT_ELEMENT, FOREACH_ELEMENT, EDIT_ELEMENT, and
        // WHILE_ELEMENT)
        Node childNode = null;
        String name = null;
        while (it.hasNext()) {
            childNode = it.next();
            name = childNode.getNodeName();
            if (name.equalsIgnoreCase(IF_ELEMENT)) {
                // Found a IF_ELEMENT
                layElt.addChildLayoutElement(createLayoutIf(layElt, childNode));
            } else if (name.equalsIgnoreCase(ATTRIBUTE_ELEMENT)) {
                // Found a ATTRIBUTE_ELEMENT
                LayoutElement childElt = createLayoutAttribute(layElt, childNode);
                if (childElt != null) {
                    layElt.addChildLayoutElement(childElt);
                }
            } else if (name.equalsIgnoreCase(MARKUP_ELEMENT)) {
                // Found a MARKUP_ELEMENT
                layElt.addChildLayoutElement(createLayoutMarkup(layElt, childNode));
            } else if (name.equalsIgnoreCase(FACET_ELEMENT)) {
                // Found a FACET_ELEMENT
                layElt.addChildLayoutElement(createLayoutFacet(layElt, childNode));
            } else if (name.equalsIgnoreCase(STATIC_TEXT_ELEMENT)) {
                // Found a STATIC_TEXT_ELEMENT
                layElt.addChildLayoutElement(createLayoutStaticText(layElt, childNode));
            } else if (name.equalsIgnoreCase(COMPONENT_ELEMENT)) {
                // Found a COMPONENT_ELEMENT
                layElt.addChildLayoutElement(createLayoutComponent(layElt, childNode));
            } else if (name.equalsIgnoreCase(EVENT_ELEMENT)) {
                // Found a EVENT_ELEMENT
                // Get the event type
                name = getAttributes(childNode).get(TYPE_ATTRIBUTE);
                // Set the Handlers for the given event type (name)
                List<Handler> handlers = layElt.getHandlers(name);
                layElt.setHandlers(name, getHandlers(childNode, handlers));
            } else if (name.equalsIgnoreCase(FOREACH_ELEMENT)) {
                // Found a FOREACH_ELEMENT
                layElt.addChildLayoutElement(createLayoutForEach(layElt, childNode));
            } else if (name.equalsIgnoreCase(WHILE_ELEMENT)) {
                // Found a WHILE_ELEMENT
                layElt.addChildLayoutElement(createLayoutWhile(layElt, childNode));
            } else if (name.equalsIgnoreCase(EDIT_ELEMENT)) {
                // Found an EDIT_ELEMENT
                layElt.addChildLayoutElement(createEditLayoutComponent(layElt, childNode));
            } else {
                throw new RuntimeException("Unknown Element Found: '" + childNode.getNodeName() + "' under '" + node.getNodeName() + "'.");
            }
        }
    }

    /**
     * <p>
     * This method creates a new {@link LayoutIf} {@link LayoutElement}.
     * </p>
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link #IF_ELEMENT} node to extract information from when creating the {@link LayoutIf}
     */
    private LayoutElement createLayoutIf(LayoutElement parent, Node node) {
        // Pull off attributes...
        String condition = getAttributes(node).get(CONDITION_ATTRIBUTE);
        if (condition == null || condition.trim().equals("")) {
            throw new RuntimeException("'" + CONDITION_ATTRIBUTE + "' attribute not found on '" + IF_ELEMENT + "' Element!");
        }

        // Create new LayoutIf
        LayoutElement ifElt = new LayoutIf(parent, condition);

        // Add children...
        addChildLayoutElements(ifElt, node);

        // Return the if
        return ifElt;
    }

    /**
     * <p>
     * This method creates a new {@link LayoutForEach} {@link LayoutElement}.
     * </p>
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link #FOREACH_ELEMENT} node to extract information from when creating the {@link LayoutForEach}.
     *
     * @return The new {@link LayoutForEach} {@link LayoutElement}.
     */
    private LayoutElement createLayoutForEach(LayoutElement parent, Node node) {
        // Pull off attributes...
        String list = getAttributes(node).get(LIST_ATTRIBUTE);
        if (list == null || list.trim().equals("")) {
            throw new RuntimeException("'" + LIST_ATTRIBUTE + "' attribute not found on '" + FOREACH_ELEMENT + "' Element!");
        }
        String key = getAttributes(node).get(KEY_ATTRIBUTE);
        if (key == null || key.trim().equals("")) {
            throw new RuntimeException("'" + KEY_ATTRIBUTE + "' attribute not found on '" + FOREACH_ELEMENT + "' Element!");
        }

        // Create new LayoutForEach
        LayoutElement forEachElt = new LayoutForEach(parent, list, key);

        // Add children...
        addChildLayoutElements(forEachElt, node);

        // Return the forEach
        return forEachElt;
    }

    /**
     * <p>
     * This method creates a new {@link LayoutWhile} {@link LayoutElement}.
     * </p>
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link #WHILE_ELEMENT} node to extract information from when creating the LayoutWhile.
     *
     * @return The new {@link LayoutWhile} {@link LayoutElement}.
     */
    private LayoutElement createLayoutWhile(LayoutElement parent, Node node) {
        // Pull off attributes...
        String condition = getAttributes(node).get(CONDITION_ATTRIBUTE);
        if (condition == null || condition.trim().equals("")) {
            throw new RuntimeException("'" + CONDITION_ATTRIBUTE + "' attribute not found on '" + WHILE_ELEMENT + "' Element!");
        }

        // Create new LayoutWhile
        LayoutElement whileElt = new LayoutWhile(parent, condition);

        // Add children...
        addChildLayoutElements(whileElt, node);

        // Return the while
        return whileElt;
    }

    /**
     *
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link #ATTRIBUTE_ELEMENT} node to extract information from when creating the {@link LayoutAttribute}
     */
    private LayoutElement createLayoutAttribute(LayoutElement parent, Node node) {
        // Pull off attributes...
        Map<String, String> attributes = getAttributes(node);
        String name = attributes.get(NAME_ATTRIBUTE);
        if (name == null || name.trim().equals("")) {
            throw new RuntimeException("'" + NAME_ATTRIBUTE + "' attribute not found on '" + ATTRIBUTE_ELEMENT + "' Element!");
        }
        LayoutElement attributeElt = null;

        // Check if we're setting this on a LayoutComponent vs. LayoutMarkup
        // Do this after checking for "name" to show correct error message
        LayoutComponent comp = null;
        if (parent instanceof LayoutComponent) {
            comp = (LayoutComponent) parent;
        } else {
            comp = LayoutElementUtil.getParentLayoutComponent(parent);
        }
        if (comp != null) {
            // Treat this as a LayoutComponent "option" instead of "attribute"
            addOption(comp, node);
        } else {
            String value = attributes.get(VALUE_ATTRIBUTE);
            String property = attributes.get(PROPERTY_ATTRIBUTE);

            // Create new LayoutAttribute
            attributeElt = new LayoutAttribute(parent, name, value, property);

            // Add children... (event children are supported)
            addChildLayoutElements(attributeElt, node);
        }

        // Return the LayoutAttribute (or null if inside LayoutComponent)
        return attributeElt;
    }

    /**
     * <p>
     * This method creates a new {@link LayoutMarkup}.
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link #MARKUP_ELEMENT} node to extract information from when creating the {@link LayoutMarkup}.
     */
    private LayoutElement createLayoutMarkup(LayoutElement parent, Node node) {
        // Pull off attributes...
        Map<String, String> attributes = getAttributes(node);
        String tag = attributes.get(TAG_ATTRIBUTE);
        if (tag == null || tag.trim().equals("")) {
            throw new RuntimeException("'" + TAG_ATTRIBUTE + "' attribute not found on '" + MARKUP_ELEMENT + "' Element!");
        }

        // Check to see if this is inside a LayoutComponent, if so, we must
        // use a LayoutComponent for it to get rendered
        LayoutElement markupElt = null;
        if (parent instanceof LayoutComponent || LayoutElementUtil.isNestedLayoutComponent(parent)) {
            // Make a "markup" LayoutComponent..
            ComponentType type = ensureMarkupType(parent);
            markupElt = new LayoutComponent(parent, MARKUP_ELEMENT + _markupCount++, type);
            LayoutComponent markupComp = (LayoutComponent) markupElt;
            markupComp.addOption("tag", tag);
            markupComp.setNested(true);

            // Add children...
            addChildLayoutComponentChildren(markupComp, node);
        } else {
            // Create new LayoutMarkup
            String type = attributes.get(TYPE_ATTRIBUTE);
            markupElt = new LayoutMarkup(parent, tag, type);

            // Add children...
            addChildLayoutElements(markupElt, node);
        }

        // Return the LayoutMarkup
        return markupElt;
    }

    /**
     * <p>
     * This method is responsible for Creating a {@link LayoutFacet} {@link LayoutElement}.
     * </p>
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link #FACET_ELEMENT} node to extract information from when creating the {@link LayoutFacet}.
     *
     * @return The new {@link LayoutFacet} {@link LayoutElement}.
     */
    private LayoutElement createLayoutFacet(LayoutElement parent, Node node) {
        // Pull off attributes...
        // id
        String id = getAttributes(node).get(ID_ATTRIBUTE);
        if (id == null || id.trim().equals("")) {
            throw new RuntimeException("'" + ID_ATTRIBUTE + "' attribute not found on '" + FACET_ELEMENT + "' Element!");
        }

        // Create new LayoutFacet
        LayoutFacet facetElt = new LayoutFacet(parent, id);

        // Set isRendered
        String rendered = getAttributes(node).get(RENDERED_ATTRIBUTE);
        boolean isRendered = true;
        if (rendered == null || rendered.trim().equals("") || rendered.equals(AUTO_RENDERED)) {
            // Automatically determine if this LayoutFacet should be rendered
            isRendered = !LayoutElementUtil.isNestedLayoutComponent(facetElt);
        } else {
            isRendered = Boolean.getBoolean(rendered);
        }
        facetElt.setRendered(isRendered);

        // Add children...
        addChildLayoutElements(facetElt, node);

        // Return the LayoutFacet
        return facetElt;
    }

    /**
     *
     * @param node The {@link #COMPONENT_ELEMENT} node to extract information from when creating the
     * {@link LayoutComponent}.
     */
    private LayoutElement createLayoutComponent(LayoutElement parent, Node node) {
        // Pull off attributes...
        Map<String, String> attributes = getAttributes(node);
        String id = attributes.get(ID_ATTRIBUTE);
        String type = attributes.get(TYPE_ATTRIBUTE);
        if (type == null || type.trim().equals("")) {
            throw new RuntimeException("'" + TYPE_ATTRIBUTE + "' attribute not found on '" + COMPONENT_ELEMENT + "' Element!");
        }

        // Create new LayoutComponent
        LayoutComponent component = new LayoutComponent(parent, id, getComponentType(parent, type));

        // Check for overwrite flag
        String overwrite = attributes.get(OVERWRITE_ATTRIBUTE);
        if (overwrite != null && overwrite.length() > 0) {
            component.setOverwrite(Boolean.valueOf(overwrite).booleanValue());
        }

        // Set flag to indicate if this LayoutComponent is nested in another
        // LayoutComponent. This is significant b/c during rendering, events
        // will need to be fired differently (the TemplateRenderer /
        // LayoutElements will not have any control). The strategy used will
        // rely on "instance" handlers, this flag indicates that "instance"
        // handlers should be used.
        // NOTE: While this could be implemented on the LayoutComponent
        // itself, I decided not to for performance reasons and to
        // allow this value to be overruled if desired.
        component.setNested(LayoutElementUtil.isNestedLayoutComponent(component));

        // Figure out if this should be stored as a facet, if so under what id
        if (!LayoutElementUtil.isLayoutComponentChild(component)) {
            // Need to add this so that it has the correct facet name
            // Check to see if this LayoutComponent is inside a LayoutFacet
            while (parent != null) {
                if (parent instanceof LayoutFacet) {
                    // Inside a LayoutFacet, use its id... only if this facet
                    // is a child of a LayoutComponent (otherwise, it is a
                    // layout facet used for layout, not for defining a facet
                    // of a UIComponent)
                    if (LayoutElementUtil.isLayoutComponentChild(parent)) {
                        id = parent.getUnevaluatedId();
                    }
                    break;
                }
                parent = parent.getParent();
            }

            // Set the facet name
            component.addOption(LayoutComponent.FACET_NAME, id);
        }

        // Add children... (different for component LayoutElements)
        addChildLayoutComponentChildren(component, node);

        // Return the LayoutComponent
        return component;
    }

    /**
     *
     */
    private void addChildLayoutComponentChildren(LayoutComponent component, Node node) {
        // Get the child nodes
        Iterator<Node> it = getChildElements(node).iterator();

        // Walk children (we care about COMPONENT_ELEMENT, FACET_ELEMENT,
        // OPTION_ELEMENT, EVENT_ELEMENT, MARKUP_ELEMENT, and EDIT_ELEMENT)
        Node childNode = null;
        String name = null;
        while (it.hasNext()) {
            childNode = it.next();
            name = childNode.getNodeName();
            if (name.equalsIgnoreCase(COMPONENT_ELEMENT)) {
                // Found a COMPONENT_ELEMENT
                component.addChildLayoutElement(createLayoutComponent(component, childNode));
            } else if (name.equalsIgnoreCase(FACET_ELEMENT)) {
                // Found a FACET_ELEMENT
                component.addChildLayoutElement(createLayoutFacet(component, childNode));
            } else if (name.equalsIgnoreCase(OPTION_ELEMENT)) {
                // Found a OPTION_ELEMENT
                addOption(component, childNode);
            } else if (name.equalsIgnoreCase(EVENT_ELEMENT)) {
                // Found a EVENT_ELEMENT
                // Get the event type
                name = getAttributes(childNode).get(TYPE_ATTRIBUTE);

                // Set the Handlers for the given event type (name)
                List<Handler> handlers = component.getHandlers(name);
                component.setHandlers(name, getHandlers(childNode, handlers));
            } else if (name.equalsIgnoreCase(EDIT_ELEMENT)) {
                // Found an EDIT_ELEMENT
                component.addChildLayoutElement(createEditLayoutComponent(component, childNode));
            } else if (name.equalsIgnoreCase(MARKUP_ELEMENT)) {
                // Found an MARKUP_ELEMENT
                component.addChildLayoutElement(createLayoutMarkup(component, childNode));
            } else if (name.equalsIgnoreCase(STATIC_TEXT_ELEMENT)) {
                // Found a STATIC_TEXT_ELEMENT
                component.addChildLayoutElement(createLayoutStaticText(component, childNode));
            } else if (name.equalsIgnoreCase(ATTRIBUTE_ELEMENT)) {
                // Found a ATTRIBUTE_ELEMENT (actually in this case it will
                // just add an "option" to the LayoutComponent), technically
                // this case should only happen for LayoutMarkup components...
                // this mess is caused by trying to support 2 .dtd's w/ 1 .dtd
                // file... perhaps it's time to split.
                createLayoutAttribute(component, childNode);
            } else {
                throw new RuntimeException("Unknown Element Found: '" + childNode.getNodeName() + "' under '<" + COMPONENT_ELEMENT + " id=\""
                        + component.getUnevaluatedId() + "\"...'.");
            }
        }
    }

    /**
     * <p>
     * This adds a special {@link LayoutComponent} to the <code>UIComponent</code> tree that serves as a marker indicating
     * that it is OK to edit this block of code. Editors can take advantage of this to know what is safe to edit. The type
     * will be "markup" the id will be prefixed by {@link #EDITABLE} and an "option" named {@link #EDITABLE}, value ==
     * <code>true</code> will be added.
     * </p>
     *
     * @param node The {@link #COMPONENT_ELEMENT} node to extract information from when creating the <em>Edit</em>
     * {@link LayoutComponent}.
     */
    private LayoutElement createEditLayoutComponent(LayoutElement parent, Node node) {
        // First Add a popupMenu around this component... use it as the parent
        parent = createEditPopupMenuLayoutComponent(parent, node);

        // Pull off attributes...
        Map<String, String> attributes = getAttributes(node);
        String id = attributes.get(ID_ATTRIBUTE);

        // Create the LayoutComponent
        ComponentType type = ensureEditAreaType(parent);
        LayoutComponent component = new LayoutComponent(parent, EDITABLE + id, type);
        parent.addChildLayoutElement(component);

        // Configure it...
        component.setNested(LayoutElementUtil.isNestedLayoutComponent(component));
        component.addOption(EDITABLE, Boolean.TRUE); // Flag

        // Add children... (different for component LayoutElements)
        addChildLayoutComponentChildren(component, node);

        return parent;
    }

    /**
     * <p>
     * This method creates a PopupMenu component w/ the Editor commands.
     * </p>
     */
    private LayoutElement createEditPopupMenuLayoutComponent(LayoutElement parent, Node node) {
        // Pull off attributes...
        Map<String, String> attributes = getAttributes(node);
        String id = attributes.get(ID_ATTRIBUTE);

        // Create the LayoutComponent
        ComponentType type = ensurePopupMenuType(parent);
        LayoutComponent popupMenu = new LayoutComponent(parent, EDIT_MENU + id, type);

        // Configure it...
        popupMenu.setNested(LayoutElementUtil.isNestedLayoutComponent(popupMenu));

        // Could add "menu" facet here, however, I decided to do it in the xml

        // Return the result
        return popupMenu;
    }

    /**
     * <p>
     * This method ensures that a "popupMenu" {@link ComponentType} has been defined so that it can be used implicitly.
     * </p>
     */
    private ComponentType ensurePopupMenuType(LayoutElement elt) {
        // See if it is defined
        LayoutDefinition ld = elt.getLayoutDefinition();
        ComponentType type = null;
        try {
            type = getComponentType(elt, POPUP_MENU_TYPE);
        } catch (IllegalArgumentException ex) {
            // Nope, define it...
            type = new ComponentType(POPUP_MENU_TYPE, POPUP_MENU_TYPE_CLASS);
            ld.addComponentType(type);
        }

        // Return the type
        return type;
    }

    /**
     * <p>
     * This method ensures that a "editArea" {@link ComponentType} has been defined so that it can be used implicitly.
     * </p>
     */
    private ComponentType ensureEditAreaType(LayoutElement elt) {
        // See if it is defined
        LayoutDefinition ld = elt.getLayoutDefinition();
        ComponentType type = null;
        try {
            type = getComponentType(elt, EDIT_AREA_TYPE);
        } catch (IllegalArgumentException ex) {
            // Nope, define it...
            type = new ComponentType(EDIT_AREA_TYPE, EDIT_AREA_TYPE_CLASS);
            ld.addComponentType(type);
        }

        // Return the type
        return type;
    }

    /**
     * <p>
     * This method ensures that a "markup" {@link ComponentType} has been defined so that it can be used implicitly.
     * </p>
     */
    private ComponentType ensureMarkupType(LayoutElement elt) {
        // See if it is defined
        LayoutDefinition ld = elt.getLayoutDefinition();
        ComponentType type = null;
        try {
            type = getComponentType(elt, MARKUP_ELEMENT);
        } catch (IllegalArgumentException ex) {
            // Nope, define it...
            type = new ComponentType(MARKUP_ELEMENT, MARKUP_FACTORY_CLASS);
            ld.addComponentType(type);
        }

        // Return the type
        return type;
    }

    /**
     * <p>
     * This method adds an option to the given {@link LayoutComponent} based on the information in the given
     * {@link #OPTION_ELEMENT} <code>Node</code>.
     * </p>
     *
     * @param component The {@link LayoutComponent}.
     * @param node The {@link #OPTION_ELEMENT} <code>Node</code>.
     */
    private void addOption(LayoutComponent component, Node node) {
        // Pull off the attributes
        Map<String, String> attributes = getAttributes(node);

        // Get the name
        String name = attributes.get(NAME_ATTRIBUTE);
        if (name == null || name.trim().equals("")) {
            throw new RuntimeException("'" + NAME_ATTRIBUTE + "' attribute not found on '" + OPTION_ELEMENT + "' Element!");
        }
        name = name.trim();

        // Get the value
        Object value = getValueFromNode(node, attributes);

        // Add the option to the component (value may be null)
        component.addOption(name, value);
    }

    /**
     * <p>
     * This method reads obtains the {@link #VALUE_ATTRIBUTE} from the given node, or from the child {@link #LIST_ELEMENT}
     * element. If neither are provided, <code>(null)</code> is returned. The attribute takes precedence over the child
     * {@link #LIST_ELEMENT} element.
     * </p>
     *
     * @param node <code>Node</code> containing the value attribute or {@link #LIST_ELEMENT}
     * @param attributes <code>Map</code> of attributes which may contain {@link #VALUE_ATTRIBUTE}
     *
     * @return The value (as a <code>String</code> or <code>List</code>), or <code>(null)</code> if not specified.
     */
    private Object getValueFromNode(Node node, Map<String, String> attributes) {
        Object value = attributes.get(VALUE_ATTRIBUTE);
        if (value == null) {
            // The value attribute may be null if multiple values are supplied.
            // Walk children (we only care about LIST_ELEMENT)
            List<String> list = new ArrayList();
            Iterator<Node> it = getChildElements(node, LIST_ELEMENT).iterator();
            while (it.hasNext()) {
                // Add a value to the List
                list.add(getAttributes(it.next()).get(VALUE_ATTRIBUTE));
            }
            if (list.size() > 0) {
                // Only use the list if it has values
                value = list;
            }
        }
        return value;
    }

    /**
     *
     * @param node The {@link #STATIC_TEXT_ELEMENT} node to extract information from when creating the
     * {@link LayoutStaticText}.
     */
    private LayoutElement createLayoutStaticText(LayoutElement parent, Node node) {
        // Create new LayoutComponent
        LayoutStaticText text = new LayoutStaticText(parent, "", getTextNodesAsString(node));

        // Add all the attributes from the static text as options
//    component.addOptions(getAttributes(node));

        // Add escape... FIXME

        // Return the LayoutStaticText
        return text;
    }

    //////////////////////////////////////////////////////////////////////
    // Utility Methods
    //////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * This method returns a <code>List</code> of all child <code>Element</code>s below the given <code>Node</code>.
     * </p>
     *
     * @param node The <code>Node</code> to pull child elements from.
     *
     * @return <code>List</code> of child <code>Element</code>s found below the given <code>Node</code>.
     */
    public List<Node> getChildElements(Node node) {
        return getChildElements(node, null);
    }

    /**
     * <p>
     * This method returns a List of all child Elements below the given Node matching the given name. If name equals null,
     * all Elements below this node will be returned.
     * </p>
     *
     * @param node The node to pull child elements from.
     * @param name The name of the Elements to return.
     *
     * @return List of child elements found below the given node matching the name (if provided).
     */
    public List<Node> getChildElements(Node node, String name) {
        // Get the child nodes
        NodeList nodes = node.getChildNodes();
        if (nodes == null) {
            // No children, just return an empty List
            return new ArrayList<>(0);
        }

        // Create a new List to store the child Elements
        List<Node> list = new ArrayList<>();

        // Add all the child Elements to the List
        Node childNode = null;
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            childNode = nodes.item(idx);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                // Skip TEXT_NODE and other Node types
                continue;
            }

            // Add to the list if name is null, or it matches the node name
            if (name == null || childNode.getNodeName().equalsIgnoreCase(name)) {
                list.add(childNode);
            }
        }

        // Return the list of Elements
        return list;
    }

    /**
     * <p>
     * This method returns the <code>String</code> representation of all the <code>Node.TEXT_NODE</code> nodes that are
     * children of the given <code>Node</code>.
     *
     * @param node The <code>Node</code> to pull child <code>Element</code>s from.
     *
     * @return The <code>String</code> representation of all the <code>Node.TEXT_NODE</code> type nodes under the given
     * <code>Node</code>.
     */
    public String getTextNodesAsString(Node node) {
        // Get the child nodes
        NodeList nodes = node.getChildNodes();
        if (nodes == null) {
            // No children, return null
            return null;
        }

        // Create a StringBuffer
        StringBuffer buf = new StringBuffer("");

        // Add all the child Element values to the StringBuffer
        Node childNode = null;
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            childNode = nodes.item(idx);
            if (childNode.getNodeType() != Node.TEXT_NODE && childNode.getNodeType() != Node.CDATA_SECTION_NODE) {
                // Skip all other Node types
                continue;
            }
            buf.append(childNode.getNodeValue());
        }

        // Return the String
        return buf.toString();
    }

    /**
     * <p>
     * This method returns a <code>Map</code> of all attributes for the given <code>Node</code>. Each attribute name will be
     * stored in the <code>Map</code> in lower case so case can be ignored.
     * </p>
     *
     * @param node The node to pull attributes from.
     *
     * @return <code>Map</code> of attributes found on the given <code>Node</code>.
     */
    public Map<String, String> getAttributes(Node node) {
        // Get the attributes
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null || attributes.getLength() == 0) {
            // No attributes, just return an empty Map
            return new HashMap<>(0);
        }

        // Create a Map to contain the attributes
        Map<String, String> map = new HashMap<>();

        // Add all the attributes to the Map
        Node attNode = null;
        for (int idx = 0; idx < attributes.getLength(); idx++) {
            attNode = attributes.item(idx);
            map.put(attNode.getNodeName().toLowerCase(), attNode.getNodeValue());
        }

        // Return the map
        return map;
    }

    /**
     * <p>
     * This utility method returns the requested {@link ComponentType}. If it is not found, it throws an
     * <code>IllegalArgumentException</code>.
     * </p>
     *
     * @param elt A {@link LayoutElement} whose root is a {@link LayoutDefinition}.
     * @param type The <code>String</code> type to lookup.
     *
     * @return The {@link ComponentType}.
     */
    public ComponentType getComponentType(LayoutElement elt, String type) {
        // Find the ComponentType
        ComponentType compType = elt.getLayoutDefinition().getComponentType(type);
        if (compType == null) {
            // Check global component types (defined via @annotations). This
            // is now the preferred way to define types, however, locally
            // defined types should have precedence
            compType = LayoutDefinitionManager.getGlobalComponentType(null, type);
            if (compType == null) {
                throw new IllegalArgumentException("ComponentType '" + type + "' not defined!");
            }
        }
        return compType;
    }

    //////////////////////////////////////////////////////////////////////
    // Constants
    //////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_ELEMENT = "attribute";
    public static final String COMPONENT_ELEMENT = "component";
    public static final String COMPONENT_TYPE_ELEMENT = "componenttype";
    public static final String EDIT_ELEMENT = "edit";
    public static final String EVENT_ELEMENT = "event";
    public static final String FACET_ELEMENT = "facet";
    public static final String FOREACH_ELEMENT = "foreach";
    public static final String HANDLER_ELEMENT = "handler";
    public static final String HANDLERS_ELEMENT = "handlers";
    public static final String HANDLER_DEFINITION_ELEMENT = "handlerdefinition";
    public static final String IF_ELEMENT = "if";
    public static final String INPUT_DEF_ELEMENT = "inputdef";
    public static final String INPUT_ELEMENT = "input";
    public static final String LAYOUT_DEFINITION_ELEMENT = "layoutdefinition";
    public static final String LAYOUT_ELEMENT = "layout";
    public static final String LIST_ELEMENT = "list";
    public static final String MARKUP_ELEMENT = "markup";
    public static final String OPTION_ELEMENT = "option";
    public static final String OUTPUT_DEF_ELEMENT = "outputdef";
    public static final String OUTPUT_MAPPING_ELEMENT = "outputmapping";
    public static final String STATIC_TEXT_ELEMENT = "statictext";
    public static final String TYPES_ELEMENT = "types";
    public static final String RESOURCES_ELEMENT = "resources";
    public static final String RESOURCE_ELEMENT = "resource";
    public static final String WHILE_ELEMENT = "while";

    public static final String CLASS_NAME_ATTRIBUTE = "classname";
    public static final String CONDITION_ATTRIBUTE = "condition";
    public static final String DEFAULT_ATTRIBUTE = "default";
    public static final String DESCRIPTION_ATTRIBUTE = "description";
    public static final String EXTRA_INFO_ATTRIBUTE = "extrainfo";
    public static final String FACTORY_CLASS_ATTRIBUTE = "factoryclass";
    public static final String ID_ATTRIBUTE = "id";
    public static final String KEY_ATTRIBUTE = "key";
    public static final String LIST_ATTRIBUTE = "list";
    public static final String METHOD_NAME_ATTRIBUTE = "methodname";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String OUTPUT_NAME_ATTRIBUTE = "outputname";
    public static final String OVERWRITE_ATTRIBUTE = "overwrite";
    public static final String PROPERTY_ATTRIBUTE = "property";
    public static final String RENDERED_ATTRIBUTE = "rendered";
    public static final String REQUIRED_ATTRIBUTE = "required";
    public static final String TAG_ATTRIBUTE = "tag";
    public static final String TARGET_KEY_ATTRIBUTE = "targetkey";
    public static final String TARGET_TYPE_ATTRIBUTE = "targettype";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String VALUE_ATTRIBUTE = "value";

    public static final String AUTO_RENDERED = "auto";
    public static final String EDITABLE = "editableContent";
    public static final String EDIT_MENU = "editMenu";

    public static final String EDIT_AREA_TYPE = "editArea";
    public static final String POPUP_MENU_TYPE = "popupMenu";
    public static final String EDIT_AREA_TYPE_CLASS = "com.sun.jsftemplating.component.factory.basic.EditAreaFactory";
    public static final String MARKUP_FACTORY_CLASS = "com.sun.jsftemplating.component.factory.basic.MarkupFactory";
    public static final String POPUP_MENU_TYPE_CLASS = "com.sun.jsftemplating.component.factory.basic.PopupMenuFactory";

    /**
     * This is used to set the "value" option for static text fields.
     */
//    public static final String VALUE_OPTION    =   "value";

    private URL _url = null;
    private EntityResolver _entityResolver = null;
    private ErrorHandler _errorHandler = null;
    private String _baseURI = null;

    private Map<String, HandlerDefinition> _handlerDefs = new HashMap<>();
    private int _markupCount = 1;
}
