/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.microprofile.telemetry.tck;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Adds the {@link TckExecutor} and the {@code META-INF/microprofile-telemetry-tck.properties}
 * file referring to it to every deployment.
 */
public class TckExecutorArchiveProcessor implements ApplicationArchiveProcessor {

    private static final String TCK_PROPERTIES = "META-INF/microprofile-telemetry-tck.properties";
    private static final String EXECUTOR_PROPERTY = "telemetry.tck.executor";

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (archive instanceof WebArchive webArchive) {
            webArchive.addClass(TckExecutor.class)
                .addAsResource(new StringAsset(EXECUTOR_PROPERTY + "=" + TckExecutor.class.getName()), TCK_PROPERTIES);
        }
    }
}
