/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.glassfish.microprofile.config;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

class GlassFishConfigsTest {

    @Test
    void entryReleasedWhenGlassFishConfigIsUnreachable() throws Exception {
        final GlassFishConfigs configs = new GlassFishConfigs();
        final GlassFishConfig kept = new GlassFishConfig(() -> null);
        final Config keptDelegate = new GlassFishConfig(() -> null);
        configs.put(kept, keptDelegate);
        for (int i = 0; i < 1000; i++) {
            configs.put(new GlassFishConfig(() -> null), new GlassFishConfig(() -> null));
        }

        final long deadline = System.nanoTime() + SECONDS.toNanos(10);
        while (configs.size() > 1 && System.nanoTime() < deadline) {
            System.gc();
            Thread.sleep(10L);
        }

        assertThat(configs.size(), equalTo(1));
        assertThat(configs.get(kept), sameInstance(keptDelegate));
    }
}
