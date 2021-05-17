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
 * InstanceCallbacks.java
 *
 * Created on February 25, 2000
 */

package com.sun.jdo.api.persistence.support;

/**
 *
 * @author Craig Russell
 * @version 0.1
 */

/**
 * A PersistenceCapable class that provides callback methods for life
 * cycle events implements this interface.
 *
 * <P>Classes which include derived fields (transient fields whose values depend
 * on the values of persistent fields) require callbacks on specific
 * JDO Instance life cycle events in order to correctly populate the
 * values in these fields.
 *
 * <P>This interface defines the methods executed
 * by the PersistenceManager for these life cycle events.  If the class
 * implements InstanceCallbacks, it must explicitly declare it in the
 * class definition.  The Reference Enhancer does not modify the declaration or
 * any of the methods in the interface.
 */
public interface InstanceCallbacks
{
    /**
     * Called after the values are loaded from the data store into
     * this instance.
     *
     * <P>Derived fields should be initialized in this method.
     *
     * <P>This method is never modified by the Reference Enhancer.
     */
    void jdoPostLoad();

    /**
     * Called before the values are stored from this instance to the
     * data store.
     *
     * <P>Database fields that might have been affected by modified derived
     * fields should be updated in this method.
     *
     * <P>This method is never modified by the Reference Enhancer.
     */
    void jdoPreStore();

    /**
     * Called before the values in the instance are cleared.
     *
     * <P>Transient fields should be cleared in this method, as they will
     * not be affected by the jdoClear method.  Associations between this
     * instance and others in the runtime environment should be cleared.
     *
     * <P>This method is never modified by the Reference Enhancer.
     */
    void jdoPreClear();
}
