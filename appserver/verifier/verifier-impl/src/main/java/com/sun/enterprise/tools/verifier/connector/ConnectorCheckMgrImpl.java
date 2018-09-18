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
* ConnectorCheckMgrImpl.java
*
* Created on September 18, 2000, 3:59 PM
*/

package com.sun.enterprise.tools.verifier.connector;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.io.ConnectorDeploymentDescriptorFile;
import com.sun.enterprise.tools.verifier.CheckMgr;
import com.sun.enterprise.tools.verifier.VerifierFrameworkContext;
import com.sun.enterprise.tools.verifier.JarCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.dd.ParseDD;

/**
 * <p/>
 * This manager is responsible for performing compliance tests on rar files. rar
 * files contains resource adpaters. </p>
 *
 * @author Jerome Dochez
 */
public class ConnectorCheckMgrImpl extends CheckMgr implements JarCheck {

    /**
     * name of the file containing the list of tests for the connector
     * architecture
     */
    private static final String testsListFileName = "TestNamesConnector.xml"; // NOI18N
    private static final String sunONETestsListFileName = getSunPrefix()
            .concat(testsListFileName);

    public ConnectorCheckMgrImpl(VerifierFrameworkContext verifierFrameworkContext) {
        this.verifierFrameworkContext = verifierFrameworkContext;
    }

    public void check(Descriptor descriptor) throws Exception {
        // run the ParseDD test
        if (getSchemaVersion(descriptor).compareTo("1.5") < 0) { // NOI18N
            ConnectorDeploymentDescriptorFile ddf = new ConnectorDeploymentDescriptorFile();
            File file = new File(new File(URI.create(getAbstractArchiveUri(descriptor))),
                    ddf.getDeploymentDescriptorPath());
            FileInputStream is = new FileInputStream(file);
            try {
                if (is != null) {
                    Result result = new ParseDD().validateConnectorDescriptor(is);
                    result.setComponentName(getArchiveUri(descriptor));
                    setModuleName(result);
                    verifierFrameworkContext.getResultManager().add(result);
                }
            } finally {
               try {
                    if(is!=null)
                        is.close();
                } catch(Exception e) {}
            }
        }

        super.check(descriptor);
    }

    /**
     * return the configuration file name for the list of tests pertinent to the
     * connector architecture
     *
     * @return <code>String</code> filename containing the list of tests
     */
    protected String getTestsListFileName() {
        return testsListFileName;
    }

    /**
     * return the configuration file name for the list of tests pertinent to the
     * connector architecture (SunONE)
     *
     * @return <code>String</code> filename containing the list of tests
     */
    protected String getSunONETestsListFileName() {
        if ((System.getProperty("verifier.tests.sunconnector", "false")).equals(
                "true")) // NOI18N
            return sunONETestsListFileName;
        else
            return null;
    }

    protected String getSchemaVersion(Descriptor descriptor) {
        return ((ConnectorDescriptor) descriptor).getSpecVersion();
    }

    protected void setModuleName(Result r) {
        r.setModuleName(Result.CONNECTOR);
    }

    protected ComponentNameConstructor getComponentNameConstructor(
            Descriptor descriptor) {
        return new ComponentNameConstructor((ConnectorDescriptor)descriptor);
    }

}
