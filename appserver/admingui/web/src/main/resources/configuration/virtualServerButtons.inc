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

<!-- configuration/virtualServerButtons.inc -->
#include "/common/shared/commonHandlers.inc"
<!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    convertArrayToCommaString(array="#{pageSession.selectedNetworkListeners}"  commaString="#{requestScope.tmp}");
                    mapPut(map="#{pageSession.valueMap}" key="networkListeners" value="$attribute{tmp}");
                    
                    gf.createEntity(endpoint="#{pageSession.selfUrl}"
                        attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}");
                    createMap(result="#{pageSession.statusAttrs}");
                    mapPut(map="#{pageSession.statusAttrs}" key="DEFAULT" value="#{pageSession.configName}" );
                    gfr.getListOfInstances();
                    if(#{pageSession.configName}=server-config){
                        listAdd(list="#{pageSession.instanceList}" value="server" index="0");
                    }
                    gf.ensureDefaultWebModule(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}",vsName="#{pageSession.valueMap['id']}",instanceList="#{pageSession.instanceList}");
                    getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                    getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    removeEmptyProps(props="#{pageSession.tableList}" modifiedProps="#{pageSession.tableList}");
                    javaToJSON(obj="#{pageSession.tableList}" json="#{requestScope.tmpJSON}");
		    gf.restRequest(endpoint="#{pageSession.selfUrl}/property.json" method="POST" data="#{requestScope.tmpJSON}"
                                        result="#{requestScope.restResponse}");
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    convertArrayToCommaString(array="#{pageSession.selectedNetworkListeners}"  commaString="#{requestScope.tmp}");
                    mapPut(map="#{pageSession.valueMap}" key="networkListeners" value="$attribute{tmp}");
                    setPageSessionAttribute(key="onlyUseAttrs" value={"id", "state", "target", "hosts", "logFile", "defaultWebModule", "networkListeners", "logfile"})
                    gf.createEntity(endpoint="#{pageSession.selfUrl}"
                            attrs="#{pageSession.valueMap}"
                            skipAttrs="#{pageSession.skipAttrsList}"
                            convertToFalse="#{pageSession.convertToFalseList}"
                            onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                    );
                    setPageSessionAttribute(key="onlyUseAttrs" value={"ssoEnabled", "ssoCookieHttpOnly", "accessLoggingEnabled", "docroot", "accessLog"})
                    gf.createEntity(endpoint="#{pageSession.selfUrl}/#{pageSession.valueMap['id']}"
                            attrs="#{pageSession.valueMap}"
                            onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                            convertToFalse="#{pageSession.convertToFalseList}"
                    );
                    createMap(result="#{pageSession.statusAttrs}");
                    mapPut(map="#{pageSession.statusAttrs}" key="DEFAULT" value="#{pageSession.configName}" );
                    gfr.getListOfInstances();
                    if(#{pageSession.configName}=server-config){
                        listAdd(list="#{pageSession.instanceList}" value="server" index="0");
                    }
                    gf.ensureDefaultWebModule(endpoint="#{pageSession.selfUrl}",vsName="#{pageSession.valueMap['id']}",instanceList="#{pageSession.instanceList}");
                    getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                    getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    removeEmptyProps(props="#{pageSession.tableList}" modifiedProps="#{pageSession.tableList}");
                    javaToJSON(obj="#{pageSession.tableList}" json="#{requestScope.tmpJSON}");
		    gf.restRequest(endpoint="#{pageSession.selfUrl}/#{pageSession.valueMap['id']}/property.json" method="POST" data="#{requestScope.tmpJSON}" result="#{requestScope.restResponse}");
                    gf.redirect(page="#{parentPage}?configName=#{configName}")
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}?configName=#{configName}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
