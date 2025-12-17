/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import jakarta.transaction.TransactionSynchronizationRegistry;

import org.glassfish.main.test.app.helpers.JakartaEE;

@Entity
public class TransactionEntity {
    @Id
    private Long id;
    @Transient
    private String transactionKey;

    public TransactionEntity() {
        setCurrentTransactionKey();
    }

    public TransactionEntity(Long id) {
        this.id = id;
        setCurrentTransactionKey();
    }

    @PostPersist
    public void setCurrentTransactionKey() {
        try {
            TransactionSynchronizationRegistry registry = JakartaEE.getDefaultTransactionSynchronizationRegistry();
            this.transactionKey = String.valueOf(registry.getTransactionKey());
        } catch (Exception e) {
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransactionKey() { return transactionKey; }
}
