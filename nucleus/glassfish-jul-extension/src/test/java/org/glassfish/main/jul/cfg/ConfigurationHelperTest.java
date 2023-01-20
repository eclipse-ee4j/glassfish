/*
 * Copyright (c) 2022, 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.cfg;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
class ConfigurationHelperTest {

    @Test
    void getBoolean() {
        final ConfigurationHelper helper = new ConfigurationHelper(ConfigurationHelperTest.class.getPackage().getName(),
            ConfigurationHelper.ERROR_HANDLER_PRINT_TO_STDERR);
        assertAll(
            () -> assertFalse(helper.getBoolean(() -> "boolean.false", true), "boolean.false"),
            () -> assertTrue(helper.getBoolean(() -> "boolean.true", false), "boolean.true"),
            () -> assertFalse(helper.getBoolean(() -> "boolean.incorrect", false), "boolean.incorrect and false as default"),
            () -> assertTrue(helper.getBoolean(() -> "boolean.incorrect", true), "boolean.incorrect and true as default"),
            () -> assertNull(helper.getBoolean(() -> "boolean.unset", null), "boolean.unset and null as default"),
            () -> assertTrue(helper.getBoolean(() -> "boolean.unset", true), "boolean.unset and true as default")
        );
    }


    @Test
    void getList() {
        final ConfigurationHelper helper = new ConfigurationHelper(ConfigurationHelperTest.class.getPackage().getName(),
            ConfigurationHelper.ERROR_HANDLER_PRINT_TO_STDERR);
        assertAll(
            () -> assertThat(helper.getList(() -> "multilineList", null), hasSize(2)),
            () -> assertThat(helper.getList(() -> "multilineList", null), contains("abc", "def"))
        );

    }
}
