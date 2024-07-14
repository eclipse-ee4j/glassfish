/*
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbc.admin.cli.test;

import com.sun.enterprise.admin.util.InstanceStateService;

import org.glassfish.tests.utils.junit.DomainXml;
import org.glassfish.tests.utils.junit.hk2.HK2JUnit5Extension;
import org.junit.jupiter.api.extension.ExtensionContext;


/**
 * @author David Matejcek
 */
public class JdbcAdminJunit5Extension extends HK2JUnit5Extension {

    @Override
    protected String getDomainXml(Class<?> testClass) {
        if (testClass.getAnnotation(DomainXml.class) == null) {
            return "DomainTest.xml";
        }
        return super.getDomainXml(testClass);
    }

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        addMockDescriptor(InstanceStateService.class);
        super.postProcessTestInstance(testInstance, context);
    }
}
