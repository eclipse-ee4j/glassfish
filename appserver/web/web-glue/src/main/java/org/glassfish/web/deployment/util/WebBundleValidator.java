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
import com.sun.enterprise.deployment.web.MultipartConfig;
import com.sun.enterprise.deployment.web.ServletFilter;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;
import org.glassfish.web.deployment.descriptor.SessionConfigDescriptor;

import java.util.Iterator;
import java.util.Set;

/**
 * This class validates the part of the web bundle descriptor 
 *
 */
public class WebBundleValidator extends ApplicationValidator implements WebBundleVisitor {
    
    public void accept (BundleDescriptor descriptor) {
        if (descriptor instanceof WebBundleDescriptor) {
            WebBundleDescriptor webBundle = (WebBundleDescriptor)descriptor;
            accept(webBundle);

            // Visit all injectables first.  In some cases, basic type
            // information has to be derived from target inject method or 
            // inject field.
            for (InjectionCapable injectable : webBundle.getInjectableResources(webBundle)) {
                accept(injectable);
            }

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
                ServletFilterDescriptor servletFilterDescriptor = (ServletFilterDescriptor) itr.next();
                accept(servletFilterDescriptor);
            }
        }
    }

    /**
     * visit a web bundle descriptor
     *
     * @param descriptor the web bundle descriptor
     */
    public void accept(WebBundleDescriptor descriptor) {
        bundleDescriptor = descriptor;
        application = descriptor.getApplication();

        if (descriptor.getSessionConfig() == null) {
            descriptor.setSessionConfig(new SessionConfigDescriptor());
        }
    }

    /**
     * visit a web component descriptor
     *
     * @param descriptor the web component
     */
    protected void accept(WebComponentDescriptor descriptor) {

        //set default value
        if (descriptor.getLoadOnStartUp() == null) {
            descriptor.setLoadOnStartUp(-1);
        }
        if (descriptor.isAsyncSupported() == null) {
            descriptor.setAsyncSupported(false);
        }

        MultipartConfig multipartConfig = descriptor.getMultipartConfig();
        if (multipartConfig != null) {
            if (multipartConfig.getMaxFileSize() == null) {
                multipartConfig.setMaxFileSize(Long.valueOf(-1));
            }
            if (multipartConfig.getMaxRequestSize() == null) {
                multipartConfig.setMaxRequestSize(Long.valueOf(-1));
            }
            if (multipartConfig.getFileSizeThreshold() == null) {
                multipartConfig.setFileSizeThreshold(Integer.valueOf(0));
            }
        }
        computeRuntimeDefault(descriptor);
    }

    private void computeRuntimeDefault(WebComponentDescriptor webComp) {
        if (!webComp.getUsesCallerIdentity()) {
            computeRunAsPrincipalDefault(
                webComp.getRunAsIdentity(), webComp.getApplication());
        }
    }

    protected void accept(ServletFilterDescriptor descriptor) {
        // set default value
        if (descriptor.isAsyncSupported() == null) {
            descriptor.setAsyncSupported(false);
        }
    }
}
