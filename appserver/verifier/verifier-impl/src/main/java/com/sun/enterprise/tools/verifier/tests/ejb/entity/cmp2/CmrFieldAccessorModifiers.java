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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2;

import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;

/**
 * Container-managed fields declaration test.
 * CMR fields accessor methods should be public and abstract
 *
 * @author  Jerome Dochez
 * @version 
 */
public class CmrFieldAccessorModifiers extends CmrFieldTest {

    /**
     * run an individual verifier test of a declated cmp field of the class
     *
     * @param entity the descriptor for the entity bean containing the cmp-field    
     * @param f the descriptor for the declared cmp field
     * @param c the class owning the cmp field
     * @parma r the result object to use to put the test results in
     * 
     * @return true if the test passed
     */    
    protected boolean runIndividualCmrTest(Descriptor entity, RelationRoleDescriptor rrd, Class c, Result result) {
        return accessorMethodModifiers(rrd.getCMRField(), c, result);
    }
}
