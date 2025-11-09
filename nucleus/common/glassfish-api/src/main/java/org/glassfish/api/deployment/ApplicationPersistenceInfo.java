/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.api.deployment;

import jakarta.persistence.EntityManagerFactory;

import java.util.Set;

/**
 * Provides info about peristence definitions in the application
 *
 * @author Ondro Mihalyi
 */
public interface ApplicationPersistenceInfo {

    /**
     * Returns the set of physical entity manager factories associated with
     * persistence units in this application.
     */
    Set<EntityManagerFactory> getEntityManagerFactories();

}
