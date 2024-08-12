/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.security.SecurityLoggerInfo;

import java.util.Iterator;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Adjusts the DAS configuration to turn off secure admin, as if by executing these commands:
 *
 * <pre>
 * {@code

asadmin -s set configs.config.server-config.network-config.network-listeners.network-listener.admin-listener.protocol=admin-listener

asadmin -s delete-protocol sec-admin-listener
asadmin -s delete-protocol admin-http-redirect
asadmin -s delete-protocol pu-protocol
}
 *
 * @author Tim Quinn
 */
@Service(name = "disable-secure-admin")
@PerLookup
@I18n("disable.secure.admin.command")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@RestEndpoints({
    @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.POST, path = "disable-secure-admin", description = "disable-secure-admin") })
@AccessRequired(resource = "domain/secure-admin", action = "disable")
public class DisableSecureAdminCommand extends SecureAdminCommand {

    @Override
    protected String transactionErrorMessageKey() {
        return SecurityLoggerInfo.disablingSecureAdminError;
    }

    @Override
    Iterator<Work<TopLevelContext>> secureAdminSteps() {
        return reverseStepsIterator(secureAdminSteps);
    }

    @Override
    Iterator<Work<ConfigLevelContext>> perConfigSteps() {
        return reverseStepsIterator(perConfigSteps);
    }

    /**
     * Iterator which returns array elements from back to front.
     *
     * @param <T>
     * @param steps
     * @return
     */
    private <T extends SecureAdminCommand.Context> Iterator<Work<T>> reverseStepsIterator(Step<T>[] steps) {
        return new Iterator<Work<T>>() {
            private Step<T>[] steps;
            private int nextSlot;

            @Override
            public boolean hasNext() {
                return nextSlot >= 0;
            }

            /**
             * Returns the disable work associated with the next step we should process for disabling secure admin.
             */
            @Override
            public Work<T> next() {
                return steps[nextSlot--].disableWork();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            Iterator<Work<T>> init(Step<T>[] steps) {
                this.steps = steps;
                nextSlot = this.steps.length - 1;
                return this;
            }

        }.init(steps);
    }
}
