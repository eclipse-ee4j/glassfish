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

package org.glassfish.ejb.deployment.descriptor.runtime;

import org.glassfish.deployment.common.Descriptor;

public class IASEjbCMPFinder extends Descriptor {

    private String method_name = null;
    private String query_params = null;
    private String query_filter = null;
    private String query_variables = null;
    private String query_ordering = null;

    public IASEjbCMPFinder () {
    }

    public IASEjbCMPFinder(String method_name, String params, String filter) {
        this.method_name = method_name;
        this.query_params = params;
        this.query_filter = filter;
        this.query_variables = "";
    }

    public IASEjbCMPFinder(String method_name, String params, String filter, String variables) {
        this.method_name = method_name;
        this.query_params = params;
        this.query_filter = filter;
        this.query_variables = variables;
    }

    public String getMethodName() {
        return method_name;
    }

    public String getQueryParameterDeclaration() {
        return query_params;
    }

    public String getQueryFilter() {
        return query_filter;
    }

    public String getQueryVariables() {
        return query_variables;
    }

    public void setMethodName(String name) {
        method_name = name;
    }

    public void setQueryParameterDeclaration(String qry) {
        query_params = qry;
    }

    public void setQueryVariables(String qryvariables) {
        query_variables = qryvariables;
    }

    public void setQueryFilter(String qryfilter) {
        query_filter = qryfilter;
    }

    public String getQueryOrdering() {
        return query_ordering;
    }

    public void setQueryOrdering(String qryordering) {
        query_ordering = qryordering;
    }
}

