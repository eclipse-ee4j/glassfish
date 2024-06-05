/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.DTDRegistry;
import com.sun.enterprise.deployment.xml.TagNames;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;


/**
 * This class implements all the callbacks for the SAX Parser in JAXP 1.1
 *
 * @author Jerome Dochez
 */
@Service
@PerLookup
public class SaxParserHandler extends DefaultHandler {

    private static final Logger LOG = DOLUtils.getDefaultLogger();
    private static final MappingStuff mappingStuff = new MappingStuff();

    private final List<XMLNode<?>> nodes = new ArrayList<>();
    private XMLNode<?> topNode;
    // FIXME: Used as a local variable
    protected String publicID;
    private StringBuffer elementData;
    private Map<String, String> prefixMapping;

    private boolean stopOnXMLErrors;

    private boolean pushedNamespaceContext;
    private final NamespaceSupport namespaces = new NamespaceSupport();
    private final Stack<String> elementStack = new Stack<>();

    private String rootElement;

    private List<VersionUpgrade> versionUpgradeList;

    private boolean doDelete;
    private String errorReportingString = "";


    protected static Map<String,String> getMapping() {
        return mappingStuff.mMapping;
    }

    protected static List<VersionUpgrade> getVersionUpgrades(String key) {
        List<VersionUpgrade> versionUpgradeList = mappingStuff.mVersionUpgrades.get(key);
        if (versionUpgradeList != null) {
            return versionUpgradeList;
        }
        List<Class<?>> classList = mappingStuff.mVersionUpgradeClasses.get(key);
        if (classList == null) {
            return null;
        }
        versionUpgradeList = new ArrayList<>();
        for (int n = 0; n < classList.size(); ++n) {
            VersionUpgrade versionUpgrade = null;
            try {
                versionUpgrade = (VersionUpgrade) classList.get(n).getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
            }
            if (versionUpgrade != null) {
                versionUpgradeList.add(versionUpgrade);
            }
        }
        mappingStuff.mVersionUpgrades.put(key, versionUpgradeList);
        return versionUpgradeList;
    }

    protected static Collection<String> getElementsAllowingEmptyValues() {
        return mappingStuff.mElementsAllowingEmptyValues;
    }

    protected static Collection<String> getElementsPreservingWhiteSpace() {
        return mappingStuff.mElementsPreservingWhiteSpace;
    }

    public static void registerBundleNode(BundleNode bundleNode, String bundleTagName) {
        /*
        * There is exactly one standard node object for each descriptor type.
        * The node's registerBundle method itself adds the publicID-to-DTD
        * entry to the mapping.  This method needs to add the tag-to-node class
        * entry to the rootNodes map.
        */
        if (mappingStuff.mBundleRegistrationStatus.containsKey(bundleTagName)) {
            LOG.log(Level.FINEST, "Mapping already contains entry for tag {0}, returning.", bundleTagName);
            return;
        }

        final Map<String, String> dtdMapping = new HashMap<>();
        final Map<String, List<Class<?>>> versionUpgrades = new HashMap<>();

        String rootNodeKey = bundleNode.registerBundle(dtdMapping);
        mappingStuff.mRootNodesMutable.putIfAbsent(rootNodeKey, bundleNode.getClass());

        /*
        * There can be multiple runtime nodes (for example, sun-xxx and
        * glassfish-xxx).  So the BundleNode's registerRuntimeBundle
        * updates the publicID-to-DTD map and returns a map of tags to
        * runtime node classes.
        */
        mappingStuff.mRootNodesMutable.putAll(bundleNode.registerRuntimeBundle(dtdMapping, versionUpgrades));

        mappingStuff.mVersionUpgradeClasses.putAll(versionUpgrades);

        // let's remove the URL from the DTD so we use local copies...
        for (Map.Entry<String, String> entry : dtdMapping.entrySet()) {
            final String publicID = entry.getKey();
            final String dtd = entry.getValue();
            String systemIDResolution = resolvePublicID(publicID, dtd);
            if (systemIDResolution == null) {
                mappingStuff.mMapping.put(publicID, dtd.substring(dtd.lastIndexOf('/') + 1));
            } else {
                mappingStuff.mMapping.put(publicID, systemIDResolution);
            }
        }
        LOG.log(Level.FINER, "Final mapping keys for root node key {0}:\n {1}",
            new Object[] {rootNodeKey, mappingStuff.mMapping.keySet()});

        // This node might know of elements which should permit empty values,
        // or elements for which we should preserve white space.  Track them.
        Collection<String> c = bundleNode.elementsAllowingEmptyValue();
        if (!c.isEmpty()) {
            mappingStuff.mElementsAllowingEmptyValuesMutable.addAll(c);
        }

        c = bundleNode.elementsPreservingWhiteSpace();
        if (!c.isEmpty()) {
            mappingStuff.mElementsPreservingWhiteSpaceMutable.addAll(c);
        }

        mappingStuff.mBundleRegistrationStatus.put(rootNodeKey, Boolean.TRUE);
    }

    // It creates the InputSource
    @SuppressWarnings("resource")
    @Override
    public InputSource resolveEntity(String publicID, String systemID) throws SAXException {
        try {
            LOG.log(Level.FINEST, "resolveEntity, publicID={0}, systemID={1}", new Object[] {publicID, systemID});
            // If public ID is there and is present in our map, use it
            if (publicID != null && getMapping().containsKey(publicID)) {
                this.publicID = publicID;
                return new InputSource(new BufferedInputStream(getDTDUrlFor(getMapping().get(publicID))));
            }
            // In case invalid public ID is given (or) public ID is null, use system ID to resolve
            // unspecified schema
            if (systemID == null || systemID.lastIndexOf('/') == systemID.length()) {
                return null;
            }

            String namespaceResolution = resolveSchemaNamespace(systemID);
            final String fileName;
            if (namespaceResolution == null) {
                fileName = getSchemaURLFor(systemID.substring(systemID.lastIndexOf('/') + 1));
            } else {
                fileName = getSchemaURLFor(namespaceResolution);
            }
            // if this is not a request for a schema located in our repository, we fail the
            // deployment
            if (fileName == null) {
                throw new SAXException("Requested schema " + systemID + " is not found in local repository, please"
                    + " ensure that there are no typos in the XML namespace declaration.");
            }
            LOG.log(Level.FINE, "Resolved publicID={0} and systemID={1} to {2}",
                new Object[] {publicID, systemID, fileName});
            return new InputSource(fileName);
        } catch (SAXException e) {
            throw e;
        } catch (Exception ioe) {
            throw new SAXException(ioe);
        }
    }

    /**
     * Sets if the parser should stop parsing and generate an SAXPArseException
     * when the xml parsed contains errors in regards to validation
     */
    public void setStopOnError(boolean stop) {
        stopOnXMLErrors = stop;
    }


    @Override
    public void error(SAXParseException spe) throws SAXParseException {
        LOG.log(Level.SEVERE, DOLUtils.INVALILD_DESCRIPTOR_LONG,
            new Object[] {errorReportingString, String.valueOf(spe.getLineNumber()),
                String.valueOf(spe.getColumnNumber()), spe.getLocalizedMessage()});
        if (stopOnXMLErrors) {
            throw spe;
        }
    }


    @Override
    public void fatalError(SAXParseException spe) throws SAXParseException {
        LOG.log(Level.SEVERE, DOLUtils.INVALILD_DESCRIPTOR_LONG,
            new Object[] {errorReportingString, String.valueOf(spe.getLineNumber()),
                String.valueOf(spe.getColumnNumber()), spe.getLocalizedMessage()});
        if (stopOnXMLErrors) {
            throw spe;
        }
    }

    /**
     * @return the input stream for a DTD public ID
     */
     protected InputStream getDTDUrlFor(String dtdFileName) {
        String dtdLoc = DTDRegistry.DTD_LOCATION.replace('/', File.separatorChar);
        File f = new File(dtdLoc + File.separatorChar + dtdFileName);
        try {
            return new BufferedInputStream(new FileInputStream(f));
        } catch(FileNotFoundException fnfe) {
            LOG.fine("Cannot find DTD " + dtdFileName);
            return null;
        }
     }

    /**
     * @return an URL for the schema location for a schema indentified by the
     * passed parameter
     * @param schemaSystemID the system id for the schema
     */
    public static String getSchemaURLFor(String schemaSystemID) throws IOException {
        File f = getSchemaFileFor(schemaSystemID);
        if (f == null) {
            return null;
        }
        return f.toURI().toURL().toString();
    }

    /**
     * @return a File pointer to the localtion of the schema indentified by the
     * passed parameter
     * @param schemaSystemID the system id for the schema
     */
    public static File getSchemaFileFor(String schemaSystemID) throws IOException {
        LOG.log(Level.FINE, "Getting Schema {0}", schemaSystemID);
        String schemaLoc = DTDRegistry.SCHEMA_LOCATION.replace('/', File.separatorChar);
        File f = new File(schemaLoc + File.separatorChar + schemaSystemID);
        if (f.exists()) {
            return f;
        }
        LOG.log(Level.INFO, "Cannot find the schema file {0}", f);
        return null;
    }


    /**
     * Determine whether the syatemID starts with a known namespace.
     * If so, strip off that namespace and return the rest.
     * Otherwise, return null
     * @param systemID The systemID to examine
     * @return the part if the namespace to find in the file system
     * or null if the systemID does not start with a known namespace
     */
    public static String resolveSchemaNamespace(String systemID) {
        List<String> namespaces = DOLUtils.getProprietarySchemaNamespaces();
        for (String namespace : namespaces) {
            if (systemID.startsWith(namespace)) {
                return systemID.substring(namespace.length());
            }
        }
        return null;
    }

    /**
     * Determine whether the publicID starts with a known proprietary value.
     * If so, strip off that value and return the rest.
     * Otherwise, return null
     * @param publicID The publicID to examine
     * @return the part if the namespace to find in the file system
     * or null if the publicID does not start with a known namespace
     */
    public static String resolvePublicID(String publicID, String dtd) {
        List<String> dtdStarts = DOLUtils.getProprietaryDTDStart();
        for (String dtdStart : dtdStarts) {
            if (dtd.startsWith(dtdStart)) {
                return dtd.substring(dtdStart.length());
            }
        }
        return null;
    }

    @Override
    public void notationDecl(java.lang.String name,
                         java.lang.String publicId,
                         java.lang.String systemId)
                         throws SAXException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Received notation " + name + " :=: " + publicId + " :=: " + systemId);
        }
    }


    @Override
    public void startPrefixMapping(String prefix,
                               String uri)
                        throws SAXException {

        if (prefixMapping==null) {
            prefixMapping = new HashMap<>();
        }

        // We need one namespace context per element, but any prefix mapping
        // callbacks occur *before* startElement is called.  So, push a
        // context on the first startPrefixMapping callback per element.
        if (!pushedNamespaceContext) {
            namespaces.pushContext();
            pushedNamespaceContext = true;
        }
        namespaces.declarePrefix(prefix,uri);
        prefixMapping.put(prefix, uri);
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        LOG.log(Level.FINEST, "startElement(qName={0})", qName);
        if (!pushedNamespaceContext) {
            // We need one namespae context per element, so push a context
            // if there weren't any prefix mappings defined.
            namespaces.pushContext();
        }
        // Always reset flag since next callback could be startPrefixMapping
        // OR another startElement.
        pushedNamespaceContext = false;

        doDelete = false;
        String lastElement = null;
        try {
            lastElement = elementStack.pop();
        } catch (EmptyStackException ex) {
        }
        if (lastElement == null) {
            rootElement = localName;
            versionUpgradeList = getVersionUpgrades(rootElement);
            if (versionUpgradeList != null) {
                for (VersionUpgrade versionUpgrade : versionUpgradeList) {
                    versionUpgrade.init();
                }
            }
            elementStack.push(localName);
        } else {
            lastElement += "/" + localName;
            elementStack.push(lastElement);
        }

        if (versionUpgradeList != null) {
            for (VersionUpgrade versionUpgrade : versionUpgradeList) {
                if (VersionUpgrade.UpgradeType.REMOVE_ELEMENT == versionUpgrade.getUpgradeType()) {
                    Map<String, String> matchXPath = versionUpgrade.getMatchXPath();
                    int entriesMatched = 0;
                    for (Map.Entry<String, String> entry : matchXPath.entrySet()) {
                        if (entry.getKey().equals(lastElement)) {
                            entry.setValue(elementData.toString());
                            ++entriesMatched;
                        }
                    }
                    if (entriesMatched == matchXPath.size()) {
                        doDelete = true;
                        break;
                    }
                }
            }
        }

        LOG.finer(() -> "Start of element with qName=" + qName + ", localName=" + localName + ", uri=" + uri);
        XMLNode<?> node = null;
        elementData = new StringBuffer();

        if (nodes.isEmpty()) {
            // this must be a root element...
            Class<?> rootNodeClass = mappingStuff.mRootNodes.get(localName);
            if (rootNodeClass == null) {
                LOG.log(Level.SEVERE, "The " + localName + " is not supported!");
                if (stopOnXMLErrors) {
                    throw new IllegalArgumentException(
                        errorReportingString + " Element [" + localName + "] is not a valid root element");
                }
            } else {
                try {
                    node = (XMLNode<?>) rootNodeClass.getDeclaredConstructor().newInstance();
                    LOG.log(Level.FINE, "Instantiating {0}", node);
                    if (node instanceof RootXMLNode) {
                        if (publicID != null) {
                            ((RootXMLNode) node).setDocType(publicID);
                        }
                        addPrefixMapping(node);
                    }
                    nodes.add(node);
                    topNode = node;
                    node.getDescriptor();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error occurred", e);
                    return;
                }
            }
        } else {
            node = nodes.get(nodes.size() - 1);
        }
        if (node != null) {
            XMLElement element = new XMLElement(qName, namespaces);
            if (node.handlesElement(element)) {
                node.startElement(element, attributes);
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Asking for new handler for " + element + " to " + node);
                }
                XMLNode<?> newNode = node.getHandlerFor(element);
                LOG.log(Level.FINE, "Got new node: {0}", newNode);
                nodes.add(newNode);
                addPrefixMapping(newNode);
                newNode.startElement(element, attributes);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {

        String lastElement = null;
        try {
            lastElement = elementStack.peek();
        } catch (EmptyStackException ex) {
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("End of element with qName=" + qName + ", localName=" + localName + ", uri=" + uri + " and value="
                + elementData);
        }
        if (nodes.isEmpty()) {
            // no more nodes to pop
            elementData = null;
            return;
        }
        XMLElement element = new XMLElement(qName, namespaces);
        XMLNode<?> topNode = nodes.get(nodes.size() - 1);
        boolean ignoredBecauseEmpty = elementData != null && elementData.isEmpty()
            && !allowsEmptyValue(element.getQName());
        if (elementData != null && ignoredBecauseEmpty) {
            LOG.fine(() -> "Ignoring empty element with qName=" + qName);
        }
        if (elementData != null && !ignoredBecauseEmpty) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("For element " + element.getQName() + " and value " + elementData);
            }
            boolean doReplace = false;
            String replacementName = null;
            String replacementValue = null;
            if (versionUpgradeList != null) {
                for (VersionUpgrade versionUpgrade : versionUpgradeList) {
                    if (VersionUpgrade.UpgradeType.REPLACE_ELEMENT == versionUpgrade.getUpgradeType()) {
                        Map<String, String> matchXPath = versionUpgrade.getMatchXPath();
                        int entriesMatched = 0;
                        for (Map.Entry<String, String> entry : matchXPath.entrySet()) {
                            if (entry.getKey().equals(lastElement)) {
                                entry.setValue(elementData.toString());
                                ++entriesMatched;
                            }
                        }
                        if (entriesMatched == matchXPath.size()) {
                            if (versionUpgrade.isValid()) {
                                doReplace = true;
                                replacementName = versionUpgrade.getReplacementElementName();
                                replacementValue = versionUpgrade.getReplacementElementValue();
                            } else {
                                StringBuilder buf = new StringBuilder();
                                buf.append("Invalid upgrade from <");
                                for (Map.Entry<String, String> entry : matchXPath.entrySet()) {
                                    buf.append(entry.getKey()).append("  ").append(entry.getValue()).append(" >");
                                }
                                LOG.log(Level.SEVERE, buf.toString());
                                // Since the elements are not replaced,
                                // there should be a parsing error
                            }
                            break;
                        }
                    }
                }
            }
            if (doReplace) {
                element = new XMLElement(replacementName, namespaces);
                topNode.setElementValue(element, replacementValue);
            } else if (doDelete) {
                // don't set a value so that the element is not written out
            } else if (getElementsPreservingWhiteSpace().contains(element.getQName())) {
                topNode.setElementValue(element, elementData.toString());
            } else if (element.getQName().equals(TagNames.ENVIRONMENT_PROPERTY_VALUE)) {
                Object envEntryDesc = topNode.getDescriptor();
                if (envEntryDesc instanceof EnvironmentProperty) {
                    EnvironmentProperty envProp = (EnvironmentProperty) envEntryDesc;
                    // we need to preserve white space for env-entry-value
                    // if the env-entry-type is java.lang.String or
                    // java.lang.Character
                    if (String.class.getName().equals(envProp.getType())
                        || Character.class.getName().equals(envProp.getType())) {
                        topNode.setElementValue(element, elementData.toString());
                    } else {
                        topNode.setElementValue(element, elementData.toString().trim());
                    }
                } else {
                    topNode.setElementValue(element, elementData.toString().trim());
                }
            } else {
                // Allow any case for true/false & convert to lower case
                String val = elementData.toString().trim();
                if ("true".equalsIgnoreCase(val)) {
                    topNode.setElementValue(element, val.toLowerCase(Locale.US));
                } else if ("false".equalsIgnoreCase(val)) {
                    topNode.setElementValue(element, val.toLowerCase(Locale.US));
                } else {
                    topNode.setElementValue(element, val);
                }
            }
            elementData = null;
        }
        if (topNode.endElement(element)) {
            LOG.log(Level.FINE, "Removing top node {0}", topNode);
            nodes.remove(nodes.size()-1);
        }

        namespaces.popContext();
        pushedNamespaceContext=false;

        try {
            lastElement = elementStack.pop();
        } catch (EmptyStackException ex) {
        }
        if (lastElement != null) {
            if (lastElement.lastIndexOf("/") >= 0) {
                lastElement = lastElement.substring(0, lastElement.lastIndexOf("/"));
                elementStack.push(lastElement);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int stop) {
        if (elementData!=null) {
            elementData = elementData.append(ch,start, stop);
        }
    }

    public XMLNode getTopNode() {
        return topNode;
    }

    public void setTopNode(XMLNode node) {
        topNode = node;
        nodes.add(node);
    }

    private void addPrefixMapping(XMLNode node) {
        if (prefixMapping != null) {
            for (Map.Entry<String, String> entry : prefixMapping.entrySet()) {
                node.addPrefixMapping(entry.getKey(), entry.getValue());
            }
            prefixMapping = null;
        }
    }

    /**
     * Sets the error reporting context string
     */
    public void setErrorReportingString(String errorReportingString) {
        this.errorReportingString = errorReportingString;
    }

    /**
     * Indicates whether the element name is one for which empty values should
     * be recorded.
     * <p>
     * If there were many tags that support empty values, it might make sense to
     * have a constant list that contains all those tag names.  Then this method
     * would search the list for the target elementName.  Because this code
     * is potentially invoked for many elements that do not support empty values,
     * and because the list is very small at the moment, the current
     * implementation uses an inelegant but fast equals test.
     * <p>
     * If the set of tags that should support empty values grows a little,
     * extending the expression to
     *
     * elementName.equals(TAG_1) || elementName.equals(TAG_2) || ...
     *
     * might make sense.  If the set of such tags grows sufficiently large, then
     * a list-based approach might make more sense even though it might prove
     * to be slower.
     * @param elementName the name of the element
     * @return boolean indicating whether empty values should be recorded for this element
     */
    private boolean allowsEmptyValue(String elementName) {
        return getElementsAllowingEmptyValues().contains(elementName);
    }


    private static final class MappingStuff {

        public final ConcurrentMap<String, Boolean> mBundleRegistrationStatus = new ConcurrentHashMap<>();
        public final ConcurrentMap<String, String> mMapping = new ConcurrentHashMap<>();

        private final ConcurrentMap<String, Class<?>> mRootNodesMutable;
        public final Map<String, Class<?>> mRootNodes;

        private final CopyOnWriteArraySet<String> mElementsAllowingEmptyValuesMutable;
        public final Collection<String> mElementsAllowingEmptyValues;

        private final CopyOnWriteArraySet<String> mElementsPreservingWhiteSpaceMutable;
        public final Collection<String> mElementsPreservingWhiteSpace;
        private final Map<String, List<Class<?>>> mVersionUpgradeClasses;
        private final Map<String, List<VersionUpgrade>> mVersionUpgrades;

        MappingStuff() {
            mRootNodesMutable = new ConcurrentHashMap<>();
            mRootNodes = Collections.unmodifiableMap(mRootNodesMutable);

            mElementsAllowingEmptyValuesMutable = new CopyOnWriteArraySet<>();
            mElementsAllowingEmptyValues = Collections.unmodifiableSet(mElementsAllowingEmptyValuesMutable);

            mElementsPreservingWhiteSpaceMutable = new CopyOnWriteArraySet<>();
            mElementsPreservingWhiteSpace = Collections.unmodifiableSet(mElementsPreservingWhiteSpaceMutable);
            mVersionUpgradeClasses = new ConcurrentHashMap<>();
            mVersionUpgrades = new ConcurrentHashMap<>();
        }
    }}
