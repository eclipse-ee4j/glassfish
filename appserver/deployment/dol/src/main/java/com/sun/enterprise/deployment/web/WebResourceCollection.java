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

package com.sun.enterprise.deployment.web;

import java.util.Set;

    /** I am an interface representing the API to an object
    ** which represents the information about a web resource collection.
    ** @author Danny Coward
    */

public interface WebResourceCollection extends WebDescriptor {

    public Set<String> getUrlPatterns();
    public void addUrlPattern(String urlPattern);

    public Set<String> getHttpMethods();
    public void addHttpMethod(String httpMethod);
    public String[] getHttpMethodsAsArray();

    public Set<String> getHttpMethodOmissions();
    public void addHttpMethodOmission(String httpMethodOmission);
    public String[] getHttpMethodOmissionsAsArray();
}
