/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.web.deployment.util;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.TracerVisitor;
import com.sun.enterprise.deployment.web.ServletFilter;

public class WebBundleTracerVisitor extends TracerVisitor implements WebBundleVisitor {

    @Override
    public void accept (BundleDescriptor descriptor) {
        if (descriptor instanceof WebBundleDescriptor) {
            WebBundleDescriptor webBundle = (WebBundleDescriptor)descriptor;
            accept(webBundle);

            for (WebComponentDescriptor aWebComp : webBundle.getWebComponentDescriptors()) {
                accept(aWebComp);
            }

            for (WebService aWebService : webBundle.getWebServices().getWebServices()) {
                accept(aWebService);
            }

            super.accept(descriptor);

            for (ServletFilter servletFilterDescriptor : webBundle.getServletFilterDescriptors()) {
                accept(servletFilterDescriptor);
            }
        }
    }


    @Override
    public void accept(WebBundleDescriptor descriptor) {
        DOLUtils.getDefaultLogger().info(descriptor.toString());
    }


    protected void accept(WebComponentDescriptor descriptor) {
        DOLUtils.getDefaultLogger().info("==================");
        DOLUtils.getDefaultLogger().info(descriptor.toString());
    }


    protected void accept(ServletFilter descriptor) {
        DOLUtils.getDefaultLogger().info("==================");
        DOLUtils.getDefaultLogger().info(descriptor.toString());
    }
}
