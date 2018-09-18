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

<!-- jca/connectorConnectionPoolAdvanceButtons.inc -->


<!facet pageButtonsTop>
<sun:panelGroup id="topButtons">
    <sun:button id="saveButton"  text="$resource{i18n.button.Save}"
            onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            if ("#{pageSession.valueMap['lazyConnectionAssociation']}") {
                setValueExpression(expression="#{pageSession.valueMap['lazyConnectionEnlistment']}", value="true");
            }
            
            //Issue 15618 : To save lazyConnectionEnlistment before lazyConnectionAssociation.
            createMap(result=>$attribute{lazyConnectionMap});
            mapPut(map="#{lazyConnectionMap}", key="lazyConnectionEnlistment", value="#{pageSession.valueMap['lazyConnectionEnlistment']}");
            gf.updateEntity(endpoint="#{pageSession.selfUrl}"
                        attrs="#{lazyConnectionMap}"
                        convertToFalse={"lazyConnectionEnlistment"});
            gf.filterMap(map="#{pageSession.valueMap}" attrNames={"lazyConnectionEnlistment"} keep="#{false}" resultMap="#{pageSession.valueMap}");
            
            gf.updateEntity(endpoint="#{pageSession.selfUrl}"  attrs="#{pageSession.valueMap}"
                skipAttrs="#{pageSession.skipAttrsList}"
                convertToFalse="#{pageSession.convertToFalseList}"
                onlyUseAttrs="#{pageSession.onlyUseAttrs}"
            );
            prepareSuccessfulMsg();
            if ("#{valueMap['ping']}") {
                urlencode(value="#{valueMap['name']}" encoding="UTF-8" result="#{pageSession.encodeName}");
                gf.createAttributeMap(keys={"id"} values={"$pageSession{encodeName}"} map="#{requestScope.attrMap}");
                if(#{pageSession.isAppScopedRes}){
                    mapPut(map="#{requestScope.attrMap}" key="appname" value="#{pageSession.encodedAppName}");
                    if(#{pageSession.moduleName}){
                        mapPut(map="#{requestScope.attrMap}" key="modulename" value="#{pageSession.encodedModuleName}");
                    }
                }
                gf.restRequest(endpoint="#{sessionScope.REST_URL}/resources/ping-connection-pool"
                               attrs="#{requestScope.attrMap}"
                               method="GET"
                               result="#{requestScope.result}");
                if ("#{requestScope.result.data['exit_code']} = SUCCESS") {
                     prepareAlertMsg(summary="$resource{i18n.msg.PingSucceed}", type="success");
                }
                if ("!(#{requestScope.result.data['exit_code']} = SUCCESS)") {
                     prepareAlertMsg(summary="$resource{i18njca.msg.warning.poolSavedPingFailed}", type="error");
                }                
            }
            gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
        />
    </sun:button>

    <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Cancel}" >
        <!command
            gf.redirect(page="#{parentPage}");
        />
    </sun:button>
</sun:panelGroup>
</facet>
