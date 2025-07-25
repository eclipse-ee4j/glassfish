/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
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

package org.glassfish.embeddable.client;


/**
 * Marks class loaders in gf-client and gf-client-module which can provide a container
 * initialized by the Java agent.
 * <p>
 * It is the only way to send an object from the agent to the main class.
 * <p>
 * {@link ClassLoader} is an abstract class and we don't want to introduce
 * multiple inheritance, so this interface is added to chosen class loaders.
 * The hierarchy in the appclient is strictly defined, so this is enough.
 * Older implementations used principially same "trick", but this is bit more
 * readable as you can always search usages of this interface.
 */
public interface ApplicationClientClassLoader {

    /**
     * Sets the container. Default implementation doesn't do anything.
     *
     * @param container
     */
    default void setApplicationClientContainer(ApplicationClientContainer container) {
        // does nothing by default.
    }

    /**
     * @return {@link ApplicationClientContainer}
     */
    ApplicationClientContainer getApplicationClientContainer();
}
