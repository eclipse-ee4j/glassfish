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
import org.glassfish.admin.amx.core.AMXMBeanMetadata;

/**
 * Identifies a Java VM being utilized by a server.
 */
@AMXMBeanMetadata(type = J2EETypes.JVM, leaf = true, singleton = true)
public interface JVM extends J2EEManagedObject {

    /**
     * Note that the Attribute name is case-sensitive
     * "javaVendor" as defined by JSR 77.
     */
    @ManagedAttribute
    String getjavaVendor();


    /**
     * Note that the Attribute name is case-sensitive
     * "javaVersion" as defined by JSR 77.
     */
    @ManagedAttribute
    String getjavaVersion();


    /**
     * Note that the Attribute name is case-sensitive
     * "node" as defined by JSR 77.
     *
     * @return the fully-qualified hostname
     */
    @ManagedAttribute
    String getnode();

}
