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

package com.sun.enterprise.web;
import org.jvnet.hk2.annotations.Contract;

/**
 * This interface provides the contract for the Listener for EJB webservice endpoint registrations and unregistrations.
 *
 * Upon receiving an EJB webservice endpoint registration event, this
 * listener will register the EJB webservice endpoint's path as an ad-hoc
 * path with the web container, along with information about the
 * ad-hoc servlet responsible for servicing any requests on this path.
 *
 * Upon receiving an EJB webservice endpoint unregistration event, this
 * listener will unregister the EJB webservice endpoint's path as an
 * ad-hoc path from the web container.
 *
 * This will be called from the WebContainer
 *
 * @author Bhakti Mehta
 */
@Contract
public interface EjbWSRegistryListener {

    public void register();

    public void unregister();

    public void setContainer(WebContainer wc);
}
