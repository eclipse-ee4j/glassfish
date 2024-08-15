/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl.algorithm;

import com.sun.enterprise.admin.servermgmt.SLogger;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Node for {@link RadixTree}.
 */
class RadixTreeNode {
    private static final Logger _logger = SLogger.getLogger();

    private static final LocalStringsImpl _strings = new LocalStringsImpl(RadixTreeNode.class);

    // Node key.
    private String _key;
    // Value of node.
    private String _value;
    // Associated child nodes.
    private Map<Character, RadixTreeNode> _childNodes;
    // Reference to parent node.
    private RadixTreeNode _parentNode;

    /**
     * Construct {@link RadixTreeNode} for the give key, value pair.
     */
    RadixTreeNode(String key, String value) {
        _key = key;
        _value = value;
    }

    /**
     * Get's the key.
     *
     * @return node key.
     */
    String getKey() {
        return _key;
    }

    /**
     * Set's the node key.
     *
     * @param key the key to set.
     */
    void setKey(String key) {
        this._key = key;
    }

    /**
     * Get's the node value.
     *
     * @return node value.
     */
    String getValue() {
        return _value;
    }

    /**
     * Set's the node value.
     *
     * @param value the value to set.
     */
    void setValue(String value) {
        this._value = value;
    }

    /**
     * Get's the parent node.
     *
     * @return the parentNode.
     */
    RadixTreeNode getParentNode() {
        return _parentNode;
    }

    /**
     * Get's the {@link Collection} of child nodes. Returns empty {@link Collection} object if no child data found.
     *
     * @return associated child nodes.
     */
    Collection<RadixTreeNode> getChildNodes() {
        if (_childNodes != null) {
            return _childNodes.values();
        } else {
            List<RadixTreeNode> list = Collections.emptyList();
            return list;
        }
    }

    /**
     * Add's a child node.
     * <p>
     * NOTE: Addition of child with empty or null key is not allowed.
     * </p>
     *
     * @param node Node to add.
     */
    void addChildNode(RadixTreeNode node) {
        if (node == null || node._key == null || node._key.isEmpty()) {
            throw new IllegalArgumentException(_strings.get("errorInEmptyNullKeyInstertion"));
        }
        char c = node._key.charAt(0);
        if (_childNodes == null) {
            _childNodes = new HashMap<Character, RadixTreeNode>();
        }
        RadixTreeNode oldNode = _childNodes.put(c, node);
        if (oldNode != null) {
            _logger.log(Level.WARNING, SLogger.CHILD_NODE_EXISTS, new Object[] { this.toString(), oldNode.toString(), node.toString() });
            oldNode._parentNode = null;
        }
        node._parentNode = this;
    }

    /**
     * Removes a child node.
     *
     * @param node child node.
     */
    void removeChildNode(RadixTreeNode node) {
        if (node == null || node._key == null || node._key.isEmpty()) {
            throw new IllegalArgumentException(_strings.get("invalidNodeKey"));
        }
        char c = node._key.charAt(0);
        if (_childNodes != null) {
            RadixTreeNode matchedNode = _childNodes.get(c);
            if (matchedNode == node) {
                node = _childNodes.remove(c);
                node._parentNode = null;
            } else {
                throw new IllegalArgumentException(_strings.get("invalidChildNode", node, this));
            }
        }
    }

    /**
     * Gets a child node associated with a given character.
     *
     * @param c input char to retrieve associated child node.
     * @return The associated node or <code>null</code> if no association found.
     */
    RadixTreeNode getChildNode(char c) {
        return _childNodes == null ? null : _childNodes.get(c);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Node Key : " + _key + ", Value : " + _value);
        return buffer.toString();
    }
}
