<!--

    Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!-- applications/lifecyclesTable.inc -->

<sun:table id="deployTable" sortPanelToggleButton="#{false}"  title="$resource{i18n.deployTable.lifeCycle}"
   deselectMultipleButton="$boolean{true}"
    deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
    selectMultipleButton="$boolean{true}"
    selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
     <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{tableId});
    />

<!facet actionsTop>
     <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{topActionGroup});
        />
        <sun:button id="newButton" text="$resource{i18n.button.New}" primary="#{true}">
                <!command gf.redirect(page="#{request.contextPath}/common/applications/lifecycleNew.jsf") />
        </sun:button>
        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
                onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
                    { return submitAndDisable(this, '$resource{i18n.button.Processing}');}
                    else {return false;} " >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                gf.deleteLifecycle(selectedList="#{requestScope.selectedRows}");
                gf.redirect(page="#{pageSession.listPageLink}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
         </sun:button>
        <sun:button id="button2" rendered="#{pageSession.onlyDASExist}" text="$resource{i18n.button.Enable}" disabled="#{true}" primary="#{false}"
                onClick="return submitAndDisable(this, '$resource{i18n.button.Processing}');" >
        <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                createMap(result="#{requestScope.attrsMap}");
                mapPut(map="#{requestScope.attrsMap}"  key="enabled" value="true");
                foreach (var="row" list="#{requestScope.selectedRows}") {
                    gf.restRequest(
                        endpoint="#{sessionScope.REST_URL}/servers/server/server/application-ref/#{row['encodedName']}"
                        attrs="#{requestScope.attrsMap}"
                        method="POST");
                }
                gf.redirect(page="#{listPageLink}");
            />
            </sun:button>
        <sun:button id="button3" rendered="#{pageSession.onlyDASExist}" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}"
            onClick="return submitAndDisable(this, '$resource{i18n.button.Processing}');" >
        <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                createMap(result="#{requestScope.attrsMap}");
                mapPut(map="#{requestScope.attrsMap}"  key="enabled" value="false");
                foreach (var="row" list="#{requestScope.selectedRows}") {
                    gf.restRequest(
                        endpoint="#{sessionScope.REST_URL}/servers/server/server/application-ref/#{row['encodedName']}"
                        attrs="#{requestScope.attrsMap}"
                        method="POST");
                }
                gf.redirect(page="#{listPageLink}");
            />
        </sun:button>
     </sun:panelGroup>
 </facet>
     
    <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
        <!afterCreate
             getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
        />
    <sun:tableColumn headerText="$resource{i18n.common.SelectHeader}" selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0)"
            />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n.deployTable.colName}" sort="Name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" url="#{editLink}?appName=#{td.value.encodedName}" />
    </sun:tableColumn>

    <sun:tableColumn rendered="#{!pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colStatus}" sort="enabled" rowHeader="$boolean{true}" id="col2">
        <sun:hyperlink rendered="#{!pageSession.onlyDASExist}" id="targetLink" text="#{td.value.enableURL}" url="#{targetLink}?appName=#{td.value.encodedName}&listPageLink=#{pageSession.listPageLink}" />
    </sun:tableColumn>

    <sun:tableColumn  rendered="#{pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colEnabled}" sort="enabled" rowHeader="$boolean{true}" id="col22">
        <sun:image rendered="#{pageSession.onlyDASExist}" id="enCol" value="#{td.value.enableURL}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n.deployTable.colLoadOrder}"  sort="loadOrder"  rowHeader="$boolean{true}" id="col3">
        <staticText id="loadOrder" value="#{td.value.loadOrder}" />
    </sun:tableColumn>

        <!--
    <sun:tableColumn  headerText="$resource{i18n.deployTable.classnameCol}" sort="className" rowHeader="$boolean{true}" id="col4">
        <sun:staticText escape="true" id="className" value="#{td.value.className}" />
    </sun:tableColumn>


    <sun:tableColumn rendered="$pageSession{showDetail}"
        headerText="$resource{i18n.common.TargetTab}"  
        rowHeader="$boolean{true}" id="colTarget">
        <event>
            <!beforeEncode
                getTargetStatus(appName="#{td.value.name}", forApp="$boolean{true}", result=>$page{listOfTargets});
                />				    
        </event>
        <foreach key="target" list="#{listOfTargets}">
            <staticText id="image" value="#{target.image}" />"&nbsp; 
            <sun:hyperlink url="#{target.targetURL}" text="#{target.target}" />
            "<br />
        </foreach>
    </sun:tableColumn>
-->
  </sun:tableRowGroup>
</sun:table>

