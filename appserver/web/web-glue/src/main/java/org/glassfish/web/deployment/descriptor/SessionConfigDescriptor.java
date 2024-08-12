/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.deployment.descriptor;

import com.sun.enterprise.deployment.web.CookieConfig;
import com.sun.enterprise.deployment.web.SessionConfig;

import jakarta.servlet.SessionTrackingMode;

import java.util.EnumSet;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;

/**
 * This represents the session-config in web.xml.
 *
 * @author Shing Wai Chan
 */

public class SessionConfigDescriptor extends Descriptor implements SessionConfig {
    public static final int SESSION_TIMEOUT_DEFAULT = 30;

    private int sessionTimeout;
    private CookieConfig cookieConfig = null;
    private Set<SessionTrackingMode> trackingModes = null;

    public SessionConfigDescriptor() {
        sessionTimeout = SESSION_TIMEOUT_DEFAULT;
    }

    /**
     * @return the value in seconds of when requests should time out.
     */
    public int getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     * Sets thew value in seconds after sessions should timeout.
     */
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public CookieConfig getCookieConfig() {
        return cookieConfig;
    }

    public void setCookieConfig(CookieConfig cookieConfig) {
        this.cookieConfig = cookieConfig;
    }

    /**
     * DeploymentDescriptorNode.addNodeDescriptor(node) need this.
     */
    public void setCookieConfig(CookieConfigDescriptor cookieConfigDesc) {
        this.cookieConfig = cookieConfigDesc;
    }

    public void addTrackingMode(String trackingMode) {
        if (trackingModes == null) {
            trackingModes = EnumSet.noneOf(SessionTrackingMode.class);
        }
        trackingModes.add(Enum.valueOf(SessionTrackingMode.class, trackingMode));
    }

    public void removeTrackingMode(String trackingMode) {
        if (trackingModes == null) {
            return;
        }
        trackingModes.remove(Enum.valueOf(SessionTrackingMode.class, trackingMode));
    }

    public Set<SessionTrackingMode> getTrackingModes() {
        if (trackingModes == null) {
            trackingModes = EnumSet.noneOf(SessionTrackingMode.class);
        }
        return trackingModes;
    }

    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\n sessionTimeout ").append(sessionTimeout);
        if (cookieConfig!= null) {
            toStringBuffer.append(cookieConfig);
        }
        if (trackingModes != null) {
            toStringBuffer.append("\n trackingModes ");
            for (SessionTrackingMode tm : trackingModes) {
                toStringBuffer.append(tm);
            }
        }
    }
}
