/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.common;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;

public class SecurityAccessFilter implements Filter {

    private static final Logger LOG = SecurityAccessValidationService._theLog;

    private static boolean javaPolicySet =
        AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return false;
            }
        });


    @Override
    public boolean matches(Descriptor d) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Descripter: " + d );
        }

        if (!javaPolicySet) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("java security policy is not set, so no validation for security servies.");
            }

            return false;
        }

        if (d == null)
            return false;

        Set<String> qualifiers = d.getQualifiers();
        if (qualifiers != null && qualifiers.size() != 0) {
            for (String s : qualifiers) {
                if (Secure.class.getCanonicalName().equals(s)) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("The instance is annotated with \'Secure\': " + s);
                    }
                    return true;
                }
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("The instance has no \'Secure\' annotated ");
        }

        return false;
    }

}
