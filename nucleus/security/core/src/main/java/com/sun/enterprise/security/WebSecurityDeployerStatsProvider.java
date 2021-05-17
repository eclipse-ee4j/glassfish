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

package com.sun.enterprise.security;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 *
 * @author nithyasubramanian
 */
@AMXMetadata(type="web-security-deployer-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description( "Web application Security Deployment statistics" )
public class WebSecurityDeployerStatsProvider {

    //Commenting the TimeStatistics to be implemented later

   /* TimeStatisticImpl deploymentTime = new TimeStatisticImpl(0, 0, 0, 0, "DeploymentTime", "milliseconds", "Deployment Time", 0, 0);

    TimeStatisticImpl generationTime = new TimeStatisticImpl(0, 0, 0, 0, "GenerationTime", "milliseconds", "Generation Time", 0, 0);

    TimeStatisticImpl undeploymentTime = new TimeStatisticImpl(0, 0, 0, 0, "UndeploymentTime", "milliseconds", "Undeployment Time", 0, 0);

    TimeStatisticImpl removalTime = new TimeStatisticImpl(0, 0, 0, 0, "RemovalTime", "milliseconds", "Removal Time", 0, 0);*/

    CountStatisticImpl secMgrCount = new CountStatisticImpl("WebSecurityManagerCount", "count", "No of Web security managers");

    CountStatisticImpl policyConfCount= new CountStatisticImpl("WebPolicyConfigurationCount", "count", "No of Policy Configuration Objects");

 /*   @ManagedAttribute(id="depolymenttime")
    public TimeStatistic getDeploymentTime() {
        return deploymentTime.getStatistic();
    }

    @ManagedAttribute(id="generationtime")
    public TimeStatistic getGenerationTime() {
        return generationTime.getStatistic();
    }

    @ManagedAttribute(id="undepolymenttime")
    public TimeStatistic getUndeploymentTime() {
        return undeploymentTime.getStatistic();
    }


    @ManagedAttribute(id="removaltime")
    public TimeStatistic getRemovalTime() {
        return removalTime.getStatistic();
    }*/

    @ManagedAttribute(id="websecuritymanagercount")
    public CountStatistic getWebSMCount() {
        return secMgrCount;

    }

    @ManagedAttribute(id="webpolicyconfigurationcount")
    public CountStatistic getPCCount() {
        return policyConfCount;
    }
/*   @ProbeListener("glassfish:core:web:webDeploymentStartedEvent")
    public void webDeploymentStartedEvent(@ProbeParam("appName")String appName){
       deploymentTime.setStartTime(System.currentTimeMillis());
    }

    @ProbeListener("glassfish:core:web:webDeploymentEndedEvent")
    public void webDeploymentEndedEvent(@ProbeParam("appName")String appName){

    }*/

    @ProbeListener("glassfish:security:web:securityManagerCreationEvent")
    public void securityManagerCreationEvent(
            @ProbeParam("appName") String appName) {
        secMgrCount.increment();
    }

    @ProbeListener("glassfish:security:web:securityManagerDestructionEvent")
    public void securityManagerDestructionEvent(
            @ProbeParam("appName") String appName) {
        secMgrCount.decrement();
    }

    @ProbeListener("glassfish:security:web:policyCreationEvent")
    public void policyConfigurationCreationEvent(
            @ProbeParam("contextId") String contextId) {
        policyConfCount.increment();
    }

    @ProbeListener("glassfish:security:web:policyDestructionEvent")
    public void policyConfigurationDestructionEvent(
            @ProbeParam("contextId") String contextId) {
        policyConfCount.decrement();
    }





}
