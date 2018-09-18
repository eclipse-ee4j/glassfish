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

<sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}" 
        onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
    <!command
        gf.updateEntity(endpoint="#{pageSession.selfUrl}", attrs="#{pageSession.valueMap}", skipAttrs="#{pageSession.skipAttrsList}",
            convertToFalse="#{pageSession.convertToFalseList}"
        );
        if(#{pageSession.resourceRefUrl}) {
            gf.updateEntity(endpoint="#{pageSession.resourceRefUrl}", attrs="#{pageSession.resourceRef}", convertToFalse={"enabled"});
        }

        removeEmptyProps(props="#{newList}" modifiedProps="#{newList}");
        javaToJSON(obj="#{pageSession.tableList}" json="#{requestScope.tmpJSON}");
        gf.restRequest(endpoint="#{pageSession.selfUrl}/property.json", method="POST", data="#{requestScope.tmpJSON}", result="#{requestScope.restResponse}");

        prepareSuccessfulMsg();
        gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
    />
</sun:button>
<sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
        onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') && checkForBackslash('#{resCompId}', '$resource{i18n.msg.JS.resources.resName}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
    <!command
        // Create connector-resource
        // Update the connector-resource with the Name specified on the connector-connection-pool
        
        mapPut(map="#{pageSession.valueMap2}", key="poolName", value="#{pageSession.valueMap['name']}-Connection-Pool");
        mapPut(map="#{pageSession.valueMap2}", key="name", value="#{pageSession.valueMap['name']}");
        mapPut(map="#{pageSession.valueMap}", key="name", value="#{pageSession.valueMap['name']}-Connection-Pool");

        // Create connector-connection-pool
        gf.createEntity(endpoint="#{pageSession.parentUrl}/connector-connection-pool",
            attrs="#{pageSession.valueMap}", convertToFalse="#{pageSession.convertToFalseList}", result => $attribute{resultEndPoint});
        // Create connector-resource
        mapPut(map="#{pageSession.valueMap2}" key="target" value="domain");
        gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType2}",
                attrs="#{pageSession.valueMap2}", convertToFalse="#{pageSession.convertToFalseList2}", result => $attribute{resultEndPoint3});

        //The selected Targets will be available as #{pageSession.selectedTargets},  which is an array.
        //We need to create source-ref of the selected targets.
        convertArrayToCommaString(array="#{pageSession.selectedTargets}" commaString="#{requestScope.cs}")
        convertStringtoList(str="#{requestScope.cs}" result="#{requestScope.targetList}");
        //println("===========#{requestScope.targetList}");
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
            mapPut(map="#{requestScope.refsMap}" key="id" value="#{pageSession.valueMap2['name']}")
            mapPut(map="#{requestScope.refsMap}" key="enabled" value="#{pageSession.resourceRef['enabled']}")
            mapPut(map="#{requestScope.refsMap}" key="target" value="#{requestScope.targetName}")
            gf.createEntity(endpoint="#{requestScope.endp}" attrs="#{requestScope.refsMap}" convertToFalse={"enabled"});
        }

        getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
        getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
        removeEmptyProps(props="#{newList}" modifiedProps="#{newList}");
        javaToJSON(obj="$attribute{newList}" json="#{requestScope.tmpJSON}");
        urlencode(value="#{pageSession.valueMap.name}" encoding="UTF-8" result="#{pageSession.encodedName}");
        gf.restRequest(endpoint="#{pageSession.parentUrl}/connector-connection-pool/#{pageSession.encodedName}/property.json",
            method="POST", data="#{requestScope.tmpJSON}", result="#{requestScope.restResponse}");
        gf.redirect(page="#{parentPage}")
    />
</sun:button>
<sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
    <!command
        gf.redirect(page="#{parentPage}");
    />
</sun:button>
