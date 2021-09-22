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

package com.sun.ejb;

import com.sun.ejb.codegen.ClassGeneratorFactory;
import com.sun.ejb.codegen.Generator;
import com.sun.ejb.codegen.ServiceInterfaceGenerator;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.glassfish.pfl.dynamic.codegen.spi.Wrapper;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import static java.lang.reflect.Modifier.PUBLIC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class EJBUtilsTest {

    private static final ClassLoader loader = EJBUtilsTest.class.getClassLoader();

    @Test
    public void generateSEI_Benchmark() throws Exception {
        Options options = new OptionsBuilder()
            .include(getClass().getName() + ".*")
            .mode(Mode.AverageTime)
            .warmupIterations(0)
            .measurementIterations(2)
            .measurementTime(TimeValue.seconds(1L))
            .forks(1)
            .threads(20)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .timeout(TimeValue.seconds(5L))
            .timeUnit(TimeUnit.NANOSECONDS)
            .build();

        Collection<RunResult> results = new Runner(options).run();
        assertThat(results, hasSize(1));
        Result<?> result = results.iterator().next().getAggregatedResult().getPrimaryResult();
        assertThat(result.getScore(), lessThan(100000d));
    }


    @Benchmark
    public void generateSei_Benchmark() throws Exception {
        ClassGeneratorFactory generator = new CustomGenerator();
        Class<?> newClass = EJBUtils.generateSEI(generator, loader, EJbUtilsEjbTestClass.class);
        assertNotNull(newClass);
        assertEquals(generator.className(), newClass.getName());
    }


    @Test
    public void generateSei_ServiceInterfaceGenerator() throws Exception {
        ServiceInterfaceGenerator generator = new ServiceInterfaceGenerator(loader, EJbUtilsEjbTestClass.class);
        // FIXME: com.sun.ejb.EJBUtilsTest$EJbUtilsEjbTestClass doesn't have expected package com.sun.ejb.internal.jaxws
        Class<?> newClass = EJBUtils.generateSEI(generator, loader, EJbUtilsEjbTestClass.class);
        assertNotNull(newClass);
        assertEquals(generator.className(), newClass.getName());
    }


    @Test
    public void loadGeneratedRemoteBusinessClasses() throws Exception {
        EJBUtils.loadGeneratedRemoteBusinessClasses(EjbUtilsTestInterface.class.getName());
        Class<?> ifaceRemote = loader.loadClass("com.sun.ejb._EJBUtilsTest$EjbUtilsTestInterface_Remote");
        assertTrue(ifaceRemote.isInterface());
        Class<?> iface30 = loader.loadClass("com.sun.ejb.EJBUtilsTest$EjbUtilsTestInterface");
        assertTrue(iface30.isInterface());
        assertDoesNotThrow(() -> EJBUtils.loadGeneratedRemoteBusinessClasses(EjbUtilsTestInterface.class.getName()));
    }


    @Test
    public void loadGeneratedGenericEJBHomeClass() throws Exception {
        Class<?> newClass = EJBUtils.loadGeneratedGenericEJBHomeClass(loader);
        assertNotNull(newClass);
        assertTrue(newClass.isInterface());
        assertEquals("com.sun.ejb.codegen.GenericEJBHome_Generated", newClass.getName());
        assertSame(newClass, EJBUtils.loadGeneratedGenericEJBHomeClass(loader));
    }

    private static class CustomGenerator extends Generator implements ClassGeneratorFactory {

        @Override
        public String getGeneratedClass() {
            return "com.sun.ejb.EJBUtilsTestImplFromCustomGenerator";
        }

        @Override
        public String className() {
            return getGeneratedClass();
        }

        @Override
        public void evaluate() {
            Wrapper._clear();
            Wrapper._package(getPackageName(className()));
            Wrapper._interface(PUBLIC, getBaseName(className()));
            Wrapper._classGenerator();
        }
    }


    interface EjbUtilsTestInterface {
        void doSomething();
    }


    public static class EJbUtilsEjbTestClass {

    }
}
