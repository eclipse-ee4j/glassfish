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

/*
 * PersistenceElementImpl.java
 *
 * Created on March 1, 2000, 5:01 PM
 */

package com.sun.jdo.api.persistence.model.jdo.impl;

import com.sun.jdo.api.persistence.model.ModelException;
import com.sun.jdo.api.persistence.model.ModelVetoException;
import com.sun.jdo.api.persistence.model.jdo.PersistenceElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceElementProperties;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

/* TODO:
    1. way to get to declaring class from here?
 */

/**
 *
 * @author raccah
 * @version %I%
 */
public abstract class PersistenceElementImpl extends Object
    implements PersistenceElement.Impl, PersistenceElementProperties
{
    /** Element */
    PersistenceElement _element;

    /** Property change support */
    private PropertyChangeSupport _support;

    /** Vetoable change support */
    private transient VetoableChangeSupport _vetoableSupport;

    /** Name of the element. */
    private String _name;

    /** Create new PersistenceElementImpl with no corresponding name.  This
     * constructor should only be used for cloning and archiving.
     */
    public PersistenceElementImpl ()
    {
        this(null);
    }

    /** Creates new PersistenceElementImpl with the corresponding name
     * @param name the name of the element
     */
    public PersistenceElementImpl (String name)
    {
        super();
        _name = name;
    }

    /** Called to attach the implementation to a specific
     * element. Will be called in the element's constructor.
     * Allows implementors of this interface to store a reference to the
     * holder class, useful for implementing the property change listeners.
     *
     * @param element the element to attach to
     */
    public void attachToElement (PersistenceElement element)
    {
        _element = element;
    }

    /** Fires property change event.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    protected void firePropertyChange (String name, Object o, Object n)
    {
        if (_support != null)
            _support.firePropertyChange(name, o, n);
    }

    /** Add a property change listener.
     * @param l the listener to add
     */
    public synchronized void addPropertyChangeListener
        (PropertyChangeListener l)
    {
        // new test under synchronized block
        if (_support == null)
            _support = new PropertyChangeSupport(_element);


        _support.addPropertyChangeListener(l);
    }

    /** Remove a property change listener.
     * @param l the listener to remove
     */
    public synchronized void removePropertyChangeListener (
        PropertyChangeListener l)
    {
        if (_support != null)
            _support.removePropertyChangeListener(l);
    }

    /** Fires vetoable change event.
     * @param name property name
     * @param o old value
     * @param n new value
     * @exception PropertyVetoException when the change is vetoed by a listener
     */
    protected void fireVetoableChange (String name, Object o, Object n)
        throws PropertyVetoException
    {
        if (_vetoableSupport != null)
            _vetoableSupport.fireVetoableChange(name, o, n);
    }

    /** Add a vetoable change listener.
     * @param l the listener to add
     */
    public synchronized void addVetoableChangeListener
        (VetoableChangeListener l)
    {
        if (_vetoableSupport == null)
            _vetoableSupport = new VetoableChangeSupport(_element);

        _vetoableSupport.addVetoableChangeListener(l);
    }

    /** Remove a vetoable change listener.
     * @param l the listener to remove
     */
    public synchronized void removeVetoableChangeListener (
        VetoableChangeListener l)
    {
        if (_vetoableSupport != null)
            _vetoableSupport.removeVetoableChangeListener(l);
    }

    /** Get the name of this persistence element.
     * @return the name
     */
    public String getName () { return _name; }

    /** Set the name of this persistence element.
     * @param name the name
     * @exception ModelException if impossible
     */
    public void setName (String name) throws ModelException
    {
        String old = getName();

        try
        {
            fireVetoableChange(PROP_NAME, old, name);
            _name = name;
            firePropertyChange(PROP_NAME, old, name);
        }
        catch (PropertyVetoException e)
        {
            throw new ModelVetoException(e);
        }
     }
}
