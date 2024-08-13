/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * JDOEJB20Helper.java
 *
 * Created on January 17, 2002
 */

package com.sun.jdo.spi.persistence.support.sqlstore.ejb;

import com.sun.jdo.api.persistence.support.PersistenceManager;

import jakarta.ejb.EJBContext;
import jakarta.ejb.EJBLocalObject;

import java.util.Collection;
import java.util.Set;

/*
 * This is the helper interface for conversion of persistence-capable instances
 * to and from EJB objects of type EJBLocalObject and Collections of those.
 * It extends generic interface JDOEJB11Helper for all other types of conversions.
 *
 * @author Marina Vatkina
 */
public interface JDOEJB20Helper extends JDOEJB11Helper {

    /**
     * Converts persistence-capable instance to EJBLocalObject.
     * @param pc the persistence-capable instance to be converted as an Object.
     * @param pm the associated instance of PersistenceManager.
     * @return instance of EJBLocalObject.
     */
    EJBLocalObject convertPCToEJBLocalObject (Object pc, PersistenceManager pm);

    /**
     * Converts persistence-capable instance to EJBLocalObject. Returns null if
     * the instance is already removed via cascade-delete operation.
     * @param pc the persistence-capable instance to be converted as an Object.
     * @param pm the associated instance of PersistenceManager.
     * @param context the EJBContext of the calling bean.
     * @return instance of EJBLocalObject.
     */
    EJBLocalObject convertPCToEJBLocalObject (Object pc, PersistenceManager pm,
        EJBContext context);

    /**
     * Converts EJBLocalObject to persistence-capable instance.
     * @param o the EJBLocalObject instance to be converted.
     * @param pm the associated instance of PersistenceManager.
     * @param validate true if the existence of the instance is to be validated.
     * @return persistence-capable instance.
     * @throws IllegalArgumentException if validate is true and instance does
     * not exist in the database or is deleted.
     */
    Object convertEJBLocalObjectToPC(EJBLocalObject o, PersistenceManager pm,
        boolean validate);

    /**
     * Converts Collection of persistence-capable instances to a Collection of
     * EJBLocalObjects.
     * @param pcs the Collection of persistence-capable instance to be converted.
     * @param pm the associated instance of PersistenceManager.
     * @return Collection of EJBLocalObjects.
     */
    Collection convertCollectionPCToEJBLocalObject (Collection pcs, PersistenceManager pm);

    /**
     * Converts Collection of persistence-capable instances to a Set of
     * EJBLocalObjects.
     * @param pcs the Collection of persistence-capable instance to be converted.
     * @param pm the associated instance of PersistenceManager.
     * @return Set of EJBLocalObjects.
     */
    Set convertCollectionPCToEJBLocalObjectSet (Collection pcs, PersistenceManager pm);

    /**
     * Converts Collection of EJBLocalObjects to a Collection of
     * persistence-capable instances.
     * @param coll the Collection of EJBLocalObject instances to be converted.
     * @param pm the associated instance of PersistenceManager.
     * @param validate true if the existence of the instances is to be validated.
     * @return Collection of persistence-capable instance.
     * @throws IllegalArgumentException if validate is true and at least one instance does
     * not exist in the database or is deleted.
     */
    Collection convertCollectionEJBLocalObjectToPC (Collection coll, PersistenceManager pm,
        boolean validate);

    /**
     * Validates that this instance is of the correct implementation class
     * of a local interface type.
     *
     * @param o the instance to validate.
     * @throws IllegalArgumentException if validation fails.
     */
    void assertInstanceOfLocalInterfaceImpl(Object o);

}
