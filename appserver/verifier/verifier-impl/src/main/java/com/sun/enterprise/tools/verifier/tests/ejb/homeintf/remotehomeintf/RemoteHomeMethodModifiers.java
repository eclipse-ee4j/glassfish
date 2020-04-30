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

package com.sun.enterprise.tools.verifier.tests.ejb.homeintf.remotehomeintf;

import com.sun.enterprise.tools.verifier.tests.ejb.homeintf.HomeMethodModifiers;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** 
 * Home methods must be public and not static
 *
 * @author Jerome Dochez
 * @version
 */
public class RemoteHomeMethodModifiers extends HomeMethodModifiers {  
    
    protected String getHomeInterfaceName(EjbDescriptor descriptor) {
	return descriptor.getHomeClassName();
    }
    protected String getSuperInterface() {
	return "jakarta.ejb.EJBHome";
    }
}
