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

<!-- jca/securityMapsTable.inc used in securityMaps.jsf and workSecurityMaps.jsf-->

<sun:table id="resourcesTable" title="#{tableTitle}"
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
        <sun:button id="newButton" text="$resource{i18n.button.New}" rendered="#{pageSession.showSecurityButtons}" >
            <!command gf.redirect(page="#{pageSession.createLink}") />
        </sun:button>

        <sun:button id="button1" text="$resource{i18n.button.Delete}" rendered="#{pageSession.showSecurityButtons}" disabled="#{true}" primary="#{false}"
            onClick="if (getConfirm(this,'$pageSession{confirmDeleteMsg}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            foreach (var="row" list="#{requestScope.selectedRows}") {
                createMap(result="#{requestScope.attrMap}");
                if("#{pageSession.childType}=work-security-map") {
                    mapPut(map="#{requestScope.attrMap}" key="raname" value="#{row['resourceAdapterName']}");
                }
                if("#{pageSession.childType}=security-map") {
                    mapPut(map="#{requestScope.attrMap}" key="poolName" value="#{pageSession.encodedName}");
                }
                gf.restRequest(
                    endpoint="#{pageSession.parentUrl}/#{pageSession.childType}/#{row['encodedName']}"
                    attrs="#{requestScope.attrMap}"
                    method="DELETE"
                    result="#{requestScope.foo}");
            }
            gf.redirect(page="#{pageSession.listLink}");
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
                toolTip="$resource{i18n.common.select}"
                selected="#{td.value.selected}"
                selectedValue="$boolean{true}"
                onClick="setTimeout('admingui.table.changeOneTableButton(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0);"
            />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n.common.name.header}" sort="name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}"
            url="#{pageSession.editLink}&mapName=#{td.value.name}" />
    </sun:tableColumn>
  </sun:tableRowGroup>

</sun:table>
