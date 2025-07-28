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

package org.glassfish.embeddable.client;


/**
 * Shared interface required to avoid circular dependency of gf-client.jar and gf-client-module.jar
 */
public interface ApplicationClientContainer {

    /**
     * Executes the client. The implementation of this interface might require some preparation.
     *
     * @param args
     * @throws UserError When user provided incorrect arguments or some requirements were not met.
     */
    void launch(String[] args) throws UserError;
}
