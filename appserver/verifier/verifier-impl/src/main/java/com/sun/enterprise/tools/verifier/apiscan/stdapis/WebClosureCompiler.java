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

/*
 * Webthis.java
 *
 * Created on August 24, 2004, 11:28 AM
 */

package com.sun.enterprise.tools.verifier.apiscan.stdapis;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoader;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompilerImpl;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WebClosureCompiler extends ClosureCompilerImpl {
    private static Logger logger = Logger.getLogger("apiscan.stdapis"); // NOI18N
    private static final String myClassName = "WebClosureCompiler"; // NOI18N
    private String specVersion;

    public WebClosureCompiler(String specVersion, ClassFileLoader cfl) {
        super(cfl);
        logger.entering(myClassName, "init<>", specVersion); // NOI18N
        this.specVersion = specVersion;
        addStandardAPIs();
    }

    //this method adds APIs specific to versions.
    protected void addStandardAPIs() {
        String apiName = "web_app_" + specVersion; // NOI18N
        Collection classes = APIRepository.Instance().getClassesFor(apiName);
        for (Iterator i = classes.iterator(); i.hasNext();) {
            addExcludedClass((String) i.next());
        }
        Collection pkgs = APIRepository.Instance().getPackagesFor(apiName);
        for (Iterator i = pkgs.iterator(); i.hasNext();) {
            addExcludedPackage((String) i.next());
        }
        Collection patterns = APIRepository.Instance().getPatternsFor(apiName);
        for (Iterator i = patterns.iterator(); i.hasNext();) {
            addExcludedPattern((String) i.next());
        }
    }
}
