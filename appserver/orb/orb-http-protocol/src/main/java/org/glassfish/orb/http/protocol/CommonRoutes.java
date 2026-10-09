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

package org.glassfish.orb.http.protocol;

/**
 * Paths that belong to no single service.
 *
 * <pre>
 * GET {ctx}/common/v1/affinity
 * </pre>
 */
public final class CommonRoutes {

    private CommonRoutes() {
    }

    /**
     * @param contextPath the path the endpoint is mounted at
     * @return the path a client calls to obtain its routing cookie
     */
    public static String affinityPath(String contextPath) {
        return contextPath + '/' + Protocol.SVC_COMMON
                + '/' + Protocol.VERSION_SEGMENT
                + '/' + Protocol.OP_AFFINITY;
    }
}
