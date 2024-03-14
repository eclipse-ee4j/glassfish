/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.shoal.ha.store;

import org.glassfish.ha.common.HACookieManager;
import org.shoal.ha.mapper.DefaultKeyMapper;

import org.glassfish.ha.common.GlassFishHAReplicaPredictor;
import org.glassfish.ha.common.HACookieInfo;

/**
 * @author Mahesh Kannan
 *
 */
public class GlassFishKeyMapper
    extends DefaultKeyMapper
    implements GlassFishHAReplicaPredictor {

    private static final String[] _EMPTY_TARGETS = new String[] {null, null};

    public GlassFishKeyMapper(String instanceName, String groupName) {
        super(instanceName, groupName);
    }


    public HACookieInfo makeCookie(String groupName, Object key, String oldReplicaCookie) {
        String cookieStr = null;

        if (key != null) {
            cookieStr = super.getMappedInstance(groupName, key);// super.getReplicaChoices(groupName, key);
        }
        HACookieInfo ha = new HACookieInfo(cookieStr, oldReplicaCookie);
        return ha;
    }

    @Override
    public String getMappedInstance(String groupName, Object key1) {
        HACookieInfo cookieInfo = HACookieManager.getCurrent();
        if (cookieInfo.getNewReplicaCookie() != null) {
            return cookieInfo.getNewReplicaCookie();
        } else {
            return super.getMappedInstance(groupName, key1);
        }
    }

    /*
    @Override
    public String getReplicaChoices(String groupName, Object key) {
        HACookieInfo cookieInfo = HACookieManager.getCurrent();
        if (cookieInfo.getOldReplicaCookie() != null) {
            return cookieInfo.getOldReplicaCookie();
        } else {
            return super.getReplicaChoices(groupName, key);
        }
    }
    */
}
