/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.api;

import org.jvnet.hk2.annotations.Contract;

/**
 * We support two policies:
 * 1. All standalone RARs are available to all other applications. This is the
 * Java EE 5 specific behavior.
 * 2. An application has visbility to only those standalone RARs that it
 * depends on. This is the new behavior defined in Java EE 6 as well as
 * JCA 1.6 spec. See https://glassfish.dev.java.net/issues/show_bug.cgi?id=5380
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Contract
public interface ConnectorClassLoaderService {

    /**
     * provides connector-class-loader for the specified application
     * If application is null, global connector class loader will be provided
     * @param appName application-name
     * @return class-loader
     */
    DelegatingClassLoader getConnectorClassLoader(String appName);
}
