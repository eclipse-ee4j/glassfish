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

import ee.jakarta.tck.data.standalone.persistence.PersistenceEntityTests;

import org.glassfish.tck.data.junit5.RunOnly;
import org.glassfish.tck.data.junit5.RunOnlyCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 * This is a group of PersistenceEntityTests tests that must run outside of a global transaction,
 * otherwise the test scenario doesn't make sense and would always fail. The rest of the tests
 * are executed in {@link JNoSqlPersistenceEntityTests}, with global transactions created with
 * {@link TransactionExtension}
 *
 * @author ondro
 */
@ExtendWith(RunOnlyCondition.class)
public class JNoSqlPersistenceEntityTestsNoGlobalTx extends PersistenceEntityTests {

    @Override
    @RunOnly
    @Test
    public void testVersionedInsertUpdateDelete() {
        super.testVersionedInsertUpdateDelete();
    }

    @Override
    @RunOnly
    @Test
    public void testMultipleInsertUpdateDelete() {
        super.testMultipleInsertUpdateDelete();
    }

}
