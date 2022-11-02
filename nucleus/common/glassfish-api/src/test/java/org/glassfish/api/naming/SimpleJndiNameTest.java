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

package org.glassfish.api.naming;

import javax.naming.CompositeName;
import javax.naming.Name;

import org.junit.jupiter.api.Test;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class SimpleJndiNameTest {

    @Test
    public void constructorValidations() {
        assertAll(
            () -> assertThrows(NullPointerException.class, () -> new SimpleJndiName(null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new SimpleJndiName("x:y:z")),
            () -> assertDoesNotThrow(() -> new SimpleJndiName(""))
        );
    }


    @Test
    public void comparisons() {
        assertAll(
            () -> assertEquals(0, SimpleJndiName.of("xxx/yyy").compareTo(SimpleJndiName.of("xxx/yyy"))),
            () -> assertEquals(-1, SimpleJndiName.of("xxx").compareTo(SimpleJndiName.of((String) null))),
            () -> assertEquals(-1, SimpleJndiName.of("xxx").compareTo(SimpleJndiName.of((Name) null))),
            () -> assertTrue(SimpleJndiName.of("a/b/c").contains("b/c")),
            () -> assertEquals(SimpleJndiName.of("xxx").hashCode(), SimpleJndiName.of("xxx").hashCode()),
            () -> assertTrue(SimpleJndiName.of("xxx").equals(SimpleJndiName.of(new CompositeName("xxx")))),
            () -> assertTrue(SimpleJndiName.of("xxx/yyy").equals(SimpleJndiName.of("xxx/yyy")))
        );
    }


    @Test
    public void java() {
        String jndiNameString = "java:x/y";
        SimpleJndiName jndiName = SimpleJndiName.of(jndiNameString);
        assertAll(
            () -> assertFalse(jndiName.isEmpty()),
            () -> assertFalse(jndiName.hasCorbaPrefix()),
            () -> assertTrue(jndiName.hasJavaPrefix()),
            () -> assertTrue(jndiName.hasPrefix("java:")),
            () -> assertFalse(jndiName.hasSuffix("java:")),
            () -> assertFalse(jndiName.isJavaApp()),
            () -> assertFalse(jndiName.isJavaGlobal()),
            () -> assertFalse(jndiName.isJavaComponent()),
            () -> assertFalse(jndiName.isJavaModule()),
            () -> assertEquals(jndiNameString, jndiName.toString()),
            () -> assertEquals(new CompositeName(jndiNameString), jndiName.toName()),
            () -> assertEquals(0, new SimpleJndiName(jndiNameString).compareTo(jndiName)),
            () -> assertFalse(jndiName.isEmpty())
        );
    }


    @Test
    public void javaSubContexts() {
        assertAll(
            () -> assertTrue(SimpleJndiName.of("java:app/x").isJavaApp()),
            () -> assertTrue(SimpleJndiName.of("java:comp/x").isJavaComponent()),
            () -> assertTrue(SimpleJndiName.of("java:comp/env/x").isJavaComponent()),
            () -> assertTrue(SimpleJndiName.of("java:module/x").isJavaModule()),
            () -> assertTrue(SimpleJndiName.of("java:global/x").isJavaGlobal())
        );
    }


    @Test
    public void removals() {
        SimpleJndiName jndiName = SimpleJndiName.of("java:module/x/y/z");
        SimpleJndiName shortName = SimpleJndiName.of("something:somewhere");
        SimpleJndiName empty = SimpleJndiName.of("");
        SimpleJndiName corba = SimpleJndiName.of("corbaname:1::3/4");
        assertAll(
            () -> assertEquals(JNDI_CTX_JAVA_MODULE, jndiName.getPrefix()),
            () -> assertEquals("something:", shortName.getPrefix()),
            () -> assertNull(corba.getPrefix()),
            () -> assertNull(empty.getPrefix()),
            () -> assertEquals(empty, empty.removePrefix()),
            () -> assertSame(corba, corba.removePrefix()),
            () -> assertEquals(SimpleJndiName.of("somewhere"), shortName.removePrefix()),
            () -> assertEquals(SimpleJndiName.of("x/y/z"), jndiName.removePrefix()),
            () -> assertEquals(SimpleJndiName.of("x/y/z"), jndiName.removePrefix(JNDI_CTX_JAVA_MODULE)),
            () -> assertEquals(jndiName, jndiName.removePrefix("notThere")),
            () -> assertEquals(jndiName, jndiName.removePrefix(null)),
            () -> assertEquals(SimpleJndiName.of("java:module/x/"), jndiName.removeSuffix("y/z")),
            () -> assertEquals(jndiName, jndiName.removeSuffix("notThere")),
            () -> assertEquals(jndiName, jndiName.removeSuffix(null))
        );
    }


    @Test
    public void corba() {
        String jndiNameString = "corbaname:x:y";
        SimpleJndiName jndiName = SimpleJndiName.of(jndiNameString);
        assertAll(
            () -> assertFalse(jndiName.isEmpty()),
            () -> assertTrue(jndiName.hasCorbaPrefix()),
            () -> assertFalse(jndiName.hasJavaPrefix()),
            () -> assertTrue(jndiName.hasPrefix("corbaname:")),
            () -> assertTrue(jndiName.hasSuffix(":y")),
            () -> assertFalse(jndiName.isJavaApp()),
            () -> assertFalse(jndiName.isJavaGlobal()),
            () -> assertFalse(jndiName.isJavaComponent()),
            () -> assertFalse(jndiName.isJavaModule()),
            () -> assertEquals(jndiNameString, jndiName.toString()),
            () -> assertEquals(new CompositeName(jndiNameString), jndiName.toName()),
            () -> assertEquals(0, new SimpleJndiName(jndiNameString).compareTo(jndiName)),
            () -> assertFalse(jndiName.isEmpty())
        );
    }
}
