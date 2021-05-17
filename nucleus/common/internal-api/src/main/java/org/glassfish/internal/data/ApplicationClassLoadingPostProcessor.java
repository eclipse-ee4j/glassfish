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

package org.glassfish.internal.data;

import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * If there has been no other PopulatorPostProcessor that has set the descriptors
 * HK2Loader then this one will set it to an appropriate loader for the application,
 * using the application classloader
 * @author jwells
 *
 */
public class ApplicationClassLoadingPostProcessor implements
        PopulatorPostProcessor {
    private final HK2Loader applicationLoader;

    /* package */
    ApplicationClassLoadingPostProcessor(final ClassLoader appClassLoader) {
        applicationLoader = new HK2Loader() {

            @Override
            public Class<?> loadClass(String className) throws MultiException {
                try {
                    return appClassLoader.loadClass(className);
                }
                catch (Throwable th) {
                    throw new MultiException(th);
                }
            }

        };
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.bootstrap.PopulatorPostProcessor#process(org.glassfish.hk2.api.ServiceLocator, org.glassfish.hk2.utilities.DescriptorImpl)
     */
    @Override
    public DescriptorImpl process(ServiceLocator serviceLocator,
            DescriptorImpl descriptorImpl) {
        if (descriptorImpl.getLoader() != null) return descriptorImpl;

        descriptorImpl.setLoader(applicationLoader);
        return descriptorImpl;
    }

}
