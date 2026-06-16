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

import com.sun.jsftemplating.resource.ResourceFactory;
import com.sun.jsftemplating.util.Util;

/**
 * <p>
 * This class holds information that describes a Resource. It provides access to a {@link ResourceFactory} for obtaining
 * the actual Resource object described by this descriptor. See the layout.dtd file for more information on how to
 * define a Resource via XML. The LayoutDefinition will add all defined Resources to the request scope for easy access
 * (including via JSF EL).
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class Resource implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * This is the id for the Resource.
     * </p>
     */
    private String _id = null;

    /**
     * <p>
     * This holds "extraInfo" for the Resource, such as a ResourceBundle baseName.
     * </p>
     */
    private String _extraInfo = null;

    /**
     * <p>
     * This is a String className for the Factory.
     * </p>
     */
    private String _factoryClass = null;

    /**
     * <p>
     * The Factory that produces the desired <code>UIComponent</code>.
     * </p>
     */
    private transient ResourceFactory _factory = null;

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public Resource(String id, String extraInfo, String factoryClass) {
        if (id == null) {
            throw new NullPointerException("'id' cannot be null!");
        }
        if (factoryClass == null) {
            throw new NullPointerException("'factoryClass' cannot be null!");
        }
        _id = id;
        _extraInfo = extraInfo;
        _factoryClass = factoryClass;
        _factory = createFactory();
    }

    /**
     * <p>
     * Accessor method for ID. This is the key the resource will be stored under in the Request scope.
     * </p>
     */
    public String getId() {
        return _id;
    }

    /**
     * <p>
     * This holds "extraInfo" for the Resource, such as a <code>ResourceBundle</code> baseName.
     * </p>
     */
    public String getExtraInfo() {
        return _extraInfo;
    }

    /**
     * <p>
     * This method provides access to the {@link ResourceFactory}.
     * </p>
     *
     * @return The {@link ResourceFactory}.
     */
    public ResourceFactory getFactory() {
        if (_factory == null) {
            _factory = createFactory();
        }
        return _factory;
    }

    /**
     * <p>
     * This method creates a new {@link ResourceFactory}.
     * </p>
     *
     * @return The new {@link ResourceFactory}.
     */
    protected ResourceFactory createFactory() {
        try {
            Class cls = Util.loadClass(_factoryClass, _factoryClass);
            return (ResourceFactory) cls.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
