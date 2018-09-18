<!--

    Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

-->

<!--
     This handler gets the Monitoring Table.
     Output: #{pageSession.listOfRows}
-->
<handler id="gfr.getMonitoringTable">
    setPageSessionAttribute(key="listOfRows" value={});
    gf.listInstances(optionKeys={"standaloneonly"} optionValues={"true"}, instances="#{pageSession.standaloneList}");
    gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/servers/server" result="#{pageSession.instancesList}");
    setAttribute(key="commonLink" value="#{request.contextPath}/common/monitor");
    foreach (var="instance", list="#{pageSession.instancesList}") {
        setAttribute(key="instanceArgs" value="?instanceName=#{instance}");
        gf.containedIn(list="#{pageSession.standaloneList}" testStr="#{instance}" contain=>$attribute{isStandalone});
        setAttribute(key="instanceLink" value="");
        setAttribute(key="clusterLink" value="");
        setAttribute(key="isCluster" value="$boolean{false}");
        if(#{isStandalone}) {
            setAttribute(key="clusterName" value="");            
            setAttribute(key="instanceLink" value="#{request.contextPath}/cluster/standalone/standaloneInstanceGeneral.jsf?instanceName=#{instance}");
            setAttribute(key="applicationsMonitorLink" value="standaloneInstanceMonitoringAppStatsPage.jsf#{instanceArgs}");
            setAttribute(key="serverMonitorLink" value="standaloneInstanceMonitoringServerPage.jsf#{instanceArgs}");
            setAttribute(key="resourcesMonitorLink" value="standaloneInstanceMonitoringResourcePage.jsf#{instanceArgs}");
        }
        if(!#{isStandalone}) {
            if("#{instance}=server") {
                setAttribute(key="clusterName" value="");
                setAttribute(key="instanceLink" value="#{request.contextPath}/common/appServer/serverInstGeneralPe.jsf#{instanceArgs}");
                setAttribute(key="applicationsMonitorLink" value="serverInstMonitoringAppsPage.jsf#{instanceArgs}");
                setAttribute(key="serverMonitorLink" value="serverInstMonitoringServerPage.jsf#{instanceArgs}");
                setAttribute(key="resourcesMonitorLink" value="serverInstMonitoringResourcesPage.jsf#{instanceArgs}");
            }
            if("!(#{instance}=server)") {
                setAttribute(key="isCluster" value="$boolean{true}");
                gf.getClusterNameForInstance(instanceName="#{instance}"  clusterName=>$attribute{clusterName});
                setAttribute(key="instanceLink" value="#{request.contextPath}/cluster/cluster/clusterInstanceEdit.jsf#{instanceArgs}&clusterName=#{clusterName}");
                setAttribute(key="clusterLink" value="#{request.contextPath}/cluster/cluster/clusterGeneral.jsf?clusterName=#{clusterName}");
                setAttribute(key="applicationsMonitorLink" value="clusterInstanceMonitoringAppStatsPage.jsf#{instanceArgs}&clusterName=#{clusterName}");
                setAttribute(key="serverMonitorLink" value="clusterInstanceMonitoringServerPage.jsf#{instanceArgs}&clusterName=#{clusterName}");
                setAttribute(key="resourcesMonitorLink" value="clusterInstanceMonitoringResourcePage.jsf#{instanceArgs}&clusterName=#{clusterName}");
            }
        }
        gf.getConfigName(target="#{instance}" configName=>$attribute{configName});
        setAttribute(key="actionLink" value="#{request.contextPath}/common/monitor/monitoringPage.jsf?configName=#{configName}");
        gf.createAttributeMap(keys={"instanceName", "instanceLink", "clusterName",
                                    "clusterLink", "actionLink", "appMonitor",
                                    "serverMonitor", "resMonitor", "isCluster"}
                              values={"${instance}", "$attribute{instanceLink}",
                                      "$attribute{clusterName}", "$attribute{clusterLink}",
                                      "$attribute{actionLink}",
                                      "$attribute{commonLink}/$attribute{applicationsMonitorLink}",
                                      "$attribute{commonLink}/$attribute{serverMonitorLink}",
                                      "$attribute{commonLink}/$attribute{resourcesMonitorLink}",
                                      "$attribute{isCluster}",}
                                    map="#{requestScope.oneRowMap}");
        listAdd(list="$pageSession{listOfRows}" value="#{requestScope.oneRowMap}");
    }
</handler>
