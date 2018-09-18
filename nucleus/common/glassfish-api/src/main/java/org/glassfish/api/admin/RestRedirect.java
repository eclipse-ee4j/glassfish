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

package org.glassfish.api.admin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * annotation to redirect a rest request from CRUD operations on the configuration
 * tree to a command invocation (like deploy, undeploy).
 *
 * @author Jerome Dochez
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RestRedirect {

    enum OpType { GET, PUT, POST, DELETE}

    /**
     * Rest operation type that should trigger a redirect to an actual asadmin
     * command invocation
     *
     * @return the rest operation type for this redirect
     */
    OpType opType();

    /**
     * Command identification for the redirection.
     *
     * @return the name of the command to invoke
     */
    String commandName();

}
