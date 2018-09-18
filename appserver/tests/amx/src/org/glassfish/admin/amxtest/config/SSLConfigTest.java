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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/config/SSLConfigTest.java,v 1.5 2007/05/05 05:23:55 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:23:55 $
*/

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.SSLConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Iterator;
import java.util.Set;


/**
 */
public final class SSLConfigTest
        extends AMXTestBase {
    public SSLConfigTest() {
    }

    public void
    testGetAttrs() {
        final QueryMgr queryMgr = getQueryMgr();

        final String props = Util.makeJ2EETypeProp(XTypes.SSL_CONFIG);
        final Set sslConfigs = queryMgr.queryPropsSet(props);
        final Iterator iter = sslConfigs.iterator();
        while (iter.hasNext()) {
            final SSLConfig ssl = (SSLConfig) iter.next();

            ssl.getCertNickname();
            ssl.getClientAuthEnabled();

            ssl.getSSL2Enabled();
            ssl.getSSL2Ciphers();

            ssl.getSSL3Enabled();
            ssl.getSSL3TLSCiphers();

            ssl.getTLSEnabled();

            ssl.getTLSRollbackEnabled();
        }
    }

}


