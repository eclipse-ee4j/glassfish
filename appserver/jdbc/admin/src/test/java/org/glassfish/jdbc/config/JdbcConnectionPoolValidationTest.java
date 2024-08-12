/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.jdbc.config;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.ConstraintViolationException;

import java.beans.PropertyVetoException;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jdbc.admin.cli.test.JdbcAdminJunit5Extension;
import org.glassfish.tests.utils.junit.DomainXml;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
@ExtendWith(JdbcAdminJunit5Extension.class)
@DomainXml("JdbcConnectionPoolValidation.xml")
public class JdbcConnectionPoolValidationTest {

    // same as the one in JdbcConnectionPoolValidation.xml
    private static final String NAME = "test";
    @Inject
    private ServiceLocator locator;
    @Inject
    @Named(NAME)
    private JdbcConnectionPool pool;

    @Test
    public void testBooleanDoesNotTakeInteger1() throws Exception {
        SingleConfigCode<JdbcConnectionPool> configCode = jdbcConnectionPool -> {
            jdbcConnectionPool.setConnectionLeakReclaim("123");
            return null;
        };
        TransactionFailure e = assertThrows(TransactionFailure.class, () -> ConfigSupport.apply(configCode, pool));
        assertThat(e.getCause(), instanceOf(RuntimeException.class));
        assertThat(e.getCause().getCause(), instanceOf(ConstraintViolationException.class));
    }


    @Test
    public void testBooleanTakesTrueFalse() {
        assertThrows(PropertyVetoException.class, () -> pool.setSteadyPoolSize("true"));
        assertThrows(PropertyVetoException.class, () -> pool.setSteadyPoolSize("false"));
        assertThrows(PropertyVetoException.class, () ->  pool.setSteadyPoolSize("TRUE"));
        assertThrows(PropertyVetoException.class, () ->  pool.setSteadyPoolSize("FALSE"));
        assertThrows(PropertyVetoException.class, () ->  pool.setSteadyPoolSize("FALSE"));
    }
}
