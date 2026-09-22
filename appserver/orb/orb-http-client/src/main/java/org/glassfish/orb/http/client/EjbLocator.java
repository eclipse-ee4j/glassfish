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

package org.glassfish.orb.http.client;

import java.util.Arrays;
import java.util.Objects;

/**
 * Identifies a target bean, and for a stateful bean the particular session.
 * <p>
 * The four name components are GlassFish's own
 * ({@code appName/moduleName/distinctName/beanName}), so a locator can be
 * built directly from a {@code java:global/...} JNDI name without translation.
 */
public final class EjbLocator {

    private final String appName;
    private final String moduleName;
    private final String distinctName;
    private final String beanName;
    private final byte[] sessionId;

    public EjbLocator(String appName, String moduleName, String distinctName, String beanName) {
        this(appName, moduleName, distinctName, beanName, null);
    }

    public EjbLocator(String appName, String moduleName, String distinctName, String beanName, byte[] sessionId) {
        this.appName = Objects.requireNonNull(appName, "appName");
        this.moduleName = Objects.requireNonNull(moduleName, "moduleName");
        this.distinctName = distinctName;
        this.beanName = Objects.requireNonNull(beanName, "beanName");
        this.sessionId = sessionId == null ? null : sessionId.clone();
    }

    public String appName() {
        return appName;
    }

    public String moduleName() {
        return moduleName;
    }

    public String distinctName() {
        return distinctName;
    }

    public String beanName() {
        return beanName;
    }

    public byte[] sessionId() {
        return sessionId == null ? null : sessionId.clone();
    }

    public boolean isStateful() {
        return sessionId != null;
    }

    /** @return a copy of this locator bound to a stateful session. */
    public EjbLocator withSession(byte[] newSessionId) {
        return new EjbLocator(appName, moduleName, distinctName, beanName, newSessionId);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EjbLocator other
                && appName.equals(other.appName)
                && moduleName.equals(other.moduleName)
                && Objects.equals(distinctName, other.distinctName)
                && beanName.equals(other.beanName)
                && Arrays.equals(sessionId, other.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, moduleName, distinctName, beanName) * 31 + Arrays.hashCode(sessionId);
    }

    @Override
    public String toString() {
        return "EjbLocator[" + appName + '/' + moduleName + '/'
                + (distinctName == null ? "-" : distinctName) + '/' + beanName
                + (isStateful() ? ", stateful" : "") + ']';
    }
}
