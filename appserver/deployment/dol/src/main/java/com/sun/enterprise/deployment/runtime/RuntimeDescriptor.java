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

package com.sun.enterprise.deployment.runtime;

import org.glassfish.deployment.common.Descriptor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * This base class defines common behaviour and data for all runtime
 * descriptors.
 *
 * @author Jerome Dochez
 */
public abstract class RuntimeDescriptor extends Descriptor {

    protected PropertyChangeSupport propListeners;

    /** Creates a new instance of RuntimeDescriptor */
    public RuntimeDescriptor(RuntimeDescriptor other) {
        super(other);
        propListeners = new PropertyChangeSupport(this); // not copied
    }


    /** Creates a new instance of RuntimeDescriptor */
    public RuntimeDescriptor() {
        propListeners = new PropertyChangeSupport(this);
    }


    /**
     * Add a property listener for this bean
     *
     * @param l the property listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propListeners.addPropertyChangeListener(l);
    }


    /**
     * removes a property listener for this bean
     *
     * @param l the property listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propListeners.removePropertyChangeListener(l);
    }


    /**
     * Add a property listener for a specific property name
     *
     * @param n the property name
     * @param l the property listener
     */
    public void addPropertyChangeListener(String n, PropertyChangeListener l) {
        propListeners.addPropertyChangeListener(n, l);
    }


    /**
     * Remover a property listener for specific property name
     *
     * @param n the property name
     * @param l the property listener
     */
    public void removePropertyChangeListener(String n, PropertyChangeListener l) {
        propListeners.removePropertyChangeListener(n, l);
    }


    /**
     * Sets a property value
     *
     * @param name the property name
     * @param value the property value
     */
    public void setValue(String name, Object value) {
        Object oldValue = getExtraAttribute(name);
        addExtraAttribute(name, value);
        propListeners.firePropertyChange(name, oldValue, value);
    }


    /**
     * @return a property value
     */
    public Object getValue(String name) {
        return getExtraAttribute(name);
    }


    /**
     * indexed property support
     */
    protected void setValue(String name, int index, Object value) {
        List list = getIndexedProperty(name);
        list.set(index, value);
        setValue(name, list);
    }


    protected Object getValue(String name, int index) {
        List list = getIndexedProperty(name);
        return list.get(index);
    }


    protected int addValue(String name, Object value) {
        List list = getIndexedProperty(name);
        list.add(value);
        setValue(name, list);
        return list.indexOf(value);
    }


    protected int removeValue(String name, Object value) {
        List list = getIndexedProperty(name);
        int index = list.indexOf(value);
        list.remove(index);
        return index;
    }


    protected void removeValue(String name, int index) {
        List list = getIndexedProperty(name);
        list.remove(index);
    }


    protected void setValues(String name, Object[] values) {
        List list = getIndexedProperty(name);
        for (int i = 0; i < values.length;) {
            list.add(values[i]);
        }
    }


    protected Object[] getValues(String name) {
        List list = (List) getValue(name);
        if (list != null && list.size() > 0) {
            Class c = list.get(0).getClass();
            Object array = java.lang.reflect.Array.newInstance(c, list.size());
            return list.toArray((Object[]) array);
        } else {
            return null;
        }
    }


    protected int size(String name) {
        List list = (List) getValue(name);
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }


    private List getIndexedProperty(String name) {
        Object o = getValue(name);
        if (o == null) {
            return new ArrayList();
        } else {
            return (List) o;
        }
    }


    // xml attributes support
    public void setAttributeValue(String elementName, String attributeName, Object value) {
        // here we have to play games...
        // the nodes cannot know if the property scalar is 0,1 or n
        // so we look if the key name is already used (means property*
        // DTD langua) and find the last one entered

        int index = 0;
        while (getValue(elementName + "-" + index + "-" + attributeName) != null) {
            index++;
        }

        setValue(elementName + "-" + index + "-" + attributeName, value);
    }


    public String getAttributeValue(String elementName, String attributeName) {
        return getAttributeValue(elementName, 0, attributeName);
    }


    // attribute stored at the descriptor level are treated like elements
    public void setAttributeValue(String attributeName, String value) {
        setValue(attributeName, value);
    }


    public String getAttributeValue(String attributeName) {
        return (String) getValue(attributeName);
    }


    // indexed xml attributes support
    public void setAttributeValue(String elementName, int index, String attributeName, Object value) {
        setValue(elementName + "-" + index + "-" + attributeName, value);
    }


    public String getAttributeValue(String elementName, int index, String attributeName) {
        return (String) getValue(elementName + "-" + index + "-" + attributeName);
    }
}
