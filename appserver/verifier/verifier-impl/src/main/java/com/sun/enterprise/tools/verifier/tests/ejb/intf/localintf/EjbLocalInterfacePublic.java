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

package com.sun.enterprise.tools.verifier.tests.ejb.intf.localintf;

import com.sun.enterprise.tools.verifier.tests.ejb.intf.InterfacePublic;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Set;

/** 
 * Declare local interface as public interface test.  
 * All enterprise bean local interfaces must be declared as public.
 * @author Sheetal Vartak
 */
public class EjbLocalInterfacePublic extends InterfacePublic { 
    
    protected Set<String> getInterfaceNames(EjbDescriptor descriptor) {
        Set<String> intfs = descriptor.getLocalBusinessClassNames();
        if(descriptor.getLocalClassName() != null)
            intfs.add(descriptor.getLocalClassName());
        return intfs;
    }
}
