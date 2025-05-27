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
package org.glassfish.main.jdke.props;

import java.lang.System.Logger;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Extension for {@link System#setProperties(java.util.Properties)} and methods around.
 */
public class SystemProperties {
    private static final Logger LOG = System.getLogger(SystemProperties.class.getName());

    /**
     * Sets a system property, logging the action.
     *
     * @param key must not be null.
     * @param value if null, will remove the property
     * @param force if false, will not override an existing property with the same key
     */
    public static void setProperty(String key, String value, boolean force) {
        final String oldValue = System.getProperty(key);
        if (oldValue == null) {
            LOG.log(DEBUG, "Setting property {0} to {1}", key, value);
        } else {
            if (oldValue.equals(value)) {
                LOG.log(TRACE, "Property {0} already set to the same value {1}", key, oldValue);
                return;
            }
            if (force) {
                LOG.log(WARNING, "Property {0} already set to {1}, overriding with {2}", key, oldValue, value);
            } else {
                LOG.log(WARNING, "Property {0} already set to {1}, not overriding with {2}", key, oldValue, value);
                return;
            }
        }
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
