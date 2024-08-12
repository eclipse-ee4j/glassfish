/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SystemPropertyBag;

import org.glassfish.internal.api.Target;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author tjquinn
 */
public class CLIUtil {

    static Config chooseConfig(final Target targetService, Config config, final String target) {
        Config targetConfig = targetService.getConfig(target);
        if (targetConfig != null) {
            config = targetConfig;
        }

        return config;
    }

    static SystemPropertyBag chooseTarget(final Domain domain, final String target) {
        SystemPropertyBag systemPropertyBag = null;

        Property domainProp = domain.getProperty("administrative.domain.name");
        String domainName = domainProp.getValue();
        if ("domain".equals(target) || target.equals(domainName)) {
            systemPropertyBag = domain;
        } else {
            systemPropertyBag = domain.getConfigNamed(target);
            if (systemPropertyBag == null) {
                systemPropertyBag = domain.getClusterNamed(target);
            }
            if (systemPropertyBag == null) {
                systemPropertyBag = domain.getServerNamed(target);
            }
        }

        return systemPropertyBag;
    }

}
