/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.server;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.xml.stream.XMLInputFactory;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Allow people to inject {@link XMLInputFactory} via {@link Inject}.
 *
 * <p>
 * Component instantiation happens only when someone requests {@link XMLInputFactory},
 * so this is as lazy as it gets.
 *
 * <p>
 * TODO: if we need to let people choose StAX implementation, this is the place to do it.
 *
 * @author Kohsuke Kawaguchi
 */
@Service
@Singleton
public class StAXParserFactory implements Factory<XMLInputFactory> {

    @Override @PerLookup
    public XMLInputFactory provide() {
        return XMLInputFactory.class.getClassLoader() == null ? XMLInputFactory.newFactory()
            : XMLInputFactory.newFactory(XMLInputFactory.class.getName(), XMLInputFactory.class.getClassLoader());
    }


    @Override
    public void dispose(XMLInputFactory instance) {

    }
}
