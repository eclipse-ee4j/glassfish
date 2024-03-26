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
 * JDOEJB20HelperImpl.java
 *
 * Created on January 17, 2002
 */

package com.sun.jdo.spi.persistence.support.ejb.cmp;

import java.util.Collection;
import java.util.Set;

import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBContext;

import com.sun.jdo.api.persistence.support.PersistenceManager;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.CMPHelper;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.JDOEJB20Helper;
import com.sun.jdo.spi.persistence.utility.logging.Logger;
import org.glassfish.persistence.common.I18NHelper;


/*
 * This is an abstract class which is a generic implementation of the
 * JDOEJBHelper interface for conversion of persistence-capable instances
 * to and from EJB objects of type EJBLocalObject and Collections of those.
 * It extends JDOEJB11HelperImpl for conversion of instances of other types.
 *
 * @author Marina Vatkina
 */
abstract public class JDOEJB20HelperImpl extends JDOEJB11HelperImpl
    implements JDOEJB20Helper {

    /**
     * Converts persistence-capable instance to EJBLocalObject.
     * @param pc the persistence-capable instance to be converted as an Object.
     * @param pm the associated instance of PersistenceManager.
     * @return instance of EJBLocalObject.
     */
    public EJBLocalObject convertPCToEJBLocalObject (Object pc, PersistenceManager pm) {
        if (pc == null) return null;
        Object jdoObjectId = pm.getObjectId(pc);
        Object key = convertObjectIdToPrimaryKey(jdoObjectId);
        try {
            return CMPHelper.getEJBLocalObject(key, getContainer());
        } catch (Exception ex) {
            EJBException e = new EJBException(I18NHelper.getMessage(messages,
                        "EXC_ConvertPCToEJBLocalObject", key.toString()), ex);// NOI18N
            logger.throwing("JDOEJB20HelperImpl", "convertPCToEJBLocalObject", e); // NOI18N
            throw e;
        }
    }

    /**
     * Converts persistence-capable instance to EJBLocalObject. Returns null if
     * the instance is already removed via cascade-delete operation.
     * @param pc the persistence-capable instance to be converted as an Object.
     * @param pm the associated instance of PersistenceManager.
     * @param context the EJBContext of the calling bean.
     * @return instance of EJBLocalObject.
     */
    public EJBLocalObject convertPCToEJBLocalObject (Object pc, PersistenceManager pm,
        EJBContext context) {
        if (pc == null) return null;
        Object jdoObjectId = pm.getObjectId(pc);
        Object key = convertObjectIdToPrimaryKey(jdoObjectId);
        try {
            return CMPHelper.getEJBLocalObject(key, getContainer(), context);
        } catch (Exception ex) {
            EJBException e = new EJBException(I18NHelper.getMessage(messages,
                        "EXC_ConvertPCToEJBLocalObjectCtx", key.toString()), ex);// NOI18N
            logger.throwing("JDOEJB20HelperImpl", "convertPCToEJBLocalObjectCtx", e); // NOI18N
            throw e;
        }
    }

    /**
     * Converts EJBLocalObject to persistence-capable instance.
     * @param o the EJBLocalObject instance to be converted.
     * @param pm the associated instance of PersistenceManager.
     * @param validate true if the existence of the instance is to be validated.
     * @return persistence-capable instance.
     * @throws IllegalArgumentException if validate is true and instance does
     * not exist in the database or is deleted.
     */
    public Object convertEJBLocalObjectToPC(EJBLocalObject o, PersistenceManager pm, boolean validate) {
        Object key = null;
        try {
            key = o.getPrimaryKey();
        } catch (Exception ex) {
            EJBException e = new EJBException(I18NHelper.getMessage(messages,
                        "EXC_ConvertEJBObjectToPC", o.getClass().getName()), ex);// NOI18N
            logger.throwing("JDOEJB20HelperImpl", "convertEJBLocalObjectToPC", e); // NOI18N
            throw e;
        }
        return convertPrimaryKeyToPC(key, pm, validate);
    }

    /**
     * Converts Collection of persistence-capable instances to a Collection of
     * EJBLocalObjects.
     * @param pcs the Collection of persistence-capable instance to be converted.
     * @param pm the associated instance of PersistenceManager.
     * @return Collection of EJBLocalObjects.
     */
    public Collection convertCollectionPCToEJBLocalObject (Collection pcs, PersistenceManager pm){
        Collection rc = new java.util.ArrayList();
        Object o = null;

        for (java.util.Iterator it = pcs.iterator(); it.hasNext();) {
            o = convertPCToEJBLocalObject((Object)it.next(), pm);
            if(logger.isLoggable(Logger.FINEST) ) {
                logger.finest(
                    "\n---JDOEJB20HelperImpl.convertCollectionPCToEJBLocalObject() adding: " + o);// NOI18N
            }
            rc.add(o);
        }
        return rc;
    }

    /**
     * Converts Collection of persistence-capable instances to a Set of
     * EJBLocalObjects.
     * @param pcs the Collection of persistence-capable instance to be converted.
     * @param pm the associated instance of PersistenceManager.
     * @return Set of EJBLocalObjects.
     */
    public Set convertCollectionPCToEJBLocalObjectSet (Collection pcs, PersistenceManager pm) {
        java.util.Set rc = new java.util.HashSet();
        Object o = null;

        for (java.util.Iterator it = pcs.iterator(); it.hasNext();) {
            o = convertPCToEJBLocalObject((Object)it.next(), pm);
            if(logger.isLoggable(Logger.FINEST) ) {
                logger.finest(
                    "\n---JDOEJB20HelperImpl.convertCollectionPCToEJBLocalObjectSet() adding: " + o);// NOI18N
            }
            rc.add(o);
        }
        return rc;
    }

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
    public Collection convertCollectionEJBLocalObjectToPC (Collection coll, PersistenceManager pm,
                                                           boolean validate) {
        Collection rc = new java.util.ArrayList();
        Object o = null;

        for (java.util.Iterator it = coll.iterator(); it.hasNext();) {
            o = convertEJBLocalObjectToPC((EJBLocalObject)it.next(), pm, validate);
            if(logger.isLoggable(Logger.FINEST) ) {
                logger.finest(
                    "\n---JDOEJB20HelperImpl.convertCollectionEJBLocalObjectToPC() adding: " + o);// NOI18N
            }
            rc.add(o);
        }
        return rc;
    }

    /**
     * Validates that this instance is of the correct implementation class
     * of a local interface type.
     *
     * @param o the instance to validate.
     * @throws IllegalArgumentException if validation fails.
     */
    abstract public void assertInstanceOfLocalInterfaceImpl(Object o);

   /**
     * Validates that this instance is of the correct implementation class
     * of a local interface.
     * Throws IllegalArgumentException if the passed
     * argument is of a wrong type.
     *
     * @param o the instance to validate.
     * @param beanName as String.
     * @throws IllegalArgumentException if validation fails.
     */
    protected void assertInstanceOfLocalInterfaceImpl(Object o,
        String beanName) {

        // We can't check if null is the correct type or not. So
        // we let it succeed.
        if (o == null)
            return;

        try {
            CMPHelper.assertValidLocalObject(o, getContainer());

        } catch (EJBException ex) {
            String msg = I18NHelper.getMessage(messages, "EXC_WrongLocalInstance", // NOI18N
                new Object[] {o.getClass().getName(), beanName,
                    ex.getMessage()});
            logger.log(Logger.WARNING, msg);
            throw new IllegalArgumentException(msg);
        }
    }

}
