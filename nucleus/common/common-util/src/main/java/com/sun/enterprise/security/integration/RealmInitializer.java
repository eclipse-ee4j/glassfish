/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.integration;

/**
 * Interface to facilitate Initialization of the injected Realm Instance with Application Descriptor info see
 * com.sun.enterprise.web.WebContainer and com.sun.web.security.RealmAdapter
 */
public interface RealmInitializer {

    void initializeRealm(Object bundledescriptor, String realmName);

    /**
     * Sets the virtual server on which the web module (with which this RealmAdapter is associated with) has been deployed.
     *
     * @param container The virtual server
     */
    //TODO: FIXME, dilution parameter type from Container to Object
    void setVirtualServer(Object container);

    /**
     * Clean up security and policy context.
     */
    void logout();

    void updateWebSecurityManager();

}
