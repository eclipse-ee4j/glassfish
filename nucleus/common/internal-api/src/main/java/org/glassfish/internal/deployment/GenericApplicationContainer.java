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

package org.glassfish.internal.deployment;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;

/**
 * Generic implementation of the ApplicationContainer interface
 *
 * @author Jerome Dochez
 */
public class GenericApplicationContainer implements ApplicationContainer {

    final ClassLoader cl;

    public GenericApplicationContainer(ClassLoader cl) {
        this.cl = cl;
    }

    public Object getDescriptor() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean start(ApplicationContext startupContext) throws Exception {
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        return true;
    }

    public boolean suspend() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean resume() throws Exception {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ClassLoader getClassLoader() {
        return cl;
    }
}
