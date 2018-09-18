/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resourcebase.resources;

/**
 * Created with IntelliJ IDEA.
 * User: naman
 * Date: 3/1/13
 * Time: 11:10 AM
 * To change this template use File | Settings | File Templates.
 */
public enum ResourceDeploymentOrder {

    /*
    The number indicates the deployment order for particular resources.

    To add new resource order add constant here and give the deployment order number for that resource.
    Define @ResourceTypeOrder(deploymentOrder=ResourceDeploymentOrder.<your resource>) for your resource. For example
    check JdbcResource class.
     */

    JDBC_RESOURCE(1) , JDBC_POOL(2), CONNECTOR_RESOURCE(3), CONNECTOR_POOL(4), ADMIN_OBJECT_RESOURCE(5),
    DIAGNOSTIC_RESOURCE(6), MAIL_RESOURCE(7), CUSTOM_RESOURCE(8), EXTERNALJNDI_RESOURCE(9),
    RESOURCEADAPTERCONFIG_RESOURCE(10), WORKSECURITYMAP_RESOURCE(11), PERSISTENCE_RESOURCE(12), CONTEXT_SERVICE(13), MANAGED_THREAD_FACTORY(14), MANAGED_EXECUTOR_SERVICE(15), MANAGED_SCHEDULED_EXECUTOR_SERVICE(16);

    private int value;

    private ResourceDeploymentOrder(int value) {
        this.value = value;
    }

    public int getResourceDeploymentOrder() {
        return value;
    }
};
