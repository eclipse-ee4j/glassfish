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

package org.glassfish.admin.amx.j2ee;

import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;

/**
 * A J2EE WebModule. Extends the J2EE management model with
 * additional product-specific Attributes and operations.
 * <br>
 * The monitoring peer as returned from getMonitoringPeer() is
 * {@link org.glassfish.admin.amx.monitor.WebModuleVirtualServerMonitor}
 */

@AMXMBeanMetadata(type = J2EETypes.WEB_MODULE, leaf = true)
public interface WebModule extends J2EEModule {

    /**
     * Note that the Attribute name is case-sensitive
     * "servlets" as defined by JSR 77.
     */
    @ManagedAttribute
    String[] getservlets();


    @ManagedOperation
    void reload();
}
