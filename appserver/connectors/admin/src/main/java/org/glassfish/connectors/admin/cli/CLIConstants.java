/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.admin.cli;


//TODO java-doc

/**
 * @author Jagadish Ramu
 */
public interface CLIConstants {

    String TARGET = "target";
    String PROPERTY = "property";
    String OPERAND = "DEFAULT";
    String ENABLED = "enabled";
    String DESCRIPTION = "description";
    String IGNORE_DUPLICATE = "ignore-duplicate";
    String DO_NOT_CREATE_RESOURCE_REF = "do-not-create-resource-ref";
    String OBJECT_TYPE = "objecttype";


    public interface WSM {
        String WSM_RA_NAME = "raname";
        String WSM_PRINCIPALS_MAP = "principalsmap";
        String WSM_GROUPS_MAP = "groupsmap";
        String WSM_MAP_NAME = "mapname";
        String WSM_CREATE_WSM_COMMAND = "create-connector-work-security-map";
    }

    public interface RAC {
        String RAC_RA_NAME = "raname";
        String RAC_THREAD_POOL_ID = "threadpoolid";
        String RAC_CREATE_RAC_COMMAND = "create-resource-adapter-config";
    }

    public interface AOR {
        String AOR_CREATE_COMMAND_NAME = "create-admin-object";
        String AOR_RES_TYPE="restype";
        String AOR_CLASS_NAME = "classname";
        String AOR_RA_NAME = "raname";
        String AOR_JNDI_NAME = "jndi_name";
    }

    public interface SM {
        String SM_CREATE_COMMAND_NAME="create-connector-security-map";
        String SM_POOL_NAME = "poolname";
        String SM_PRINCIPALS = "principals";
        String SM_USER_GROUPS="usergroups";
        String SM_MAPPED_NAME = "mappedusername";
        String SM_MAPPED_PASSWORD = "mappedpassword";
        String SM_MAP_NAME = "mapname";
    }

    public interface CCP {
        String CCP_RA_NAME = "raname";
        String CCP_CON_DEFN_NAME = "connectiondefinition";
        String CCP_STEADY_POOL_SIZE = "steadypoolsize";
        String CCP_MAX_POOL_SIZE = "maxpoolsize";
        String CCP_MAX_WAIT_TIME = "maxwait";
        String CCP_POOL_RESIZE_QTY = "poolresize";
        String CCP_IDLE_TIMEOUT = "idletimeout";
        String CCP_IS_VALIDATION_REQUIRED = "isconnectvalidatereq";
        String CCP_FAIL_ALL_CONNS = "failconnection";
        String CCP_LEAK_TIMEOUT = "leaktimeout";
        String CCP_LEAK_RECLAIM = "leakreclaim";
        String CCP_CON_CREATION_RETRY_ATTEMPTS = "creationretryattempts";
        String CCP_CON_CREATION_RETRY_INTERVAL = "creationretryinterval";
        String CCP_LAZY_CON_ENLISTMENT = "lazyconnectionenlistment";
        String CCP_LAZY_CON_ASSOC = "lazyconnectionassociation";
        String CCP_ASSOC_WITH_THREAD = "associatewiththread";
        String CCP_MATCH_CONNECTIONS = "matchconnections";
        String CCP_MAX_CON_USAGE_COUNT = "maxconnectionusagecount";
        String CCP_PING = "ping";
        String CCP_POOLING = "pooling";
        String CCP_VALIDATE_ATMOST_PERIOD = "validateatmostonceperiod";
        String CCP_TXN_SUPPORT = "transactionsupport";
        String CCP_POOL_NAME = "poolname";
        String CCP_CREATE_COMMAND_NAME="create-connector-connection-pool";
    }

    public interface CR {
        String CR_POOL_NAME = "poolname";
        String CR_OBJECT_TYPE = "objecttype";
        String CR_JNDI_NAME = "jndi_name";
        String CR_CREATE_COMMAND_NAME="create-connector-resource";
    }

}
