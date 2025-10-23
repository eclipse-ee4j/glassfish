/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
package org.glassfish.admingui.cdi;

import com.sun.enterprise.v3.admin.AdminCommandJob;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.Job;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

import static org.glassfish.admingui.common.plugin.ConsoleClassLoader.HABITAT_ATTRIBUTE;

@ApplicationScoped
public class Hk2ServicesProducer {

    @Produces
    ServiceLocator produceLocator() {
        ServletContext servletCtx = (ServletContext)
            (FacesContext.getCurrentInstance().getExternalContext()).getContext();

        ServiceLocator locator = (ServiceLocator) servletCtx.getAttribute(HABITAT_ATTRIBUTE);

        if (locator == null) {
            return Globals.getDefaultHabitat();
        }
        return locator;
    }

    @Produces
    CommandRunner getCommandRunner() {
        return produceLocator().getService(CommandRunner.class);
    }

    @Produces
    public CommandRunner<AdminCommandJob> produceAdminCommandJobCommandRunner() {
        return getCommandRunner();
    }

    @Produces
    public CommandRunner<Job> produceJobCommandRunner() {
        return getCommandRunner();
    }

    @Produces
    ActionReport getActionReport() {
        return produceLocator().getService(ActionReport.class);
    }

}
