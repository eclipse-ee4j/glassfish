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

<!-- resourceNode/resourceEditPageButtons.inc -->

#include "/common/shared/editPageLoadDefaultButton.inc"

    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    if ("#{pageSession.isConcurrent}=true") {
                        convertArrayToCommaString(array="#{pageSession.contextInfo}"  commaString="#{requestScope.tmp}");
                        mapPut(map="#{pageSession.valueMap}" key="contextInfo" value="#{requestScope.tmp}");
                    }

                    //control enable status in the ref link. always ensure that the resource itself is true. This will be called for DAS only case.
                    mapPut(map="#{pageSession.valueMap}", key="enabled", value="true");

                    gf.createEntity(endpoint="#{pageSession.selfUrl}"
                        attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                    );
                    if(#{pageSession.has2Objects}){
                        gf.checkIfEndPointExist(endpoint="#{pageSession.selfUrl2}" exists="#{requestScope.exist}" );
                        if("! #{requestScope.exist}"){
                             createMap(result="#{requestScope.refsMap}")
                             mapPut(map="#{requestScope.refsMap}" key="id" value="#{pageSession.encodedName}")
                             mapPut(map="#{requestScope.refsMap}" key="enabled" value="#{pageSession.valueMap2['enabled']}")
                             gf.createEntity(endpoint="#{sessionScope.REST_URL}/servers/server/server/resource-ref",
                                 attrs="#{requestScope.refsMap}"
                                 skipAttrs="#{pageSession.skipAttrsList2}"
                                 convertToFalse="#{pageSession.convertToFalseList2}"
                                 onlyUseAttrs="#{pageSession.onlyUseAttrs2}"
                             );
                        }
                        if ("#{requestScope.exist}"){
                            gf.createEntity(endpoint="#{pageSession.selfUrl2}"
                                attrs="#{pageSession.valueMap2}"
                                skipAttrs="#{pageSession.skipAttrsList2}"
                                convertToFalse="#{pageSession.convertToFalseList2}"
                                onlyUseAttrs="#{pageSession.onlyUseAttrs2}"
                            );
                        }
                    }
                    if (#{pageSession.hasPropertyTable}){
                        if (#{pageSession.hasConfidential}){
                            gf.combineProperties(tableList="#{pageSession.tableList}" combined="#{requestScope.newList}");
                        }
                        if (!#{pageSession.hasConfidential}){
                            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                        }
                    }
                    removeEmptyProps(props="#{newList}" modifiedProps="#{newList}");
                    javaToJSON(obj="#{newList}" json="#{requestScope.tmpJSON}");
                    gf.restRequest(endpoint="#{pageSession.selfUrl}/property.json" method="POST" data="#{requestScope.tmpJSON}" result="#{requestScope.restResponse}");
                    prepareSuccessfulMsg();                
                    gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>


            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') && checkForBackslash('#{resCompId}', '$resource{i18n.msg.JS.resources.resName}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    if (#{pageSession.hasConfidential}){
                        gf.combineProperties(tableList="#{pageSession.tableList}" combined="#{requestScope.newList}");
                    }

                    if (!#{pageSession.hasConfidential}){
                        getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                        getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    }
                    if ("#{pageSession.isConcurrent}=true") {
                        convertArrayToCommaString(array="#{pageSession.contextInfo}"  commaString="#{requestScope.tmp}");
                        mapPut(map="#{pageSession.valueMap}" key="contextInfo" value="#{requestScope.tmp}");
                    }
                    //TODO: remove the test for admin-object-resource after GLASSFISH-19533
                    if ("#{pageSession.childType}=admin-object-resource") {
                        mapPut(map="#{pageSession.valueMap}" key="target" value="server-config");
                    }
                    if ("!(#{pageSession.childType}=admin-object-resource)") {
                        mapPut(map="#{pageSession.valueMap}" key="target" value="domain");
                    }
                    gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        result => $attribute{objStr});
                    <!--
                        For creating any resource, we should
                        1. create the resource with "domain" as the Target
                        2. for each of the target choosen, create the resource-ref for it.

                        The following and the above line that set domain as target has been commented out because backend throws exception
                        when using "domain" as target.  When it is resolved, we can uncomment and code accordingly.
                     -->
                    //The selected Targets will be available as #{pageSession.selectedTargets},  which is an array.
                    //We need to create source-ref of the selected targets.
                    convertArrayToCommaString(array="#{pageSession.selectedTargets}" commaString="#{requestScope.cs}")
                    convertStringtoList(str="#{requestScope.cs}" result="#{requestScope.targetList}");
                    setAttribute(key="nTargets" value="#{requestScope.targetList.size()}");
                    dec(number="#{requestScope.nTargets}" value="#{requestScope.endRef}");
                    forLoop(end="#{requestScope.endRef}"  varName="nn"){
                        setAttribute(key="targetName" value="#{requestScope.targetList[nn]}");
                        gf.checkIfEndPointExist(endpoint="#{sessionScope.REST_URL}/servers/server/#{requestScope.targetName}", exists="#{requestScope.isServer}");
                        if (#{requestScope.isServer}){
                            setAttribute(key="endp" value="#{sessionScope.REST_URL}/servers/server/#{requestScope.targetName}/resource-ref");
                        }
                        if (!#{requestScope.isServer}){
                            setAttribute(key="endp" value="#{sessionScope.REST_URL}/clusters/cluster/#{requestScope.targetName}/resource-ref");
                        }
                        createMap(result="#{requestScope.refsMap}")
                        mapPut(map="#{requestScope.refsMap}" key="id" value="#{pageSession.valueMap['name']}")
                        mapPut(map="#{requestScope.refsMap}" key="enabled" value="#{pageSession.valueMap2['enabled']}")
                        mapPut(map="#{requestScope.refsMap}" key="target" value="#{requestScope.targetName}")
                        gf.createEntity(endpoint="#{requestScope.endp}" attrs="#{requestScope.refsMap}" convertToFalse={"enabled"});
                    }

                    removeEmptyProps(props="#{newList}" modifiedProps="#{newList}");
                    javaToJSON(obj="#{newList}" json="#{requestScope.tmpJSON}");
                    urlencode(value="#{pageSession.valueMap['name']}" encoding="UTF-8" result="#{requestScope.encodeName}");
                    gf.restRequest(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}/#{requestScope.encodeName}/property.json" method="POST" data="#{requestScope.tmpJSON}" result="#{requestScope.restResponse}");
                    
                    if (#{pageSession.showSuccessfulMsg}){
                        prepareSuccessfulMsg();
                        gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                    }
                    gf.redirect(page="#{pageSession.parentPage}?configName=#{configName}")
                />
            </sun:button>


            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}?configName=#{configName}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
