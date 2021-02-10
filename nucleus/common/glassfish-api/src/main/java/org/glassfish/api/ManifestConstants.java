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

package org.glassfish.api;

/**
 * Authorized manifest entries to hook up to the module management subsystem. These are extensions to the OSGi
 * specifications and therefore not portable.
 *
 * @Author Jerome Dochez
 */
public class ManifestConstants {

    /**
     * Hooks up a module class loader to all implementation of the comma separated list of contracts.
     */
    public final static String GLASSFISH_REQUIRE_SERVICES = "GlassFish-require-services";

    /**
     * Adds a directory as an additional OSGi repository.
     */
    public final static String GLASSFISH_REQUIRE_REPOSITORY = "GlassFish-require-repository";
}
