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

    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') && checkClassname()) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    // GlassFish 4 seemingly does not support DELETE and POST for the user-group or principal,
                    // so use update-connector-security-map instead of the separate requests from GlassFish 3.
                    // The "add" and "remove" lists cannot contain the same items for this endpoint.

                    // Get the old user groups and principals.
                    gf.restRequest(endpoint="#{pageSession.selfUrl}/user-group" method="GET" result="#{requestScope.resultU}");
                    setAttribute(key="oldGroupsList" value="#{requestScope.resultU.data.extraProperties.leafList}");
                    gf.restRequest(endpoint="#{pageSession.selfUrl}/principal" method="GET" result="#{requestScope.resultP}");
                    setAttribute(key="oldPrincipalsList" value="#{requestScope.resultP.data.extraProperties.leafList}");

                    // Create the request attributes, which has either user groups or principals (not both).
                    createMap(result="#{requestScope.attrsMap}");
                    mapPut(map="#{requestScope.attrsMap}" key="poolname" value="$pageSession{Name}");
                    mapPut(map="#{requestScope.attrsMap}" key="mapname" value="$pageSession{mapName}");

                    if("#{requestScope.usersOptionU}=users"){
                        // Remove common elements from the old and new "user groups" lists.
                        convertStringtoList(str="#{pageSession.valueMap.userGroupCommaStr}" result="#{requestScope.newGroupsList}");
                        gf.listCombine(list="#{requestScope.oldGroupsCopy}" list2="#{requestScope.oldGroupsList}" result="#{requestScope.oldGroupsCopy}")
                        foreach(var="oneGroup" list="#{requestScope.oldGroupsCopy}"){
                            gf.containedIn(list="#{requestScope.newGroupsList}" testStr="#{requestScope.oneGroup}" contain="containsGroup");
                            if("#{requestScope.containsGroup}"){
                                listRemove(list="#{requestScope.oldGroupsList}" value="#{requestScope.oneGroup}" result="#{requestScope.oldGroupsList}");
                                listRemove(list="#{requestScope.newGroupsList}" value="#{requestScope.oneGroup}" result="#{requestScope.newGroupsList}");
                            }
                        }
                        convertListToCommaString(list="#{requestScope.newGroupsList}" commaString="#{requestScope.newGroupsCommaStr}");
                        mapPut(map="#{requestScope.attrsMap}" key="addusergroups" value="$attribute{newGroupsCommaStr}");
                    }
                    if("#{requestScope.usersOptionP}=principals"){
                        // Remove common elements from the old and new "principals" lists.
                        convertStringtoList(str="#{pageSession.valueMap.principalCommaStr}" result="#{requestScope.newPrincipalsList}");
                        gf.listCombine(list="#{requestScope.oldPrincipalsCopy}" list2="#{requestScope.oldPrincipalsList}" result="#{requestScope.oldPrincipalsCopy}")
                        foreach(var="onePrincipal" list="#{requestScope.oldPrincipalsCopy}"){
                            gf.containedIn(list="#{requestScope.newPrincipalsList}" testStr="#{requestScope.onePrincipal}" contain="containsPrincipal");
                            if("#{requestScope.containsPrincipal}"){
                                listRemove(list="#{requestScope.oldPrincipalsList}" value="#{requestScope.onePrincipal}" result="#{requestScope.oldPrincipalsList}");
                                listRemove(list="#{requestScope.newPrincipalsList}" value="#{requestScope.onePrincipal}" result="#{requestScope.newPrincipalsList}");
                            }
                        }
                        convertListToCommaString(list="#{requestScope.newPrincipalsList}" commaString="#{requestScope.newPrincipalsCommaStr}");
                        mapPut(map="#{requestScope.attrsMap}" key="addprincipals" value="$attribute{newPrincipalsCommaStr}");
                    }

                    convertListToCommaString(list="#{requestScope.oldPrincipalsList}" commaString="#{requestScope.oldPrincipalsCommaStr}");
                    mapPut(map="#{requestScope.attrsMap}" key="removeprincipals" value="$attribute{oldPrincipalsCommaStr}");
                    convertListToCommaString(list="#{requestScope.oldGroupsList}" commaString="#{requestScope.oldGroupsCommaStr}");
                    mapPut(map="#{requestScope.attrsMap}" key="removeusergroups" value="$attribute{oldGroupsCommaStr}");

                    mapPut(map="#{requestScope.attrsMap}" key="mappedpassword" value="#{pageSession.valueMap['mappedPassword']}");

                    gf.restRequest(endpoint="#{sessionScope.REST_URL}/resources/update-connector-security-map" attrs="#{requestScope.attrsMap}" method="POST");

                    prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') &&  checkClassname() && checkForBackslash('#{resCompId}', '$resource{i18n.msg.JS.resources.resName}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    if (#{pageSession.valueMap.userGroupCommaStr}) {
                        convertStringtoList(str="#{pageSession.valueMap.userGroupCommaStr}" result=>$attribute{usersSA});
                    }
                    if (#{pageSession.valueMap.principalCommaStr}) {
                        convertStringtoList(str="#{pageSession.valueMap.principalCommaStr}" result=>$attribute{principalsSA});
                    }
                    if($attribute{usersSA}){
                       mapPut(map="#{pageSession.valueMap}" key="userGroups" value="#{pageSession.valueMap.userGroupCommaStr}")
                    }
                    if($attribute{principalsSA}) {
                        mapPut(map="#{pageSession.valueMap}" key="principals" value="#{pageSession.valueMap.principalCommaStr}")
                    }
                    mapPut(map="#{pageSession.valueMap}" key="poolName" value="#{pageSession.Name}");
                    gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        result => $attribute{objStr});                    
                    gf.redirect(page="#{pageSession.parentPage}")
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
