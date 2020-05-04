/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.cdi.ejb1.scoped;

import javax.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * @author jwells
 *
 */
@RequestScoped
public class CustomScopedEjb {
    @Inject
    private HK2Service hk2Service;
    
    @Inject @Named
    private HK2NamedService rumplestiltskin;
    
    @Inject @Named(HK2NamedServiceFactory2.NAME)
    private HK2NamedService carol;
    
    public void checkMe() {
        int jobValue = hk2Service.doAJob();
        
        if (jobValue != HK2Service.RETURN_VALUE) {
            throw new AssertionError("The doAJob method should have returned " + HK2Service.RETURN_VALUE +
                    " but returned " + jobValue);
        }
        
        if (!rumplestiltskin.getName().equals(HK2NamedServiceFactory.NAMED_SERVICE_NAME)) {
            throw new AssertionError("The naked @Named HK2NamedService was not set or had the wrong name: " + rumplestiltskin.getName());
        }
        
        if (!carol.getName().equals(HK2NamedServiceFactory2.NAME)) {
            throw new AssertionError("The specific @Named HK2NamedService was not set or had the wrong name: " + carol.getName());
        }
    }

}
