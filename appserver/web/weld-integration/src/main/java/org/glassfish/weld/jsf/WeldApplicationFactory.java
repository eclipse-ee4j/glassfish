/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.jsf;

import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;

public class WeldApplicationFactory extends ApplicationFactory {
    
    private ApplicationFactory delegate;
   
    private Application application;

    // required for CDI
    public WeldApplicationFactory() {

    }
    public WeldApplicationFactory(ApplicationFactory delegate) {
        this.delegate = delegate;
    }
   
    @Override
    public void setApplication(Application application) {
        this.application = application;
        delegate.setApplication(application);
    }

    @Override
    public Application getApplication() {
        if (application == null) {
            application = new WeldApplication(delegate.getApplication());
        }
        return application;
    }
}
