/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package com.sun.gjc.spi.jdbc40;

import com.sun.gjc.spi.JdbcObjectsFactory;
import com.sun.gjc.spi.ManagedConnectionFactoryImpl;
import com.sun.gjc.spi.ManagedConnectionImpl;
import com.sun.gjc.spi.base.ConnectionHolder;
import com.sun.gjc.util.SQLTraceDelegator;

import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;

import java.sql.Connection;

import javax.sql.DataSource;

/**
 * Test stub for the real {@code com.sun.gjc.spi.jdbc40.Jdbc40ObjectsFactory},
 * which lives in the separate {@code jdbc40} module and is therefore not on the
 * test classpath of {@code jdbc-core}.
 *
 * <p>{@link JdbcObjectsFactory#getInstance()} loads that class by name via
 * {@code Class.forName(...)}. Without this stub the lookup fails with a
 * {@code ClassNotFoundException} that is logged as a {@code RAR7118} warning
 * (with a stack trace) during the {@code jdbc-core} unit tests. Providing this
 * stub on the test classpath lets the lookup succeed so the tests run without
 * the misleading warning.
 */
public class Jdbc40ObjectsFactory extends JdbcObjectsFactory {

    private static final long serialVersionUID = 1L;

    @Override
    public DataSource getDataSourceInstance(ManagedConnectionFactoryImpl mcfObject, ConnectionManager cmObject) {
        return null;
    }

    @Override
    public ConnectionHolder getConnection(Connection conObject, ManagedConnectionImpl mcObject,
            ConnectionRequestInfo criObject, boolean statementWrapping, SQLTraceDelegator sqlTraceDelegator) {
        return null;
    }
}
