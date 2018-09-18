/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.examples.jersey_cdi.resources;

import java.util.logging.Logger;
import javax.inject.Inject;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * CDI component provider.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */

@Service
public final class JerseyResolver implements JustInTimeInjectionResolver {

    static final Logger LOGGER = Logger.getLogger(JerseyResolver.class.getName());

    @Inject
    ServiceLocator locator;

    @Override
    public boolean justInTimeResolution(Injectee failedInjectionPoint) {
        LOGGER.severe("failedInjectionPoint = " + failedInjectionPoint);
        return false;
    }
}
