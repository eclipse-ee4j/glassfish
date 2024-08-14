/*
 * Copyright (c) 2021-2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.ejb;

import com.sun.ejb.codegen.EjbClassGeneratorFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
public class EJBUtilsTest {

    private static final ClassLoader loader = EJBUtilsTest.class.getClassLoader();

    private EjbClassGeneratorFactory factory;

    @BeforeEach
    public void createFactory() {
        factory = new EjbClassGeneratorFactory(loader);
    }

    @AfterEach
    public void closeFactory() {
        factory.close();
    }


    @Test
    @Order(10)
    public void loadGeneratedRemoteBusinessClasses() throws Exception {
        Class<?> remoteIface = factory.ensureRemote(GeneratorTestExperiment.class.getName());
        assertEquals("com.sun.ejb._GeneratorTestExperiment_Remote", remoteIface.getName());
        Class<?> ifaceRemote = loader.loadClass("com.sun.ejb._GeneratorTestExperiment_Remote");
        assertTrue(ifaceRemote.isInterface());
        Class<?> iface30 = loader.loadClass("com.sun.ejb._GeneratorTestExperiment_Wrapper");
        assertFalse(iface30.isInterface());
        assertEquals(remoteIface, factory.ensureRemote(GeneratorTestExperiment.class.getName()));
    }


    @Test
    @Order(20)
    public void loadGeneratedGenericEJBHomeClass() throws Exception {
        Class<?> newClass = EJBUtils.loadGeneratedGenericEJBHomeClass(loader, GeneratorTestExperiment.class);
        assertNotNull(newClass);
        assertTrue(newClass.isInterface());
        assertEquals("com.sun.ejb.codegen.GenericEJBHome_Generated", newClass.getName());
        assertSame(newClass, factory.ensureGenericHome(GeneratorTestExperiment.class));
    }


    @Test
    @Order(30)
    public void generateSEI() throws Exception {
        Class<?> newClass = factory.ensureServiceInterface(GeneratorTestExperiment.class);
        assertNotNull(newClass);
        assertEquals("com.sun.ejb.GeneratorTestExperiment_GeneratedSEI", newClass.getName());
        assertSame(newClass, factory.ensureServiceInterface(GeneratorTestExperiment.class));
    }
}
