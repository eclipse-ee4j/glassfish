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

package org.glassfish.ha.common;

/**
 * A Class to hold various info for the {KeyMapper}
 *  from HTTP cookies
 *
 * @author Mahesh Kannan
 *
 */
public class HACookieInfo {

    String newReplica;

    String oldReplica;

    public HACookieInfo(String newReplica, String oldReplica) {
        initialize(newReplica, oldReplica);
    }

    public HACookieInfo initialize(String newReplica, String oldReplica) {
        update(newReplica, oldReplica);

        return this;
    }

    public void update(String newReplica, String oldReplica) {
        if (newReplica != null && newReplica.trim().length() > 0) {
            this.newReplica = newReplica.trim();
        }

        if (oldReplica != null && oldReplica.trim().length() > 0) {
            this.oldReplica = oldReplica.trim();
        }
    }

    void reset() {
        newReplica = null;
        oldReplica = null;
    }

    public String getNewReplicaCookie() {
        return newReplica;
    }

    public String getOldReplicaCookie() {
        return oldReplica;
    }

    @Override
    public String toString() {
        return "HACookieInfo{" +
                "newReplica='" + newReplica + '\'' +
                ", oldReplica='" + oldReplica + '\'' +
                '}';
    }
}
