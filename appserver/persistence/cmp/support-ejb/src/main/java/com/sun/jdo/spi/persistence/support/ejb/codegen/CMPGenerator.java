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

package com.sun.jdo.spi.persistence.support.ejb.codegen;

import java.io.File;
import java.util.Collection;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.IASEjbCMPEntityDescriptor;

/**
 * This interface must be implemented by all CMP code generators.
 */

public interface CMPGenerator {

    /**
     * This method is called once for each ejb module in the application
     * that contains CMP beans.
     * Only one #init() method can be called.
     * @deprecated
     * This method is not used by the deployment back end, and should be removed
     * as soon as the TestFramework is fixed.
     * @param ejbBundleDescriptor the EjbBundleDescriptor associated with this
     * ejb module.
     * @param cl the ClassLoader that loaded user defined classes.
     * @param bundlePathName full path to the directory where this bundle's
     * files are located.
     * @throws GeneratorException if there is a problem initializing bean
     * processing.
     */
    void init(EjbBundleDescriptorImpl ejbBundleDescriptor, ClassLoader cl,
        String bundlePathName) throws GeneratorException;

    /**
     * This method is called once for each ejb module in the application
     * that contains CMP beans.
     * Only one #init() method can be called.
     * @param ejbBundleDescriptor the EjbBundleDescriptor associated with this
     * ejb module.
     * @param ctx the DeploymentContext associated with the deployment request.
     * @param bundlePathName full path to the directory where this bundle's
     * files are located.
     * @param generatedXmlsPathName full path to the directory where the
     * generated files are located.
     * @throws GeneratorException if there is a problem initializing bean
     * processing.
     */
    void init(EjbBundleDescriptorImpl ejbBundleDescriptor, DeploymentContext ctx,
        String bundlePathName, String generatedXmlsPathName)
            throws GeneratorException;

    /**
     * This method is called once for each CMP bean of the corresponding ejb module.
     * @param descr the IASEjbCMPEntityDescriptor associated with this CMP bean.
     * @param srcout the location of the source files to be generated.
     * @param classout the location of the class files to be generated.
     * @throws GeneratorException if there is a problem processing the bean.
     */
    void generate(IASEjbCMPEntityDescriptor descr, File srcout, File classout)
        throws GeneratorException;

    /**
     * This method is called once for each ejb module in the application
     * that contains CMP beans. It is called at the end of the module processing.
     * @return a Collection of files to be compiled by the deployment process.
     * @throws GeneratorException if there is any problem.
     */
    Collection<File> cleanup() throws GeneratorException;

    /**
     * This method may be called once for each CMP bean of the corresponding
     * ejb module to perform the validation.
     * @param descr the IASEjbCMPEntityDescriptor associated with this CMP bean.
     * @return a Collection of Exceptions if there are any problems processing the bean.
     * Returns an empty Collection if validation succeeds.
     */
    Collection validate(IASEjbCMPEntityDescriptor descr);

}
