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

<!-- jdbc/jdbcConnectionPoolEditButtons.inc -->

<!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                    onClick="if (isClassNamePresent('$resource{i18njdbc.msg.Error.classNameCannotBeEmpty}') && guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    gf.updateEntity(endpoint="#{pageSession.resourceUrl}"
                        attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        onlyUseAttrs="#{pageSession.onlyUseAttrs}"
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
                                       method="GET"
                                       result="#{requestScope.result}");
                    }
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}");
                />
            </sun:button>
        </sun:panelGroup>
</facet>
