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

/**
 * Perform's string substitution for the given input. Substitution process look for the matching input in the given
 * {@link RadixTree} and replaced the string with the corresponding matching value.
 *
 * @see {@link RadixTree}
 */
class RadixTreeSubstitution {
    /** {@link RadixTree} used sub. */
    private RadixTree _tree;

    /**
     * Buffer to store the current processing characters, reset when match found for the processed character.
     */
    private StringBuffer _processedChars;

    /** Reference to the currently processing node. */
    private RadixTreeNode _currentNode;

    /** No of matched character in currently processing node. */
    private int _nodeMatchedChars;

    /** Last matched node value. */
    private String _lastMatchedValue;

    /** Buffer to store the characters need to re-process from root. */
    private StringBuffer _reProcessChars;

    /**
     * Construct {@link RadixTreeSubstitution} for the given {@link RadixTree}.
     *
     * @param tree
     */
    RadixTreeSubstitution(RadixTree tree) {
        if (tree == null) {
            throw new IllegalArgumentException("Invalid tree.");
        }
        _tree = tree;
        _processedChars = new StringBuffer();
        _currentNode = _tree.getRootNode();
    }

    /**
     * Perform substitution by allowing continuous character feeding. Once the character sequence matched completely to
     * {@link RadixTree} node and no further/extended match is available then the output is returned and the values reset to
     * re-look the new input from root node.
     * <p>
     * Method maintains the processed characters, currently processing node and other parameters require in substitution.
     * </p>
     * <p>
     * <b>NOTE:</b> A <code>null</code> input signify the end of the processing.
     * </p>
     *
     * @param c Input character to match with the node key.
     * @return
     * <li>Value of the matching node, if no further match available.</li>
     * <li>Return the string of processed characters if no matching node found or the node value is null.</li>
     * <li><code>null</code> if waiting for more input char.</li>
     */
    String substitute(Character c) {
        StringBuffer outputBuffer = null;
        boolean finalCall = (c == null) ? true : false;
        do {
            if (c != null) {
                if (_reProcessChars != null && _reProcessChars.length() > 0) {
                    c = _reProcessChars.charAt(0);
                    _reProcessChars.delete(0, 1);
                }
                String nodeKey = _currentNode.getKey();
                if (_nodeMatchedChars < nodeKey.length()) {
                    if (c == nodeKey.charAt(_nodeMatchedChars)) {
                        _processedChars.append(c);
                        _nodeMatchedChars++;
                        continue;
                    }
                } else {
                    if (_currentNode.getValue() != null) {
                        _lastMatchedValue = _currentNode.getValue();
                        _processedChars.delete(0, _processedChars.length());
                    }
                    RadixTreeNode childNode = _currentNode.getChildNode(c);
                    if (childNode != null) {
                        _processedChars.append(c);
                        _currentNode = childNode;
                        _nodeMatchedChars = 1;
                        continue;
                    }
                }
            } else if (_currentNode.getValue() != null && _nodeMatchedChars == _currentNode.getKey().length()) {
                _lastMatchedValue = _currentNode.getValue();
                _processedChars.delete(0, _processedChars.length());
            }

            if (outputBuffer == null) {
                outputBuffer = new StringBuffer();
            }
            // write to the output buffer.
            if (_lastMatchedValue != null) {
                outputBuffer.append(_lastMatchedValue);
                if (c != null) {
                    _processedChars.append(c);
                }
                _lastMatchedValue = null;
            } else {
                // If no match found than append the first character and start fresh from second character.
                if (_processedChars.length() > 0) {
                    outputBuffer.append(_processedChars.charAt(0));
                    _processedChars.delete(0, 1);
                    if (c != null) {
                        _processedChars.append(c);
                    }
                } else {
                    if (c != null) {
                        outputBuffer.append(c);
                    }
                    _processedChars.delete(0, _processedChars.length());
                }
            }
            if (_processedChars.length() > 0) {
                if (_reProcessChars == null) {
                    _reProcessChars = new StringBuffer(_processedChars);
                } else {
                    _processedChars.append(_reProcessChars);
                    _reProcessChars.delete(0, _reProcessChars.length());
                    _reProcessChars.append(_processedChars);
                }
                c = _reProcessChars.charAt(0);
                _processedChars.delete(0, _processedChars.length());
            }
            _currentNode = _tree.getRootNode();
            _nodeMatchedChars = 0;
        } while (_reProcessChars != null && _reProcessChars.length() > 0);

        //Append the last process character sequence.
        if (finalCall) {
            if (_nodeMatchedChars == _currentNode.getKey().length() && _currentNode.getValue() != null) {
                outputBuffer.append(_currentNode.getValue());
            } else {
                outputBuffer.append(_currentNode.getKey().substring(0, _nodeMatchedChars));
            }
            _processedChars.delete(0, _processedChars.length());
            _currentNode = _tree.getRootNode();
            _nodeMatchedChars = 0;
            _lastMatchedValue = null;
        }
        return outputBuffer == null || outputBuffer.toString().isEmpty() ? null : outputBuffer.toString();
    }
}
