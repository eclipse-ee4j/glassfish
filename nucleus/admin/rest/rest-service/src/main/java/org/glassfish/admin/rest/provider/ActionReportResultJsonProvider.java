/*
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

package org.glassfish.admin.rest.provider;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;

/**
 * @author Ludovic Champenois
 * @author Jason Lee
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
public class ActionReportResultJsonProvider extends BaseProvider<ActionReportResult> {

    public ActionReportResultJsonProvider() {
        super(ActionReportResult.class, MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public String getContent(ActionReportResult proxy) {
        RestActionReporter ar = (RestActionReporter) proxy.getActionReport();
        ActionReportJsonProvider provider = new ActionReportJsonProvider();
        return provider.getContent(ar);
    }

}
