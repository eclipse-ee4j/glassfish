/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.batch.spi.impl;

import com.ibm.jbatch.spi.BatchSecurityHelper;
import com.sun.enterprise.config.serverbeans.Config;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;

@Service
public class GlassFishBatchSecurityHelper
    implements BatchSecurityHelper {

    @Inject
    private InvocationManager invocationManager;

    @Inject
    Config config;

    private ThreadLocal<Boolean> invocationPrivilege = new ThreadLocal<>();

    public void markInvocationPrivilege(boolean isAdmin) {
        invocationPrivilege.set(isAdmin);
    }

    @Override
    public String getCurrentTag() {
        ComponentInvocation compInv = invocationManager.getCurrentInvocation();
        return compInv == null
            ? null : (config.getName() + ":" + compInv.getAppName());
    }

    @Override
    public boolean isAdmin(String tag) {
        Boolean result =  invocationPrivilege.get();
        return result != null ? result : tag == null;
    }

    public boolean isVisibleToThisInstance(String tagName) {
        return tagName.startsWith(config.getName());
    }
}
