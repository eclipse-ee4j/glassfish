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

package test;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@jakarta.servlet.annotation.WebListener
public class TestHttpSessionListener implements HttpSessionListener {
    @Inject  MyBean myBean;

    public void sessionCreated(HttpSessionEvent se) {
        String value = myBean.getText();
        if (!"CDI_VALUE".equals(value)) {
            throw new RuntimeException();
        }
        se.getSession().setAttribute("myAttr", value);
        System.out.println(">>> sessionCreated: " + value);
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println(">>> sessionDestroyed");
    }
}
