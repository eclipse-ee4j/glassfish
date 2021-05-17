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

import com.sun.enterprise.deployment.EjbDescriptor;
import org.glassfish.apf.context.AnnotationContext;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;


/**
 * This provides a context for a collection of Ejbs with the ejb class name.
 *
 * @Author Shing Wai Chan
 */
public class EjbsContext extends AnnotationContext
        implements ComponentContext {

    private EjbContext[] ejbContexts;
    private String componentClassName;

    public EjbsContext(EjbDescriptor[] ejbDescs, Class ejbClass) {
        ejbContexts = new EjbContext[ejbDescs.length];
        for (int i = 0; i < ejbDescs.length ; i++) {
            ejbContexts[i] = new EjbContext(ejbDescs[i], ejbClass);
        }
        this.componentClassName = ejbClass.getName();
    }

    /**
     * Create a new instance of EjbContext.
     * Note that, for performance, we don't make a safe copy of array here.
     */
    public EjbsContext(EjbContext[] ejbContexts) {
        this.ejbContexts = ejbContexts;
        this.componentClassName = ejbContexts[0].getComponentClassName();
    }

    /**
     * Note that, for performance, we don't make a safe copy of array here.
     */
    public EjbContext[] getEjbContexts() {
        return ejbContexts;
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
