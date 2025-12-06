/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.ejte.ccl.reporter;

/**
 * Class holding One TestCase info.
 *
 * @author : Ramesh Mandava 2001
 */
public class TestCase {

    private String id;
    private String name;
    private String description;

    private String status;
    private String statusDescription;

    private String expected;
    private String actual;

    public TestCase(String id, String name, String description) {
        this.id = id == null ? null : id.strip();
        this.name = name;
        this.description = description;
        this.status = ReporterConstants.DID_NOT_RUN;
        this.statusDescription = ReporterConstants.NA;
        this.expected = null;
        this.actual = null;

    }


    public TestCase(String id, String name) {
        this.id = id == null ? null : id.strip();
        this.name = name;
        this.description = ReporterConstants.NA;
        this.status = ReporterConstants.DID_NOT_RUN;
        this.statusDescription = ReporterConstants.NA;
        this.expected = null;
        this.actual = null;

    }


    public TestCase(String id) {
        this.id = id == null ? null : id.strip();
        this.name = ReporterConstants.NA;
        this.description = ReporterConstants.NA;
        this.status = ReporterConstants.DID_NOT_RUN;
        this.statusDescription = ReporterConstants.NA;
        this.expected = null;
        this.actual = null;

    }


    public void setStatus(String status) {
        this.status = status;
    }


    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
        this.expected = null;
        this.actual = null;

    }


    public void setExpected(String expected) {
        this.expected = expected;
    }


    public void setActual(String actual) {
        this.actual = actual;
    }


    public String getId() {
        return id;
    }


    public String getName() {
        return name;
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


    public String getExpected() {
        return expected;
    }


    public String getActual() {
        return actual;
    }

}
