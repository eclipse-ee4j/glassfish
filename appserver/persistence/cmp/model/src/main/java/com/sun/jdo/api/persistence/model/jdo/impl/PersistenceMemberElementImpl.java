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
 * PersistenceMemberElementImpl.java
 *
 * Created on March 2, 2000, 5:17 PM
 */

package com.sun.jdo.api.persistence.model.jdo.impl;

import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceMemberElement;

import java.beans.PropertyVetoException;

/**
 *
 * @author raccah
 * @version %I%
 */
public abstract class PersistenceMemberElementImpl
    extends PersistenceElementImpl implements PersistenceMemberElement.Impl
{
    /** Create new PersistenceMemberElementImpl with no corresponding name.
     * This constructor should only be used for cloning and archiving.
     */
    public PersistenceMemberElementImpl ()
    {
        this(null);
    }

    /** Creates new PersistenceMemberElementImpl with the corresponding name
     * @param name the name of the element
     */
    public PersistenceMemberElementImpl (String name)
    {
        super(name);
    }

    /** Fires property change event.  This method overrides that of
     * PersistenceElementImpl to update the PersistenceClassElementImpl's
     * modified status.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    protected final void firePropertyChange (String name, Object o, Object n)
    {
        // even though o == null and n == null will signify a change, that
        // is consistent with PropertyChangeSupport's behavior and is
        // necessary for this to work
        boolean noChange = ((o != null) && (n != null) && o.equals(n));
        PersistenceClassElement classElement =
            ((PersistenceMemberElement)_element).getDeclaringClass();

        super.firePropertyChange(name, o, n);

        if ((classElement != null) && !noChange)
            classElement.setModified(true);
    }

    /** Fires vetoable change event.  This method overrides that of
     * PersistenceElementImpl to give listeners a chance to block
     * changes on the persistence class element modified status.
     * @param name property name
     * @param o old value
     * @param n new value
     * @exception PropertyVetoException when the change is vetoed by a listener
     */
    protected final void fireVetoableChange (String name, Object o, Object n)
        throws PropertyVetoException
    {
        // even though o == null and n == null will signify a change, that
        // is consistent with PropertyChangeSupport's behavior and is
        // necessary for this to work
        boolean noChange = ((o != null) && (n != null) && o.equals(n));
        PersistenceClassElement classElement =
            ((PersistenceMemberElement)_element).getDeclaringClass();

        super.fireVetoableChange(name, o, n);

        if ((classElement != null) && !noChange)
        {
            ((PersistenceElementImpl)classElement.getImpl()).
                fireVetoableChange(PROP_MODIFIED, Boolean.FALSE, Boolean.TRUE);
        }
    }
}
