/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.descriptor.runtime;

import org.glassfish.deployment.common.Descriptor;

@Deprecated(forRemoval = true, since = "3.1")
public class PersistenceManagerInUse extends Descriptor {

    private String pm_identifier;
    private String pm_version;

    public PersistenceManagerInUse () {

    }

    public PersistenceManagerInUse(String id, String ver) {
        pm_identifier = id;
        pm_version = ver;
    }

    public String get_pm_identifier() {
        return pm_identifier;
    }

    public String get_pm_version() {
        return pm_version;
    }

    public void set_pm_identifier(String id) {
        pm_identifier = id;
    }

    public void set_pm_version(String ver) {
        pm_version = ver;
    }

}
