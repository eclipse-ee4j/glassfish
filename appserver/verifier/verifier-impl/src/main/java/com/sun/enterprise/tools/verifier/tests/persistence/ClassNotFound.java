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

package com.sun.enterprise.tools.verifier.tests.persistence;

import com.sun.enterprise.tools.verifier.tests.VerifierTest;
import com.sun.enterprise.tools.verifier.tests.VerifierCheck;
import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;

/**
 * The names of classes specified in persistence.xml must be loadable.
 * TopLink simply ignores if class could not be loaded. So we need this test.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ClassNotFound extends VerifierTest implements VerifierCheck {
    public Result check(Descriptor descriptor) {
        Result result = getInitializedResult();
        addErrorDetails(result, getVerifierContext().getComponentNameConstructor());
        result.setStatus(Result.PASSED);
        final PersistenceUnitDescriptor pu = PersistenceUnitDescriptor.class.cast(descriptor);
        for(String className : pu.getClasses()) {
            try {
                Class.forName(className, false, getVerifierContext().getClassLoader());
            } catch (ClassNotFoundException e) {
                result.failed(smh.getLocalString(getClass().getName() + "failed1",
                        "Class [ {0} ] could not be loaded", new Object[]{className}));
            } catch (NoClassDefFoundError e) {
                result.failed(smh.getLocalString(getClass().getName() + "failed2",
                        "Class [ {0} ] could not be loaded " +
                        "because a dependent class could not be loaded. See reason:\n [ {1} ]",
                        new Object[]{className,e.getMessage()}));
            }
        }
        return result;
    }
}
