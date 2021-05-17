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
import org.glassfish.apf.context.AnnotationContext;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;

/**
 * This provides a context for a collection of web components with the same
 * impl class name.
 *
 * @author Shing Wai Chan
 */
public class WebComponentsContext extends AnnotationContext
            implements ComponentContext {

    private WebComponentContext[] webCompContexts;
    private String componentClassName;

    public WebComponentsContext(WebComponentDescriptor[] webComps) {
        webCompContexts = new WebComponentContext[webComps.length];
        for (int i = 0; i < webComps.length ; i++) {
            webCompContexts[i] = new WebComponentContext(webComps[i]);
        }
        if (webComps[0].isServlet()) {
            componentClassName = webComps[0].getWebComponentImplementation();
        }
    }

    /**
     * Create a new instance of WebComponentContext.
     * Note that, for performance, we don't make a safe copy of array here.
     */
    public WebComponentsContext(WebComponentContext[] webCompContexts) {
        this.webCompContexts = webCompContexts;
        this.componentClassName = webCompContexts[0].getComponentClassName();
    }

    /**
     * Note that, for performance, we don't make a safe copy of array here.
     */
    public WebComponentContext[] getWebComponentContexts() {
        return webCompContexts;
    }

    public void endElement(ElementType type, AnnotatedElement element) {

        if (ElementType.TYPE.equals(type)) {
            // done with processing this class, let's pop this context
            getProcessingContext().popHandler();
        }
    }

    public String getComponentClassName() {
        return componentClassName;
    }
}
