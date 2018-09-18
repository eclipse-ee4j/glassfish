<!--

    Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.

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
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command                   
                    gf.createEntity(endpoint="#{pageSession.selfUrl}"
                        attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                    );

                    gf.combineProperties(tableList="#{pageSession.tableList}" combined="#{requestScope.combined}");
                    removeEmptyProps(props="#{requestScope.combined}" modifiedProps="#{requestScope.combined}");
                    javaToJSON(obj="#{requestScope.combined}" json="#{requestScope.tmpJSON}");
                    gf.restRequest(endpoint="#{pageSession.selfUrl}/property.json", method="POST", data="#{requestScope.tmpJSON}", result="#{requestScope.restResponse}");
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>


            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                    onClick="if (CheckForEmptyName('#{pageSession.myOption}', '$resource{i18njca.msg.Error.resourceAdapterNameCannotBeEmpty}') &&
                        guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    if ("#{pageSession.myOption}=dropdown") {
                        setValueExpression(expression="#{pageSession.valueMap['name']}", value="#{pageSession.nameDropDown}");
                    }
                    if ("#{pageSession.myOption}=text") {
                        setValueExpression(expression="#{pageSession.valueMap['name']}", value="#{pageSession.nameText}");
                    }
                    gf.combineProperties(tableList="#{pageSession.tableList}" combined="#{requestScope.combined}");
                    removeEmptyProps(props="#{requestScope.combined}" modifiedProps="#{requestScope.combined}");
                    gf.createPropertyString(properties="#{requestScope.combined}", string="#{requestScope.props}");
                    mapPut(map="#{pageSession.valueMap}", key="property", value="#{requestScope.props}");
                    gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        result => $attribute{objStr});
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
