/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.resources.custom;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import jakarta.ws.rs.core.Context;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.Dom;

/**
 *
 * @author jasonlee
 */
public class JmxServiceUrlsResource {
    @Context
    protected ServiceLocator habitat;

    public void setEntity(Dom p) {
        // ugly no-op hack. For now.
    }

    @GET
    @Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5" })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public ActionReportResult getJmxServiceUrl() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            JMXServiceURL[] urls = (JMXServiceURL[]) mBeanServer.getAttribute(getBootAMXMBeanObjectName(), "JMXServiceURLs");
            List<String> jmxUrls = new ArrayList<>();
            for (JMXServiceURL url : urls) {
                jmxUrls.add(url.getURLPath());
            }
            RestActionReporter ar = new RestActionReporter();
            ar.setActionDescription("Get JMX Service URLs");
            ar.setSuccess();
            ar.getExtraProperties().put("jmxServiceUrls", jmxUrls);
            return new ActionReportResult(ar);
        } catch (final JMException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectName getBootAMXMBeanObjectName() {
        try {
            return new ObjectName("amx-support:type=boot-amx");
        } catch (final Exception e) {
            throw new RuntimeException("bad ObjectName", e);
        }
    }
}
