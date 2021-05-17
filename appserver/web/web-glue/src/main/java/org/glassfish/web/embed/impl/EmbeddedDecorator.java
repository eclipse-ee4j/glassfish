/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.embed.impl;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.web.deployment.archivist.WebArchivist;

/**
 * @author Jerome Dochez
 */
public class EmbeddedDecorator implements PopulatorPostProcessor {

    public String getName() {
        return "Embedded";
    }

//    public void decorate(InhabitantsParser inhabitantsParser) {
//        inhabitantsParser.replace(WebArchivist.class, EmbeddedWebArchivist.class);
//
//        // use the fully qualified string class name for WebEntityResolver to avoid dependency on web-glue.
//        inhabitantsParser.replace("org.glassfish.web.WebEntityResolver", EmbeddedWebEntityResolver.class);
//        // inhabitantsParser.replace(WebEntityResolver.class, EmbeddedWebEntityResolver.class);
//    }

    @Override
    public DescriptorImpl process(ServiceLocator serviceLocator, DescriptorImpl descriptorImpl) {

        if (WebArchivist.class.getCanonicalName().equals(descriptorImpl.getImplementation())) {
            descriptorImpl.setImplementation(EmbeddedWebArchivist.class.getCanonicalName());
            // use the fully qualified string class name for WebEntityResolver to avoid dependency on web-glue:
        } else if ("org.glassfish.web.WebEntityResolver".equals(descriptorImpl.getImplementation())) {
            descriptorImpl.setImplementation(EmbeddedWebEntityResolver.class.getCanonicalName());
        }



        return descriptorImpl;
    }

}
