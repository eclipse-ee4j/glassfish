/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests.validation;

import com.sun.enterprise.config.serverbeans.AdminService;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import java.beans.PropertyVetoException;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test Field validation
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class FieldsValidationTest {

    @Inject
    private ServiceLocator locator;

    @Test
    public void testNotNullField() {
        AdminService admin = locator.getService(AdminService.class);
        assertNotNull(admin);
        try {
            ConfigSupport.apply(new SingleConfigCode<AdminService>() {
                @Override
                public Object run(AdminService wAdmin) throws PropertyVetoException, TransactionFailure {
                    wAdmin.setDasConfig(null);
                    return null;
                }
            }, admin);
            fail("Exception not raised when setting a @NotNull annotated field with null");
        } catch(TransactionFailure e) {
            assertThat(e.getCause(), instanceOf(ConstraintViolationException.class));
        }
    }
}
