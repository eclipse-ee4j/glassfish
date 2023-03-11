/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.annotation.factory;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.annotation.context.AppClientContext;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.context.RarBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;

import org.glassfish.apf.context.AnnotationContext;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

/**
 * The Factory is reponsible for creating AnnotatedElementHandler.
 *
 * @author Shing Wai Chan
 */
public class AnnotatedElementHandlerFactory {

    private AnnotatedElementHandlerFactory() {
    }


    public static AnnotationContext createAnnotatedElementHandler(RootDeploymentDescriptor bundleDesc) {
        if (bundleDesc instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor<EjbDescriptor> ejbBundleDesc = (EjbBundleDescriptor<EjbDescriptor>) bundleDesc;
            return new EjbBundleContext(ejbBundleDesc);
        } else if (bundleDesc instanceof ApplicationClientDescriptor) {
            ApplicationClientDescriptor appClientDesc = (ApplicationClientDescriptor) bundleDesc;
            return new AppClientContext(appClientDesc);
        } else if (bundleDesc instanceof WebBundleDescriptor) {
            WebBundleDescriptor webBundleDesc = (WebBundleDescriptor) bundleDesc;
            return new WebBundleContext(webBundleDesc);
        } else if (bundleDesc instanceof ConnectorDescriptor) {
            ConnectorDescriptor connectorDesc = (ConnectorDescriptor) bundleDesc;
            return new RarBundleContext(connectorDesc);
        } else {
            return null;
        }
    }
}
