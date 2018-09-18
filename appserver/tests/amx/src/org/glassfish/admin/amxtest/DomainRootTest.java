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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/DomainRootTest.java,v 1.6 2007/05/05 05:23:50 tcfujii Exp $
* $Revision: 1.6 $
* $Date: 2007/05/05 05:23:50 $
*/
package org.glassfish.admin.amxtest;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.NotificationEmitterService;

import java.io.IOException;
import java.util.Map;

/**
 */
public final class DomainRootTest
        extends AMXTestBase {

    public DomainRootTest()
            throws IOException {
    }

    public void
    testGetDomain() {
        getDomainRoot();
    }

    public void
    testGetDottedNames() {
        if (checkNotOffline("testGetDottedNames")) {
            assert (getDomainRoot().getDottedNames() != null);
        }
    }


    public void
    testGetDomainNotificationEmitterService() {
        assert (getDomainRoot().getDomainNotificationEmitterService() != null);
    }


    public void
    testGetDomainNotificationEmitterServiceMap() {
        final Map<String, NotificationEmitterService> services =
                getDomainRoot().getNotificationEmitterServiceMap();
        assert (services != null);

        for (NotificationEmitterService s : services.values()) {
            s.getListenerCount();
        }
    }


    public void
    testAMXReady() {
        final DomainRoot domainRoot = getDomainRoot();

        while (!domainRoot.getAMXReady()) {
            mySleep(10);
        }
    }

    public void
    testWaitAMXReady() {
        final DomainRoot domainRoot = getDomainRoot();

        domainRoot.waitAMXReady();
    }
}


