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

package org.glassfish.connectors.admin.cli.test;

import com.sun.enterprise.admin.util.InstanceStateService;

import org.glassfish.tests.utils.HK2JUnit5Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.addOneDescriptor;
import static org.glassfish.tests.utils.Utils.createMockDescriptor;

/**
 * @author David Matejcek
 */
public class ConnectorsAdminJunit5Extension extends HK2JUnit5Extension {

    @Override
    protected String getDomainXml(final Class<?> testClass) {
        return "DomainTest.xml";
    }


    @Override
    protected Class<TestDocument> getDomainXmlDomClass(Class<?> testClass) {
        return TestDocument.class;
    }


    @Override
    public void postProcessTestInstance(final Object testInstance, ExtensionContext context) throws Exception {
        addOneDescriptor(getLocator(), createMockDescriptor(InstanceStateService.class));
        super.postProcessTestInstance(testInstance, context);
    }
}
