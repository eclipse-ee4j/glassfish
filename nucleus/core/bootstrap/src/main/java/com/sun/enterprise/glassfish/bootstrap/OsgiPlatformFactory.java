/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.glassfish.bootstrap.osgi.impl.EquinoxAdapter;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.FelixAdapter;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.KnopflerfishAdapter;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.OsgiPlatform;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.OsgiPlatformAdapter;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.StaticAdapter;

import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.PLATFORM_PROPERTY_KEY;

class OsgiPlatformFactory {

    public static synchronized OsgiPlatformAdapter getOsgiPlatformAdapter(Properties properties) {
        OsgiPlatform osgiPlatform = OsgiPlatform.valueOf(properties.getProperty(PLATFORM_PROPERTY_KEY));
        OsgiPlatformAdapter osgiPlatformAdapter;
        switch (osgiPlatform) {
            case Felix:
                osgiPlatformAdapter = new FelixAdapter(properties);
                break;
            case Knopflerfish:
                osgiPlatformAdapter = new KnopflerfishAdapter(properties);
                break;
            case Equinox:
                osgiPlatformAdapter = new EquinoxAdapter(properties);
                break;
            case Static:
                osgiPlatformAdapter = new StaticAdapter(properties);
                break;
            default:
                throw new RuntimeException("Unsupported platform " + osgiPlatform);
        }
        return osgiPlatformAdapter;
    }
}
