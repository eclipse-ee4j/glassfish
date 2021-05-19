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

package org.glassfish.ejb.deployment.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;
import org.glassfish.apf.impl.AnnotationUtils;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 * Implementation of the Scanner interface for Ejb jar.
 *
 * @author Shing Wai Chan
 */
@Service(name="ejb")
@PerLookup
public class EjbJarScanner extends ModuleScanner<EjbBundleDescriptor> {

    @Override
    public void process(File af, EjbBundleDescriptor desc, ClassLoader cl)
            throws IOException {
        this.archiveFile = af;
        this.classLoader = cl;

        if (AnnotationUtils.getLogger().isLoggable(Level.FINE)) {
            AnnotationUtils.getLogger().fine("archiveFile is " + archiveFile);
            AnnotationUtils.getLogger().fine("classLoader is " + classLoader);
        }

        if (!archiveFile.isDirectory()) return ; // in app client jar

        addScanDirectories();
        addClassesFromDescriptor(desc);
    }

    protected void addScanDirectories() throws IOException {
        addScanDirectory(archiveFile);
    }

    protected void addClassesFromDescriptor(EjbBundleDescriptor desc) {
        // always add session beans, message driven beans,
        // interceptor classes that are defined in ejb-jar.xml
        // regardless of they have annotation or not
        for (EjbDescriptor ejbDesc : desc.getEjbs()) {
            if (ejbDesc instanceof EjbSessionDescriptor ||
                ejbDesc instanceof EjbMessageBeanDescriptor) {
                addScanClassName(ejbDesc.getEjbClassName());
            }
        }

        for (EjbInterceptor ei : desc.getInterceptors()) {
            addScanClassName(ei.getInterceptorClassName());
        }
    }
}
