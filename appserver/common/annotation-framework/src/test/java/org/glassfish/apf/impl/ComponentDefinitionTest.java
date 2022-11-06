/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.apf.impl;

import com.sun.enterprise.module.single.SingleModulesRegistry;

import jakarta.servlet.http.HttpServlet;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Feel free to update numbers used in tests after you update some of used classes.
 * <p>
 * About greaterThan - be careful, reflection is quite poisonous thing; ie. JaCoCo, HK2, etc. add
 * methods and fields to classes.
 *
 * @author David Matejcek
 */
public class ComponentDefinitionTest {

    @Test
    public void testPrimitives() {
        ComponentDefinition component = new ComponentDefinition(int.class);
        assertThat(component.getFields(), emptyArray());
        assertThat(component.getConstructors(), emptyArray());
        assertThat(component.getMethods(), emptyArray());
    }


    @Test
    public void testNull() {
        assertThrows(NullPointerException.class, () -> new ComponentDefinition(null));
    }


    @Test
    public void testJavaLang() {
        ComponentDefinition component = new ComponentDefinition(String.class);
        assertThat(component.getFields(), emptyArray());
        assertThat(component.getConstructors(), emptyArray());
        assertThat(getMethodNames(component), emptyArray());
    }


    @Test
    public void testServlet() {
        ComponentDefinition component = new ComponentDefinition(HttpServlet.class);
        assertThat(component.getFields(), emptyArray());
        assertThat(component.getConstructors(), emptyArray());
        assertThat(getMethodNames(component), emptyArray());
    }


    @Test
    public void testComponentDefinitionTest() {
        ComponentDefinition component = new ComponentDefinition(ComponentDefinitionTest.class);
        assertAll(
            () -> assertThat(component.getFields(), arrayWithSize(1)),
            () -> assertThat(component.getConstructors(), arrayWithSize(1)),
            () -> assertThat(component.getConstructors()[0].getName(), equalTo(ComponentDefinitionTest.class.getName())),
            () -> assertThat(getMethodNames(component), arrayWithSize(equalTo(37)))
        );
    }


    @Test
    public void testHandlerProcessingResultImpl() {
        ComponentDefinition component = new ComponentDefinition(HandlerProcessingResultImpl.class);
        assertAll(
            () -> assertThat(component.getFields(), arrayWithSize(2)),
            () -> assertThat(component.getFields()[0].getName(), equalTo("results")),
            () -> assertThat(component.getConstructors(), arrayWithSize(2)),
            () -> assertThat(component.getConstructors()[0].getName(), equalTo(HandlerProcessingResultImpl.class.getName())),
            () -> assertThat(getMethodNames(component), arrayWithSize(equalTo(5)))
        );
    }


    @Test
    public void testMethodKeyCollisions() {
        ComponentDefinition component1 = new ComponentDefinition(GrandParent.class);
        ComponentDefinition child1 = new ComponentDefinition(Parent.class);
        ComponentDefinition collision2 = new ComponentDefinition(Child.class);
        ComponentDefinition collision3 = new ComponentDefinition(GrandChild.class);
        assertAll(
            () -> assertThat(component1.getFields(), arrayWithSize(0)),
            () -> assertThat(component1.getConstructors(), arrayWithSize(0)),
            () -> assertThat(getMethodNames(component1),
                arrayContaining("p1", "p2", "p3", "x", "x", "x", "y", "z")),
            () -> assertThat(child1.getFields(), arrayWithSize(0)),
            () -> assertThat(child1.getConstructors(), arrayWithSize(0)),
            () -> assertThat(getMethodNames(child1),
                arrayContaining("p1", "p1", "p2", "p3", "x", "x", "x", "x", "x", "y", "z")),
            () -> assertThat(collision2.getFields(), arrayWithSize(0)),
            () -> assertThat(collision2.getConstructors(), arrayWithSize(1)),
            () -> assertThat(getMethodNames(collision2),
                arrayContaining("p1", "p1", "p1", "p2", "p3", "x", "x", "x", "x", "x", "y", "z")),
            () -> assertThat(collision3.getFields(), arrayWithSize(0)),
            () -> assertThat(collision3.getConstructors(), arrayWithSize(2)),
            () -> assertThat(getMethodNames(collision3),
                arrayContaining("p1", "p1", "p1", "p2", "p3", "x", "x", "x", "x", "x", "y", "y", "z"))
        );
    }


    @Test
    public void testDifferentPackages() {
        ComponentDefinition component1 = new ComponentDefinition(SingleModulesRegistry.class);
        String[] methodNames = getMethodNames(component1);
        assertAll(
            () -> assertThat(component1.getFields(), arrayWithSize(greaterThan(11))),
            () -> assertThat(component1.getConstructors(), arrayWithSize(3)),
            () -> assertThat(Arrays.toString(methodNames), methodNames, arrayWithSize(equalTo(48)))
        );
    }


    @Test
    public void testExclusionOfClassWithNoCanonicalName() {
        // getCanonicalName():
        // ...
        // Returns null if the underlying class does not have a canonical name
        // (i.e., if it is a local or anonymous class ...).
        class Local {};
        assert Local.class.getCanonicalName() == null; // This is test-testing assertion

        assertThat(ComponentDefinition.isExcludedFromAnnotationProcessing(Local.class), is(false));
    }


    private String[] getMethodNames(ComponentDefinition component) {
        return Stream.of(component.getMethods()).map(Method::getName).filter(m -> !"$jacocoInit".equals(m))
            .toArray(String[]::new);
    }

    private static class GrandParent {
        private static void p1() {
        }
        static void p2() {
        }
        public static void p3() {
        }
        public void x() {
        }
        void x(String c) {
        }
        private void x(Integer c) {
        }
        private int y() {
            return 0;
        }
        int z() {
            return 0;
        }
    }
    private static class Parent extends GrandParent {
        static void x(Object o) {
        }
        @Override
        public void x() {
        }
        @Override
        void x(String c) {
        }
        void x(Integer c) {
        }
        @Override
        public int z() {
            return super.z();
        }
        private void p1() {
        }
    }
    public static class Child extends Parent {
        static void x(Object x) {
        }
        @Override
        public void x(Integer c) {
        }
        @Override
        public void x(String c) {
        }
        void p1() {
        }
    }
    public static class GrandChild extends Child {
        private int y() {
            return 0;
        }
        @Override
        public void x(Integer c) {
        }
    }
}
