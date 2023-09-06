/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.persistence.transform.provider;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.ProviderUtil;

import java.util.Map;

public class PersistenceProviderImpl implements PersistenceProvider {

    @Override
    public EntityManagerFactory createEntityManagerFactory(String name, Map properties) {
        return null;
    }

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo, Map properties) {
        return new EntityManagerFactoryImpl(persistenceUnitInfo);
    }

    @Override
    public void generateSchema(PersistenceUnitInfo persistenceUnitInfo, Map properties) {

    }

    @Override
    public boolean generateSchema(String persistenceUnitName, Map properties) {
        return false;
    }

    @Override
    public ProviderUtil getProviderUtil() {
        return null;
    }
}
