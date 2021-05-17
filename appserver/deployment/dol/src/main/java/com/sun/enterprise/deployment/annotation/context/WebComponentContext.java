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

package com.sun.enterprise.deployment.annotation.context;

import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.SecurityConstraint;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This provides a context for a of web component.
 *
 * @author Shing Wai Chan
 */
public class WebComponentContext extends ResourceContainerContextImpl {
    private WebComponentDescriptor webComp = null;

    public WebComponentContext(WebComponentDescriptor wComp) {
        setDescriptor(wComp);
        if (wComp.isServlet()) {
            componentClassName = wComp.getWebComponentImplementation();
        }
    }

    public WebComponentDescriptor getDescriptor() {
        return webComp;
    }

    public void setDescriptor(WebComponentDescriptor webComp) {
        this.webComp = webComp;
        descriptor = webComp.getWebBundleDescriptor();
    }

    public void endElement(ElementType type, AnnotatedElement element) {

        if (ElementType.TYPE.equals(type)) {
            // done with processing this class, let's pop this context
            getProcessingContext().popHandler();
        }
    }
}
