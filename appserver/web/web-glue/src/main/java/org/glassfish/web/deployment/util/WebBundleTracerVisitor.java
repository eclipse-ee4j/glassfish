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

package org.glassfish.web.deployment.util;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.web.ServletFilter;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.TracerVisitor;
import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;

import java.util.Iterator;

public class WebBundleTracerVisitor extends TracerVisitor implements WebBundleVisitor {

    public WebBundleTracerVisitor() {
    }

    public void accept (BundleDescriptor descriptor) {
        if (descriptor instanceof WebBundleDescriptor) {
            WebBundleDescriptor webBundle = (WebBundleDescriptor)descriptor;
            accept(webBundle);

            for (Iterator<WebComponentDescriptor> i = webBundle.getWebComponentDescriptors().iterator(); i.hasNext();) {
                WebComponentDescriptor aWebComp = i.next();
                accept(aWebComp);
            }

            for (Iterator<WebService> itr = webBundle.getWebServices().getWebServices().iterator(); itr.hasNext();) {
                WebService aWebService = itr.next();
                accept(aWebService);
            }

            super.accept(descriptor);

            for (Iterator<ServletFilter> itr = webBundle.getServletFilterDescriptors().iterator(); itr.hasNext();) {
                ServletFilter servletFilterDescriptor = itr.next();
                accept(servletFilterDescriptor);
            }
        }
    }

   /**
     * visit a web bundle descriptor
     *
     * @param the web bundle descriptor
     */
    public void accept(WebBundleDescriptor descriptor) {
        DOLUtils.getDefaultLogger().info(descriptor.toString());
    }

   /**
     * visit a web component descriptor
     *
     * @param the web component
     */
    protected void accept(WebComponentDescriptor descriptor) {
        DOLUtils.getDefaultLogger().info("==================");
        DOLUtils.getDefaultLogger().info(descriptor.toString());
    }

   /**
     * visit a servlet filter descriptor
     *
     * @param the servlet filter
     */
    protected void accept(ServletFilter descriptor) {
        DOLUtils.getDefaultLogger().info("==================");
        DOLUtils.getDefaultLogger().info(descriptor.toString());
    }
}

