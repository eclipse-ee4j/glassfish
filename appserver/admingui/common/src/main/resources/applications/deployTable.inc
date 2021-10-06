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

<!-- applications/deployTable.inc -->


<sun:table id="deployTable" title="$resource{i18n.deployTable.applications}" sortPanelToggleButton="#{false}"
       deselectMultipleButton="$boolean{true}"
        deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
        selectMultipleButton="$boolean{true}"
        selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />

#include "/common/applications/deployTableButtons.inc"

   <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
    <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
    />

   <sun:tableColumn headerText="$resource{i18n.common.SelectHeader}" selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select"
                toolTip="$resource{i18n.common.select}" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="admingui.table.changeThreeTableButtons('#{pageSession.topActionGroup}', '#{pageSession.tableId}'); admingui.table.initAllRows('#{pageSession.tableId}');"
            />
    </sun:tableColumn>    
        
    <sun:tableColumn headerText="$resource{i18n.deployTable.colName}" sort="name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" url="#{editLink}?appName=#{td.value.encodedName}&listPageLink=#{pageSession.listPageLink}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n.deployTable.colDeploymentOrder}"  sort="deploymentOrder"   id="colorder">
        <staticText id="order" value="#{td.value.deploymentOrder}" />
    </sun:tableColumn>
        
    <sun:tableColumn rendered="#{!pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colStatus}" sort="enableURL"  id="col2">
        <sun:hyperlink rendered="#{!pageSession.onlyDASExist}" id="targetLink" text="#{td.value.enableURL}" url="#{targetLink}?appName=#{td.value.encodedName}&listPageLink=#{pageSession.listPageLink}" />
    </sun:tableColumn>

    <sun:tableColumn  rendered="#{pageSession.onlyDASExist}" headerText="$resource{i18n.deployTable.colEnabled}" sort="enableURL"  id="col22">
        <sun:image rendered="#{pageSession.onlyDASExist}" id="enCol" value="#{td.value.enableURL}" />
    </sun:tableColumn>

    
    <sun:tableColumn headerText="$resource{i18n.deployTable.ColEngines}"  sort="sniffers"   id="colsnif">
        <staticText id="sniffer" value="#{td.value.sniffers}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n.ComponentTable.colAction}" embeddedActions="#{false}"  id="col4">
            <sun:hyperlink id="link" rendered="#{td.value.hasLaunch}" text="$resource{i18n.deployTable.launch}"
                onClick="var win=window.open('#{request.contextPath}/common/applications/webApplicationLinks.jsf?appID=#{td.value.name}&contextRoot=#{td.value.contextRoot}','ViewerWindow','width='+.75*screen.width+',height='+.75*screen.height+',top='+.1*screen.height+',left='+.1*screen.width+',toolbar=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,directories=yes,location=yes');win.focus(); return false;" />
        "&#160;|&#160;

        <sun:hyperlink id="redeployLink" text="$resource{i18n.deployTable.redeploy}" 
            url="#{request.contextPath}/common/applications/redeploy.jsf?appName=#{td.value.name}&appType=$pageSession{appType}&nextPage=#{listPageLink}" />

        "&#160;|&#160;

        <sun:hyperlink id="reloadLink" text="$resource{i18n.deployTable.reload}">
            <!command
                gf.reloadApplication(appName="#{td.value.name}");
                gf.redirect(page="#{pageSession.listPageLink}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
        </sun:hyperlink>
        
    </sun:tableColumn>
    
  </sun:tableRowGroup>
  
  
</sun:table>
