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

package com.oracle.hk2.devtest.cdi.ejb1.ppp;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.utilities.DescriptorImpl;

import com.oracle.hk2.devtest.cdi.ejb1.scoped.HK2Service;

/**
 * This post-processor will be placed into the META-INF/services
 * of the application.  The test will ensure that it has
 * been properly run
 *
 * @author jwells
 *
 */
public class ApplicationPopulatorPostProcessor implements
        PopulatorPostProcessor {
    public static final String KEY = "key";
    public static final String VALUE = "value";

    /* (non-Javadoc)
     * @see org.glassfish.hk2.bootstrap.PopulatorPostProcessor#process(org.glassfish.hk2.api.ServiceLocator, org.glassfish.hk2.utilities.DescriptorImpl)
     */
    @Override
    public DescriptorImpl process(ServiceLocator serviceLocator,
            DescriptorImpl descriptorImpl) {
        if (!descriptorImpl.getAdvertisedContracts().contains(HK2Service.class.getName())) return descriptorImpl;

        descriptorImpl.addMetadata(KEY, VALUE);
        return descriptorImpl;
    }

}
