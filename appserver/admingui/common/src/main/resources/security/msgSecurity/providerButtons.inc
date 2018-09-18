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

<!-- providerButtons.inc -->

    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    saveMsgProviderInfo(edit="true" attrMap="#{pageSession.valueMap}" configName="#{pageSession.configName}" )
                    javaToJSON(obj="#{pageSession.tableList}" json="#{requestScope.tmpJSON}");
                    gf.restRequest(endpoint="#{pageSession.selfUrl}/property.json" method="POST" data="#{requestScope.tmpJSON}" result="#{requestScope.restResponse}");
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    saveMsgProviderInfo(edit="false" attrMap="#{pageSession.valueMap}" configName="#{pageSession.configName}" )
                    removeEmptyProps(props="#{pageSession.tableList}" modifiedProps="#{pageSession.tableList}");
                    javaToJSON(obj="#{pageSession.tableList}" json="#{requestScope.tmpJSON}");
                    setPageSessionAttribute(key="selfUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/security-service/message-security-config/#{pageSession.msgSecurityName}/provider-config/#{pageSession.valueMap['Name']}");
                    gf.restRequest(endpoint="#{pageSession.selfUrl}/property.json"
                                   method="POST" data="#{requestScope.tmpJSON}" result="#{requestScope.restResponse}");
                    gf.redirect(page="#{pageSession.parentPage}");
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{pageSession.cancelPage}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
