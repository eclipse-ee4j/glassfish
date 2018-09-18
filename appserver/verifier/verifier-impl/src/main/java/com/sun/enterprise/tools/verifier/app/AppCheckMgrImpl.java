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

package com.sun.enterprise.tools.verifier.app;

import com.sun.enterprise.deployment.Application;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.tools.verifier.CheckMgr;
import com.sun.enterprise.tools.verifier.VerifierFrameworkContext;
import com.sun.enterprise.tools.verifier.JarCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

/**
 * Application harness
 */

public class AppCheckMgrImpl extends CheckMgr implements JarCheck {

    /**
     * name of the file containing the list of tests for the Application
     * architecture
     */
    private static final String testsListFileName = "TestNamesApp.xml"; // NOI18N
    private static final String sunONETestsListFileName = getSunPrefix()
            .concat(testsListFileName);

    public AppCheckMgrImpl(VerifierFrameworkContext verifierFrameworkContext) {
        this.verifierFrameworkContext = verifierFrameworkContext;
    }

    /**
     * Check application for spec. conformance
     *
     * @param descriptor Application descriptor
     */
    public void check(Descriptor descriptor) throws Exception {
        // run persistence tests first.
        checkPersistenceUnits(Application.class.cast(descriptor));

        if (verifierFrameworkContext.isPartition() &&
                !verifierFrameworkContext.isApp())
            return;
        // all tests for embedding application
        super.check(descriptor);
    }

    /**
     * return the configuration file name for the list of tests pertinent to the
     * web app space (jsp and servlet)
     *
     * @return <code>String</code> filename containing the list of tests
     */
    protected String getTestsListFileName() {
        return testsListFileName;
    }

    /**
     * return the configuration file name for the list of tests pertinent to the
     * application scope (SunONE)
     *
     * @return <code>String</code> filename containing the list of tests
     */
    protected String getSunONETestsListFileName() {
        return sunONETestsListFileName;
    }

    /**
     *
     * @param descriptor
     * @return the name of the archive
     */
    protected String getArchiveUri(Descriptor descriptor) {
        return ((Application) descriptor).getName();
    }

    /**
     *
     * @param descriptor
     * @return version of spec the archive corresponds to.
     */
    protected String getSchemaVersion(Descriptor descriptor) {
        return ((RootDeploymentDescriptor) descriptor).getSpecVersion();
    }

    /**
     * Sets the module name in Result object.
     *
     * @param r result object
     */
    protected void setModuleName(Result r) {
        r.setModuleName(Result.APP);
    }

    protected ComponentNameConstructor getComponentNameConstructor(
            Descriptor descriptor) {
        return new ComponentNameConstructor((Application)descriptor);
    }

}
