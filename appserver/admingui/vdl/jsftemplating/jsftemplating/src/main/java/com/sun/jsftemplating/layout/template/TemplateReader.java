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

package com.sun.jsftemplating.layout.template;

import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.ProcessingCompleteException;
import com.sun.jsftemplating.layout.SyntaxException;
import com.sun.jsftemplating.layout.descriptors.ComponentType;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.layout.descriptors.LayoutFacet;
import com.sun.jsftemplating.layout.descriptors.LayoutForEach;
import com.sun.jsftemplating.layout.descriptors.LayoutIf;
import com.sun.jsftemplating.layout.descriptors.LayoutWhile;
import com.sun.jsftemplating.util.LayoutElementUtil;
import com.sun.jsftemplating.util.LogUtil;

import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * <p>
 * This class is responsible for parsing templates. It produces a {@link LayoutElement} tree with a
 * {@link LayoutDefinition} object at the root of the tree.
 * </p>
 *
 * <p>
 * This class is intended to "read" the template one time. Often it may be useful to cache the result as it would be
 * inefficient to call {@link TemplateReader#read()} multiple time (and therefor parse the template multiple times).
 * Templates that are generated from this class are intended to be static and safe to share (but not alter).
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class TemplateReader {

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param id The identifier of the {@link LayoutDefinition} to read.
     * @param url <code>URL</code> to the {@link LayoutDefinition} file.
     */
    public TemplateReader(String id, URL url) {
        if (id == null) {
            throw new IllegalArgumentException("Template id's may not be null!");
        }
        _id = id;
        _idNumber = LayoutElementUtil.getStartingIdNumber(null, id);
        _tpl = new TemplateParser(url);
    }

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param stream The <code>InputStream</code> for the {@link LayoutDefinition}.
     * @param id The identifier of the {@link LayoutDefinition} to read.
     */
    public TemplateReader(String id, InputStream stream) {
        if (id == null) {
            throw new IllegalArgumentException("Template id's may not be null!");
        }
        _id = id;
        _tpl = new TemplateParser(stream);
    }

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param id The identifier of the {@link LayoutDefinition} to read.
     * @param parser {@link TemplateParser} ready to read the {@link LayoutDefinition}.
     */
    public TemplateReader(String id, TemplateParser parser) {
        _id = id;
        _tpl = parser;
    }

    /**
     * <p>
     * Accessor for the {@link TemplateParser}.
     * </p>
     *
     * @return The {@link TemplateParser}.
     */
    public TemplateParser getTemplateParser() {
        return _tpl;
    }

    /**
     * <p>
     * The read method uses the {@link TemplateParser} to parses the template. It populates a {@link LayoutDefinition}
     * structure, which is returned.
     * </p>
     *
     * @return The {@link LayoutDefinition}
     *
     * @throws IOException
     */
    public LayoutDefinition read() throws IOException {
        // Open the Template
        TemplateParser parser = getTemplateParser();
        parser.open();

        try {
            // Populate the LayoutDefinition from the Document
            return readLayoutDefinition();
        } finally {
            parser.close();
        }
    }

    /**
     * <p>
     * This method is responsible for creating and populating the {@link LayoutDefinition}.
     * </p>
     *
     * @return The new {@link LayoutDefinition} Object.
     */
    private LayoutDefinition readLayoutDefinition() throws IOException {
        // Create a new LayoutDefinition (the id is not propagated here)
        LayoutDefinition ld = new LayoutDefinition(_id);

        // For now we will only support global resources. In the future, we
        // may want to allow resources to be overriden at the page level and /
        // or additional page-specific resources to be added.
        //
        // NOTE: Resources may be locale specific, so we can't easily share
        // this at the application scope. Here, "global" means across
        // pages, not across sessions.
// FIXME: This isn't implemented yet...
        ld.setResources(LayoutDefinitionManager.getGlobalResources(null));

        /*
         * // Look to see if there is an EVENT_ELEMENT defined childElements = getChildElements(node, EVENT_ELEMENT); it =
         * childElements.iterator(); if (it.hasNext()) { // Found the EVENT_ELEMENT, there is at most 1 // Get the event type
         * Node eventNode = (Node) it.next(); String type = (String) getAttributes(eventNode). get(TYPE_ATTRIBUTE);
         *
         * // Set the Handlers for the given event type (name) List<Handler> handlers = ld.getHandlers(type);
         * ld.setHandlers(type, getHandlers(eventNode, handlers)); }
         */
        try {
            ld = (LayoutDefinition) process(LAYOUT_DEFINITION_CONTEXT, ld, false);
        } catch (ProcessingCompleteException pc) {
            // Some tags can abort processing early. This isn't an error, but
            // we use this exception to abort immediately.
            ld = pc.getLayoutDefinition();
        }
        return ld;
    }

    /**
     * <p>
     * This method does the walking through the file. The file has different "contexts" in which it may be processing. For
     * example, when processing at the top-level, certain syntax is valid. However, when processing content inside a
     * component, the valid syntax may be different. This method delegates handling of various syntaxes to the
     * {@link ProcessingContext}. This enables the walking and the processing to be separated.
     * </p>
     *
     * <p>
     * The <code>nested</code> flag is used to indicate whether processing is nested inside a {@link LayoutComponent}. This
     * is important to know because the rules change when <code>UIComponent</code>s become nested. The
     * <code>ViewHandler</code> has control at the top level (non-nested), but does not have control inside
     * <code>UIComponent</code>s. When nested often a <code>UIComponent</code> must be utilized instead of a
     * {@link LayoutElement}. In a page situation, most tags will be nested; when defining a <code>Renderer</code> most tags
     * are not likely to be nested.
     * </p>
     *
     * @param ctx The {@link ProcessingContext}
     * @param parent The parent {@link LayoutElement}
     * @param nested <code>true</code> if nested in a {@link LayoutComponent}
     */
    public LayoutElement process(ProcessingContext ctx, LayoutElement parent, boolean nested) throws IOException {
        // Get the parser...
        TemplateParser parser = getTemplateParser();

        // Skip White Space...
        parser.skipCommentsAndWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE);

        ProcessingContextEnvironment env = new ProcessingContextEnvironment(this, parent, nested);
        int ch = parser.nextChar();
        String startTag = null;
        String tmpstr = null;
        boolean finished = false;
        while (ch != -1) {
            switch (ch) {
            case '<':
                parser.skipCommentsAndWhiteSpace( // Skip white space
                        TemplateParser.SIMPLE_WHITE_SPACE);
                ch = parser.nextChar();
                if (ch == '/') {
                    // Closing tag
                    parser.skipCommentsAndWhiteSpace( // Skip white space
                            TemplateParser.SIMPLE_WHITE_SPACE);
                    // Get the expected String
                    if (isTagStackEmpty()) {
                        parser.skipCommentsAndWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE + '!');
                        throw new SyntaxException("Found end tag '&lt;/" + parser.readToken() + "...' but did not find matching begin tag!");
                    }
                    startTag = popTag();
                    if (startTag.length() > 0 && startTag.charAt(0) == '!') {
                        // Check for special flag, might have a '!'
                        ch = parser.nextChar();
                        // Ignore the '!' if there, if not push it back
                        if (ch == '!') {
                            // Ignore '!', but skip white space after it
                            parser.skipCommentsAndWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE);
                        } else {
                            // No optional '!', push back extra read char
                            parser.unread(ch);
                        }
                        tmpstr = parser.readToken();
                        if (!startTag.contains(tmpstr)) {
                            throw new SyntaxException("Expected to find closing tag '&lt;/" + startTag + "...&gt;' but instead found '&lt;/'"
                                    + (ch == '!' ? '!' : "") + tmpstr + "...&gt;'.");
                        }
                        ctx.endSpecial(env, tmpstr);
                    } else {
                        tmpstr = parser.readToken();
                        if (!startTag.equals(tmpstr)) {
                            throw new SyntaxException(
                                    "Expected to find closing tag '&lt;/" + startTag + "...&gt;' but instead found '&lt;/'" + tmpstr + "...&gt;'.");
                        }
                        ctx.endComponent(env, tmpstr);
                    }
                    finished = true; // Indicate done with this context
                    parser.skipCommentsAndWhiteSpace( // Skip white space
                            TemplateParser.SIMPLE_WHITE_SPACE);
                    ch = parser.nextChar(); // Throw away '>' character
                    if (ch != '>') {
                        throw new SyntaxException("While processing closing tag '&lt;/" + tmpstr + "...' expected to encounter closing '&gt;' but" + " found '"
                                + (char) ch + "' instead!");
                    }
                } else if (ch == '!') {
                    // We have a reserved tag...
                    tmpstr = parser.readToken();
                    pushTag("!" + tmpstr);
                    ctx.beginSpecial(env, tmpstr);
                } else {
                    // Open tag
                    parser.unread(ch);
                    tmpstr = parser.readToken();
                    if (tmpstr.equals("f:verbatim")) {
                        parser.skipCommentsAndWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE);
                        parser.nextChar(); // Get rid of '>'
                        tmpstr = parser.readUntil("</f:verbatim>", false);
                        tmpstr = tmpstr.substring(0, tmpstr.length() - "</f:verbatim>".length());
                        ctx.staticText(env, tmpstr);
                    } else if (tmpstr.equals("ui:event")) {
                        // Hard-code special case for event...
// FIXME: Should also support substituting ! for a custom prefix
                        pushTag("!" + tmpstr); // Mark as special
                        // Must pass in "event" to get correct behavior
                        ctx.beginSpecial(env, "event");
                    } else if (tmpstr.equals("handler")) {
                        // Hard-code special case for handler...
// FIXME: Should also support substituting ! for a custom prefix
                        pushTag("!" + tmpstr); // Mark as special
                        // Must pass in "handler" to get correct behavior
                        ctx.beginSpecial(env, "handler");
                    } else {
                        pushTag(tmpstr);
                        ctx.beginComponent(env, tmpstr);
                    }
                }
                break;
            case '\'':
                // Escape HTML
                ctx.escapedStaticText(env, parser.readLine());
                break;
            case '"':
                // Write output directly to stdout
                ctx.staticText(env, parser.readLine());
                break;
            default:
                parser.unread(ch);
                ctx.handleDefault(env, null);
            }
            if (finished) {
                // Done w/ this context...
                return parent;
            }
            parser.skipCommentsAndWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE);
            ch = parser.nextChar();
        }

        // Return the LayoutElement
        return parent;
    }

    /**
     * <p>
     * This method is responsible for parsing and creating a {@link LayoutComponent}.
     * </p>
     *
     * @param parent The parent {@link LayoutElement}.
     * @param nested <code>true</code> if nested inside another {@link LayoutComponent}.
     * @param type The type of component to create.
     */
    public LayoutComponent createLayoutComponent(LayoutElement parent, boolean nested, String type) throws IOException {
        // Ensure type is defined
        ComponentType componentType = LayoutDefinitionManager.getGlobalComponentType(null, type);
        if (componentType == null) {
            // Look for local mapping...
            String mappedType = getMappedType(type);
            if (mappedType != null) {
                componentType = LayoutDefinitionManager.getGlobalComponentType(null, mappedType);
            }
            if (componentType == null) {
                // Still not found...
                throw new IllegalArgumentException("ComponentType '" + type + "' not defined!");
            }
        }

        // Get the NVPs
        List<NameValuePair> nvps = readNameValuePairs(type, null, true);

        // Check to see if this is a single tag (start and close)
        boolean single = false;
        TemplateParser parser = getTemplateParser();
        int ch = parser.nextChar();
        if (ch == '/') {
            single = true;
        } else {
            parser.unread(ch);
        }

        // Check for id / overwrite attributes
        NameValuePair nvpID = null;
        String id = null;
        NameValuePair overwrite = null;
        for (NameValuePair nvp : nvps) {
            if (nvp.getName().equals(ID_ATTRIBUTE)) {
                // Found id...
                nvpID = nvp;
            } else if (nvp.getName().equals(OVERWRITE_ATTRIBUTE)) {
                // Found overwrite... (must be a String, not an array / List)
                overwrite = nvp;
            }
        }
        if (nvpID != null) {
            id = nvpID.getValue().toString();
            nvps.remove(nvpID);
        }

        // Create the LayoutComponent
        if (id == null) {
            id = LayoutElementUtil.getGeneratedId(type, getNextIdNumber());
        }
        LayoutComponent component = new LayoutComponent(parent, id, componentType);

        // Set Overwrite flag if needed
        if (overwrite != null) {
            nvps.remove(overwrite);
            component.setOverwrite(Boolean.valueOf(overwrite.getValue().toString()).booleanValue());
        }

        // Set options...
        for (NameValuePair np : nvps) {
            component.addOption(np.getName(), np.getValue());
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
        component.setNested(nested);

        // Let calling method see if this is a single tag, or if there should
        // be a closing tag as well
        parser.unread('>');
        if (single) {
            parser.unread('/');
        }

        return component;
    }

    /**
     * <p>
     * Attempt to find the correct component type after applying a locally defined mapping.
     * </p>
     */
    private String getMappedType(String compType) {
        int colonIdx = compType.indexOf(NAMESPACE_SEPARATOR);
        String result = null;
        if (colonIdx != -1) {
            // It appears we have a namespace prefix...
            String newPrefix = getNamespace(compType.substring(0, colonIdx));
            if (newPrefix != null) {
                result = newPrefix + compType.substring(colonIdx);
            }
        }
        return result;
    }

    /**
     * <p>
     * This method attempts to find a mapping for the requested component type.
     * </p>
     */
    public String getNamespace(String compType) {
        return _nsMappings.get(compType);
    }

    /**
     * <p>
     * This method creates a namespace mapping.
     * </p>
     *
     * @param longName The long name (i.e. http://foo/bar/).
     * @param shortName The short name (i.e. foo).
     */
    public void setNamespace(String shortName, String longName) {
        _nsMappings.put(shortName, longName);
    }

    /**
     * <p>
     * This method read all the {@link NameValuePair}s, usually for a component. It assumes the {@link TemplateParser} is
     * ready to start reading the NVPs.
     * </p>
     *
     * @param tagName The name of the tag for which we are reading attributes.
     */
    protected List<NameValuePair> readNameValuePairs(String tagName, String defAttName, boolean requireQuotes) throws IOException {
        // Continue until we find "[/]>".
        List<NameValuePair> nvps = new ArrayList<>();
        int ch = 0;
        TemplateParser parser = getTemplateParser();
        while (ch != -1) {
            parser.skipCommentsAndWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE);
            ch = parser.nextChar();
            if (ch == '>') {
                // We're at the end of the parameters
                break;
            }
            if (ch == '/') {
                parser.skipCommentsAndWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE);
                ch = parser.nextChar();
                if (ch != '>') {
                    throw new SyntaxException("'" + tagName + "' tag contained " + "'/' that was not followed by a '&gt;' character!");
                }

                // We're at the end of the parameters and the component,
                // put this information back into the parser
                parser.unread('/');
                break;
            }
            parser.unread(ch);
// FIXME: An Illegal argument exception may be thrown, in this case the
// FIXME: component name is not available and is hard to find.  Catch this
// FIXME: error here and add more information so the stack trace is readable.
            nvps.add(parser.getNVP(defAttName, requireQuotes));
        }

        // Return the result
        return nvps;
    }

    /**
     * <p>
     * This method creates a new {@link LayoutForEach} {@link LayoutElement}.
     * </p>
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link #FOREACH_ELEMENT} node to extract information from when creating the {@link LayoutForEach}.
     *
     * @return The new {@link LayoutForEach} {@link LayoutElement}. private LayoutElement createLayoutForEach(LayoutElement
     * parent, Node node) { // Pull off attributes... String list = (String) getAttributes(node).get( LIST_ATTRIBUTE); if
     * ((list == null) || (list.trim().equals(""))) { throw new RuntimeException("'" + LIST_ATTRIBUTE + "' attribute not
     * found on '" + FOREACH_ELEMENT + "' Element!"); } String key = (String) getAttributes(node).get( KEY_ATTRIBUTE); if
     * ((key == null) || (key.trim().equals(""))) { throw new RuntimeException("'" + KEY_ATTRIBUTE + "' attribute not found
     * on '" + FOREACH_ELEMENT + "' Element!"); }
     *
     * // Create new LayoutForEach LayoutElement forEachElt = new LayoutForEach(parent, list, key);
     *
     * // Add children... addChildLayoutElements(forEachElt, node);
     *
     * // Return the forEach return forEachElt; }
     */

    /**
     * <p>
     * This method creates a new {@link LayoutWhile} {@link LayoutElement}.
     * </p>
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link #WHILE_ELEMENT} node to extract information from when creating the LayoutWhile.
     *
     * @return The new {@link LayoutWhile} {@link LayoutElement}. private LayoutElement createLayoutWhile(LayoutElement
     * parent, Node node) { // Pull off attributes... String condition = (String) getAttributes(node).get(
     * CONDITION_ATTRIBUTE); if ((condition == null) || (condition.trim().equals(""))) { throw new RuntimeException("'" +
     * CONDITION_ATTRIBUTE + "' attribute not found on '" + WHILE_ELEMENT + "' Element!"); }
     *
     * // Create new LayoutWhile LayoutElement whileElt = new LayoutWhile(parent, condition);
     *
     * // Add children... addChildLayoutElements(whileElt, node);
     *
     * // Return the while return whileElt; }
     */

    /**
     *
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link #ATTRIBUTE_ELEMENT} node to extract information from when creating the {@link LayoutAttribute}
     * private LayoutElement createLayoutAttribute(LayoutElement parent, Node node) { // Pull off attributes... Map
     * attributes = getAttributes(node); String name = (String) attributes.get(NAME_ATTRIBUTE); if ((name == null) ||
     * (name.trim().equals(""))) { throw new RuntimeException("'" + NAME_ATTRIBUTE + "' attribute not found on '" +
     * ATTRIBUTE_ELEMENT + "' Element!"); } LayoutElement attributeElt = null;
     *
     * // Check if we're setting this on a LayoutComponent vs. LayoutMarkup // Do this after checking for "name" to show
     * correct error message LayoutComponent comp = null; if (parent instanceof LayoutComponent) { comp = (LayoutComponent)
     * parent; } else { comp = getParentLayoutComponent(parent); } if (comp != null) { // Treat this as a LayoutComponent
     * "option" instead of "attribute" addOption(comp, node); } else { String value = (String)
     * attributes.get(VALUE_ATTRIBUTE); String property = (String) attributes.get(PROPERTY_ATTRIBUTE);
     *
     * // Create new LayoutAttribute attributeElt = new LayoutAttribute(parent, name, value, property);
     *
     * // Add children... (event children are supported) addChildLayoutElements(attributeElt, node); }
     *
     * // Return the LayoutAttribute (or null if inside LayoutComponent) return attributeElt; }
     */

    /**
     * <p>
     * This method creates a new {@link LayoutMarkup}.
     *
     * @param parent The parent {@link LayoutElement}.
     * @param node The {@link MARKUP_ELEMENT} node to extract information from when creating the {@link LayoutMarkup}.
     * private LayoutElement createLayoutMarkup(LayoutElement parent, Node node) { // Pull off attributes... Map attributes
     * = getAttributes(node); String tag = (String) attributes.get(TAG_ATTRIBUTE); if ((tag == null) ||
     * (tag.trim().equals(""))) { throw new RuntimeException("'" + TAG_ATTRIBUTE + "' attribute not found on '" +
     * MARKUP_ELEMENT + "' Element!"); }
     *
     * // Check to see if this is inside a LayoutComponent, if so, we must // use a LayoutComponent for it to get rendered
     * LayoutElement markupElt = null; if ((parent instanceof LayoutComponent) ||
     * LayoutElementUtil.isNestedLayoutComponent(parent)) { // Make a "markup" LayoutComponent.. ComponentType type =
     * ensureMarkupType(parent); markupElt = new LayoutComponent( parent, MARKUP_ELEMENT + _markupCount++, type);
     * LayoutComponent markupComp = ((LayoutComponent) markupElt); markupComp.addOption("tag", tag);
     * markupComp.setNested(true); markupComp.setFacetChild(false);
     *
     * // Add children... addChildLayoutComponentChildren(markupComp, node); } else { // Create new LayoutMarkup String type
     * = (String) attributes.get(TYPE_ATTRIBUTE); markupElt = new LayoutMarkup(parent, tag, type);
     *
     * // Add children... addChildLayoutElements(markupElt, node); }
     *
     * // Return the LayoutMarkup return markupElt; }
     */

    /**
     * <p>
     * This method removes a tag from the Stack. This should be called outside of <code>TemplateReader</code> when writing
     * {@link ProcessingContext} code and a tag starts and ends in a single tag (i.e. &lt;tag /&gt;). In other cases, it
     * should be handled within the <code>TemplateReader</code>.
     * </p>
     */
    public String popTag() {
        return _tagStack.pop();
    }

    /**
     * <p>
     * This method exists because popTag() does, it likely doesn't have much use outside of <code>TemplateReader</code>.
     * </p>
     */
    public void pushTag(String tag) {
        _tagStack.push(tag);
    }

    /**
     * /**
     * <p>
     * This method checks to see if the tag <code>Stack</code> is empty.
     * </p>
     */
    public boolean isTagStackEmpty() {
        return _tagStack.empty();
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

    //////////////////////////////////////////////////////////////////////
    // Utility Methods
    //////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * This method provides access to registered {@link CustomParserCommand}s.
     * </p>
     */
    public CustomParserCommand getCustomParserCommand(String id) {
        return parserCmds.get(id);
    }

    /**
     * <p>
     * Provides access to the application-scoped Map which stores the parser commands for this application.
     * </p>
     */
    private Map<String, CustomParserCommand> getCustomParserCommandMap(FacesContext ctx) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        Map<String, CustomParserCommand> commandMap = null;
        if (ctx != null) {
            commandMap = (Map<String, CustomParserCommand>) ctx.getExternalContext().getApplicationMap().get(PARSER_COMMANDS);
        }
        if (commandMap == null) {
            // 1st time... initialize it
            commandMap = initCustomParserCommands();
            if (ctx != null) {
                ctx.getExternalContext().getApplicationMap().put(PARSER_COMMANDS, commandMap);
            }
        }

        // Return the map...
        return commandMap;
    }

    /**
     * <p>
     * This method allows you to set a {@link CustomParserCommand}.
     * </p>
     */
    public void setCustomParserCommand(String id, CustomParserCommand command) {
        // Shared application-scope map, but not synchronized!
        parserCmds.put(id, command);
    }

    /**
     * <p>
     * This method initializes the {@link CustomParserCommand}s.
     * </p>
     */
    protected Map<String, CustomParserCommand> initCustomParserCommands() {
// FIXME: Do initialization via @annotations??
        Map<String, CustomParserCommand> map = new HashMap<>();
        map.put("if", new IfParserCommand());
        map.put("while", new WhileParserCommand());
        map.put("foreach", new ForeachParserCommand());
        map.put("facet", new FacetParserCommand());
        map.put("composition", new CompositionParserCommand(true, TEMPLATE_ATTRIBUTE));
        map.put("include", new CompositionParserCommand(false, SRC_ATTRIBUTE));
        map.put("decorate", new CompositionParserCommand(false, TEMPLATE_ATTRIBUTE));
        map.put("insert", new InsertParserCommand());
        map.put("namespace", new NamespaceParserCommand());
        map.put("event", EVENT_PARSER_COMMAND);
        return map;
    }

    //////////////////////////////////////////////////////////////////////
    // Inner Classes
    //////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * This is the {@link ProcessingContext} for the {@link LayoutDefinition}.
     * </p>
     */
    protected static class LayoutDefinitionContext extends BaseProcessingContext {
    }

    /**
     * <p>
     * This is the {@link ProcessingContext} for {@link LayoutIf}s.
     * </p>
     */
    protected static class LayoutIfContext extends BaseProcessingContext {
    }

    /**
     * <p>
     * This is the {@link ProcessingContext} for {@link LayoutForEach}es.
     * </p>
     */
    protected static class LayoutForEachContext extends BaseProcessingContext {
    }

    /**
     * <p>
     * This is the {@link ProcessingContext} for {@link LayoutWhile}s.
     * </p>
     */
    protected static class LayoutWhileContext extends BaseProcessingContext {
    }

    /**
     * <p>
     * This is the {@link ProcessingContext} for {@link LayoutComponent}s.
     * </p>
     */
    protected static class LayoutComponentContext extends BaseProcessingContext {

        /**
         * <p>
         * This method is invoked when nothing else matches.
         * </p>
         *
         * <p>
         * This implementation uses this to store "body content". This content is used as the <code>value</code> of the
         * component. If a value is already set, then this content will be ignored.
         * </p>
         */
        @Override
        public void handleDefault(ProcessingContextEnvironment env, String content) throws IOException {
            TemplateParser parser = env.getReader().getTemplateParser();
// FIXME: **ignore comments and allow escaping**
// Store body content in env until end component, set as 'value' if value is not set?
            parser.readUntil('<', true);
            parser.unread('<');
        }

        /**
         *
         */
        @Override
        public void beginSpecial(ProcessingContextEnvironment env, String content) throws IOException {
            super.beginSpecial(env, content);
        }
    }

    /**
     * <p>
     * This is the {@link ProcessingContext} for {@link LayoutFacet}s.
     * </p>
     */
    protected static class LayoutFacetContext extends BaseProcessingContext {
// FIXME: May want to do some special processing to ensure a single
// FIXME: UIComponent is used, create a panel group if not.
    }

    /**
     * <p>
     * This {@link CustomParserCommand} handles "if" statements. To obtain the condition, it simply reads until it finds
     * '&gt;'. This means '&gt;' must be escaped if it appears in the condition.
     * </p>
     */
    public static class IfParserCommand implements CustomParserCommand {
        @Override
        public void process(ProcessingContext ctx, ProcessingContextEnvironment env, String name) throws IOException {
            // Get the condition for this if statement. We simply read until
            // we find '>'. This means '>' must be escaped if it appears in
            // the condition.
            TemplateReader reader = env.getReader();
            TemplateParser parser = reader.getTemplateParser();
            String condition = parser.readUntil('>', false).trim();

            // Create new LayoutIf
            LayoutElement parent = env.getParent();
// FIXME: the 'if' below checks to see if 'condition' ends with '/', yet I don't see this code removing the '/'... isn't that a problem?  Test and fix!
            LayoutElement ifElt = new LayoutIf(parent, condition);
            parent.addChildLayoutElement(ifElt);

            if (condition.endsWith("/")) {
                reader.popTag(); // Don't look for end tag
            } else {
                // Process child LayoutElements (recurse)
                reader.process(TemplateReader.LAYOUT_IF_CONTEXT, ifElt, LayoutElementUtil.isLayoutComponentChild(ifElt));
            }
        }
    }

    /**
     * <p>
     * This {@link CustomParserCommand} handles "while" statements. To obtain the condition, it simply reads until it finds
     * '&gt;'. This means '&gt;' must be escaped if it appears in the condition.
     * </p>
     */
    public static class WhileParserCommand extends IfParserCommand {
        @Override
        public void process(ProcessingContext ctx, ProcessingContextEnvironment env, String name) throws IOException {
            // Get the condition for this while statement. We simply read
            // until we find '>'. This means '>' must be escaped if it
            // appears in the condition.
            TemplateReader reader = env.getReader();
            TemplateParser parser = reader.getTemplateParser();
            String condition = parser.readUntil('>', false).trim();

            // Create new LayoutWhile
            LayoutElement parent = env.getParent();
// FIXME: Support condition="..."
            LayoutElement elt = new LayoutWhile(parent, condition);
            parent.addChildLayoutElement(elt);

            if (condition.endsWith("/")) {
                reader.popTag(); // Don't look for end tag
            } else {
                // Process child LayoutElements (recurse)
                reader.process(TemplateReader.LAYOUT_WHILE_CONTEXT, elt, LayoutElementUtil.isLayoutComponentChild(elt));
            }
        }
    }

    /**
     * <p>
     * This {@link CustomParserCommand} handles "foreach" statements.
     * </p>
     *
     * <p>
     * The syntax must look like:
     * </p>
     *
     * <code>
     *        &lt;!foreach key : $something{something}>
     *        ...
     *        &lt;/!foreach>
     *    </code>
     *
     * <p>
     * The "key" is a <code>String</code> that will be used to store each <code>Object</code> in a request attribute on each
     * iteration. $something{something} must resolve to a <code>List</code>. It may also be in the form
     * <code>#{value.binding}</code> if you prefer, in either case it must resolve to a <code>List</code>.
     * </p>
     */
    public static class ForeachParserCommand implements CustomParserCommand {
        @Override
        public void process(ProcessingContext ctx, ProcessingContextEnvironment env, String name) throws IOException {
            TemplateReader reader = env.getReader();
            TemplateParser parser = reader.getTemplateParser();

            // First get the key
// FIXME: Don't do it this way... read until the '>' first then split the String.  This will allow detecting the missing ':' much more easily.
// FIXME: Consider allowing #{} expressions -- they're interpretted as comments as written... fix this.
            String key = parser.readUntil(':', true).trim();
            String listExp = parser.readUntil('>', true).trim();

            // Create new LayoutForEach
            LayoutElement parent = env.getParent();
            LayoutElement elt = new LayoutForEach(parent, listExp, key);
            parent.addChildLayoutElement(elt);

            if (listExp.endsWith("/")) {
                reader.popTag(); // Don't look for end tag
            } else {
                // Process child LayoutElements (recurse)
                reader.process(TemplateReader.LAYOUT_FOREACH_CONTEXT, elt, LayoutElementUtil.isLayoutComponentChild(elt));
            }
        }
    }

    /**
     * <p>
     * This {@link CustomParserCommand} handles "facets".
     * </p>
     */
    public static class FacetParserCommand implements CustomParserCommand {
        @Override
        public void process(ProcessingContext ctx, ProcessingContextEnvironment env, String name) throws IOException {
            // Get the facet id
            TemplateReader reader = env.getReader();
            TemplateParser parser = reader.getTemplateParser();
            String id = parser.readUntil('>', true).trim();

            // Check to see if this is a single tag
            boolean singleTag = false;
            if (id.endsWith("/")) {
                reader.popTag(); // Don't look for end tag
                id = id.substring(0, id.length() - 1).trim();
                singleTag = true;
            }

            // Check to see if the 'id' is wrapped in quotes.
            char first = id.charAt(0);
            if (first == '"' || first == '\'') {
                if (id.indexOf('>') != -1) {
                    // Means we didn't have an ending quote before '>'
                    throw new SyntaxException("Unable to find ending (" + first + ") on !facet declaration with id (" + id.substring(0, id.indexOf('>'))
                            + ") on component (" + env.getParent().getUnevaluatedId() + ").");
                }
                id = id.substring(1, id.length() - 1).trim();
            }

            // Create new LayoutFacet
            LayoutElement parent = env.getParent();
            LayoutFacet facetElt = new LayoutFacet(parent, id);
            parent.addChildLayoutElement(facetElt);

            // Determine if this is a facet place holder (i.e. we're defining
            // a renderer w/ a facet), or if it is a facet value to set on a
            // containing component.
            boolean isRendered = !LayoutElementUtil.isNestedLayoutComponent(facetElt);
            facetElt.setRendered(isRendered);
            if (singleTag && !isRendered && LogUtil.configEnabled()) {
                LogUtil.config(this, "Facet (" + id + ") specified, however, there is no component specified " + "inside this facet.  Nothing will happen.");
            }

            // Process child LayoutElements (recurse)
            reader.process(TemplateReader.LAYOUT_FACET_CONTEXT, facetElt, LayoutElementUtil.isLayoutComponentChild(facetElt));
        }
    }

    //////////////////////////////////////////////////////////////////////
    // Constants
    //////////////////////////////////////////////////////////////////////

    public static final String FACET_ELEMENT = "facet";
    public static final String FOREACH_ELEMENT = "foreach";
    public static final String IF_ELEMENT = "if";
    public static final String LIST_ELEMENT = "list";
    public static final String MARKUP_ELEMENT = "markup";
    public static final String WHILE_ELEMENT = "while";

    public static final String ID_ATTRIBUTE = "id";
    public static final String OVERWRITE_ATTRIBUTE = "overwrite";
    public static final String TEMPLATE_ATTRIBUTE = "template";
    public static final String SRC_ATTRIBUTE = "src";
    public static final char NAMESPACE_SEPARATOR = ':';

    public static final ProcessingContext LAYOUT_DEFINITION_CONTEXT = new LayoutDefinitionContext();

    public static final ProcessingContext LAYOUT_COMPONENT_CONTEXT = new LayoutComponentContext();

    public static final ProcessingContext LAYOUT_FACET_CONTEXT = new LayoutFacetContext();

    public static final ProcessingContext LAYOUT_IF_CONTEXT = new LayoutIfContext();

    public static final ProcessingContext LAYOUT_FOREACH_CONTEXT = new LayoutForEachContext();

    public static final ProcessingContext LAYOUT_WHILE_CONTEXT = new LayoutWhileContext();

    public static final CustomParserCommand EVENT_PARSER_COMMAND = new EventParserCommand();

    private Map<String, CustomParserCommand> parserCmds = getCustomParserCommandMap(null);

    private static final String PARSER_COMMANDS = "__jsft_CustParserCMDs";

    /**
     * <p>
     * This <code>Stack</code> keep track of the nesting.
     * </p>
     */
    private Stack<String> _tagStack = new Stack<>();
    private Map<String, String> _nsMappings = new HashMap<>();

    private TemplateParser _tpl = null;
    private int _idNumber;
    private String _id = null; // The id of the LayoutDefinition
}
