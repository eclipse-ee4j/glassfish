/*
 * Copyright (c) 2000, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.enterprise.iiop.util;

public interface MonitoringConstants
{
    public static final String DEFAULT_MONITORING_ROOT = "orb";
    public static final String DEFAULT_MONITORING_ROOT_DESCRIPTION = 
        "ORB Management and Monitoring Root";

    //
    // Connection Monitoring
    //

    public static final String CONNECTION_MONITORING_ROOT =
	"Connections";
    public static final String CONNECTION_MONITORING_ROOT_DESCRIPTION =
	"Statistics on inbound/outbound connections";

    public static final String INBOUND_CONNECTION_MONITORING_ROOT =
	"Inbound";
    public static final String INBOUND_CONNECTION_MONITORING_ROOT_DESCRIPTION=
	"Statistics on inbound connections";

    public static final String OUTBOUND_CONNECTION_MONITORING_ROOT =
	"Outbound";
    public static final String OUTBOUND_CONNECTION_MONITORING_ROOT_DESCRIPTION=
	"Statistics on outbound connections";

    public static final String CONNECTION_MONITORING_DESCRIPTION =
	"Connection statistics";

    public static final String CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS =
	"NumberOfConnections";
    public static final String CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS_DESCRIPTION =
	"The total number of connections";
    public static final String CONNECTION_NUMBER_OF_IDLE_CONNECTIONS =
	"NumberOfIdleConnections";
    public static final String CONNECTION_NUMBER_OF_IDLE_CONNECTIONS_DESCRIPTION =
	"The number of idle connections";
    public static final String CONNECTION_NUMBER_OF_BUSY_CONNECTIONS =
	"NumberOfBusyConnections";
    public static final String CONNECTION_NUMBER_OF_BUSY_CONNECTIONS_DESCRIPTION =
	"The number of busy connections";
 
    //
    // ThreadPool and WorkQueue monitoring constants
    //

    public static final String THREADPOOL_MONITORING_ROOT = "threadpool";
    public static final String THREADPOOL_MONITORING_ROOT_DESCRIPTION =
	"Monitoring for all ThreadPool instances";
    public static final String THREADPOOL_MONITORING_DESCRIPTION =
	"Monitoring for a ThreadPool";
    public static final String THREADPOOL_CURRENT_NUMBER_OF_THREADS =
	"currentNumberOfThreads";
    public static final String THREADPOOL_CURRENT_NUMBER_OF_THREADS_DESCRIPTION =
	"Current number of total threads in the ThreadPool";
    public static final String THREADPOOL_NUMBER_OF_AVAILABLE_THREADS =
	"numberOfAvailableThreads";
    public static final String THREADPOOL_NUMBER_OF_AVAILABLE_THREADS_DESCRIPTION =
	"Number of available threads in the ThreadPool";
    public static final String THREADPOOL_NUMBER_OF_BUSY_THREADS =
	"numberOfBusyThreads";
    public static final String THREADPOOL_NUMBER_OF_BUSY_THREADS_DESCRIPTION =
	"Number of busy threads in the ThreadPool";
    public static final String THREADPOOL_AVERAGE_WORK_COMPLETION_TIME =
	"averageWorkCompletionTime";
    public static final String THREADPOOL_AVERAGE_WORK_COMPLETION_TIME_DESCRIPTION =
	"Average elapsed time taken to complete a work item by the ThreadPool";
    public static final String THREADPOOL_CURRENT_PROCESSED_COUNT =
	"currentProcessedCount";
    public static final String THREADPOOL_CURRENT_PROCESSED_COUNT_DESCRIPTION =
	"Number of Work items processed by the ThreadPool";

    public static final String WORKQUEUE_MONITORING_DESCRIPTION =
	"Monitoring for a Work Queue";
    public static final String WORKQUEUE_TOTAL_WORK_ITEMS_ADDED =
	"totalWorkItemsAdded";
    public static final String WORKQUEUE_TOTAL_WORK_ITEMS_ADDED_DESCRIPTION =
	"Total number of Work items added to the Queue";
    public static final String WORKQUEUE_WORK_ITEMS_IN_QUEUE =
	"workItemsInQueue";
    public static final String WORKQUEUE_WORK_ITEMS_IN_QUEUE_DESCRIPTION =
	"Number of Work items in the Queue to be processed";
    public static final String WORKQUEUE_AVERAGE_TIME_IN_QUEUE =
	"averageTimeInQueue";
    public static final String WORKQUEUE_AVERAGE_TIME_IN_QUEUE_DESCRIPTION =
	"Average time a work item waits in the work queue";
}

// End of file.
