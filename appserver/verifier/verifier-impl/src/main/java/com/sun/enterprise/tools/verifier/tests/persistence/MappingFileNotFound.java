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

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.VerifierCheck;
import com.sun.enterprise.tools.verifier.tests.VerifierTest;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapping files specified using <mapping-file> element in persistence.xml
 * should be resource-loadable from the application classpath.
 *
 * @author bshankar@sun.com
 */
public class MappingFileNotFound extends VerifierTest implements VerifierCheck {
    
    public Result check(Descriptor descriptor) {
        Result result = getInitializedResult();
        result.setStatus(Result.PASSED);
        addErrorDetails(result, getVerifierContext().getComponentNameConstructor());
        
        PersistenceUnitDescriptor pu = PersistenceUnitDescriptor.class.cast(descriptor);
        List<String> mappingFileNames = new ArrayList<String>(pu.getMappingFiles());
        for (String mappingFileName : mappingFileNames) {
            if(getVerifierContext().getClassLoader().getResource(mappingFileName) == null) {
                result.failed(smh.getLocalString(
                        getClass().getName() + "failed",
                        "Mapping file [ {0} ] specified in persistence.xml does not exist in the application.",
                        new Object[]{mappingFileName}));
            }
        }
        return result;
    }
    
}
