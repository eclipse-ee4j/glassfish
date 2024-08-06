/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.resources;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import org.glassfish.admin.rest.adapter.LocatorBridge;
import org.jvnet.hk2.config.Dom;

/**
 * This is the root class for the generated DomainResource that bootstrap the dom tree with the domain object and add a
 * few sub resources like log viewer or log-level setup which are not described as configbeans but more external config
 * or files (server.log or JDK logger setup
 *
 * @author ludo
 */
public class GlassFishDomainResource extends TemplateRestResource {

    public GlassFishDomainResource() {
        //moved init code in the setHabitat callback from Jersey, to get the correct habitat
        //otherwise we cannot used jersey injected values in a constructor (which does not have a param)
    }

    @Inject
    public void setBaseServiceLocator(LocatorBridge locatorBridge) {
        Dom dom1 = Dom.unwrap(locatorBridge.getRemoteLocator().<Domain>getService(Domain.class));
        childModel = dom1.document.getRoot().model;
        entity = dom1.document.getRoot();
    }
}
