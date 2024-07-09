/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.commandrecorder.admingui.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.security.enterprise.SecurityContext;
import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;

@ApplicationScoped
public class SecurityProducer {

    @Produces
    Subject getSubject(SecurityContext securityContext) {
        return new Subject(true, securityContext.getPrincipalsByType(Principal.class), Set.of(), Set.of());
    }

}
