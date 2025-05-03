/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.main.core.kernel.test;

import com.sun.enterprise.admin.util.InstanceStateService;
import com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.admin.ObjectInputStreamWithServiceLocator;

import java.nio.file.Path;
import java.util.Set;

import org.glassfish.tests.utils.junit.HK2JUnit5Extension;
import org.glassfish.tests.utils.junit.JUnitSystem;
import org.junit.jupiter.api.extension.ExtensionContext;


/**
 * Simplifies configuration of tests
 *
 * @author David Matejcek
 */
public class KernelJUnitExtension extends HK2JUnit5Extension {

    static {
        Path installRoot = JUnitSystem.detectBasedir();
        Path instanceRoot = installRoot.resolve(Path.of("target", "test-domain"));
        FileUtils.mkdirsMaybe(instanceRoot.toFile());
        System.setProperty(BootstrapKeys.INSTALL_ROOT_PROP_NAME, installRoot.toString());
        System.setProperty(BootstrapKeys.INSTANCE_ROOT_PROP_NAME, instanceRoot.toString());
    }

    @Override
    protected String getDomainXml(final Class<?> testClass) {
        return "DomainTest.xml";
    }


    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        addMockDescriptor(InstanceStateService.class);
        super.postProcessTestInstance(testInstance, context);
    }


    @Override
    protected Set<Class<?>> getExcludedClasses(final Class<?> testClass) {
        // this class doesn't have public constructor -> HK2 throws exception
        return Set.of(ObjectInputStreamWithServiceLocator.class);
    }
}
