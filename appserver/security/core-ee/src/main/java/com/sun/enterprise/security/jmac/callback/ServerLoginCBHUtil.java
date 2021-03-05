/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.jmac.callback;

import com.sun.enterprise.security.common.AppservAccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import jakarta.security.auth.message.callback.GroupPrincipalCallback;
import org.glassfish.security.common.Group;

/**
 *
 * @author vbkumarjayanti
 */
public class ServerLoginCBHUtil {

    private static void processGP(GroupPrincipalCallback gpCallback) {
        final Subject fs = gpCallback.getSubject();
        final String[] groups = gpCallback.getGroups();
        if (groups != null && groups.length > 0) {
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                public java.lang.Object run() {
                    for (String group : groups) {
                        fs.getPrincipals().add(new Group(group));
                    }
                    return fs;
                }
            });
        } else if (groups == null) {
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                public java.lang.Object run() {
                    Set<Principal> principalSet = fs.getPrincipals();
                    principalSet.removeAll(fs.getPrincipals(Group.class));
                    return fs;
                }
            });
        }
    }

    // NOTE: this method is called by reflection from ServerLoginCallbackHandler
    public static void processGroupPrincipal(Callback gpCallback) {
        if (gpCallback instanceof GroupPrincipalCallback) {
            processGP((GroupPrincipalCallback) gpCallback);
        }
    }

}
