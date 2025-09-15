/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.jdbc.jta.timeout.war;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;

@Path("/timeout")
@RequestScoped
public class TimeoutingRestEndpoint {

    @Inject
    private AsynchronousTimeoutingJob job;

    @POST
    @Path("/{type}")
    public void fireJob(@PathParam("type") Integer type) {
        switch(type){
            case 1: job.tmeoutingAsync();
            break;
            case 2: job.timeoutingAsyncWithFailingNextStep();
            break;
            case 3: job.timeoutingAsyncWithFailingNextStepCatchingExceptionAndRedo();
            break;
            default:
                throw new WebApplicationException(403);
        }
    }
}
