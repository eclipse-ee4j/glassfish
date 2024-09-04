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

import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.EquinoxAdapter;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.FelixAdapter;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.KnopflerfishAdapter;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.OsgiPlatform;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.OsgiPlatformAdapter;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.StaticAdapter;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.PLATFORM_PROPERTY_KEY;

class OsgiPlatformFactory {

    static OsgiPlatformAdapter getOsgiPlatformAdapter(StartupContextCfg cfg) {
        OsgiPlatform osgiPlatform = OsgiPlatform.valueOf(cfg.getProperty(PLATFORM_PROPERTY_KEY));
        OsgiPlatformAdapter osgiPlatformAdapter;
        switch (osgiPlatform) {
            case Felix:
                osgiPlatformAdapter = new FelixAdapter(cfg);
                break;
            case Knopflerfish:
                osgiPlatformAdapter = new KnopflerfishAdapter(cfg);
                break;
            case Equinox:
                osgiPlatformAdapter = new EquinoxAdapter(cfg);
                break;
            case Static:
                osgiPlatformAdapter = new StaticAdapter(cfg);
                break;
            default:
                throw new RuntimeException("Unsupported platform " + osgiPlatform);
        }
        return osgiPlatformAdapter;
    }
}
