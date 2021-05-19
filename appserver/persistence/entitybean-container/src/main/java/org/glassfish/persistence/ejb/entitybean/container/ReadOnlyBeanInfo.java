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

package org.glassfish.persistence.ejb.entitybean.container;

import java.util.Date;

/**
 * Per-primary key information stored for read-only beans.
 *
 * @author Kenneth Saks
 */

final class ReadOnlyBeanInfo
{

    Object primaryKey;

    // Used to track staleness versus the bean-level refresh.
    int beanLevelSequenceNum;

    // Set to true when a programmatic refresh takes place.
    boolean refreshNeeded;

    // Sequence number associated with a point in time when refresh occurred.
    // Each context for this pk also has a sequence number value.  If they
    // differ it means the context needs an ejbLoad.
    int pkLevelSequenceNum;

    // last time when refresh was programattically requested for this PK.
    long lastRefreshRequestedAt;

    // time at which refresh actually occurred.
    long lastRefreshedAt;

    Object    cachedEjbLocalObject;        //Cached only for findByPK

    Object    cachedEjbObject;        //Cached only for findByPK

    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("Read Only Bean Info for " + primaryKey + "\n");
        buffer.append("Refresh needed = " + refreshNeeded + "\n");
        buffer.append("Bean level sequence num = " + beanLevelSequenceNum
                      + "\n");
        buffer.append("PK level sequence num = " + pkLevelSequenceNum + "\n");
        if( lastRefreshRequestedAt > 0 ) {
            buffer.append("Last refresh requested at " +
                          new Date(lastRefreshRequestedAt)
                          + "\n");
        } else {
            buffer.append("Refresh has never been requested\n");
        }
        if( lastRefreshedAt > 0 ) {
            buffer.append("Last refreshed at " +
                          new Date(lastRefreshedAt) + "\n");
        } else {
            buffer.append("Never refreshed\n");
        }

        return buffer.toString();
    }

}
