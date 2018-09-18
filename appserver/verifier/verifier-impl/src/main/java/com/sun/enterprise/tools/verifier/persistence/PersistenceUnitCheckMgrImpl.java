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

package com.sun.enterprise.tools.verifier.persistence;

import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.deployment.common.ModuleDescriptor;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

/**
 * This class is responsible for checking a PU represented by a {@link
 * PersistenceUnitDescriptor}
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitCheckMgrImpl extends CheckMgr {

    // module for which this check mgr is running the test.
    // This string is one of the types defined in Result class.
    private String moduleName;
    private LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
    

    public PersistenceUnitCheckMgrImpl(
            VerifierFrameworkContext verifierFrameworkContext, VerifierTestContext context) {
        this.verifierFrameworkContext = verifierFrameworkContext;
        this.context = context;
    }

    @Override protected void check(Descriptor descriptor) throws Exception {
        PersistenceUnitDescriptor pu =
                PersistenceUnitDescriptor.class.cast(descriptor);
        RootDeploymentDescriptor rootDD = pu.getParent().getParent();
        if(rootDD.isApplication()) {
            moduleName = Result.APP;
        } else {
            ModuleDescriptor mdesc =
                    BundleDescriptor.class.cast(rootDD).getModuleDescriptor();
            final ArchiveType moduleType = mdesc.getModuleType();
            if(moduleType != null && moduleType.equals(DOLUtils.ejbType())) {
                moduleName = Result.EJB;
            } else if (moduleType != null && moduleType.equals(DOLUtils.warType())) {
                moduleName = Result.WEB;
            } else if (moduleType != null && moduleType.equals(DOLUtils.carType())) {
                moduleName = Result.APPCLIENT;
            } else {
                throw new RuntimeException(
                        smh.getLocalString(getClass().getName()+".exception", // NOI18N
                                "Unknown module type : {0}", // NOI18N
                                new Object[] {moduleType}));
            }
        }
        super.check(descriptor);
    }

    /**
     * We override here because there is nothing like sun-persistence.xml.
     * @param uri
     */
    @Override protected void setRuntimeDDPresent(String uri) {
        isDDPresent = false;
    }

    protected ComponentNameConstructor getComponentNameConstructor(
            Descriptor descriptor) {
        return new ComponentNameConstructor(
                PersistenceUnitDescriptor.class.cast(descriptor));
    }

    protected String getTestsListFileName() {
        return "TestNamesPersistence.xml"; // NOI18N
    }

    protected void setModuleName(Result r) {
        r.setModuleName(moduleName);
    }

    protected String getSchemaVersion(Descriptor descriptor) {
        // A PU inherits its schema version from its parent.
        return PersistenceUnitDescriptor.class.cast(descriptor).getParent().
                getSpecVersion();
    }

    protected String getSunONETestsListFileName() {
        return null;
    }

    /**
     * This method returns the path to the module.
     * @param descriptor is a PersistenceUnitDescriptor
     * @return the path to the module
     */
    protected String getAbstractArchiveUri(Descriptor descriptor) {
        String archBase = context.getAbstractArchive().getURI().toString();
        RootDeploymentDescriptor rootDD =
                PersistenceUnitDescriptor.class.cast(descriptor).getParent().getParent();
        if(rootDD.isApplication()) {
            return archBase;
        } else {
            ModuleDescriptor mdesc =
                    BundleDescriptor.class.cast(rootDD).getModuleDescriptor();
            if(mdesc.isStandalone()) {
                return archBase;
            } else {
                return archBase + "/" +
                        FileUtils.makeFriendlyFilename(mdesc.getArchiveUri());
            }
        }
    }
}
