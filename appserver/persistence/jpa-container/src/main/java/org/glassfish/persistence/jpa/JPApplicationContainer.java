/*
 * Copyright (c) 2005, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.persistence.jpa;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;


/**
 * Represents Application Container for JPA
 * One instance of this object is created per deployed bundle.
 * @author Mitesh Meswani
 */
public class JPApplicationContainer implements ApplicationContainer {

    public JPApplicationContainer() {
    }

    //-------------- Begin Methods implementing ApplicationContainer interface -------------- //
    public Object getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContxt) {
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        return true;
    }

    /**
     * Suspends this application container.
     *
     * @return true if suspending was successful, false otherwise.
     */
    public boolean suspend() {
        // Not (yet) supported
        return false;
    }

    /**
     * Resumes this application container.
     *
     * @return true if resumption was successful, false otherwise.
     */
    public boolean resume() {
        // Not (yet) supported
        return false;
    }

    public ClassLoader getClassLoader() {
        //TODO: Check with Jerome. Should this return anything but null? currently it does not seem so.
        return null;
    }

    //-------------- End Methods implementing ApplicationContainer interface -------------- //

}
