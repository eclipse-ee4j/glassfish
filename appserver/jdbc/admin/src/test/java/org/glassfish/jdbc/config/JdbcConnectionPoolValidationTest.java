/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jdbc.config.JdbcConnectionPool;

import java.beans.PropertyVetoException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;


import javax.validation.ConstraintViolationException;

/**
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
public class JdbcConnectionPoolValidationTest extends ConfigApiTest {

    private JdbcConnectionPool pool = null;
    private static final String NAME = "test"; //same as the one in JdbcConnectionPoolValidation.xml

    public JdbcConnectionPoolValidationTest() {
    }

    @Override
    public String getFileName() {
        return ("JdbcConnectionPoolValidation");
    }

    @Before
    public void setUp() {
        pool = super.getHabitat().getService(JdbcConnectionPool.class, NAME);
    }

    @After
    public void tearDown() {
        pool = null;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test (expected= ConstraintViolationException.class)
    public void testBooleanDoesNotTakeInteger1() throws Throwable {
        try {
            ConfigSupport.apply(new SingleConfigCode<JdbcConnectionPool>() {
                public Object run(JdbcConnectionPool jdbcConnectionPool) throws PropertyVetoException, TransactionFailure {
                    jdbcConnectionPool.setConnectionLeakReclaim("123"); //this method should only take boolean;
                    return null;
                }
            }, pool);

        } catch(TransactionFailure e) {
            throw e.getCause().getCause();
        }
    }


    @Test
    public void testBooleanTakesTrueFalse() {
        try {
            pool.setSteadyPoolSize("true"); //this only takes a boolean
            pool.setSteadyPoolSize("false"); //this only takes a boolean
            pool.setSteadyPoolSize("TRUE"); //this only takes a boolean
            pool.setSteadyPoolSize("FALSE"); //this only takes a boolean
            pool.setSteadyPoolSize("FALSE"); //this only takes a boolean
        } catch(PropertyVetoException pv) {
            //ignore?
        }
    }
}
