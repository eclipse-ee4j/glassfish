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

<!-- jdbc/poolTable.inc -->

<sun:table id="poolTable" title="#{tableTitle}" 
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />
    
#include "/jdbc/poolTableButtons.inc"

   <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
     <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
        />
   <sun:tableColumn headerText="$resource{i18n.common.SelectHeader}" selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0);"
            />
    </sun:tableColumn>    

    <sun:tableColumn headerText="$resource{i18njdbc.resourcesTable.colJndiName}" sort="Name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" 
            url="#{pageSession.editLink}?name=#{td.value.name}" />
    </sun:tableColumn>


    <sun:tableColumn headerText="#{resHeader}" sort="ResourceAdapterName" rowHeader="$boolean{true}" id="col22">
        <staticText id="typeCol" value="#{td.value.resourceAdapterName}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="#{extraInfoHeader}" sort="ConnectionDefinitionName"   rowHeader="$boolean{true}" id="col3">
        <staticText id="typeCol" value="#{td.value.connectionDefinitionName}" />
    </sun:tableColumn>  
    
    <sun:tableColumn headerText="$resource{i18njdbc.resourcesTable.colDesc}" rowHeader="$boolean{true}" id="col4">
        <staticText id="typeDesc" value="#{td.value.description}" />
    </sun:tableColumn>
    
  </sun:tableRowGroup>
  
</sun:table>
