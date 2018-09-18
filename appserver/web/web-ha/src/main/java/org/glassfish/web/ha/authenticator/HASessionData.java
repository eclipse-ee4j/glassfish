/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.ha.authenticator;

import java.io.Serializable;

/**
 * @author Shing Wai Chan
 */
public class HASessionData implements Serializable {
    private String sessionId = null;

    private String contextPath = null;

    public HASessionData(String sessionId, String contextPath) {
        this.sessionId = sessionId;
        this.contextPath = contextPath;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getContextPath() {
        return contextPath;
    }

    @Override
    public int hashCode() {
        int hc = 0;
        if (sessionId != null) {
            hc = sessionId.hashCode() << 4;
        }
        if (contextPath != null) {
            hc = contextPath.hashCode();
        }
        return hc;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HASessionData)) {
            return false;
        }

        HASessionData other = (HASessionData)obj;
        return equals(sessionId, other.sessionId) && equals(contextPath, other.contextPath);
    }

    private boolean equals(String s1, String s2) {
        return (s1 == null && s2 == null) || (s1 != null && s1.equals(s2));
    }
}
