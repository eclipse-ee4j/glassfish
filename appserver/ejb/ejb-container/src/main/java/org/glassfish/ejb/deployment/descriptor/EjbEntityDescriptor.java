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

package org.glassfish.ejb.deployment.descriptor;

import java.util.logging.Logger;

import org.glassfish.deployment.common.Descriptor;

import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * This class contains deployment information for an EntityBean with
 * bean-managed persistence.
 * Subclasses contains additional information for EJB1.1/EJB2.0 CMP EntityBeans.
 *
 * @author Danny Coward
 * @author Sanjeev Krishnan
 * @author Vivek Nagar
 */
public class EjbEntityDescriptor extends EjbDescriptor {

    public static final String TYPE = "Entity";
    public static final String BEAN_PERSISTENCE = "Bean";
    public static final String CONTAINER_PERSISTENCE = "Container";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    // EntityBean attributes
    protected String persistenceType;
    protected boolean isReentrant = false;
    protected String primaryKeyClassName;

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EjbEntityDescriptor.class);

    static Logger _logger = DOLUtils.getDefaultLogger();

    /**
     * The default constructor.
     */
    public EjbEntityDescriptor() {
    }

    /**
     * The copy constructor.
     */
    public EjbEntityDescriptor(EjbDescriptor other) {
        super(other);
        if (other instanceof EjbEntityDescriptor) {
            EjbEntityDescriptor entity = (EjbEntityDescriptor) other;
            this.persistenceType = entity.persistenceType;
            this.isReentrant = entity.isReentrant;
            this.primaryKeyClassName = entity.primaryKeyClassName;
        }
    }

    @Override
    public String getEjbTypeForDisplay() {
        return "EntityBean";
    }

    /**
     * Gets the container transaction type for this entity bean. Entity
     * beans always have CONTAINER_TRANSACTION_TYPE transaction type.
     */
    @Override
    public String getTransactionType() {
        return super.transactionType;
    }

    /**
     * Sets the transaction type for this entity bean.
     * Throws an illegal argument exception if this type is not
     * CONTAINER_TRANSACTION_TYPE.
     */
    @Override
    public void setTransactionType(String transactionType) {
        if (!CONTAINER_TRANSACTION_TYPE.equals(transactionType) && Descriptor.isBoundsChecking()) {
            throw new IllegalArgumentException(localStrings.getLocalString(
                "enterprise.deployment.exceptionentitybeancanonlyhavecntnrtxtype",
                "Entity beans can only have Container transaction type. The type was being set to {0}",
                new Object[] {transactionType}));
        }
        super.transactionType = transactionType;
    }

    @Override
    public String getContainerFactoryQualifier() {
        return "EntityContainerFactory";
    }


    /**
     * Return true if this entity bean is reentrant, false else.
     */
    public boolean isReentrant() {
        return this.isReentrant;
    }

    public String getReentrant() {
        if (this.isReentrant()) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public void setReentrant(String reentrantString) {
        if (TRUE.equalsIgnoreCase(reentrantString)) {
            this.setReentrant(true);
            return;
        }
        if (FALSE.equalsIgnoreCase(reentrantString)) {
            this.setReentrant(false);
            return;
        }
        if (Descriptor.isBoundsChecking()) {
            throw new IllegalArgumentException(localStrings.getLocalString(
                   "enterprise.deployment.exceptionstringnotlegalvalue",
                  "{0} is not a legal value for entity reentrancy", new Object[] {reentrantString}));
        }
    }


    /**
     * Sets the isReentrant flag for this bean.
     */
    public void setReentrant(boolean isReentrant) {
        this.isReentrant = isReentrant;
    }

    /**
     * Returns the persistence type for this entity bean. Defaults to BEAN_PERSISTENCE.
     */
    public String getPersistenceType() {
        if (this.persistenceType == null) {
            this.persistenceType = BEAN_PERSISTENCE;
        }
        return this.persistenceType;
    }

    /**
     * Sets the persistence type for this entity bean. Allowable values are BEAN_PERSISTENCE
     * or CONTAINER_PERSISTENCE, or else an IllegalArgumentException is thrown.
     */
    public void setPersistenceType(String persistenceType) {
        boolean isValidChange = BEAN_PERSISTENCE.equals(persistenceType) || CONTAINER_PERSISTENCE.equals(persistenceType);
        if (isValidChange || !Descriptor.isBoundsChecking()) {
            this.persistenceType = persistenceType;
        } else {
            //_logger.log(Level.FINE,"Warning " + persistenceType + " is not an allowed persistence type");
            throw new IllegalArgumentException(localStrings.getLocalString(
                "enterprise.deployment.exceptionpersistenceisnotallowedtype",
                "{0} is not an allowed persistence type", new Object[] {persistenceType}));
        }
    }

    /**
     * Return the classname of the primary key for this bean, or the empty
     * string if none has been set.
     */
    public String getPrimaryKeyClassName() {
        if (this.primaryKeyClassName == null) {
            this.primaryKeyClassName = Object.class.getName();
        }
        return this.primaryKeyClassName;
    }

    /**
     * Set the classname of the primary key used by this bean.
     */
    public void setPrimaryKeyClassName(String primaryKeyClassName) {
        this.primaryKeyClassName = primaryKeyClassName;
    }

    /**
     * Returns the type of this bean. EjbEntityDescriptor.TYPE
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Sets my type String.
     */
    @Override
    public void setType(String type) {
        throw new IllegalArgumentException(localStrings.getLocalString(
               "enterprise.deployment.exceptioncannotsettypeonentitybean",
               "Cannon set type on an entity bean"));
    }

    /**
     * Return my formatted string representation.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n Entity descriptor");
        toStringBuffer.append("\n isReentrant ").append(isReentrant);
        toStringBuffer.append("\n primaryKeyClassName ").append(primaryKeyClassName);
        toStringBuffer.append("\n persistenceType ").append(persistenceType);
    }
}
