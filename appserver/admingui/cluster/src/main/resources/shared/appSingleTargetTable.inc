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

<sun:table id="deployTable" title="$resource{i18n.deployTable.applications}" sortPanelToggleButton="#{false}"
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
            <sun:button id="deployButton" text="$resource{i18n.button.Deploy}" primary="#{true}">
                <!command
		    gf.redirect(page="#{pageSession.nextPageLink}") />
            </sun:button>
            <sun:button id="button1" text="$resource{i18n.button.Undeploy}" disabled="#{true}" primary="#{false}"
                    onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
                        { return submitAndDisable(this, '$resource{i18n.button.Processing}');}
                        else {return false;} " >
                <!command
                    getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    gf.undeploy(selectedRows="${selectedRows}" );
                    gf.redirect(page="#{listPageLink}?#{extraArgs}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
             </sun:button>s

            <sun:button id="button2" text="$resource{i18n.button.Remove}" disabled="#{true}" primary="#{false}"
                    onClick="if (!getConfirm(this,'$resource{i18n.msg.JS.confirmRemoveTarget}'))
                               return false;
                    return submitAndDisable(this, '$resource{i18n.button.Processing}');"  >
                <!command
                    getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    foreach (var="row" list="#{requestScope.selectedRows}") {
                       urlencode(value="#{row['name']}" result="#{requestScope.encodedName}");
                       createMap(result="#{requestScope.attrMap}");
                       mapPut(map="#{requestScope.attrMap}" key="target" value="#{row['targetName']}");
                       gf.restRequest(
                        endpoint="#{sessionScope.REST_URL}#{pageSession.appRefEndpoint}#{requestScope.encodedName}"
                        attrs="#{requestScope.attrMap}"
                        method="DELETE");
                    }
                    gf.redirect(page="#{listPageLink}?#{extraArgs}");
                />
             </sun:button>

            <sun:dropDown id="dropdown1" submitForm="#{true}"  disabled="#{false}"
                labels={ "$resource{i18n.common.moreActionDropDown}"
                        "$resource{i18n.button.Enable}"  "$resource{i18n.button.Disable}"
                        "$resource{i18n.targetListing.table.lbEnable}"  "$resource{i18n.targetListing.table.lbDisable}"
                     }
                values={"" "enable" "disable" "lb-enable" "lb-disable" } >
                <!command
                    getUIComponent(clientId="#{tableRowGroupId}", component=>$attribute{tableRowGroup});
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="actionValue" value="#{click.selected}")

                    if( ${actionValue}=enable){
                        gf.changeTargetStatus(selectedRows="${selectedRows}"  Enabled="#{true}" forLB="$boolean{false}")
                    }
                    if( ${actionValue}=disable){
                        gf.changeTargetStatus(selectedRows="${selectedRows}"  Enabled="#{false}" forLB="$boolean{false}")
                    }
                    if( ${actionValue}=lb-enable){
                        gf.changeTargetStatus(selectedRows="${selectedRows}" Enabled="#{true}" forLB="$boolean{true}")
                    }
                    if( ${actionValue}=lb-disable){
                        gf.changeTargetStatus(selectedRows="${selectedRows}"  Enabled="#{false}" forLB="$boolean{true}")
                    }
                    gf.redirect(page="#{listPageLink}?#{extraArgs}");
                />
            </sun:dropDown>
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
                onClick="admingui.table.changeThreeTableButtons('#{pageSession.topActionGroup}', '#{pageSession.tableId}'); admingui.table.initAllRows('#{pageSession.tableId}');"
            />
    </sun:tableColumn>    
        
    <sun:tableColumn headerText="$resource{i18n.deployTable.colName}" sort="name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" url="#{editLink}?appName=#{td.value.name}&listPageLink=#{pageSession.listPageLink}" />
    </sun:tableColumn>

    <sun:tableColumn  headerText="$resource{i18n.deployTable.colEnabled}" rowHeader="$boolean{true}" id="col22">
        <sun:image id="enablecol" value="#{td.value.enabled}" />
    </sun:tableColumn>

    <sun:tableColumn  headerText="$resource{i18n.targetListing.LBEnabled}"  rowHeader="$boolean{true}" id="col33">
        <sun:image id="lbEnablecol" value="#{td.value.lbEnabled}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n.deployTable.ColEngines}"  sort="sniffers"  rowHeader="$boolean{true}" id="colsnif">
        <staticText id="sniffer" value="#{td.value.sniffers}" />
    </sun:tableColumn>
  </sun:tableRowGroup>
  
</sun:table>
