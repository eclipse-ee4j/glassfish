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

package org.glassfish.api.naming;

import org.jvnet.hk2.annotations.Contract;

/**
 */

@Contract
public interface ComponentNamingUtil {

    /**
     * Utility for discovering the internal global name under which an application-wide resource is made available (e.g. for
     * access from an app client)
     *
     * @param appName name of application within which dependency is defined
     * @param origJavaAppName logical java:app name of the dependency
     * @return
     */
    String composeInternalGlobalJavaAppName(String appName, String origJavaAppName);

}
