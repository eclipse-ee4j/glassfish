/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.webservices.metroglue;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.deployment.common.SimpleDeployer;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
@Service
public class MetroDeployer extends SimpleDeployer<MetroContainer, MetroDeployer.MetroApplicationContainer> {

    public static final class MetroApplicationContainer implements ApplicationContainer<Object> {

        @Override
        public Object getDescriptor() {
            return null;
        }

        @Override
        public boolean start(ApplicationContext startupContxt) {
            return true;
        }

        @Override
        public boolean stop(ApplicationContext stopContext) {
            return true;
        }

        @Override
        public boolean suspend() {
            return true;
        }

        @Override
        public boolean resume() {
            return true;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }
    }

}
