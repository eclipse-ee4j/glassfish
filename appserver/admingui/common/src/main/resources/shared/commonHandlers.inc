<!--

    Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.

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
     This handler gets a list of password aliases
     Input:  #{requestScope.doEscape}
     Output: #{pageSession.listOfAliases}
-->
<handler id="gfr.getPswdAliases">
    gf.restRequest(
        endpoint="#{sessionScope.REST_URL}/list-password-aliases"
        method="GET"
        result="#{requestScope.result}");
    setAttribute(key="_listOfMap" value="#{requestScope.result.data.children}");
    setPageSessionAttribute(key="listOfAliases" value={});
    if( "!(#{requestScope._listOfMap} = #{null})" ){
      foreach(var="oneChildMap" list="#{requestScope._listOfMap}"){
        if ("#{requestScope.doEscape}"){
            htmlEscape(value="#{requestScope.oneChildMap.message}" result="#{requestScope.encodedAliasName}");
            listAdd(list="#{pageSession.listOfAliases}" value="#{requestScope.encodedAliasName}" );
        }
        if ("!#{requestScope.doEscape}"){
         listAdd(list="#{pageSession.listOfAliases}" value="#{requestScope.oneChildMap.message}" );
      }
      }
      gf.listSort(list="#{pageSession.listOfAliases}");
    }
</handler>



<!--
   get a list of running instance.
   output = #{pageSession.runningInstances}
-->
<handler id="gfr.getListOfRunningInstances">
    getDefaultAdminTimeout(result="#{requestScope.adminTimeout}");
    createMap(result="#{requestScope.listInstanceAttrMap}");
    mapPut(map="#{requestScope.listInstanceAttrMap}" key="timeoutmsec" value="#{requestScope.adminTimeout}" );
    gf.restRequest(endpoint="#{sessionScope.REST_URL}/list-instances",  attrs="#{requestScope.listInstanceAttrMap}", method="get", result="#{requestScope.results}");
    listAdd(value="server", result="#{pageSession.runningInstances}");
    foreach (var="instance", list="#{requestScope.results.data.extraProperties.instanceList}") {
        if ('!(#{instance.status} = NOT_RUNNING)') {
            listAdd(list="#{pageSession.runningInstances}", value="#{instance.name}");
        }
    }
</handler>


<!-- get a list of instances based on attrMap
   input:   #{requestScope.listInstanceAttrMap}
   output:  #{pageSession.instanceList}
 -->
<handler id="gfr.getListOfInstances">
    if ("#{requestScope.listInstanceAttrMap} = ${null}") {
        createMap(result="#{requestScope.listInstanceAttrMap}");
    }
    mapPut(map="#{requestScope.listInstanceAttrMap}" key="nostatus" value="true" );
    gf.restRequest(endpoint="#{sessionScope.REST_URL}/list-instances",  attrs="#{requestScope.listInstanceAttrMap}" method="get", result="#{requestScope.result}");
    setPageSessionAttribute(key="instanceList" value={});
    foreach (var="instance", list="#{requestScope.result.data.extraProperties.instanceList}") {
        listAdd(list="#{pageSession.instanceList}", value="#{instance.name}");
    }
</handler>

 <!-- To test whether the monitoring is off or not.
   input:   #{pageSession.configName}
   output:  #{pageSession.isMonitoringOff}
 -->
<handler id="gfr.isMonitoringOff">
    urlencode(value="#{configName}" encoding="UTF-8" result="#{encodedConfigName}");
    setPageSessionAttribute(key="isMonitoringOff" value="#{true}");
    gf.getEntityAttrs(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/monitoring-service" valueMap="#{requestScope.monitorResult}");
    if ("#{requestScope.monitorResult['monitoringEnabled']}=true") {
        gf.getMonitorLevels(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}" monitorCompList="#{requestScope.tableList}")
        setPageSessionAttribute(key="monitorDisable" value="monitorLevels");
        foreach(var="oneRow" list="#{requestScope.tableList}") {
            if("!(#{oneRow.level}=OFF)") {
                setPageSessionAttribute(key="isMonitoringOff" value="#{false}");
            }
        }
    }
</handler>
