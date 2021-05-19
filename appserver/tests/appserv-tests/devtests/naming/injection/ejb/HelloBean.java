/*
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

package test;

import jakarta.ejb.*;
import jakarta.annotation.*;
import java.net.*;
import java.util.*;

@Stateless
public class HelloBean implements Hello {
    private static URL expectedURL;
    private static URL[] expectedURLs = new URL[4];

    static {
        try {
            expectedURL = new URL("http://java.net");
            for(int i = 0; i < expectedURLs.length; i++) {
                expectedURLs[i] = expectedURL;
            }
        } catch (MalformedURLException e) {
            //igore
        }
    }

    @Resource(name="java:module/env/url/url2", lookup="url/testUrl")
    private URL url2;

    @Resource(name="java:module/env/url/url1", lookup="java:module/env/url/url2")
    private URL url1;

    @Resource(lookup="java:module/env/url/url1")
    private URL url3;

    @Resource(mappedName="url/testUrl")
    private URL url4;

    public String injectedURL() {
        URL[] actualURLs = {url1, url2, url3, url4};
        if(Arrays.equals(expectedURLs, actualURLs)) {
            return ("Got expected " + Arrays.toString(actualURLs));
        } else {
            throw new EJBException("Expecting " + Arrays.toString(expectedURLs) +
                ", actual " + Arrays.toString(actualURLs));
        }
    }

}
