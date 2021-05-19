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
 * PersistenceCapable.java
 *
 * Created on February 28, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore;


/**
 * Internal PersistenceCapable interface.
 */
public interface PersistenceCapable
    extends com.sun.jdo.api.persistence.support.PersistenceCapable
{
    /**
     * Returns the associated state manager.
     */
    StateManager jdoGetStateManager();

    /**
     * Sets the associated state manager.
     */
    void jdoSetStateManager(StateManager sm);

    /**
     * Returns the value of the JDO flags.
     */
    byte jdoGetFlags();

    /**
     * Sets the value of the JDO flags, and returns the previous value.
     */
    void jdoSetFlags(byte flags);

    /**
     * Returns the value of the specified field.
     *
     * Primitive valued fields are wrapped with the corresponding
     * Object wrapper type.
     */
    Object jdoGetField(int fieldNumber);

    /**
     * Sets the value of the specified field.
     *
     * Primitive valued fields are wrapped with the corresponding
     * Object wrapper type.
     */
    void jdoSetField(int fieldNumber, Object value);

    /**
     * Creates an instance of the same class as this object.
     */
    // added new method
    Object jdoNewInstance(StateManager statemanager);

    /**
     * Clears the fields of each persistent field.
     *
     * This method stores zero or null values into each persistent
     * field of the instance, in effect reverting it to its initial
     * state. Clearing fields allows objects referred to by this
     * instance to be garbage collected. The associated StateManager
     * calls this method when transitioning an instance to the hollow
     * state. This will normally be during post completion.
     */
    // removed parameter: StateManager sm
    void jdoClear();

    /**
     * Copies values from each transient, derived, or persistent field
     * from the target instance.
     *
     * The target instance must have exactly the same type as this instance.
     *
     * This method might be used by the StateManager to make a shallow
     * copy of an instance, or might be used to restore values of an
     * instance after transaction rollback. It might be used by the
     * application to make a shallow copy (clone) of a transient or
     * persistent instance.
     *
     * The enhancement-added fields (jdoFlags and jdoStateManager) are not
     * affected by jdoCopy().
     */
    //@olsen: fix 4435059: this method is not generated anymore
    // additional parameter: boolean cloneSCOs
    //void jdoCopy(Object o, boolean cloneSCOs);
}
