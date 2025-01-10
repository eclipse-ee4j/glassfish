/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.api.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Ondro Mihalyi
 */
public class ParameterMapTest {

    @ParameterizedTest
    @MethodSource("valuesForMaskValues")
    void maskValues(final Map<String, String> mapWithSecrets, final Set<String> secretKeys) {
        ParameterMap parameterMap = new ParameterMap();
        mapWithSecrets.forEach(parameterMap::add);
        final ParameterMap maskedMap = parameterMap.getMaskedMap(secretKeys);

        assertIterableEquals(mapWithSecrets.keySet(), maskedMap.keySet(), "Masked map should contian the same keys");
        maskedMap.entrySet().forEach(entry -> {
            assertTrue(!keyIsSecret(entry, secretKeys) || valueIsMasked(entry),
                    () -> "Entry is either not secret or should contain masked value. Value is: " + entry.getValue());
        });
    }

    private static Stream<Arguments> valuesForMaskValues() {
        return Stream.of(
                Arguments.of(
                        Map.of("secretKey", "secret"),
                        Set.of("secretKey")
                ),
                Arguments.of(
                        Map.of(),
                        Set.of("secretKey")
                ),
                Arguments.of(
                        Map.of("secretKey", "secret",
                                "notSecretKey", "hello"),
                        Set.of("secretKey")
                ),
                Arguments.of(
                        Map.of("anotherNotSecretKey", "hello",
                                "secretKey", "secret",
                                "anotherSecretKey", "secret",
                                "notSecretKey", "hello"),
                        Set.of("secretKey", "anotherSecretKey")
                ),
                Arguments.of(
                        Map.of("anotherNotSecretKey", "hello",
                                "notSecretKey", "hello"),
                        Set.of("")
                )
        );
    }

    static boolean keyIsSecret(Map.Entry<String, List<String>> entry, Set<String> secretKeys) {
        return secretKeys.contains(entry.getKey());
    }

    static boolean valueIsMasked(Map.Entry<String, List<String>> entry) {
        return entry.getValue().size() == 1 && entry.getValue().get(0).startsWith("*");
    }
}
