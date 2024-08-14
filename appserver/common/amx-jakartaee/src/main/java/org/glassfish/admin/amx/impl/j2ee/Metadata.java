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

import java.util.Map;

import javax.management.ObjectName;

/**
 * Used to store extra data in MBeans without having to impact differing constructors.
 * @author llc
 */
public interface Metadata {
    public <T> T getMetadata(final String name, final Class<T> clazz);

    public void add( final String key, final Object value);

    public ObjectName getCorrespondingConfig();
    public ObjectName getCorrespondingRef();
    public String getDeploymentDescriptor();

    public Map<String,Object> getAll();

    /** ObjectName of corresponding Config MBean, if any */
    public static final String CORRESPONDING_CONFIG = "Config";

    /** ObjectName of corresponding Config reference MBean, if any */
    public static final String CORRESPONDING_REF = "CorrespondingRef";

    /** Object reference to parent mbean (the object, not the ObjectName) */
    public static final String PARENT = "Parent";

    /** Object reference to parent mbean (the object, not the ObjectName) */
    public static final String DEPLOYMENT_DESCRIPTOR = "DeploymentDescriptor";

}
