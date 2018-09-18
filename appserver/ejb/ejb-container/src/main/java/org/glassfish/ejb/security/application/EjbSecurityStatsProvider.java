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

package org.glassfish.ejb.security.application;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 *
 * @author nithyasubramanian
 */
@AMXMetadata(type="ejb-security-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description( "Ejb Security Deployment statistics" )
public class EjbSecurityStatsProvider {

    //Commenting the TimeStatistics to be implemented later
    // TimeStatisticImpl deploymentTime = new TimeStatisticImpl(0, 0, 0, 0, "DeploymentTime", "milliseconds", "Deployment Time", 0, 0);
    CountStatisticImpl ejbSMCount = new CountStatisticImpl("SecurityManagerCount", "count", "Count of EJB Security managers");
    CountStatisticImpl ejbPCCount = new CountStatisticImpl("PolicyConfigurationCount", "count", "Count of Policy Configuration");



  /*  @ManagedAttribute(id="depolymenttime")
    public TimeStatistic getDeploymentTime() {
        return deploymentTime.getStatistic();
    }*/

    @ManagedAttribute(id="securitymanagercount")
    public CountStatistic getSecurityManagerCount() {
        return ejbSMCount;
    }

    @ManagedAttribute(id="policyconfigurationcount")
    public CountStatistic getPolicyConfigurationCount() {
        return ejbPCCount;
    }




   @ProbeListener("glassfish:security:ejb:securityManagerCreationEvent")
    public void securityManagerCreationEvent(@ProbeParam("appName")String appName){
       //deploymentTime.setStartTime(System.currentTimeMillis());
       ejbSMCount.increment();
    }

    @ProbeListener("glassfish:security:ejb:securityManagerDestructionEvent")
    public void securityManagerDestructionEvent(@ProbeParam("appName")String appName){
      ejbSMCount.decrement();
    }



    @ProbeListener("glassfish:security:ejbpolicy:policyCreationEvent")
    public void policyCreationEvent(@ProbeParam("contextId")String contextId){
        ejbPCCount.increment();

    }

    @ProbeListener("glassfish:security:ejb:policyDestructionEvent")
    public void policyDestructionEvent(@ProbeParam("contextId")String contextId){
       ejbPCCount.decrement();
    }





}
