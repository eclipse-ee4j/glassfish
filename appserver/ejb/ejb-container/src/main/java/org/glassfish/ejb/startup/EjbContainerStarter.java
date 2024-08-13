/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.startup;

import com.sun.ejb.containers.EjbContainerUtil;

import jakarta.inject.Inject;

import org.glassfish.api.container.Container;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.jvnet.hk2.annotations.Service;


/**
 * Ejb container service
 *
 * @author Mahesh Kannan
 */
@Service(name="org.glassfish.ejb.startup.EjbContainerStarter")
public class EjbContainerStarter
    implements Container, PostConstruct, PreDestroy {

    /**
     * Initializes EjbContainerUtilImpl instance with this injection so that
     * its instance is available to subsequent request, e.g., with
     * EjbContainerUtilImpl.getInstance().
     */
    @Inject
    EjbContainerUtil ejbContainerUtilImpl;

    public void postConstruct() {
    }

    public void preDestroy() {
        if (ejbContainerUtilImpl instanceof PreDestroy) {
            ((PreDestroy)ejbContainerUtilImpl).preDestroy();
        }
    }

    public String getName() {
        return "EjbContainer";
    }

    public Class<? extends org.glassfish.api.deployment.Deployer> getDeployer() {
        return EjbDeployer.class;
    }
}
