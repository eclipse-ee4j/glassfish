/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.internal.api.Public;
import org.glassfish.internal.api.Visibility;
import org.jvnet.hk2.annotations.Service;

/**
 * Adapter for public administrative commands.
 *
 * @author Jerome Dochez
 */
@Service
public class PublicAdminAdapter extends AdminAdapter {

    public final static String VS_NAME = "__asadmin";
    public final static String PREFIX_URI = "/" + VS_NAME;

    public PublicAdminAdapter() {
        super(Public.class);
    }

    @Override
    public String getContextRoot() {
        return PREFIX_URI;
    }

    @Override
    protected boolean validatePrivacy(AdminCommand command) {
        Visibility visibility = command.getClass().getAnnotation(Visibility.class);
        return (visibility == null ? true : visibility.value().equals(Public.class));
    }
}
