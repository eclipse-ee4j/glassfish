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

package com.sun.enterprise.tools.verifier.tests.web;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.WebResourceCollection;

public class URLPatternUniqueInSecurityConstraints extends URLPatternUnique{ 
    protected Collection getUrlPatterns(WebBundleDescriptor descriptor) {
        ArrayList<Object>patterns=new ArrayList<Object>();
        for (Enumeration e=descriptor.getSecurityConstraints(); e.hasMoreElements();){
            for (WebResourceCollection c :  ((SecurityConstraint) e.nextElement()).getWebResourceCollections()) {
                for (String s : c.getUrlPatterns()) {
                    patterns.add(s);
                }
            }
        }
	return patterns;
    }
}
