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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.types.MessageDestinationReferencer;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.DescriptorVisitor;

/**
 *
 * @author  dochez
 * @version
 */
public class TracerVisitor extends DefaultDOLVisitor implements ApplicationVisitor {

    @Override
    public void accept (BundleDescriptor descriptor) {
        if (descriptor instanceof Application) {
            Application application = (Application)descriptor;
            accept(application);

            for (BundleDescriptor ebd : application.getBundleDescriptorsOfType(DOLUtils.ejbType())) {
                ebd.visit(getSubDescriptorVisitor(ebd));
            }

            for (BundleDescriptor wbd : application.getBundleDescriptorsOfType(DOLUtils.warType())) {
                // This might be null in the case of an appclient
                // processing a client stubs .jar whose original .ear contained
                // a .war.  This will be fixed correctly in the deployment
                // stage but until then adding a non-null check will prevent
                // the validation step from bombing.
                if (wbd != null) {
                    wbd.visit(getSubDescriptorVisitor(wbd));
                }
            }

            for (BundleDescriptor cd :  application.getBundleDescriptorsOfType(DOLUtils.rarType())) {
                cd.visit(getSubDescriptorVisitor(cd));
            }

            for (BundleDescriptor acd : application.getBundleDescriptorsOfType(DOLUtils.carType())) {
                acd.visit(getSubDescriptorVisitor(acd));
            }
            super.accept(descriptor);
        } else {
            super.accept(descriptor);
        }
    }


    /**
     * visit an application object
     *
     * @param the application descriptor
     */
    @Override
    public void accept(Application application) {
        DOLUtils.getDefaultLogger().info("Application");
        DOLUtils.getDefaultLogger().info("name " + application.getName());
        DOLUtils.getDefaultLogger().info("smallIcon " + application.getSmallIconUri());
    }


    /**
     * visits an ejb reference for the last J2EE component visited
     *
     * @param the ejb reference
     */
    @Override
    protected void accept(EjbReference ejbRef) {
        DOLUtils.getDefaultLogger().info(ejbRef.toString());
    }


    @Override
    protected void accept(MessageDestinationReferencer referencer) {
        DOLUtils.getDefaultLogger().info(referencer.getMessageDestinationLinkName());
    }


    protected void accept(WebService webService) {
        DOLUtils.getDefaultLogger().info(webService.getName());
    }


    @Override
    protected void accept(ServiceReferenceDescriptor serviceRef) {
        DOLUtils.getDefaultLogger().info(serviceRef.getName());
    }


    protected void accept(EnvironmentProperty envEntry) {
        DOLUtils.getDefaultLogger().info(envEntry.toString());
    }


    /**
     * visits a J2EE descriptor
     */
    @Override
    public void accept(Descriptor descriptor) {
        DOLUtils.getDefaultLogger().info(descriptor.toString());
    }


    /**
     * get the visitor for its sub descriptor
     *
     * @param subDescriptor sub descriptor to return visitor for
     */
    @Override
    public DescriptorVisitor getSubDescriptorVisitor(Descriptor subDescriptor) {
        if (subDescriptor instanceof BundleDescriptor) {
            DescriptorVisitor tracerVisitor = ((BundleDescriptor) subDescriptor).getTracerVisitor();
            if (tracerVisitor == null) {
                return this;
            }
            return tracerVisitor;
        }
        return super.getSubDescriptorVisitor(subDescriptor);
    }
}
