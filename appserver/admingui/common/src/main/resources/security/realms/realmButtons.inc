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

<sun:panelGroup id="topButtons">
     <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
        onClick="if (checkClassname()) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            prepareSuccessfulMsg();
            saveRealm(
                endpoint="#{parentUrl}"
                classnameOption="#{myOption}",
                attrMap="#{attrMap}",
                propList="${newList}"
                edit="#{true}"
                newPropList="#{requestScope.newPropList}"
            );
            javaToJSON(obj="#{requestScope.newPropList}" json="#{requestScope.tmpJSON}");
            gf.restRequest(endpoint="#{pageSession.parentUrl}/property", method="POST", data="#{requestScope.tmpJSON}", result="#{requestScope.restResponse}");
            gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
        />    
    </sun:button>

    <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"  
        onClick="if (checkClassname()) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            saveRealm(
                endpoint="#{parentUrl}",
                classnameOption="#{classnameOption}",
                attrMap="#{attrMap}",
                propList="${newList}"
                edit="#{false}"
            );            
            gf.redirect(page="#{parentPage}?configName=#{configName}")
        />
    </sun:button>
    <sun:button id="cancelButton" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
        <!command
            gf.redirect(page="#{parentPage}?configName=#{configName}")
        />
  </sun:button>

</sun:panelGroup>
