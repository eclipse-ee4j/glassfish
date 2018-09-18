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

<!-- jdbc/jdbcConnectionPoolAdvanceButtons.jsf -->

<!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="saveButton"  text="$resource{i18n.button.Save}"
                onClick="if (checkPoolAttr('#{timeoutId}', '#{reqInt}') && guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                if ("#{pageSession.valueMap.isConnectionValidationRequired}=true") {
                    if ("#{pageSession.valueMap.connectionValidationMethod}=table") {
                        if ("#{pageSession.myTableOption}=dropdown") {
                            setValueExpression(expression="#{pageSession.valueMap['validationTableName']}", value="#{pageSession.tableDropDownValue}");
                        }
                        if ("#{pageSession.myTableOption}=text") {
                            setValueExpression(expression="#{pageSession.valueMap['validationTableName']}", value="#{pageSession.tableTextValue}");
                        }
                    }
                    if ("#{pageSession.valueMap.connectionValidationMethod}=custom-validation") {
                        if ("#{pageSession.myValidCNOption}=dropdown") {
                            setValueExpression(expression="#{pageSession.valueMap['validationClassname']}", value="#{pageSession.validCNDropDownValue}");
                        }
                        if ("#{pageSession.myValidCNOption}=text") {
                            setValueExpression(expression="#{pageSession.valueMap['validationClassname']}", value="#{pageSession.validCNTextValue}");
                        }
                        gf.filterMap(map="#{pageSession.valueMap}" attrNames={'validationClassname'} keep="#{true}" resultMap="#{validationClassNameMap}");
                        gf.createEntity(endpoint="#{pageSession.resourceUrl}"
                                attrs="#{validationClassNameMap}"
                         );
                    }
                }

                if ("!(#{pageSession.valueMap.isConnectionValidationRequired}=true)") {
                    //While saving the connection validation settings, first save the connection validation to false.
                    gf.filterMap(map="#{pageSession.valueMap}" attrNames={'isConnectionValidationRequired'} keep="#{true}" resultMap="#{validationMap}");
                    gf.createEntity(endpoint="#{pageSession.resourceUrl}"
                            attrs="#{validationMap}"
                            convertToFalse="#{pageSession.convertToFalseList}"
                     );
                }
                
                if ("#{pageSession.valueMap['lazyConnectionAssociation']}") {
                        setValueExpression(expression="#{pageSession.valueMap['lazyConnectionEnlistment']}", value="true");
                }

                gf.createEntity(endpoint="#{pageSession.resourceUrl}"
                            attrs="#{pageSession.valueMap}"
                            skipAttrs="#{pageSession.skipAttrsList}"
                            convertToFalse="#{pageSession.convertToFalseList}"
                        );
                if ("#{valueMap['ping']}") {
                    urlencode(value="#{valueMap['name']}" encoding="UTF-8" result="#{pageSession.encodeName}");
                    gf.createAttributeMap(keys={"id"} values={"$pageSession{encodeName}"} map="#{requestScope.attrMap}");
                    if(#{pageSession.isAppScopedRes}){
                        mapPut(map="#{requestScope.attrMap}" key="appname" value="#{pageSession.encodedAppName}");
                        if(#{pageSession.moduleName}){
                            mapPut(map="#{requestScope.attrMap}" key="modulename" value="#{pageSession.encodedModuleName}");
                        }
                    }
                    gf.restRequest(endpoint="#{sessionScope.REST_URL}/resources/ping-connection-pool.json"
                                   attrs="#{requestScope.attrMap}"
                                   method="get"
                                   result="#{requestScope.result}");
                }
                prepareSuccessfulMsg();
                gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
        </sun:button>
        <sun:button id="cancelButton" immediate="#{true}" primary="#{false}"text="$resource{i18n.button.Cancel}" >
            <!command
                gf.redirect(page="#{parentPage}");
            />
        </sun:button>
    </sun:panelGroup>
</facet>
