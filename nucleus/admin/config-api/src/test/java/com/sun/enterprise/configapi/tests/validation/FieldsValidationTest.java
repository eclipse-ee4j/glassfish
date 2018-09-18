/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.configapi.tests.ConfigApiTest;
import junit.framework.Assert;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import javax.validation.ConstraintViolationException;
import java.beans.PropertyVetoException;

/**
 * Test Field validation
 */

public class FieldsValidationTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }    

    @Test
    public void testNotNullField() {
        AdminService admin = super.getHabitat().getService(AdminService.class);
        Assert.assertNotNull(admin);
        try {
            ConfigSupport.apply(new SingleConfigCode<AdminService>() {
                @Override
                public Object run(AdminService wAdmin) throws PropertyVetoException, TransactionFailure {
                    wAdmin.setDasConfig(null);
                    return null;
                }
            }, admin);
            Assert.fail("Exception not raised when setting a @NotNull annotated field with null");
        } catch(TransactionFailure e) {
            if (e.getCause()!=null) {
                Assert.assertTrue(e.getCause() instanceof ConstraintViolationException);
            }
        }
    }
}
