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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;

import org.glassfish.main.test.app.helpers.JakartaEE;



@ApplicationScoped
@Transactional
public class TransactionService {

    @Inject
    private TransactionRepository repository;

    public String processTransaction() {
        String serviceTransactionKey;
        TransactionSynchronizationRegistry registry = JakartaEE.getDefaultTransactionSynchronizationRegistry();
        serviceTransactionKey = String.valueOf(registry.getTransactionKey());

        TransactionEntity entity = repository.save(new TransactionEntity(1L));
        return serviceTransactionKey + SEPARATOR + entity.getTransactionKey();
    }
    public static final String SEPARATOR = "#####";

}
