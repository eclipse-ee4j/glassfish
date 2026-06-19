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

package com.sun.jsftemplating.layout.descriptors.handler;

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This class holds OutputMapping value meta information for individual instances of Handler Objects. This information
 * is necessary to provide the location to store the output value for a specific invocation of a handler. This is data
 * consists of the name the Handler uses for the output, the OutputType, and optionally the OutputType key to use when
 * storing/retrieving the output value.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class OutputMapping implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor with targetKey as null. This constructor will throw an IllegalArgumentException if outputName or
     * targetOutputType is null.
     * </p>
     *
     * @param outputName The name the Handler uses for output value
     * @param targetOutputType OutputType that will store the output value
     *
     * @see OutputTypeManager
     *
     * @throws IllegalArumentException If outputName / targetOutputType is null
     */
    public OutputMapping(String outputName, String targetOutputType) {
        this(outputName, null, targetOutputType);
    }

    /**
     * <p>
     * Constructor with all values supplied as Strings. This constructor will throw an IllegalArgumentException if
     * outputName or targetOutputType is null.
     * </p>
     *
     * @param outputName The name the Handler uses for output value
     * @param targetKey The key the OutputType will use
     * @param targetOutputType OutputType that will store the output value
     *
     * @see OutputTypeManager
     *
     * @throws NullPointerException If outputName / targetOutputType is null
     */
    public OutputMapping(String outputName, String targetKey, String targetOutputType) {
        // Sanity checks...
        if (outputName == null || outputName.length() == 0) {
            throw new NullPointerException("'outputName' is required!");
        }
        if (targetOutputType == null) {
            throw new NullPointerException("'targetOutputType' is required!");
        }
        _outputName = outputName;
        _targetKey = targetKey;
        _targetOutputType = targetOutputType;
    }

    /**
     * Accessor for outputName.
     */
    public String getOutputName() {
        return _outputName;
    }

    /**
     * Accessor for targetKey.
     */
    public String getOutputKey() {
        return _targetKey;
    }

    /**
     * Accessor for targetOutputType.
     */
    public OutputType getOutputType() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return OutputTypeManager.getManager(ctx).getOutputType(ctx, _targetOutputType);
    }

    /**
     * <p>
     * Accessor for targetOutputType as a String.
     * </p>
     */
    public String getStringOutputType() {
        return _targetOutputType;
    }

    private String _outputName = null;
    private String _targetKey = null;
    private String _targetOutputType = null;
}
