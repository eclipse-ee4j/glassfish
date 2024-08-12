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

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.config.serverbeans.Config;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;

/**
 *
 * @author tjquinn
 */
public class CLIUtil {

    static Config updateConfigIfNeeded(final Config initConfig,
            final String target,
            final ServiceLocator locator) {
        Target targetUtil = locator.getService(Target.class);
        return updateConfigIfNeeded(initConfig, targetUtil, target);

    }

    static Config updateConfigIfNeeded(final Config initConfig,
            final Target targetUtil,
            final String target) {
        Config result = initConfig;
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            result = newConfig;
        }
        return result;
    }

}
