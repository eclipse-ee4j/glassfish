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

package com.sun.enterprise.tools.verifier.tests.ejb.ejb30;

import com.sun.enterprise.tools.verifier.tests.InjectionTargetTest;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.InjectionCapable;

import java.util.List;

/**
 * The field or method where injection annotation is used may have any access 
 * qualifier (public , private , etc.) but must not be static or final.
 * 
 * @author Vikas Awasthi
 */
public class EJBInjectionAnnotationModifiers extends InjectionTargetTest {
    
    protected List<InjectionCapable> getInjectables(String className) {
        EjbDescriptor descriptor = (EjbDescriptor) getDescriptor();
        return descriptor.getInjectableResourcesByClass(getClassName());
    }

    protected String getClassName() {
        EjbDescriptor descriptor = (EjbDescriptor) getDescriptor();
        return descriptor.getEjbClassName();
    }
}
