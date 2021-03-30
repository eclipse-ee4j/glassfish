/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest;

import org.glassfish.admin.restconnector.ProxyRestCommandAdapter;
import org.glassfish.admin.restconnector.ProxyRestManagementAdapter;
import org.glassfish.admin.restconnector.ProxyRestMonitoringAdapter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * Inhabitant decorator to removed unwanted services in embedded mode
 *
 * @author Jerome Dochez
 */
public class EmbeddedInhabitantsParser implements PopulatorPostProcessor {
    public String getName() {
        return "Embedded";
    }

    //    public void decorate(InhabitantsParser inhabitantsParser) {
    //        inhabitantsParser.drop(RestService.class);
    //        inhabitantsParser.drop(ProxyRestManagementAdapter.class);
    //        inhabitantsParser.drop(ProxyRestMonitoringAdapter.class);
    //        inhabitantsParser.drop(ProxyRestAdminAdapter.class);
    //    }

    @Override
    public DescriptorImpl process(ServiceLocator serviceLocator, DescriptorImpl descriptorImpl) {

        boolean skip = RestService.class.getCanonicalName().equals(descriptorImpl.getImplementation())
                || ProxyRestManagementAdapter.class.getCanonicalName().equals(descriptorImpl.getImplementation())
                || ProxyRestMonitoringAdapter.class.getCanonicalName().equals(descriptorImpl.getImplementation())
                || ProxyRestCommandAdapter.class.getCanonicalName().equals(descriptorImpl.getImplementation());

        if (!skip) {
            return descriptorImpl;
        }

        return null;

    }
}
