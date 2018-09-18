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

<sun:table id="resourcesTable" title="#{tableTitle}"
           deselectMultipleButton="$boolean{true}"
           deselectMultipleButtonOnClick="setTimeout('admingui.table.toggleButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
           selectMultipleButton="$boolean{true}"
           selectMultipleButtonOnClick="setTimeout('admingui.table.toggleButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
    <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{tableId});
    />

#include "/common/resourceNode/resourceHandlers.inc"
#include "/jms/shared/tableButtons.inc"

    <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$pageSession{listOfRows}"} sourceVar="td">
        <!beforeCreate
            if (#{pageSession.isConnPage}) {
                gfr.getJmsConnectionFactories();
                getJmsResourcesInfo( resourcesList="#{requestScope.children}" isConnectionFactory="#{true}" result="#{pageSession.tmpRows}");
                gfr.getResourcesTableData();
            }
            if (!#{pageSession.isConnPage}) {
                gfr.getJmsDestResources();
                getJmsResourcesInfo( resourcesList="#{requestScope.children}" isConnectionFactory="#{false}" result="#{pageSession.tmpRows}");
                gfr.getResourcesTableData();
            }
        />
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
        />
        <sun:tableColumn id="colSelect" headerText="$resource{i18n.common.SelectHeader}" selectId="select" rowHeader="$boolean{false}">
            <sun:checkbox id="select"
                          toolTip="$resource{i18n.common.select}"
                          selected="#{td.value.selected}"
                          selectedValue="$boolean{true}"
                          onClick="setTimeout('admingui.table.toggleButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0);"/>
        </sun:tableColumn>

        <sun:tableColumn id="colName" headerText="$resource{i18njms.resourcesTable.colJndiName}"  sort="name" rowHeader="$boolean{true}">
            <sun:hyperlink id="link" text="#{td.value.name}" url="#{pageSession.editLink}?name=#{td.value.encodedName}" />
        </sun:tableColumn>
        
        <sun:tableColumn id="colLogicalName" headerText="$resource{i18n.common.resourcesTable.colLogicalJndiName}"  sort="logicalJndiName" rendered="#{isConnPage}" rowHeader="$boolean{false}">
            <h:outputText id="lname" value="#{td.value.logicalJndiName}"/>
        </sun:tableColumn>

        <sun:tableColumn rendered="#{!pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colStatus}" sort="enabled" rowHeader="$boolean{false}" id="col22">
            <sun:hyperlink rendered="#{!pageSession.onlyDASExist}" id="targetLink" text="#{td.value.enableURL}" url="#{targetPage}?name=#{td.value.encodedName}&generalPage=#{pageSession.generalPage}?name=#{td.value.encodedName}" />
        </sun:tableColumn>

        <sun:tableColumn  rendered="#{pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colEnabled}" sort="enabled" rowHeader="$boolean{false}" id="col222">
            <sun:image rendered="#{pageSession.onlyDASExist}" id="enCol" value="#{td.value.enableURL}" />
        </sun:tableColumn>

        <sun:tableColumn id="colResType" headerText="$resource{i18njms.resourcesTable.colResType}" sort="resType"  rowHeader="$boolean{false}">
            <h:outputText id="resType" value="#{td.value.resType}"/>
        </sun:tableColumn>

        <sun:tableColumn id="colTypeDesc" headerText="$resource{i18njms.resourcesTable.colDesc}" sort="description" rowHeader="$boolean{false}">
            <h:outputText id="typeDesc" value="#{td.value.description}" />
        </sun:tableColumn>

    </sun:tableRowGroup>

</sun:table>
