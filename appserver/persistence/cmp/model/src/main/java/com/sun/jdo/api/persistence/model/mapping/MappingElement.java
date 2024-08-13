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
 * MappingElement.java
 *
 * Created on March 3, 2000, 1:11 PM
 */

package com.sun.jdo.api.persistence.model.mapping;

import com.sun.jdo.api.persistence.model.ModelException;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

/**
 *
 * @author raccah
 * @version %I%
 */
public interface MappingElement extends MappingElementProperties, Comparable
{
    /** Add a property change listener.
     * @param l the listener to add
     */
    public void addPropertyChangeListener (PropertyChangeListener l);

    /** Remove a property change listener.
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (PropertyChangeListener l);

    /** Add a vetoable change listener.
     * @param l the listener to add
     */
    public void addVetoableChangeListener (VetoableChangeListener l);

    /** Remove a vetoable change listener.
     * @param l the listener to remove
     */
    public void removeVetoableChangeListener (VetoableChangeListener l);

    /** Get the name of this mapping element.
     * @return the name
     */
    public String getName ();

    /** Set the name of this mapping element.
     * @param name the name
     * @exception ModelException if impossible
     */
    public void setName (String name) throws ModelException;
}
