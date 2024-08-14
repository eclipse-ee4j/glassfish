/*
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.j2ee;

import java.util.Set;

import javax.management.ObjectName;

import org.glassfish.admin.amx.j2ee.EJBModule;
import org.glassfish.admin.amx.util.SetUtil;

import static org.glassfish.admin.amx.j2ee.J2EETypes.ENTITY_BEAN;
import static org.glassfish.admin.amx.j2ee.J2EETypes.MESSAGE_DRIVEN_BEAN;
import static org.glassfish.admin.amx.j2ee.J2EETypes.STATEFUL_SESSION_BEAN;
import static org.glassfish.admin.amx.j2ee.J2EETypes.STATELESS_SESSION_BEAN;

public final class EJBModuleImpl extends J2EEModuleImplBase {
    public static final Class<EJBModule> INTF = EJBModule.class;

    public EJBModuleImpl(
            final ObjectName parentObjectName, final Metadata meta) {
        super(parentObjectName, meta, INTF);
    }

    private static final Set<String> EJB_TYPES    = SetUtil.newUnmodifiableStringSet(
        ENTITY_BEAN,
        STATELESS_SESSION_BEAN,
        STATEFUL_SESSION_BEAN,
        MESSAGE_DRIVEN_BEAN
    );

    public String[] getejbs() {
        return getChildrenAsStrings(EJB_TYPES);
    }
}
