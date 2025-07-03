/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *   Maximillian Arruda
 */
package org.glassfish.main.jnosql.jakartapersistence.mapping.reflection.nosql;

/**
 * Utility class providing operations for working with strings and character sequences.
 * This class contains methods to check for blankness (presence of whitespace or being null)
 * and to perform various string-related operations.
 */
final class StringUtils {

    private StringUtils() {
        // Private constructor to prevent instantiation of the utility class.
    }

    /**
     * Checks if the given character sequence is blank, which means it is either null or
     * consists only of whitespace characters.
     *
     * @param cs The character sequence to be checked for blankness.
     * @return {@code true} if the character sequence is blank, otherwise {@code false}.
     */
    public static boolean isBlank(final CharSequence cs) {
        if (cs == null || cs.isEmpty()) {
            return true;
        }

        for (int i = 0; i < cs.length(); i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given character sequence is not blank, which means it is not null and
     * contains at least one non-whitespace character.
     *
     * @param cs The character sequence to be checked for non-blankness.
     * @return {@code true} if the character sequence is not blank, otherwise {@code false}.
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }
}

