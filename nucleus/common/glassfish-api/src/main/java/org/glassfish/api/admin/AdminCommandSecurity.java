/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import java.util.Collection;

/**
 *
 * @author tjquinn
 */
public interface AdminCommandSecurity {
    
    /**
     * Behavior required of all command classes which provide any of their
     * own custom authorization enforcement. The system will invoke the
     * class's {@code getAccessChecks} method after it has injected {@code @Inject}
     * and {@code @Param} fields and after it has invoked any {@code @PostConstruct}
     * methods.  The getAccessChecks method returns one or more {@link AccessCheck}
     * objects, indicating additional authorization checking that secure
     * admin should perform beyond what is triggered by the annotations.
     */
    public interface AccessCheckProvider {
        
        /**
         * Returns the {@code AccessCheck}s the command has computed at runtime 
         * which should be included in the authorization, added to checks that 
         * secure admin infers from the command's CRUD or RestEndpoint characteristics
         * or {@code AccessRequired} annotations.
         * 
         * @return the {@code AccessCheck}s 
         */
        Collection<? extends AccessRequired.AccessCheck> getAccessChecks();
    }
    
    public interface Preauthorization {
        public boolean preAuthorization(AdminCommandContext context);
    }
}
