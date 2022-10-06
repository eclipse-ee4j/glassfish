/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.concurrent.runtime.deployer.cfg;

import java.io.Serializable;

public final class ConcurrentServiceCfg implements Serializable {
    private static final long serialVersionUID = -9039607497553448223L;

    private final String jndiName;
    private final String contextInfo;
    private final boolean contextInfoEnabled;
    private final String context;

    public ConcurrentServiceCfg(String jndiName) {
        this(jndiName, null, false, null);
    }


    // FIXME: contextInfo can be directly parsed in deployers
    public ConcurrentServiceCfg(String jndiName, String contextInfo, String contextInfoEnabled, String context) {
        this.jndiName = jndiName;
        this.contextInfo = contextInfo;
        this.contextInfoEnabled = Boolean.parseBoolean(contextInfoEnabled);
        this.context = context;
    }


    public ConcurrentServiceCfg(String jndiName, String contextInfo, boolean contextInfoEnabled, String context) {
        this.jndiName = jndiName;
        this.contextInfo = contextInfo;
        this.contextInfoEnabled = contextInfoEnabled;
        this.context = context;
    }


    public String getJndiName() {
        return jndiName;
    }


    public String getContextInfo() {
        return contextInfo;
    }


    public boolean isContextInfoEnabled() {
        return contextInfoEnabled;
    }


    public String getContext() {
        return context;
    }


    @Override
    public String toString() {
        return "ConcurrentServiceCfg[" + jndiName + ", context=" + context + "]";
    }


    protected static int parseInt(String strValue, int defaultValue) {
        if (strValue != null) {
            try {
                return Integer.parseInt(strValue);
            } catch (NumberFormatException e) {
                // ignore, just return default in this case
            }
        }
        return defaultValue;
    }


    protected static long parseLong(String strValue, long defaultValue) {
        if (strValue != null) {
            try {
                return Long.parseLong(strValue);
            } catch (NumberFormatException e) {
                // ignore, just return default in this case
            }
        }
        return defaultValue;
    }
}
