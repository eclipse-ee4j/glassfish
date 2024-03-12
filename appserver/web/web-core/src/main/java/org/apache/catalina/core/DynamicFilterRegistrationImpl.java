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

package org.apache.catalina.core;

import org.apache.catalina.LogFacade;
import org.apache.catalina.deploy.FilterDef;

import jakarta.servlet.FilterRegistration;
import java.text.MessageFormat;
import java.util.ResourceBundle;


public class DynamicFilterRegistrationImpl
    extends FilterRegistrationImpl
    implements FilterRegistration.Dynamic {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    /**
     * Constructor
     */
    public DynamicFilterRegistrationImpl(FilterDef filterDef,
            StandardContext ctx) {
        super(filterDef, ctx);
    }

    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.DYNAMIC_FILTER_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"async-supported", filterDef.getFilterName(),
                                                            ctx.getName()});
            throw new IllegalStateException(msg);
        }

        filterDef.setIsAsyncSupported(isAsyncSupported);
    }
}

