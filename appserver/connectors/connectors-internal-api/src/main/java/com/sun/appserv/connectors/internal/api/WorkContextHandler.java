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

package com.sun.appserv.connectors.internal.api;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface WorkContextHandler {
    /**
     * indicates whether the provided workContextClass is supported by the container
     *
     * @param strict                 indicates whether the type-check need to be strict or not i.e.,
     *                               exact type or its super-class type
     * @param workContextClassName inflow context class name
     * @return boolean indicating whether the workContextClass is supported or not
     */
    public boolean isContextSupported(boolean strict, String workContextClassName);


    /**
     * initialize the work-context-handler
     * @param raName resource-adapter name
     * @param loader class-loader of the resource-adapter
     */
    public void init(String raName, ClassLoader loader);
}
