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

package org.glassfish.appclient.server.core.jws;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * XPath-based logic to combine generated JNLP elements with the developer-
 * provided JNLP.
 * <p>
 * Three types of combinations:
 * <ul>
 * <li>owned - the generated content overrides the developer's (we "own" the content)
 * <li>merged - the generated content is merged in with the developer's content
 * <li>defaulted - the generated content is used only if no corresponding developer content exists
 * </ul>
 * <p>
 * This is the abstract superclass for the various types of combinations of
 * generated and developer-provided elements.
 * <p>
 * The client-jnlp-config.properties file contains properties which define
 * which JNLP elements will be combined, one property for each of the types of
 * combinations supported.  Each property's value is a comma-separated list of
 * this form:
 * <p>
 * <code>parent-path:path-within-parent</code>
 * <p>
 * Both the parent-path and the path-within-parent are valid XPath expressions.
 * We need to separate them like this because if the node is not present we
 * need to insert it into the parent, so we need the parent piece separate.
 * For example, the setting
 * <p>
 * <code>/jnlp:/@codebase</code>
 * <p>
 * refers to the codebase attribute within the jnlp element, while
 * <p><code>/jnlp/resources:/property</code><p>
 * refers to the property element within the resources element within the
 * jnlp element.
 *
 * @author tjquinn
 */
abstract class CombinedXPath {

    private static final Logger logger = Logger.getLogger(JavaWebStartInfo.APPCLIENT_SERVER_MAIN_LOGGER,
                JavaWebStartInfo.APPCLIENT_SERVER_LOGMESSAGE_RESOURCE);

    /** property names for the types of combined JNLP content */
    private static final String OWNED_PROPERTY_NAME = "owned";
    private static final String DEFAULTED_PROPERTY_NAME = "defaulted";
    private static final String MERGED_PROPERTY_NAME = "merged";

    private final static XPathFactory xPathFactory = XPathFactory.newInstance();

    private final static XPath xPath = xPathFactory.newXPath();

    private static LSSerializer lsSerializer = null;
    private static LSOutput lsOutput = null;

    private final String parentPath;
    private final String targetRelativePath;

    /** xpath expression for the target node in the DOM for the developer's XML */
    private final XPathExpression targetExpr;

    /** if developer didn't provide the target, this is the parent where we'll
     * create a new child.
     */
    private final XPathExpression parentExpr;

    private static enum Type {
        OWNED(OWNED_PROPERTY_NAME) {
            CombinedXPath combinedXPath(String pathA, String pathB) {
                return new OwnedXPath(xPath, pathA, pathB);
            }
        },
        DEFAULTED(DEFAULTED_PROPERTY_NAME) {
            CombinedXPath combinedXPath(String pathA, String pathB) {
                return new DefaultedXPath(xPath, pathA, pathB);
            }
        },
        MERGED(MERGED_PROPERTY_NAME) {
            CombinedXPath combinedXPath(String pathA, String pathB) {
                return new MergedXPath(xPath, pathA, pathB);
            }
        };

        private String propertyName;

        Type(final String propName) {
            propertyName = propName;
        }

        abstract CombinedXPath combinedXPath(String pathA, String pathB);

    }

    static List<CombinedXPath> parse(final Properties p) {
        List<CombinedXPath> result = new ArrayList<CombinedXPath>();
//        result.addAll(CombinedXPath.parse(p, CombinedXPath.Type.OWNED));
        result.addAll(CombinedXPath.parse(p, CombinedXPath.Type.DEFAULTED));
        result.addAll(CombinedXPath.parse(p, CombinedXPath.Type.MERGED));
        return result;
    }

    /**
     * For the given combination type fetch the corresponding property value
     * from the config properties and then parse it into the separate
     * parent:with-parent pairs, creating for each pair the correct type of
     * CombinedXPath object and returning a List of them.
     * @param p
     * @param type
     * @return
     */
    private static List<CombinedXPath> parse(
                final Properties p,
                Type type) {

            final List<CombinedXPath> result = new
                    ArrayList<CombinedXPath>();
            final String refs = p.getProperty(type.propertyName);
            for (String ref : refs.split(",")) {
                final String paths[] = ref.split(":");
                if (paths.length != 2) {
                    throw new IllegalArgumentException(ref);
                }
                result.add(type.combinedXPath(paths[0], paths[1]));
            }
            return result;
        }

    /**
     * Creates a new combined XPath.
     *
     * @param xPath XPath available for searching
     * @param parentPath path to parent for new child (if developer's document lacks the target)
     * @param targetRelativePath path relative to the parent for the target node in the developer DOM
     */
    CombinedXPath(
            final XPath xPath,
            final String parentPath,
            final String targetRelativePath) {
        this.parentPath = parentPath;
        this.targetRelativePath = targetRelativePath;
        try {
            parentExpr = xPath.compile(parentPath);
            targetExpr = xPath.compile(parentPath + targetRelativePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String parentPath() {
        return parentPath;
    }

    String targetRelativePath() {
        return targetRelativePath;
    }

    XPathExpression targetExpr() {
        return targetExpr;
    }

    XPathExpression parentExpr() {
        return parentExpr;
    }

    /**
     * Processes the given combination: replaces, defaults, or merges.
     * <p>
     * Note that the template - which takes the form at this point of the
     * generatedDOM - contains some text nodes that are important.  So we
     * start with the generatedDOM as the "master" and then combine the
     * developer content into it.
     *
     * @param developerDOM
     * @param generatedDOM
     * @throws XPathExpressionException
     */
    abstract void process(final Document developerDOM, final Document generatedDOM) throws XPathExpressionException;

    protected void insert(
            final Document originalDOM,
            final Node insertionPoint,
            final Node newNode) throws XPathExpressionException {
        if (newNode instanceof Attr) {
            setAttr(originalDOM, insertionPoint, (Attr) newNode);
        } else {
            insertNode(originalDOM, insertionPoint, newNode);
        }
    }

    private void setAttr(
            final Document originalDOM,
            final Node insertionPoint,
            final Attr newAttr) throws XPathExpressionException {
        final Element parent = (insertionPoint == null) ?
            (Element) parentExpr().evaluate(originalDOM, XPathConstants.NODE) :
            ((Attr) insertionPoint).getOwnerElement();

        parent.setAttribute(newAttr.getName(), newAttr.getValue());
    }

    private void insertNode(
            final Document originalDOM,
            final Node insertionPoint,
            final Node newNode) throws XPathExpressionException {
        final Node parentNode = (insertionPoint == null) ?
            (Node) parentExpr().evaluate(originalDOM, XPathConstants.NODE) :
            insertionPoint.getParentNode();
        parentNode.insertBefore(originalDOM.adoptNode(newNode), insertionPoint);
    }

    /**
     * Represents a node in the document which we completely determine,
     * overriding any corresponding node from the developer's DOM.
     */
    static class OwnedXPath extends CombinedXPath {

        OwnedXPath(
                final XPath xPath,
                final String parentPath,
                final String targetRelativePath) {
            super(xPath, parentPath, targetRelativePath);
        }

        @Override
        void process(Document developerDOM, Document generatedDOM) throws XPathExpressionException {
        }

    }

    /**
     * Represents a combination of the two XML documents resulting from
     * merging the two input documents.
     */
    static class MergedXPath extends CombinedXPath {

        MergedXPath(
                final XPath xPath,
                final String parentPath,
                final String targetRelativePath) {
            super(xPath, parentPath, targetRelativePath);
        }

        @Override
        void process(Document developerDOM, Document generatedDOM) throws XPathExpressionException {
            NodeList developerNodes = (NodeList) targetExpr().evaluate(developerDOM, XPathConstants.NODESET);
            NodeList generatedNodes = (NodeList) targetExpr().evaluate(generatedDOM, XPathConstants.NODESET);

            final Node insertionPoint = (generatedNodes.getLength() > 0) ?
                generatedNodes.item(0) : null;

            final boolean isDetailed = logger.isLoggable(Level.FINER);
            for (int i = 0; i < developerNodes.getLength(); i++) {
                if (isDetailed) {
                    logger.log(Level.FINER,
                            "Inserting new node due to {0}:{1}",
                            new Object[]{parentPath(), targetRelativePath()});

                }
                insert(generatedDOM, insertionPoint, developerNodes.item(i));
                if (isDetailed) {
                    logger.log(Level.FINER, toXML(generatedDOM));
                }
            }
        }

    }

    /**
     * Represents a combination in which the developer's setting is used if
     * present; otherwise the generated document's setting is used.
     */
    static class DefaultedXPath extends CombinedXPath {

        DefaultedXPath(
                final XPath xPath,
                final String parentPath,
                final String targetRelativePath) {
            super(xPath, parentPath, targetRelativePath);
        }

        @Override
        void process(Document developerDOM, Document generatedDOM) throws XPathExpressionException {
            /*
             * The developer provided content for this XPath.  So remove the
             * generated node(s) for this XPath and replace them with the
             * ones from the developer's content.
             */

            final NodeList replacementNodes = (NodeList) targetExpr().evaluate(developerDOM, XPathConstants.NODESET);
            if (replacementNodes.getLength() == 0) {
                return;
            }
            final NodeList originalNodes = (NodeList) targetExpr().evaluate(generatedDOM, XPathConstants.NODESET);

            /*
             * Replace all the matching original children (if any) with the
             * replacement ones.
             */
            final Node insertionPoint = (originalNodes.getLength() > 0) ?
                originalNodes.item(0).getPreviousSibling() : null;

                /*
                 * Remove the old node first.  They could be attributes and, if so,
                 * we need to remove them first before setting them with the
                 * replacement values.  Otherwise, if we removed the old nodes
                 * after setting the new ones, we could accidentally erase new
                 * settings that were intended to replace old settings.
                 */
            final boolean isDetailed = logger.isLoggable(Level.FINER);

            for (int i = 0; i < originalNodes.getLength(); i++) {
                if (isDetailed) {
                    logger.log(Level.FINER,
                            "Removing generated node to make way for developer node based on {0}:{1}",
                            new Object[] {parentPath(), targetRelativePath()});
                }
                remove(originalNodes.item(i));
                if (isDetailed) {
                    logger.log(Level.FINER, toXML(generatedDOM));
                }
            }

            for (int i = 0; i < replacementNodes.getLength(); i++) {
                insert(generatedDOM, insertionPoint, replacementNodes.item(i));
                if (isDetailed) {
                    logger.log(Level.FINER, toXML(generatedDOM));
                }

            }
        }

        private void remove(final Node originalNode) {
            if (originalNode instanceof Attr) {
                removeAttr((Attr) originalNode);
            } else {
                removeNode(originalNode);
            }
        }

        private void removeNode(final Node originalNode) {
            originalNode.getParentNode().removeChild(originalNode);
        }

        private void removeAttr(final Attr originalAttr) {
            final Element parent = originalAttr.getOwnerElement();
            parent.removeAttribute(originalAttr.getName());
        }
    }

    private static String toXML(final Node node) {

        Writer writer = new StringWriter();
        writeXML(node, writer);
        return writer.toString();
    }

    private synchronized static void writeXML(final Node node, final Writer writer) {
        try {
            if (lsSerializer == null) {
                final DOMImplementation domImpl = DOMImplementationRegistry.newInstance().
                        getDOMImplementation("");
                final DOMImplementationLS domLS = (DOMImplementationLS) domImpl.getFeature("LS", "3.0");
                lsOutput = domLS.createLSOutput();
                lsOutput.setEncoding("UTF-8");
                lsSerializer = domLS.createLSSerializer();
            }
            lsOutput.setCharacterStream(writer);
            lsSerializer.write(node, lsOutput);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
