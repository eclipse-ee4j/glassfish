<!--

    Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.

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

   <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                onClick="if (isSecurityMappingPresent('$resource{i18n.msg.JS.Error.securityMappingCannotBeEmpty}') && guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    mapPut(map="#{pageSession.valueMap}" key="resourceAdapterName" value="#{pageSession.valueMap['raname']}");
                    gf.createEntity(endpoint="#{pageSession.selfUrl}"
                        attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}");
                    if ('#{usersOptionG}=users') {
                        convertStringToMap(str="#{pageSession.valueMap2['groupMapping']}" result="#{requestScope.groupsMap}");
                        gf.getMapKeys(Map="#{requestScope.groupsMap}" Keys="#{requestScope.groupsList}");
                        gf.getChildList(parentEndpoint="#{pageSession.selfUrl}", childType="group-map", result="#{requestScope.oldGroupsInfo}", id="eisGroup");
                        gf.getMessageProps(messageListProps="#{requestScope.oldGroupsInfo}"
                           id="eisGroup"
                           keyList="#{pageSession.oldGroupsList}");
                        setPageSessionAttribute(key="groupsToAddList" value={});
                        foreach(var="group" list="#{requestScope.groupsList}") {
                            //verify whether this group already existing or not.
                            //If it is already there, update the entity or add to the new groups list.
                            gf.containedIn(list="#{pageSession.oldGroupsList}" testStr="#{group}" contain="#{requestScope.isExists}");
                            if (#{requestScope.isExists}) {
                                createMap(result="#{requestScope.attrsMap}");
                                mapGet(Map="#{requestScope.groupsMap}" Key="#{group}" Value="#{requestScope.val}");
                                mapPut(map="#{requestScope.attrsMap}" key="mappedGroup" value="#{val}");
                                gf.restRequest(endpoint="#{pageSession.selfUrl}/group-map/#{group}" attrs="#{requestScope.attrsMap}" method="POST");
                                removeListElement(list="#{pageSession.oldGroupsList}" name="#{group}" finalList="#{pageSession.oldGroupsList}");
                            }
                            if (!#{requestScope.isExists}) {
                                mapGet(Map="#{requestScope.groupsMap}" Key="#{group}" Value="#{requestScope.val}");
                                listAdd(list="#{pageSession.groupsToAddList}" value="#{group}=#{val}" result="#{pageSession.groupsToAddList}");                                
                            }                            
                        }
                        if(!#{pageSession.isAppScopedRes}){
                            convertListToCommaString(list="#{pageSession.groupsToAddList}" commaString=>$attribute{groupsToAddStr});
                            convertListToCommaString(list="#{pageSession.oldGroupsList}" commaString=>$attribute{oldGroupsStr});
                            createMap(result="#{requestScope.attrsMap}");
                            mapPut(map="#{requestScope.attrsMap}" key="raname" value="#{pageSession.valueMap['resourceAdapterName']}");
                            mapPut(map="#{requestScope.attrsMap}" key="addgroups" value="#{groupsToAddStr}");
                            mapPut(map="#{requestScope.attrsMap}" key="removegroups" value="#{oldGroupsStr}");
                            gf.restRequest(endpoint="#{pageSession.selfUrl}/update-connector-work-security-map" attrs="#{requestScope.attrsMap}" method="POST");
                        }
                    }
                    if ('#{usersOptionP}=principals') {
                        convertStringToMap(str="#{pageSession.valueMap2['principalMapping']}" result="#{requestScope.principalsMap}");
                        gf.getMapKeys(Map="#{requestScope.principalsMap}" Keys="#{requestScope.principalsList}");
                        gf.getChildList(parentEndpoint="#{pageSession.selfUrl}", childType="principal-map", result="#{requestScope.oldPrincipalsInfo}", id="eisPrincipal");
                        gf.getMessageProps(messageListProps="#{requestScope.oldPrincipalsInfo}"
                           id="eisPrincipal"
                           keyList="#{pageSession.oldPrincipalsList}");
                        setPageSessionAttribute(key="principalsToAddList" value={});
                        foreach(var="principal" list="#{requestScope.principalsList}") {
                            //verify whether this principal already existing or not.
                            //If it is already there, update the entity or add to the new principals list.
                            gf.containedIn(list="#{pageSession.oldPrincipalsList}" testStr="#{principal}" contain="#{requestScope.isExists}");
                            if (#{requestScope.isExists}) {
                                createMap(result="#{requestScope.attrsMap}");
                                mapGet(Map="#{requestScope.principalsMap}" Key="#{principal}" Value="#{requestScope.val}");
                                mapPut(map="#{requestScope.attrsMap}" key="mappedPrincipal" value="#{val}");
                                gf.restRequest(endpoint="#{pageSession.selfUrl}/principal-map/#{principal}" attrs="#{requestScope.attrsMap}" method="POST");
                                removeListElement(list="#{pageSession.oldPrincipalsList}" name="#{principal}" finalList="#{pageSession.oldPrincipalsList}");
                            }
                            if (!#{requestScope.isExists}) {
                                mapGet(Map="#{requestScope.principalsMap}" Key="#{principal}" Value="#{requestScope.val}");
                                listAdd(list="#{pageSession.principalsToAddList}" value="#{principal}=#{val}" result="#{pageSession.principalsToAddList}");
                            }
                        }
                        if(!#{pageSession.isAppScopedRes}){
                            convertListToCommaString(list="#{pageSession.principalsToAddList}" commaString=>$attribute{principalsToAddStr});
                            convertListToCommaString(list="#{pageSession.oldPrincipalsList}" commaString=>$attribute{oldPrincipalsStr});
                            createMap(result="#{requestScope.attrsMap}");
                            mapPut(map="#{requestScope.attrsMap}" key="raname" value="#{pageSession.valueMap['resourceAdapterName']}");
                            mapPut(map="#{requestScope.attrsMap}" key="addprincipals" value="#{principalsToAddStr}");
                            mapPut(map="#{requestScope.attrsMap}" key="removeprincipals" value="#{oldPrincipalsStr}");
                            gf.restRequest(endpoint="#{pageSession.selfUrl}/update-connector-work-security-map" attrs="#{requestScope.attrsMap}" method="POST");
                        }

                    }                   
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                onClick="if (isSecurityMappingPresent('$resource{i18n.msg.JS.Error.securityMappingCannotBeEmpty}') && guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') && checkForBackslash('#{resCompId}', '$resource{i18n.msg.JS.resources.resName}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    if ('#{usersOptionG}=users') {                        
                        mapPut(map="#{pageSession.valueMap}" key="groupsmap" value="#{pageSession.valueMap2['groupMapping']}");
                    }
                    if ('#{usersOptionP}=principals') {
                        mapPut(map="#{pageSession.valueMap}" key="principalsmap" value="#{pageSession.valueMap2['principalMapping']}");
                    }
                    gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}");
                    
                    gf.redirect(page="#{pageSession.parentPage}")
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}"  text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
