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

<!-- jdbc/jdbcConnectionPoolNew2Buttons.inc -->
<!facet pageButtonsTop>
  <sun:panelGroup id="topButtons">
    <sun:button id="previousButton" primary="#{false}"
        text="$resource{i18n.button.Previous}"
        onClick="document.getElementById('form2:validate').value='false'; return submitAndDisable(this, '$resource{i18n.button.Processing}');" >
        <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            setSessionAttribute(key="wizardPoolProperties", value="$attribute{newList}");
            setAttribute(key="fromStep2" value="#{true}")
            gf.navigate(page="#{request.contextPath}/jdbc/#{step1Page}");
        />
    </sun:button>

    <sun:button id="finishButton" text="$resource{i18n.button.Finish}"
        onClick="document.getElementById('form2:validate').value='true';
            if (isClassNamePresent('#{wizardPoolExtra.datasourceClassname}', '#{wizardPoolExtra.driverClassname}', '#{datasourceClassNameFieldId}', '#{driverClassNameFieldId}', '$resource{i18njdbc.msg.Error.classNameCannotBeEmpty}')
            && guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
       <!command
            updateJdbcConnectionPoolWizardStep2();
            setAttribute(key="pingValue" value="#{wizardMap['ping']}");
            mapPut(map="#{wizardMap}" key="ping" value="false");
            gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}",
                    attrs="#{wizardMap}"
                    convertToFalse="#{pageSession.convertToFalseList}"
                    result => $attribute{resultEndPoint});
            if (#{pageSession.hasPropertyTable}){
                getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                removeEmptyProps(props="#{newList}" modifiedProps="#{newList}");
                javaToJSON(obj="#{newList}" json="#{requestScope.tmpJSON}");
                urlencode(value="#{wizardMap['name']}" encoding="UTF-8" result="#{pageSession.encodeName}");
                gf.restRequest(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}/#{pageSession.encodeName}/property.json"
                               method="POST"
                               data="#{requestScope.tmpJSON}"
                               result="#{requestScope.restResponse}");
            }
            if ("#{pingValue}") {
                urlencode(value="#{wizardMap['name']}" encoding="UTF-8" result="#{pageSession.encodedName}");
                gf.createAttributeMap(keys={"id"} values={"$pageSession{encodedName}"} map="#{requestScope.attrMap}");
                gf.restRequest(endpoint="#{sessionScope.REST_URL}/resources/ping-connection-pool.json"
                               attrs="#{requestScope.attrMap}"
                               method="get"
                               throwException="#{false}"
                               result="#{requestScope.result}");
                if ("#{requestScope.result.data['exit_code']} = SUCCESS") {
                     prepareAlertMsg(summary="$resource{i18n.msg.PingSucceed}", type="success");
                }
                if ("!(#{requestScope.result.data['exit_code']} = SUCCESS)") {
                     prepareAlertMsg(summary="$resource{i18njdbc.msg.warning.poolCreatedPingFailed}", detail="#{requestScope.result.data.message}", type="warning");
                }
                setSessionAttribute(key="wizardMap" value="#{null}")
                setSessionAttribute(key="wizardPoolExtra" value="#{null}")
                setSessionAttribute(key="wizardPoolProperties" value="#{null}")
                gf.redirect(page="#{parentPage}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            }
            if ("!#{pingValue}") {
                setSessionAttribute(key="wizardMap" value="#{null}")
                setSessionAttribute(key="wizardPoolExtra" value="#{null}")
                setSessionAttribute(key="wizardPoolProperties" value="#{null}")
                gf.redirect(page="#{parentPage}");
            }
        />    
    </sun:button>
    <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Cancel}" > 
       <!command
            setSessionAttribute(key="wizardMap" value="#{null}")
            setSessionAttribute(key="wizardPoolExtra" value="#{null}")
            setSessionAttribute(key="wizardPoolProperties" value="#{null}")
            gf.redirect(page="#{parentPage}");
        />    
    </sun:button>
  </sun:panelGroup>
</facet>
