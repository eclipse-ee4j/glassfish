/*
 * Copyright (c) 2001, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.test.util.results;

/**
 * @Class: TestCase
 * @Description: Class holding One TestCase info.
 * @Author : Ramesh Mandava
 * @Last Modified :Initial creation By Ramesh on 10/24/2001
 * @Last Modified : By Justin Lee on 10/05/2009
 */
public class TestCase {
    private String id;
    private String name = ReporterConstants.NA;
    private String description = ReporterConstants.NA;
    private String status = ReporterConstants.DID_NOT_RUN;
    private String statusDescription = ReporterConstants.NA;

    public TestCase() {
    }

    public TestCase(String name) {
        this();
        this.name = SimpleReporterAdapter.checkNA(name);
        id = name;
    }

    public TestCase(String name, String description) {
        this(name);
        this.description = SimpleReporterAdapter.checkNA(description);
    }

    public void setStatus(String status) {
        this.status = SimpleReporterAdapter.checkNA(status);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        name = SimpleReporterAdapter.checkNA(value);
        id = name;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    @Override
    public String toString() {
        return "TestCase{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", status='" + status + '\'' +
            '}';
    }

    public String toXml() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<testcase>\n");
        buffer.append("<id>" + id + "</id>\n");
        if (!name.equals(ReporterConstants.NA)) {
            buffer.append("<name>" + name.trim() + "</name>\n");
        }
        if (!description.equals(ReporterConstants.NA)) {
            buffer.append("<description><![CDATA[" + description.trim() + "]]></description>\n");
        }
        if (!statusDescription.equals(ReporterConstants.NA)) {
            buffer.append("<status value=\"" + status.trim() + "\"><![CDATA[" + statusDescription.trim() + "]]></status>\n");
        } else {
            buffer.append("<status value=\"" + status.trim() + "\">" + "</status>\n");
        }
        buffer.append("</testcase>\n");

        return buffer.toString();
    }
}
