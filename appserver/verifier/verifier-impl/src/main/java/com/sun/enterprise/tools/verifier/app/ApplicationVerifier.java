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
import com.sun.enterprise.tools.verifier.BaseVerifier;
import com.sun.enterprise.tools.verifier.CheckMgr;
import com.sun.enterprise.tools.verifier.VerifierFrameworkContext;

/**
 * @author Vikas Awasthi
 */
public class ApplicationVerifier extends BaseVerifier {

    private Application application = null;

    public ApplicationVerifier(VerifierFrameworkContext verifierFrameworkContext) {
        this.verifierFrameworkContext = verifierFrameworkContext;
        this.application = verifierFrameworkContext.getApplication();
    }

    /**
     * Responsible for running application based verifier tests on the the web archive.
     * Called from runVerifier in {@link BaseVerifier} class.
     *
     * @throws Exception
     */
    public void verify() throws Exception {
        if (areTestsNotRequired(verifierFrameworkContext.isApp()) &&
                areTestsNotRequired(verifierFrameworkContext.isPersistenceUnits()))
            return;
        preVerification();
        if(verifierFrameworkContext.isPortabilityMode())
            application.setClassLoader(context.getClassLoader());
        CheckMgr checkMgrImpl = new AppCheckMgrImpl(verifierFrameworkContext);
        verify(application, checkMgrImpl);
    }

    public Descriptor getDescriptor() {
        return application;
    }

    protected ClassLoader createClassLoader() {
        return application.getClassLoader();
    }

    /**
     * @return String archive base
     */
    protected String getArchiveUri() {
        return verifierFrameworkContext.getJarFileName();
    }

    protected String[] getDDString() {
        String dd[] = {"META-INF/sun-application.xml", // NOI18N
                       "META-INF/application.xml", 
                       "META-INF/glassfish-application.xml"}; // NOI18N
        return dd;
    }

    protected String getClassPath() {
        return null;
    }
}
