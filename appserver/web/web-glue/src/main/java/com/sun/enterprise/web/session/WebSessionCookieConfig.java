/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web.session;

import org.apache.catalina.core.SessionCookieConfigImpl;
import org.apache.catalina.core.StandardContext;

/**
 * This class extends SessionCookieConfigImpl to handle additional
 * secure cookie functionality from glassfish-web.xml
 *
 * @author  Shing Wai Chan
 */
public final class WebSessionCookieConfig extends SessionCookieConfigImpl {

    // the following enum match cookieSecure property value in glassfish-web.xml
    public enum CookieSecureType {
        TRUE, FALSE, DYNAMIC
    };

    // default web.xml(secure=false) = glassfish-web.xml(cookieSecure=dynamic)
    private CookieSecureType secureCookieType = CookieSecureType.DYNAMIC;

    public WebSessionCookieConfig(StandardContext context) {
        super(context);
    }

    @Override
    public void setSecure(boolean secure) {
        super.setSecure(secure);

        secureCookieType = ((secure)? CookieSecureType.TRUE : CookieSecureType.DYNAMIC);
    }

    public void setSecure(String secure) {
        boolean isTrue = Boolean.parseBoolean(secure);

        super.setSecure(isTrue);

        if (isTrue) {
            secureCookieType = CookieSecureType.TRUE;
        } else if ("false".equalsIgnoreCase(secure)) {
            secureCookieType = CookieSecureType.FALSE;
        } else {
            secureCookieType = CookieSecureType.DYNAMIC;
        }
    }

    public CookieSecureType getSecure() {
        return secureCookieType;
    }
}
