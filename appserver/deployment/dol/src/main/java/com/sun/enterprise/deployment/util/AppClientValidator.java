/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;

/**
 * This class validates an application client descriptor
 *
 */
public class AppClientValidator extends ApplicationValidator implements AppClientVisitor {

    @Override
    public void accept (BundleDescriptor descriptor) {
        if (descriptor instanceof ApplicationClientDescriptor) {
            ApplicationClientDescriptor appClientDesc = (ApplicationClientDescriptor)descriptor;
            accept(appClientDesc);

            // Visit all injectables first.  In some cases, basic type
            // information has to be derived from target inject method or
            // inject field.
            for(InjectionCapable injectable : appClientDesc.getInjectableResources(appClientDesc)) {
                accept(injectable);
            }

            super.accept(descriptor);
        }
    }

    @Override
    public void accept(ApplicationClientDescriptor appclientdescriptor) {
        bundleDescriptor = appclientdescriptor;
        application = appclientdescriptor.getApplication();

        // set the default lifecycle callback class
        for (LifecycleCallbackDescriptor next :
            appclientdescriptor.getPreDestroyDescriptors()) {
            next.setDefaultLifecycleCallbackClass(
                appclientdescriptor.getMainClassName());
        }

        for (LifecycleCallbackDescriptor next :
            appclientdescriptor.getPostConstructDescriptors()) {
            next.setDefaultLifecycleCallbackClass(
                appclientdescriptor.getMainClassName());
        }
    }
}
