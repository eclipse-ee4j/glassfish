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

package org.glassfish.admin.amx.monitoring;

import java.util.Map;

import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.base.Singleton;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.AMXProxy;


/**
        Top-level interface for accessing all MonitoringRoot information.
    <p>
    Call {@link AMXProxy#childrenMap} to get the ServerMon children.
 */
@AMXMBeanMetadata(type="mon", singleton=true, globalSingleton=true)
public interface MonitoringRoot extends AMXProxy, Singleton
{
    @ManagedAttribute
    public Map<String,ServerMon> getServerMon();
}
