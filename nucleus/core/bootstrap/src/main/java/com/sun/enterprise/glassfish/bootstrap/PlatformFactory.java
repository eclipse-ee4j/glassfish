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

import com.sun.enterprise.glassfish.bootstrap.osgi.impl.EquinoxHelper;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.FelixHelper;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.KnopflerfishHelper;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.Platform;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.PlatformHelper;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.StaticHelper;

import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.PLATFORM_PROPERTY_KEY;

class PlatformFactory {

    public static synchronized PlatformHelper getPlatformHelper(Properties properties) {
        Platform platform = Platform.valueOf(properties.getProperty(PLATFORM_PROPERTY_KEY));
        PlatformHelper platformHelper;
        switch (platform) {
            case Felix:
                platformHelper = new FelixHelper(properties);
                break;
            case Knopflerfish:
                platformHelper = new KnopflerfishHelper(properties);
                break;
            case Equinox:
                platformHelper = new EquinoxHelper(properties);
                break;
            case Static:
                platformHelper = new StaticHelper(properties);
                break;
            default:
                throw new RuntimeException("Unsupported platform " + platform);
        }
        return platformHelper;
    }
}
