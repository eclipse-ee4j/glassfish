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

import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.deployment.common.Descriptor;

/**
 * This descriptor represents a specification of a transactional behavior.
 *
 * @author Danny Coward
 */
public final class ContainerTransaction extends Descriptor {
    private String transactionAttribute;
    /** Transactions are not supported. */
    public static final String NOT_SUPPORTED = "NotSupported";
    /** Transactions need support. */
    public static final String SUPPORTS = "Supports";
    /** A transaction is required. */
    public static final String REQUIRED = "Required";
    /** A new transaction must be created. */
    public static final String REQUIRES_NEW = "RequiresNew";
    /** Transaction is mandatory.*/
    public static final String MANDATORY = "Mandatory";
    /** Never supply a transaction. */
    public static final String NEVER = "Never";
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ContainerTransaction.class);

    /**
     * Copy constructor.
     */
    public ContainerTransaction(ContainerTransaction other) {
        if (other != null) {
            this.transactionAttribute = other.transactionAttribute;
            this.setDescription(other.getDescription());
        }
    }

    /**
     * Create a new transaction descriptor with the given attribute. Throws
     * an IllegalArgumentException if the attribute is not an allowed type.
     * The allowed types are enumeration ny this class.
     * @param transactionAttribute .
     * @param description .
     */
    public ContainerTransaction(String transactionAttribute, String description) {
        super("a Container Transaction", description);
        boolean isValidAttribute = (
            NOT_SUPPORTED.equals(transactionAttribute)
            || SUPPORTS.equals(transactionAttribute)
            || REQUIRED.equals(transactionAttribute)
            || REQUIRES_NEW.equals(transactionAttribute)
            || MANDATORY.equals(transactionAttribute)
            || NEVER.equals(transactionAttribute) );
        if (!isValidAttribute && Descriptor.isBoundsChecking()) {
            throw new IllegalArgumentException(localStrings.getLocalString(
                "enterprise.deployment.exceptionunknowncontainertxtype",
                "Unknown ContainerTransaction type: {0}",
                new Object[] {transactionAttribute}));
        } else {
            this.transactionAttribute = transactionAttribute;
        }
    }

    /**
     * The transaction attribute that I specify.
     * @return the transaction attribute.
     */
    public String getTransactionAttribute() {
        return this.transactionAttribute;
    }

    /**
     * Equality iff the other object is another container transaction with the
     * same transaction attribute.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof ContainerTransaction) {
            ContainerTransaction otherContainerTransaction = (ContainerTransaction) other;
            if (otherContainerTransaction.getTransactionAttribute().equals(this.getTransactionAttribute())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + getTransactionAttribute().hashCode();
        return result;
    }

    /**
     * Appends a formatted String representing my state.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Container Transaction: ").append(getTransactionAttribute()).append("@")
            .append(getDescription());
    }
}

