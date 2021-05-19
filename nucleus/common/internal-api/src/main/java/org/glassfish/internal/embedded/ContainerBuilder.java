/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.embedded;

import org.jvnet.hk2.annotations.Contract;

/**
 * Defines the builder for an embdded container. This is mostly a
 * tag interface that will be implemented by the embedded container
 * main configuration element (like http-service for web, network-listener
 * for grizzly)
 *
 * @author Jerome Dochez
 */
@Contract
public interface ContainerBuilder<T extends EmbeddedContainer> {

    /**
     * Default sets of container that can be built.
     * Other containers can be added not using one of this predefined types.
     */
    public enum Type {
        /**
         * ejb container type
         */
        ejb,
        /**
         * web container type
         */
        web,
        /**
         * jruby container type
         */
        jruby,
        /**
         * persistence container type
         */
        jpa,
        /**
         * webservices container type
         */
        webservices,
        /**
         * all installed containers
         */
        all }

    /**
     * Creates a embedded container
     *
     * @param server the embedded server in which the container resides.
     * @return the embedded container instance
     */
    T create(Server server);
}
