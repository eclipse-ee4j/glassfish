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

package org.glassfish.concurrent.runtime.deployer;


import java.io.Serializable;

public abstract class BaseConfig implements Serializable {

    public int parseInt(String strValue, int defaultValue) {
        if (strValue != null) {
            try {
                int intValue = Integer.parseInt(strValue);
                return intValue;
            } catch (NumberFormatException e) {
                // ignore, just return default in this case
            }
        }
        return defaultValue;
    }

    public long parseLong(String strValue, long defaultValue) {
        if (strValue != null) {
            try {
                long longValue = Long.parseLong(strValue);
                return longValue;
            } catch (NumberFormatException e) {
                // ignore, just return default in this case
            }
        }
        return defaultValue;
    }

    public enum TYPE {
        MANAGED_EXECUTOR_SERVICE,
        MANAGED_SCHEDULED_EXECUTOR_SERVICE,
        MANAGED_THREAD_FACTORY,
        CONTEXT_SERVICE
    }

    protected String jndiName = null;
    protected String contextInfo = null;
    protected String contextInfoEnabled;

    public BaseConfig(String jndiName, String contextInfo, String contextInfoEnabled) {
        this.jndiName = jndiName;
        this.contextInfo = contextInfo;
        this.contextInfoEnabled = contextInfoEnabled;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getContextInfo() {
        return contextInfo;
    }

    public String getContextInfoEnabled() {
        return contextInfoEnabled;
    }

    abstract TYPE getType();
}
