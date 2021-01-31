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

import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Singleton;

/**
 * Process environment allow access to information related to the execution or process. This is a bit tricky to rely
 * of @Contract/@Service service lookup for this API since different implementations (server, clients, etc..) can be
 * present of the classpath.
 *
 * @author Jerome Dochez
 */
@Service
@Singleton
public class ProcessEnvironment {

    /**
     * Default initialization is unkown process environment
     */
    public ProcessEnvironment() {
        type = ProcessType.Other;
    }

    /**
     * Enumeration of the supported process types Server is the application server ACC is the application client Other is a
     * standalone java.
     */
    public enum ProcessType {
        Server, ACC, Embedded, Other;

        public boolean isServer() {
            return this == Server || this == Embedded;
        }

        public boolean isStandaloneServer() {
            return this == Server;
        }

        public boolean isEmbedded() {
            return this == Embedded;
        }
    }

    /**
     * Determine and return the modes in which the code is behaving, like application server or application client modes.
     *
     * @return the process type
     */
    public ProcessType getProcessType() {
        return type;
    }

    /**
     * Creates a process environemnt for the inten
     *
     * @param type of the execution environemnt
     */
    public ProcessEnvironment(ProcessType type) {
        this.type = type;
    }

    final private ProcessType type;

}
