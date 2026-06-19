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

package com.sun.jsftemplating.layout.descriptors;

import com.sun.jsftemplating.component.factory.ComponentFactory;
import com.sun.jsftemplating.util.Util;

import java.io.Serializable;
import java.util.Formatter;

/**
 * <p>
 * This class holds information that describes a {@link LayoutComponent} type. It provides access to a
 * {@link ComponentFactory} for instantiating an instance of a the <code>UIComponent</code> described by this
 * descriptor. See the layout.dtd file for more information on how to declare types via XML.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ComponentType implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public ComponentType(String id, String factoryClass) {
        if (id == null) {
            throw new NullPointerException("'id' cannot be null!");
        }
        if (factoryClass == null) {
            throw new NullPointerException("'factoryClass' cannot be null!");
        }
        _id = id;
        _factoryClass = factoryClass;
    }

    public ComponentType(String id, String factoryClass, Serializable extraInfo) {
        this(id, factoryClass);
        this.setExtraInfo(extraInfo);
    }

    public String getId() {
        return _id;
    }

    /**
     * <p>
     * This method provides access to the {@link ComponentFactory}.
     * </p>
     *
     * @return The {@link ComponentFactory}.
     */
    public ComponentFactory getFactory() {
        if (_factory == null) {
            _factory = createFactory();
        }
        return _factory;
    }

    /**
     * <p>
     * This method creates a new factory.
     * </p>
     *
     * @return The new {@link ComponentFactory}.
     */
    protected ComponentFactory createFactory() {
        // Create it...
        ComponentFactory factory = null;
        try {
            Class cls = Util.loadClass(_factoryClass, this);
            factory = (ComponentFactory) cls.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        // Set the extraInfo if any...
        if (_extraInfo != null) {
            factory.setExtraInfo(_extraInfo);
        }

        // Return the new ComponentFactory
        return factory;
    }

    /**
     * <p>
     * This method allows you to provide extra information that can be used to initialize a <code>ComponentType</code>. For
     * example, if you wanted to pass in the JSF component type via the property, you could do that. That would allow 1
     * factory class to instatiate multiple components based on the JSF component type. However, it would require a
     * differnet <code>ComponentType</code> instance for each different JSF component type.
     * </p>
     */
    public void setExtraInfo(Serializable extraInfo) {
        _extraInfo = extraInfo;
    }

    /**
     * <p>
     * This method returns the extraInfo that was set for this <code>ComponentType</code>. This information will be passed
     * to the {@link ComponentFactory} when it is first created. It will only be created 1 time, not each time a Component
     * is created.
     * </p>
     */
    public Serializable getExtraInfo() {
        return _extraInfo;
    }

    /**
     * <p>
     * This <code>toString()</code> method produces information about this <code>ComponentType</code>.
     * </p>
     */
    @Override
    public String toString() {
        Formatter println = new Formatter();
        println.format("%-30s  %s\n", _id, _factoryClass);
        return println.toString();
    }

    /**
     * <p>
     * This is the id for the ComponentType.
     * </p>
     */
    private String _id = null;

    /**
     * <p>
     * This is a String className for the Factory.
     * </p>
     */
    private String _factoryClass = null;

    /**
     * <p>
     * The {@link ComponentFactory} that produces the desired <code>UIComponent</code>.
     * </p>
     */
    private transient ComponentFactory _factory = null;

    /**
     * <p>
     * Extra information associated with this ComponentType.
     * </p>
     */
    private Serializable _extraInfo = null;
}
