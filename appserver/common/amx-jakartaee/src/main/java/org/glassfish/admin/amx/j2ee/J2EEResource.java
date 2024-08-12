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

package org.glassfish.admin.amx.j2ee;

import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;

/**
 * J2EEResource is the base model for all J2EE resources.
 * J2EE resources are resources utilized by the J2EE core
 * server to provide the J2EE standard services required by
 * the J2EE platform architecture.
 */
@AMXMBeanMetadata(leaf = true)
public interface J2EEResource extends J2EEManagedObject {

    @ManagedAttribute
    @Description("Get the ObjectName of the corresponding config MBean, if any")
    ObjectName getCorrespondingConfig();


    @ManagedAttribute
    @Description("Get the ObjectName of the corresponding config resource-ref")
    ObjectName getCorrespondingRef();
}
