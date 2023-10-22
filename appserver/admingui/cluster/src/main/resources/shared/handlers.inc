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
     This handler check if there is a cluster or standalone instance, node or config  with the name already existed.
     Input : #{requestScope.nameToCheck}     - required
     Output: #{pageSession.nameAlreadyExists}
-->
<handler id="gfj.checkNameExist">
    urlencode(value="#{requestScope.nameToCheck}" result="#{requestScope.encodedNameToCheck}");
    gf.checkIfEndPointExist(endpoint="#{sessionScope.REST_URL}/clusters/cluster/#{requestScope.encodedNameToCheck}", exists="#{requestScope.exx}");
    if (#{requestScope.exx}){
        setAttribute(key="nameAlreadyExists" value="#{true}");
    }
    if ("!#{requestScope.exx}"){
        gf.checkIfEndPointExist(endpoint="#{sessionScope.REST_URL}/servers/server/#{requestScope.encodedNameToCheck}", exists="#{requestScope.exx}");
    }
    if (#{requestScope.exx}){
        setAttribute(key="nameAlreadyExists" value="#{true}");
    }
    if ("!#{requestScope.exx}"){
        gf.checkIfEndPointExist(endpoint="#{sessionScope.REST_URL}/configs/config/#{requestScope.encodedNameToCheck}", exists="#{requestScope.exx}");
    }
    if (#{requestScope.exx}){
        setAttribute(key="nameAlreadyExists" value="#{true}");
    }
    if ("!#{requestScope.exx}"){
        gf.checkIfEndPointExist(endpoint="#{sessionScope.REST_URL}/nodes/node/#{requestScope.encodedNameToCheck}", exists="#{requestScope.nameAlreadyExists}");
    }

    if (#{requestScope.nameAlreadyExists}){
        setAttribute(key="errormsg" value="$resource{i18ncs.msg.error.nameExists, #{requestScope.encodedNameToCheck}}")
        gf.handleError(detail="#{requestScope.errormsg}");
    }

</handler>



<!--
 This handler calls list-instances and return a map that has the instance name as the key and the status as value.
 Input : #{requestScope.listInstanceAttrMap}     - required
 Output: #{pageSession.instanceStatusMap}
-->
<handler id="gfr.getInstancesStatus">
    if ("#{requestScope.listInstanceAttrMap} = ${null}") {
        createMap(result="#{requestScope.listInstanceAttrMap}");
    }
    getDefaultAdminTimeout(result="#{requestScope.adminTimeout}");
    mapPut(map="#{requestScope.listInstanceAttrMap}" key="timeoutmsec" value="#{requestScope.adminTimeout}" );
    gf.restRequest( endpoint="#{sessionScope.REST_URL}/list-instances"
        attrs="#{requestScope.listInstanceAttrMap}"
        method="GET"
        result="#{pageSession.props}");
    setAttribute(key="insList" value="#{pageSession.props.data.extraProperties.instanceList}" );
    //println("======= insList = #{requestScope.insList}");
    createMap(result="#{pageSession.instanceStatusMap}");
    // if( "(#{requestScope.insList} = #{null})" ){
        //println("============= no instances found");
    //}
    if( "!(#{requestScope.insList} = #{null})" ){
        foreach(var="oneIns" list="#{requestScope.insList}"){
            mapPut(map="#{pageSession.instanceStatusMap}"  key="#{requestScope.oneIns.name}" value="#{requestScope.oneIns.status}");
        }
    }
</handler>


<!--
 This handler gets the running status of an instance.  The String returned is the running status in word followed by icon.
 Input : #{pageSession.instanceName}     - required
 Output: #{pageSession.statusString}
-->
<handler id="gfr.getStatusString" >
    createMap(result="#{requestScope.listInstanceAttrMap}");
    mapPut(map="#{requestScope.listInstanceAttrMap}" key="whichtarget" value="#{pageSession.instanceName}")
    gfr.getInstancesStatus();
    //println("-------- instanceStatusMap = #{pageSession.instanceStatusMap}");
    mapGet(Map="#{pageSession.instanceStatusMap}" Key="#{pageSession.instanceName}" Value="#{pageSession.status}")
    setPageSessionAttribute(key="statusString" value="$resource{i18nc.status.$pageSession{status}} $resource{i18nc.status.image.$pageSession{status}} ");
    setPageSessionAttribute(key="statusImage" value="$resource{i18nc.status.image.$pageSession{status}} ");

</handler>


    <!--
    This handler gets the JDPA debug information.
    Input: #{pageSession.instanceName}
    Output" #{pageSession.instanceDebugInfo}
    -->
    <handler id="gfr.getInstanceDebugInfo" >
        setAttribute(key="saveType", value="#{requestScope.alertType}");
        setAttribute(key="saveSummary", value="#{requestScope.alertSummary}");
        setAttribute(key="saveDetail", value="#{requestScope.alertDetail}");
        createMap(result="#{requestScope._debugAttrMap}")
        mapPut(map="#{requestScope._debugAttrMap}" key="target" value="#{pageSession.instanceName}")
        gf.restRequest(endpoint="#{sessionScope.REST_URL}/get-runtime-info" attrs="#{requestScope._debugAttrMap}"
            method="GET" result="#{requestScope.debugResult}");
        setAttribute(key="debugF" value="#{requestScope.debugResult.data.subReports.get(0).properties.debug}");
        setPageSessionAttribute(key="instanceDebugInfo" value="$resource{i18n.inst.notEnabled}");
        if ("#{requestScope.debugF}=true"){
            setAttribute(key="debugPort" value="#{requestScope.debugResult.data.subReports.get(0).properties.debugPort}");
            setPageSessionAttribute(key="instanceDebugInfo" value="$resource{i18n.inst.debugEnabled} ${debugPort}");
        }
        setAttribute(key="alertType", value="#{requestScope.saveType}");
        setAttribute(key="alertSummary", value="#{requestScope.saveSummary}");
        setAttribute(key="alertDetail", value="#{requestScope.saveDetail}");
    </handler>
