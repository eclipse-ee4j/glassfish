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

/*
 * TestInformation.java
 *
 * Created on December 4, 2000, 4:29 PM
 */

package com.sun.enterprise.tools.verifier;

/**
 * Holds information about a particular test like the name of the class
 * implementing the test or the version of the spec it applies to
 *
 * @author Jerom Dochez
 */
public class TestInformation extends Object {

    private String className = null;
    private String minimumVersion = null;
    private String maximumVersion = null;

    /**
     * @return the className implementing the test
     */
    public String getClassName() {
        return className;
    }

    /*
     * set the class name implementing the test
     * 
     * @param className is the class name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the minimum version of the spec this test applies to
     */
    public String getMinimumVersion() {
        return minimumVersion;
    }

    /**
     * set the minimum version of the spec this test applies to
     *
     * @param minimumVersion is the version
     */
    public void setMinimumVersion(String minimumVersion) {
        this.minimumVersion = minimumVersion;
    }

    /**
     * @return maximumVersion the maximum version of the spec this test applies to
     */
    public String getMaximumVersion() {
        return maximumVersion;
    }

    /**
     * set the minimum version of the spec this test applies to
     *
     * @param maximumVersion is the version
     */
    public void setMaximumVersion(String maximumVersion) {
        this.maximumVersion = maximumVersion;
    }
}
