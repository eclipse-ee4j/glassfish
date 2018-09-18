<!--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!-- jdbc/resourcesTable.inc -->

<sun:table id="resourcesTable" title="#{tableTitle}" 
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />
    
#include "/common/resourceNode/resourceHandlers.inc"
#include "/jdbc/tableButtons.inc"

   <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$pageSession{listOfRows}"} sourceVar="td">
       <!beforeCreate
            gf.getChildList(parentEndpoint="#{pageSession.parentUrl}", childType="#{pageSession.childType}", id="jndiName", result="#{pageSession.tmpRows}");
            gfr.getResourcesTableData();
            if(#{pageSession.showLogicalJndi}){
                gf.restRequest(endpoint="#{sessionScope.REST_URL}/resources/list-jdbc-resources"  method="GET" result="#{requestScope.tmpJMap}");
                gf.getLogicalJndiName(logicalMapList="#{requestScope.tmpJMap.data.extraProperties.jdbcResources}", listRow="#{pageSession.listOfRows}" result="#{pageSession.listOfRows}");
            }
        />
     <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
        />
   <sun:tableColumn headerText="$resource{i18n.common.SelectHeader}" selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select"
                toolTip="$resource{i18n.common.select}" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0);"
            />
    </sun:tableColumn>    

    <sun:tableColumn headerText="$resource{i18njdbc.resourcesTable.colJndiName}" sort="name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" 
            url="#{pageSession.editLink}?name=#{td.value.name}&logicalJndiName=#{td.value.encodedLogicalJndiName}" />
    </sun:tableColumn>

    <sun:tableColumn id="colLogicalName" headerText="$resource{i18n.common.resourcesTable.colLogicalJndiName}" rendered="#{pageSession.showLogicalJndi}"  sort="logicalJndiName"  rowHeader="$boolean{false}">
        <h:outputText id="lname" value="#{td.value.logicalJndiName}"/>
    </sun:tableColumn>

    <sun:tableColumn rendered="#{!pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colStatus}" sort="enabled" rowHeader="$boolean{false}" id="col22">
        <sun:hyperlink rendered="#{!pageSession.onlyDASExist}" id="targetLink" text="#{td.value.enableURL}" url="#{targetPage}?name=#{td.value.encodedName}&generalPage=#{pageSession.generalPage}?name=#{td.value.encodedName}" />
    </sun:tableColumn>

    <sun:tableColumn  rendered="#{pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colEnabled}" sort="enabled" rowHeader="$boolean{false}" id="col222">
        <sun:image rendered="#{pageSession.onlyDASExist}" id="enCol" value="#{td.value.enableURL}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18njdbc.resourcesTable.colPool}" sort="poolName" rendered="#{pageSession.hasPool}"  rowHeader="$boolean{false}" id="col3">
        <sun:hyperlink id="pool" text="#{td.value.poolName}" 
            url="#{pageSession.editPoolLink}?name=#{td.value.poolName}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18njdbc.resourcesTable.colResType}" sort="resType" rendered="#{pageSession.hasResType}"  rowHeader="$boolean{false}" id="coltype">
        <sun:staticText escape="true" id="resType" rendered="#{pageSession.hasResType}" value="#{td.value.resType}" />
    </sun:tableColumn>

    <!-- TODO enable when we enable cluster support
    <sun:tableColumn rendered="$pageSession{showDetail}"
            headerText="$resource{i18n.common.TargetTab}"  
            rowHeader="$boolean{true}" id="colTarget">
            <event>
                <!beforeEncode
                    getTargetStatus(appName="#{td.value.name}", forApp="$boolean{false}",  result=>$page{listOfTargets});
                />				    
            </event>
            <foreach key="target" list="#{listOfTargets}">
		<staticText id="image" value="#{target.image}" />"&nbsp; 
                <sun:hyperlink url="#{target.targetURL}" text="#{target.target}" />
                "<br />
            </foreach>
        </sun:tableColumn> 
    -->
     
    
    <sun:tableColumn headerText="$resource{i18njdbc.resourcesTable.colDesc}" rowHeader="$boolean{true}" id="col4">
        <sun:staticText escape="true" id="typeDesc" value="#{td.value.description}" />
    </sun:tableColumn>
    
  </sun:tableRowGroup>
  
</sun:table>
