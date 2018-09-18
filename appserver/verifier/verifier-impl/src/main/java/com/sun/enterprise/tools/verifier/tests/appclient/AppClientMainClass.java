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

package com.sun.enterprise.tools.verifier.tests.appclient;

import java.lang.reflect.Modifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;

/**
 * Application clients start execution at the main method of the class specified
 * in the Main-Class attribute in the manifest file of the application client’s
 * JAR file. It must be specified in the MANIFEST file.
 * @author Sudipto Ghosh
 */
public class AppClientMainClass extends AppClientTest implements AppClientCheck  {

    public Result check(ApplicationClientDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        String mainClass = descriptor.getMainClassName();
        if (mainClass != null && mainClass.length() > 0) {
            try { 
                Class c = Class.forName(mainClass, false, getVerifierContext().getClassLoader());
                if(!Modifier.isPublic(c.getModifiers())) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName() + ".failed2",
                            "ERROR: Appclient main-class [ {0} ] as specified in the Manifest file is not public.",
                            new Object[] {mainClass}));
                }
            } catch (ClassNotFoundException cnfe) {
                if(debug)
                    cnfe.printStackTrace();
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString(getClass().getName() + ".failed1",
                        "ERROR: Appclient main-class [ {0} ] as specified in the" +
                        " Manifest file is not loadable.",
                        new Object[] {mainClass}));
            }
        } else {
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                            "Appclient main-class is not found. Please check the " +
                    "main-class entry of your appclient manifest file."));
        }
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString(getClass().getName() + ".passed",
                    "main-class entry is defined properly."));
        }
        return result;
    }
}
