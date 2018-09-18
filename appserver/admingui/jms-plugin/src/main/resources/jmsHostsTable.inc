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

<!-- configuration/configsTable.jsf -->

<sun:table id="configs" title="#{tableTitle}"
    deselectMultipleButton="$boolean{true}"
    deselectMultipleButtonOnClick="setTimeout('admingui.table.changeOneTableButton(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
    selectMultipleButton="$boolean{true}"
    selectMultipleButtonOnClick="setTimeout('admingui.table.changeOneTableButton(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
    <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{tableId});
    />
    <!facet actionsTop>
         <sun:panelGroup id="topActionsGroup1">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />

            <sun:button id="newButton" text="$resource{i18n.button.New}" >
                <!command gf.redirect(page="#{createLink}?configName=#{configName}"); />
            </sun:button>
              
            <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
                onClick="if (getConfirm(this,'$pageSession{confirmDeleteMsg}') ) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command 
                getUIComponent(clientId="$pageSession{configsTableRowGroupId}", component=>$attribute{tableRowGroup});      
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});

                foreach (var="row" list="#{requestScope.selectedRows}") {
                    urlencode(value="#{row['name']}" result="#{requestScope.encodedName}");
                    gf.restRequest(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}/#{requestScope.encodedName}?target=#{pageSession.configName}", 
                        method="delete", result="#{pageSession.result}");
                }

                setAttribute(key="selectedRows" value="${selectedRows}");
                gf.redirect(page="#{listPageLink}?configName=#{configName}");
            />            
            </sun:button>
            
         </sun:panelGroup>
     </facet>

    <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
        <!afterCreate
             getClientId(component="$this{component}" clientId=>$page{configsTableRowGroupId});
        />

        <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col0">
                <sun:checkbox id="select" selected="#{td.value.selected}" selectedValue="$boolean{true}"
                      onClick="setTimeout('admingui.table.changeOneTableButton(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0);"/>
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.common.PropertyName}"  sort="name" rowHeader="$boolean{true}" id="colName">
            <sun:hyperlink id="link" text="#{td.value.name}" url="#{editLink}?name=#{td.value.name}&configName=#{configName}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18njms.resourcesTable.Host}" sort="host" rowHeader="$boolean{true}" id="colHost">
            <sun:staticText escape="true" id="hostCol" rendered="#{hasHost}" value="#{td.value.host}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18njms.edJmsHost.colPort}" sort="port" rowHeader="$boolean{true}" id="colPort">
            <sun:staticText escape="true" id="portCol" rendered="#{hasPort}" value="#{td.value.port}" />
        </sun:tableColumn>
    </sun:tableRowGroup>
</sun:table>
