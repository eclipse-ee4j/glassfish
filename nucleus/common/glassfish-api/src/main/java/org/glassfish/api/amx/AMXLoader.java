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

package org.glassfish.api.amx;

import javax.management.ObjectName;

import org.glassfish.external.amx.AMXGlassfish;
import org.jvnet.hk2.annotations.Contract;

/**
 * A loader of AMX MBeans. Any module that wants automatic support for loading AMX MBeans should implement this
 * contract, choosing an appropriate name. The loader will be found and instantiated when AMX is loaded.
 *
 * @see AMXValues
 */
@Contract
@org.glassfish.external.arc.Taxonomy(stability = org.glassfish.external.arc.Stability.UNCOMMITTED)
public interface AMXLoader {
    /** property prefix used by AMXLoader MBeans, name to be suffixed to it */
    String LOADER_PREFIX = AMXGlassfish.DEFAULT.amxSupportDomain() + ":type=amx-loader,name=";

    /**
     * Loader a hierarchy of AMX MBeans, returning the ObjectName of the root of the hierarchy.
     */
    ObjectName loadAMXMBeans();

    /**
     *
     * Unload (unregister) AMX MBeans.
     */
    void unloadAMXMBeans();

}
