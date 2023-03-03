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

package org.glassfish.main.admin.test.tool;

import com.sun.enterprise.config.serverbeans.BindableResource;

import org.glassfish.main.itest.tools.RandomGenerator;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;


/**
 * @author David Matejcek
 */
class RandomGeneratorTest {

    @Test
    void generateRandomString() {
        for (int i = 0; i < 1000; i++) {
            assertThat(RandomGenerator.generateRandomString(), matchesPattern(BindableResource.PATTERN_JNDI));
        }
    }
}
