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

package org.glassfish.loadbalancer.admin.cli.reader.api;

/**
 * Provides cluster information relavant to Load balancer tier.
 *
 * @author Satish Viswanatham
 */
public interface ClusterReader extends BaseReader {

    /**
     * Get the name of the cluster
     *
     * @return String   name of the cluster
     */
    public String getName() throws LbReaderException;

    /**
     * Get the instance readers in the cluster
     *
     * @return InstanceReader[]   array of instance readers in the cluser
     */
    public InstanceReader[] getInstances() throws LbReaderException;

    /**
     * Returns the health checker for the cluster
     *
     * @return HealthCheckerReader health checker information for the cluster
     */
    public HealthCheckerReader getHealthChecker() throws LbReaderException;

    /**
     * Returns all the web modules in the cluster
     *
     * @return WebModuleReader[]    array of web module readers in the cluster
     */
    public WebModuleReader[] getWebModules() throws LbReaderException;

    /**
     * Returns the lb policy
     *
     * @return LbPolicy    String
     */
    public String getLbPolicy() throws LbReaderException;

    /**
     * Returns the lb policy module
     *
     * @return LbPolicyModule    String
     */
    public String getLbPolicyModule() throws LbReaderException;
}
