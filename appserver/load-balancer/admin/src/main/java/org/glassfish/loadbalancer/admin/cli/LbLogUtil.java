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

package org.glassfish.loadbalancer.admin.cli;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;

/**
 *
 * @author Kshitiz Saxena
 */
public class LbLogUtil {

    private static final StringManager _strMgr =
            StringManager.getManager(LbLogUtil.class);

    private static final Logger _logger = LogDomains.getLogger(LbLogUtil.class, LogDomains.ADMIN_LOGGER);

    public static StringManager getStringManager(){
        return _strMgr;
    }

    public static Logger getLogger(){
        return _logger;
    }
}
