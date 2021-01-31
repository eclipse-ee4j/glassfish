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

package org.glassfish.api.deployment;

import java.io.IOException;

import org.jvnet.hk2.annotations.Contract;

/**
 * Implementations of this interface are providing deployment application metadata
 *
 * @author Jerome Dochez
 */
@Contract
public interface ApplicationMetaDataProvider<T> {

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    MetaData getMetaData();

    /**
     * Load the metadata associated with the deployment event
     *
     * @param dc the deployment context
     * @return the loaded metadata
     * @throws IOException when the underlying archive cannot be processed correctly
     */
    T load(DeploymentContext dc) throws IOException;
}
