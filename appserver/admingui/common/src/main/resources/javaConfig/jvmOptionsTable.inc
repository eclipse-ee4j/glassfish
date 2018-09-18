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

<!-- shared/propertyDescTable.inc -->

<!-- Table .... -->
<sun:table id="basicTable"  style="padding: 10pt" title="$resource{i18nc.jvmOptions.TableTitle}"
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('admingui.table.changeOneTableButton(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('admingui.table.changeOneTableButton(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />

<!-- Actions (Top) -->
    <!facet actionsTop>
    <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />
        <sun:button id="addSharedTableButton" disabled="#{false}" text="$resource{i18nc.jvmOptions.AddJvmOption}" >
            <!command
                getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                addRowToTable(TableRowGroup="$attribute{tableRowGroup}", NameList={"name", "value", "description"});
            />                                 
        </sun:button>
        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}">
            <!command
                getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{trg});   
                getSelectedTableRowKeys(tableRowGroup="${trg}" rowKeys=>$attribute{rowKeys});
                deleteTableRows(tableRowGroup="${trg}" rowKeys="${rowKeys}");
                commitTableRowGroup(tableRowGroup="${trg}");
            />                                
        </sun:button>
    </sun:panelGroup>
    </facet>       
    <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{tableList}"}  sourceVar="td">
        <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{propertyTableRowGroupId});
        />
        <sun:tableColumn headerText="$resource{i18n.common.SelectHeader}" selectId="select" rowHeader="$boolean{false}" id="col1">
            <sun:checkbox id="select" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="setTimeout('admingui.table.changeOneTableButton(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0);"
            />
        </sun:tableColumn>                    
        <sun:tableColumn headerText="$resource{i18n.common.PropertyValue}" sort="value" rowHeader="$boolean{false}" id="col3">
            <sun:textField columns="$int{100}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.PropertyValue']}" id="col1St" value="#{td.value.value}" />
        </sun:tableColumn>
        "<br/>
    </sun:tableRowGroup>
</sun:table>
