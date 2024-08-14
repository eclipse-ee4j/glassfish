/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.mdb.monitoring.stats;

import com.sun.ejb.monitoring.stats.EjbMonitoringStatsProvider;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Probe listener for the Message-Driven Beans part of the EJB monitoring events.
 *
 * @author Marina Vatkina
 */
@AMXMetadata(type="message-driven-bean-mon", group="monitoring", isSingleton=false)
@ManagedObject
public class MessageDrivenBeanStatsProvider extends EjbMonitoringStatsProvider {

    private CountStatisticImpl messageCount = new CountStatisticImpl("MessageCount",
            "count", "Number of messages received for a message-driven bean");

    public MessageDrivenBeanStatsProvider(long beanId, String appName, String moduleName,
            String beanName) {
        super(beanId, appName, moduleName, beanName);
    }

    @ManagedAttribute(id="messagecount")
    @Description( "Number of messages received for a message-driven bean")
    public CountStatistic getMessageCount() {
        return messageCount;
    }

    @ProbeListener("glassfish:ejb:bean:messageDeliveredEvent")
    public void messageDeliveredEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (this.beanId == beanId) {
            log ("messageDeliveredEvent", "MessageDrivenBeanStatsProvider");
            messageCount.increment();
        }
    }

}
