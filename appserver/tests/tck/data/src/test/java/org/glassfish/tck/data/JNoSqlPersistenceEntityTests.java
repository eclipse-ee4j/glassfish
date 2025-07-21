/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Ondro Mihalyi
 */
package org.glassfish.tck.data;

import org.glassfish.tck.data.junit5.TransactionExtension;

import ee.jakarta.tck.data.standalone.persistence.PersistenceEntityTests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(value = TransactionExtension.class)
public class JNoSqlPersistenceEntityTests extends PersistenceEntityTests {

    /**
     * This test expects running outside of a global transaction. It should be
     * executed in {@link JNoSqlPersistenceEntityTestsNoGlobalTx}
     */
    @Override
    @Disabled
    public void testVersionedInsertUpdateDelete() {
        super.testVersionedInsertUpdateDelete();
    }

    /**
     * This test expects running outside of a global transaction. It should be
     * executed in {@link JNoSqlPersistenceEntityTestsNoGlobalTx}
     */
    @Override
    @Disabled
    public void testMultipleInsertUpdateDelete() {
        super.testMultipleInsertUpdateDelete();
    }

}
