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

import com.sun.jsftemplating.util.Util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A HandlerDefinition defines a "handler" that may be invoked in the process of executing an event. A HandlerDefinition
 * has an <strong>id</strong>, <strong>java method</strong>, <strong>input definitions</strong>, <strong>output
 * definitions</strong>, and <strong>child handlers</strong>.
 * </p>
 *
 * <p>
 * The <strong>java method</strong> to be invoked must have the following method signature:
 * </p>
 *
 * <p>
 * <BLOCKQUOTE><CODE> public void beginDisplay(HandlerContext handlerCtx) </CODE></BLOCKQUOTE>
 * </p>
 *
 * <p>
 * <code>void</code> above can return a value. Depending on the type of event, return values may be handled differently.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class HandlerDefinition implements java.io.Serializable {

    /**
     * Constructor
     */
    public HandlerDefinition(String id) {
        _id = id;
    }

    /**
     * This method returns the id for this handler.
     */
    public String getId() {
        return _id;
    }

    /**
     * For future tool support
     */
    public String getDescription() {
        return _description;
    }

    /**
     * For future tool support
     */
    public void setDescription(String desc) {
        _description = desc;
    }

    /**
     * <p>
     * This method sets the event handler (method) to be invoked. The method should be public and accept a prameter of type
     * "HandlerContext" Example:
     * </p>
     *
     * <p>
     * <BLOCKQUOTE> public void beginDisplay(HandlerContext handlerCtx) </BLOCKQUOTE>
     * </p>
     *
     * @param cls The full class name containing method
     * @param methodName The method name of the handler within class
     */
    public void setHandlerMethod(String cls, String methodName) {
        if (cls == null || methodName == null) {
            throw new IllegalArgumentException("Class name and method name must be non-null!");
        }
        _methodClass = cls;
        _methodName = methodName;
    }

    /**
     *
     */
    public void setHandlerMethod(Method method) {
        if (method != null) {
            _methodName = method.getName();
            _methodClass = method.getDeclaringClass().getName();
        } else {
            _methodName = null;
            _methodClass = null;
        }
        _method = method;
    }

    /**
     * <p>
     * This method determines if the handler is static.
     * </p>
     */
    public boolean isStatic() {
        if (_static == null) {
            _static = Boolean.valueOf(Modifier.isStatic(getHandlerMethod().getModifiers()));
        }
        return _static.booleanValue();
    }

    /**
     *
     */
    public Method getHandlerMethod() {
        if (_method != null) {
            // return cached Method
            return _method;
        }

        // See if we have the info to find it
        if (_methodClass != null && _methodName != null) {
            // Find the class
            Class clzz = null;
            try {
                clzz = Util.loadClass(_methodClass, _methodClass);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("'" + _methodClass + "' not found for method '" + _methodName + "'!", ex);
            }

            // Find the method on the class
            Method method = null;
            try {
                method = clzz.getMethod(_methodName, EVENT_ARGS);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException("Method '" + _methodName + "' not found!", ex);
            }

            // Cache the _method
            _method = method;
        }

        // Return the Method if there is one
        return _method;
    }

    /**
     * This method adds an IODescriptor to the list of input descriptors. These descriptors define the input parameters to
     * this handler.
     *
     * @param desc The input IODescriptor to add
     */
    public void addInputDef(IODescriptor desc) {
        _inputDefs.put(desc.getName(), desc);
    }

    /**
     * This method sets the input IODescriptors for this handler.
     *
     * @param inputDefs The Map of IODescriptors
     */
    public void setInputDefs(Map<String, IODescriptor> inputDefs) {
        if (inputDefs == null) {
            throw new IllegalArgumentException("inputDefs cannot be null!");
        }
        _inputDefs = inputDefs;
    }

    /**
     * This method retrieves the Map of input IODescriptors.
     *
     * @return The Map of IODescriptors
     */
    public Map<String, IODescriptor> getInputDefs() {
        return _inputDefs;
    }

    /**
     * This method returns the requested IODescriptor, null if not found.
     */
    public IODescriptor getInputDef(String name) {
        return _inputDefs.get(name);
    }

    /**
     * This method adds an IODescriptor to the list of output descriptors. These descriptors define the output parameters to
     * this handler.
     *
     * @param desc The IODescriptor to add
     */
    public void addOutputDef(IODescriptor desc) {
        _outputDefs.put(desc.getName(), desc);
    }

    /**
     * This method sets the output IODescriptors for this handler.
     *
     * @param outputDefs The Map of output IODescriptors
     */
    public void setOutputDefs(Map<String, IODescriptor> outputDefs) {
        if (outputDefs == null) {
            throw new IllegalArgumentException("outputDefs cannot be null!");
        }
        _outputDefs = outputDefs;
    }

    /**
     * This method retrieves the Map of output IODescriptors.
     *
     * @return The Map of output IODescriptors
     */
    public Map<String, IODescriptor> getOutputDefs() {
        return _outputDefs;
    }

    /**
     * This method returns the requested IODescriptor, null if not found.
     */
    public IODescriptor getOutputDef(String name) {
        return _outputDefs.get(name);
    }

    /**
     * <p>
     * This method adds a {@link Handler} to the list of child {@link Handler}s. Child {@link Handler}s are executed AFTER
     * this {@link Handler} is executed.
     * </p>
     *
     * @param desc The {@link Handler} to add.
     */
    public void addChildHandler(Handler desc) {
        if (_childHandlers == _emptyList) {
            _childHandlers = new ArrayList();
        }
        _childHandlers.add(desc);
    }

    /**
     * <p>
     * This method sets the <code>List</code> of child {@link Handler}s for this <code>HandlerDefinition</code>.
     *
     * @param childHandlers The <code>List</code> of child {@link Handler}s.
     */
    public void setChildHandlers(List<Handler> childHandlers) {
        if (childHandlers == null || childHandlers.size() == 0) {
            childHandlers = _emptyList;
        }
        _childHandlers = childHandlers;
    }

    /**
     * <p>
     * This method retrieves the <code>List</code> of child {@link Handler}s. This <code>List</code> should not be changed
     * directly. Call {@link #addChildHandler(Handler)}, or make a copy and call {@link #setChildHandlers(List)}.
     * </p>
     *
     * @return The <code>List</code> of child {@link Handler}s for this <code>HandlerDefinition</code>.
     */
    public List<Handler> getChildHandlers() {
        return _childHandlers;
    }

    /**
     * <p>
     * This toString() provides detailed information about this <code>HandlerDefinition</code>.
     * </p>
     */
    @Override
    public String toString() {
        // Print the basic info...
        Formatter printf = new Formatter();
        printf.format("%-40s  %s.%s\n", _id, _methodClass, _methodName);

        // Print the description
        if (_description != null) {
            printf.format("%s\n", _description);
        }

        // Print the Inputs
        Iterator<IODescriptor> it = _inputDefs.values().iterator();
        while (it.hasNext()) {
            printf.format("    INPUT>  %s\n", it.next().toString());
        }

        // Print the Outputs
        it = _outputDefs.values().iterator();
        while (it.hasNext()) {
            printf.format("    OUTPUT> %s\n", it.next().toString());
        }

        // Print the Child Handlers (TBD...)

        // Return the result
        return printf.toString();
    }

    public static final Class[] EVENT_ARGS = new Class[] { HandlerContext.class };

    private String _id = null;
    private String _description = null;
    private String _methodClass = null;
    private String _methodName = null;
    private transient Method _method = null;
    private Map<String, IODescriptor> _inputDefs = new HashMap<>(5);
    private Map<String, IODescriptor> _outputDefs = new HashMap<>(5);
    private List<Handler> _childHandlers = _emptyList;
    private transient Boolean _static = null;

    private static final List<Handler> _emptyList = new ArrayList<>(0);
    private static final long serialVersionUID = 0xA8B7C6D5E4F30211L;
}
