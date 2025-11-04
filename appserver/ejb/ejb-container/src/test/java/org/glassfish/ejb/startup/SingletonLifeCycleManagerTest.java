/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.ejb.startup;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SingletonLifeCycleManagerTest {

    @Test
    public void testDependencies() {
        SingletonLifeCycleManager manager = new SingletonLifeCycleManager(false);

        manager.addDependencies("A", dependsOn("B", "C", "D"));
        manager.addDependencies("B", dependsOn("F"));
        manager.addDependencies("C", dependsOn("E"));
        manager.addDependencies("D", dependsOn("B"));
        manager.addDependencies("E", dependsOn("B"));

        assertThat(manager.computeDependencies("A"), contains("F", "B", "D", "E", "C"));
    }

    @Test
    public void testComplexDependencies() {
        SingletonLifeCycleManager manager = new SingletonLifeCycleManager(false);

        manager.addDependencies("A", dependsOn("D", "E"));
        manager.addDependencies("B", dependsOn("F"));
        manager.addDependencies("C", dependsOn("G", "H"));
        manager.addDependencies("D", dependsOn("I"));
        manager.addDependencies("E", dependsOn("J", "X", "W"));
        manager.addDependencies("F", dependsOn("J", "K"));
        manager.addDependencies("H", dependsOn("L"));
        manager.addDependencies("I", dependsOn("M", "N"));
        manager.addDependencies("J", dependsOn("U"));
        manager.addDependencies("K", dependsOn("U"));
        manager.addDependencies("L", dependsOn("O"));
        manager.addDependencies("U", dependsOn("N", "O"));
        manager.addDependencies("N", dependsOn("P", "Q"));
        manager.addDependencies("O", dependsOn("Q", "R"));
        manager.addDependencies("Q", dependsOn("S", "T"));
        manager.addDependencies("E", dependsOn("X", "W"));
        manager.addDependencies("X", dependsOn("Y"));
        manager.addDependencies("W", dependsOn("Y"));
        manager.addDependencies("Y", dependsOn("Z"));
        manager.addDependencies("Z", dependsOn("O"));

        assertAll(
            () -> assertThat(manager.computeDependencies("A"),
                contains("R", "S", "T", "Q", "O", "Z", "Y", "W", "X", "P", "N", "U", "J", "E", "M", "I", "D")),
            () -> assertThat(manager.computeDependencies("B"),
                contains("R", "S", "T", "Q", "O", "P", "N", "U", "K", "J", "F")),
            () -> assertThat(manager.computeDependencies("C"),
                contains("G", "R", "S", "T", "Q", "O", "L", "H")),
            () -> assertThat(manager.computeDependencies("D"),
                contains("M", "P", "S", "T", "Q", "N", "I")),
            () -> assertThat(manager.computeDependencies("E"),
                contains("R", "S", "T", "Q", "O", "Z", "Y", "W", "X", "P", "N", "U", "J")),
            () -> assertThat(manager.computeDependencies("F"),
                contains("R", "S", "T", "Q", "O", "P", "N", "U", "K", "J")),
            () -> assertThat(manager.computeDependencies("H"), contains("R", "S", "T", "Q", "O", "L")),
            () -> assertThat(manager.computeDependencies("I"), contains("M", "P", "S", "T", "Q", "N")),
            () -> assertThat(manager.computeDependencies("J"),
                contains("R", "S", "T", "Q", "O", "P", "N", "U")),
            () -> assertThat(manager.computeDependencies("K"),
                contains("R", "S", "T", "Q", "O", "P", "N", "U")),
            () -> assertThat(manager.computeDependencies("L"), contains("R", "S", "T", "Q", "O")),
            () -> assertThat(manager.computeDependencies("U"), contains("R", "S", "T", "Q", "O", "P", "N")),
            () -> assertThat(manager.computeDependencies("N"), contains("P", "S", "T", "Q")),
            () -> assertThat(manager.computeDependencies("O"), contains("R", "S", "T", "Q")),
            () -> assertThat(manager.computeDependencies("Q"), contains("S", "T")),
            () -> assertThat(manager.computeDependencies("X"), contains("R", "S", "T", "Q", "O", "Z", "Y")),
            () -> assertThat(manager.computeDependencies("W"), contains("R", "S", "T", "Q", "O", "Z", "Y")),
            () -> assertThat(manager.computeDependencies("Y"), contains("R", "S", "T", "Q", "O", "Z")),
            () -> assertThat(manager.computeDependencies("Z"), contains("R", "S", "T", "Q", "O"))
        );
    }

    @Test
    public void testEmptyDependencies() {
        SingletonLifeCycleManager manager = new SingletonLifeCycleManager(false);

        manager.addDependencies("A", Set.of());
        manager.addDependencies("B", Set.of());
        manager.addDependencies("C", Set.of());

        assertAll(
            () -> assertThat(manager.computeDependencies("A"), empty()),
            () -> assertThat(manager.computeDependencies("B"), empty()),
            () -> assertThat(manager.computeDependencies("C"), empty())
        );
    }

    @Test
    public void testCyclicDependencies() {
        SingletonLifeCycleManager manager = new SingletonLifeCycleManager(false);

        manager.addDependencies("A", dependsOn("D", "E"));
        manager.addDependencies("B", dependsOn("F"));
        manager.addDependencies("C", dependsOn("G", "H"));
        manager.addDependencies("D", dependsOn("I"));
        manager.addDependencies("E", dependsOn("J", "X", "W"));
        manager.addDependencies("F", dependsOn("J", "K"));
        manager.addDependencies("H", dependsOn("L"));
        manager.addDependencies("I", dependsOn("M", "N"));
        manager.addDependencies("J", dependsOn("U"));
        manager.addDependencies("K", dependsOn("U"));
        manager.addDependencies("L", dependsOn("O"));
        manager.addDependencies("U", dependsOn("N", "O"));
        manager.addDependencies("N", dependsOn("P", "Q"));
        manager.addDependencies("O", dependsOn("Q", "R"));
        manager.addDependencies("Q", dependsOn("S", "T"));
        manager.addDependencies("E", dependsOn("X", "W"));
        manager.addDependencies("X", dependsOn("Y"));
        manager.addDependencies("W", dependsOn("Y"));
        manager.addDependencies("Y", dependsOn("Z"));
        manager.addDependencies("Z", dependsOn("O"));
        manager.addDependencies("R", dependsOn("J"));
        manager.addDependencies("S", dependsOn("J"));

        Exception e = assertThrows(RuntimeException.class, () -> manager.computeDependencies("A"));

        assertThat(e.getMessage(), endsWith("U => O? O => R => J => U; O => Q => S => J => U"));
    }

    private Set<String> dependsOn(String... singletons) {
        Set<String> dependsOn = Collections.newSetFromMap(new LinkedHashMap<>());
        dependsOn.addAll(Arrays.asList(singletons));
        return dependsOn;
    }
}
