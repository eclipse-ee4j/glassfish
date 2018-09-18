<!--

    Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!-- concurrent/resourcesTable.inc -->

<sun:table id="resourcesTable" title="#{tableTitle}" 
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />
    
#include "/common/resourceNode/resourceHandlers.inc"
#include "/concurrent/tableButtons.inc"

   <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$pageSession{listOfRows}"} sourceVar="td">
       <!beforeCreate
           gf.getChildList(parentEndpoint="#{pageSession.parentUrl}", childType="#{pageSession.childType}", id="jndiName", result="#{pageSession.tmpRows}");
           gfr.getResourcesTableData();
           gf.restRequest(endpoint="#{sessionScope.REST_URL}/resources/#{pageSession.listCommand}"  method="GET" result="#{requestScope.tmpJMap}");
           mapGet(Map="#{requestScope.tmpJMap.data.extraProperties}", Key="#{pageSession.logicalJndiMapKey}"  Value="#{requestScope.logicalMapList}")
           gf.getLogicalJndiName(logicalMapList="#{requestScope.logicalMapList}", listRow="#{pageSession.listOfRows}" result="#{pageSession.listOfRows}");
           foreach (var="oneRow" list="#{pageSession.listOfRows}") {
               if (#{oneRow.contextInfoEnabled}=true){
                    mapPut(map="#{requestScope.oneRow}" key="ctxInfoEnableImage" value="/resource/images/enabled.png");
                }
                if (#{oneRow.contextInfoEnabled}=false){
                    mapPut(map="#{requestScope.oneRow}" key="ctxInfoEnableImage" value="/resource/images/disabled.png");
                    mapPut(map="#{requestScope.oneRow}" key="contextInfo" value="" );
               }
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

    <sun:tableColumn headerText="$resource{i18ncon.resourcesTable.colJndiName}" sort="name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" url="#{pageSession.editLink}?name=#{td.value.encodedName}&logicalJndiName=#{td.value.encodedLogicalJndiName}" />
    </sun:tableColumn>

    <sun:tableColumn id="colLogicalName" headerText="$resource{i18n.common.resourcesTable.colLogicalJndiName}"  sort="logicalJndiName"  rowHeader="$boolean{false}">
        <h:outputText id="lname" value="#{td.value.logicalJndiName}"/>
    </sun:tableColumn>

    <sun:tableColumn rendered="#{!pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colStatus}" sort="enabled" rowHeader="$boolean{false}" id="col22">
        <sun:hyperlink rendered="#{!pageSession.onlyDASExist}" id="targetLink" text="#{td.value.enableURL}" url="#{targetPage}?name=#{td.value.encodedName}&generalPage=#{pageSession.generalPage}?name=#{td.value.encodedName}" />
    </sun:tableColumn>

    <sun:tableColumn  rendered="#{pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colEnabled}" sort="enabled" rowHeader="$boolean{false}" id="coldd">
        <sun:image rendered="#{pageSession.onlyDASExist}" id="enCol" value="#{td.value.enableURL}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18ncon.resourcesTable.colContextInfo}" sort="contextInfo"  rowHeader="$boolean{false}" id="coltype">
        <sun:staticText escape="true" id="contextInfo" value="#{td.value.contextInfo}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18ncon.resourcesTable.colThreadPriority}" sort="threadPriority" rendered="#{pageSession.hasThreadPriority}"  rowHeader="$boolean{false}" id="thread">
        <sun:staticText escape="true" id="threadPriority" rendered="#{pageSession.hasThreadPriority}" value="#{td.value.threadPriority}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18ncon.resourcesTable.colDesc}" rowHeader="$boolean{false}" id="col4">
        <sun:staticText escape="true" id="typeDesc" value="#{td.value.description}" />
    </sun:tableColumn>
    
  </sun:tableRowGroup>
  
</sun:table>
