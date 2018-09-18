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

package org.glassfish.tests.kernel.deployment.container;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.internal.deployment.GenericDeployer;
import org.jvnet.hk2.annotations.Service;

/**
 * Fake container for testing purposes
 *
 * @author Jerome Dochez
 */
@Service(name="FakeContainer")
public class FakeContainer implements Container {

    public Class<? extends Deployer> getDeployer() {
        return GenericDeployer.class;
    }

    public String getName() {
        return "Fake";
    }
}
