/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * HttpAlgorithmParameterSpec.java
 *
 * Created on 10 Aug, 2007, 4:40:22 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.security.ee.authentication.glassfish.digest.impl;

import jakarta.servlet.http.HttpServletRequest;

import java.security.spec.AlgorithmParameterSpec;

/**
 * represents HttpServlet request parameter
 *
 * @author K.Venugopal@sun.com
 */
public class HttpAlgorithmParameterImpl implements AlgorithmParameterSpec {

    private HttpServletRequest req = null;

    public HttpAlgorithmParameterImpl(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletRequest getValue() {
        return req;
    }
}
