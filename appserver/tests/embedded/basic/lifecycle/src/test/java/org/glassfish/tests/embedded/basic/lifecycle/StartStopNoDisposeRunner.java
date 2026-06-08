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

package org.glassfish.tests.embedded.basic.lifecycle;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

/**
 * Boots an embedded GlassFish instance in the temporary directory given as the first argument,
 * starts and stops it, and then lets the JVM exit <em>without</em> calling
 * {@link GlassFish#dispose()}.
 * <p>
 * Run in a forked JVM by {@code LifeCycleTest} to verify that the JVM shutdown hook deletes the
 * temporary directory even when {@code dispose()} is never called (issue #25545).
 */
public final class StartStopNoDisposeRunner {

    private StartStopNoDisposeRunner() {
    }

    public static void main(String[] args) throws Exception {
        GlassFishProperties props = new GlassFishProperties();
        props.setProperty("glassfish.embedded.tmpdir", args[0]);

        GlassFishRuntime runtime = GlassFishRuntime.bootstrap();
        GlassFish glassfish = runtime.newGlassFish(props);
        glassfish.start();
        glassfish.stop();

        // Intentionally NOT calling dispose(). System.exit triggers the JVM shutdown hooks,
        // and the embedded GlassFish cleanup hook must remove the temporary directory.
        System.exit(0);
    }
}
