/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.adapter;

import org.glassfish.admin.restconnector.Constants;
import org.glassfish.hk2.api.ServiceLocator;

import java.util.HashSet;
import java.util.Set;

/**
 * ReST adapter for monitoring requests via HttpServlet interface
 */
public class RestMonitoringResourceProvider extends AbstractRestResourceProvider {

    @Override
    public Set<Class<?>> getResourceClasses(ServiceLocator habitat) {
        //        return getLazyJersey().getResourcesConfigForMonitoring(habitat);
        //    @Override
        //    public Set<Class<?>> getResourcesConfigForMonitoring(ServiceLocator habitat) {
        final Set<Class<?>> r = new HashSet<>();
        r.add(org.glassfish.admin.rest.resources.MonitoringResource.class);

        r.add(org.glassfish.admin.rest.provider.ActionReportResultHtmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.ActionReportResultJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.ActionReportResultXmlProvider.class);

        return r;
    }

    @Override
    public String getContextRoot() {
        return Constants.REST_MONITORING_CONTEXT_ROOT;
    }
}
