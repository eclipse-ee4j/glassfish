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

package org.glassfish.gms.admin;

import com.sun.enterprise.ee.cms.core.GroupManagementService;

import java.util.List;

import org.glassfish.gms.bootstrap.GMSAdapter;

class GMSAnnounceSupplementalInfo {
    final public List<String> clusterMembers;
    final public boolean gmsInitiated;
    final public GMSAdapter gmsAdapter;
    final public GroupManagementService gms;

    GMSAnnounceSupplementalInfo(List<String> members, boolean initiated, GMSAdapter adapter) {
        this.clusterMembers = members;
        this.gmsInitiated = initiated;
        this.gmsAdapter = adapter;
        this.gms = adapter.getModule();
    }
}
