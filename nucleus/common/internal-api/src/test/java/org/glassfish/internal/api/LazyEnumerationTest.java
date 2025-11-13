/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.internal.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for LazyEnumeration to ensure it properly handles multiple enumerations
 * and returns only distinct elements.
 */
class LazyEnumerationTest {

    @Test
    void shouldReturnDistinctElementsFromMultipleEnumerations() {
        Vector<String> vector1 = new Vector<>();
        vector1.add("element1");
        vector1.add("element2");
        vector1.add("duplicate");

        Vector<String> vector2 = new Vector<>();
        vector2.add("element3");
        vector2.add("duplicate"); // This should be filtered out
        vector2.add("element4");

        List<Enumeration<String>> enumerations = List.of(
            vector1.elements(),
            vector2.elements()
        );

        LazyEnumeration<String> lazyEnum = new LazyEnumeration<>(enumerations);
        List<String> result = Collections.list(lazyEnum);

        assertThat(result, hasSize(5));
        assertThat(result, containsInAnyOrder("element1", "element2", "element3", "element4", "duplicate"));
    }

    @Test
    void shouldHandleEmptyEnumerations() {
        Vector<String> emptyVector = new Vector<>();
        Vector<String> nonEmptyVector = new Vector<>();
        nonEmptyVector.add("element1");

        List<Enumeration<String>> enumerations = List.of(
            emptyVector.elements(),
            nonEmptyVector.elements(),
            emptyVector.elements()
        );

        LazyEnumeration<String> lazyEnum = new LazyEnumeration<>(enumerations);
        List<String> result = Collections.list(lazyEnum);

        assertThat(result, hasSize(1));
        assertThat(result, contains("element1"));
    }

    @Test
    void shouldHandleAllEmptyEnumerations() {
        Vector<String> emptyVector1 = new Vector<>();
        Vector<String> emptyVector2 = new Vector<>();

        List<Enumeration<String>> enumerations = List.of(
            emptyVector1.elements(),
            emptyVector2.elements()
        );

        LazyEnumeration<String> lazyEnum = new LazyEnumeration<>(enumerations);

        assertThat(lazyEnum.hasMoreElements(), is(false));
    }

    @Test
    void shouldThrowNoSuchElementExceptionWhenEmpty() {
        List<Enumeration<String>> emptyEnumerations = new ArrayList<>();
        LazyEnumeration<String> lazyEnum = new LazyEnumeration<>(emptyEnumerations);

        assertThrows(NoSuchElementException.class, lazyEnum::nextElement);
    }

    @Test
    void shouldMaintainOrderFromFirstEnumerationThenSecond() {
        Vector<String> vector1 = new Vector<>();
        vector1.add("first");
        vector1.add("second");

        Vector<String> vector2 = new Vector<>();
        vector2.add("third");
        vector2.add("fourth");

        List<Enumeration<String>> enumerations = List.of(
            vector1.elements(),
            vector2.elements()
        );

        LazyEnumeration<String> lazyEnum = new LazyEnumeration<>(enumerations);
        List<String> result = new ArrayList<>();

        while (lazyEnum.hasMoreElements()) {
            result.add(lazyEnum.nextElement());
        }

        assertThat(result, contains("first", "second", "third", "fourth"));
    }

    @Test
    void shouldFilterDuplicatesAcrossMultipleEnumerations() {
        Vector<String> vector1 = new Vector<>();
        vector1.add("same");
        vector1.add("different1");

        Vector<String> vector2 = new Vector<>();
        vector2.add("same"); // Duplicate
        vector2.add("different2");

        Vector<String> vector3 = new Vector<>();
        vector3.add("same"); // Another duplicate
        vector3.add("different3");

        List<Enumeration<String>> enumerations = List.of(
            vector1.elements(),
            vector2.elements(),
            vector3.elements()
        );

        LazyEnumeration<String> lazyEnum = new LazyEnumeration<>(enumerations);
        List<String> result = Collections.list(lazyEnum);

        assertThat(result, hasSize(4));
        assertThat(result, containsInAnyOrder("same", "different1", "different2", "different3"));
    }
}
