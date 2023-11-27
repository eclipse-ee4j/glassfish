<!--

    Copyright (c) 2023 Contributors to the Eclipse Foundation
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

<!-- applications/depoyTableButtons.inc -->

<!facet actionsTop>
     <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{topActionGroup});
        />
        <sun:button id="button1" text="$resource{i18n.button.Enable}" disabled="#{true}" primary="#{false}" >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                // enable the resource for a target
                foreach (var="row" list="#{requestScope.selectedRows}") {
                    gf.createAttributeMap(keys={"enabled"} values={"true"} map="#{requestScope.attrMap}");
                    urlencode(value="#{row['Name']}" result="#{requestScope.encodedResName}");
                    gf.restRequest(
                        endpoint="#{pageSession.restURL}/#{requestScope.encodedResName}"
                        method="GET"
                        result="#{requestScope.result}");
                    if ("#{requestScope.result.responseCode}=200") {
                        gf.restRequest(
                            endpoint="#{pageSession.restURL}/#{requestScope.encodedResName}"
                            attrs="#{requestScope.attrMap}"
                            method="POST"
                            result="#{requestScope.foo}");
                    }
                }
                setAttribute(key="selectedRows" value="${selectedRows}");
                prepareAlertMsg(summary="$resource{i18n.msg.enableResourceSuccessful}", type="success");
                gf.redirect(page="#{pageSession.listLink}&filterValue=#{pageSession.filterValue}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
        </sun:button>
        <sun:button id="button2" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}" >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                // disable the resource for a target
                foreach (var="row" list="#{requestScope.selectedRows}") {
                    gf.createAttributeMap(keys={"enabled"} values={"false"} map="#{requestScope.attrMap}");
                    urlencode(value="#{row['Name']}" result="#{requestScope.encodedResName}");
                    gf.restRequest(
                        endpoint="#{pageSession.restURL}/#{requestScope.encodedResName}"
                        method="GET"
                        result="#{requestScope.result}");
                    if ("#{requestScope.result.responseCode}=200") {
                        gf.restRequest(
                            endpoint="#{pageSession.restURL}/#{requestScope.encodedResName}"
                            attrs="#{requestScope.attrMap}"
                            method="POST"
                            result="#{requestScope.foo}");
                    }
                }
                setAttribute(key="selectedRows" value="${selectedRows}");
                prepareAlertMsg(summary="$resource{i18n.msg.disableResourceSuccessful}", type="success");
                gf.redirect(page="#{pageSession.listLink}&filterValue=#{pageSession.filterValue}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
        </sun:button>
        
        <sun:image id="actionTopSeparator1" 
           align="top"
           height="$int{18}"
           width="$int{18}"
           url="#{request.contextPath}/theme/com/sun/webui/jsf/suntheme/images/table/actions_separator.gif"
        />
        <sun:dropDown id="actions" selected="#{actionValue}" submitForm="#{true}" immediate="#{true}" toolTip="$resource{i18n.common.newDropDown}"
                      labels="$attribute{labels}" values="$attribute{values}"
                >
              <!beforeCreate
                    getContentOfIntegrationPoints(type="org.glassfish.admingui:resourceTypeDropdown" labels="#{requestScope.labels}" values="#{requestScope.values}" );
                    listAdd(list="#{requestScope.labels}" value="$resource{i18n.common.newDropDown}",  index="0");
                    listAdd(list="#{requestScope.values}" value=""  index="0" );
                />
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="actionValue" value="#{click.selected}");
                    gf.redirect(page="#{request.contextPath}/#{click.selected}?target=#{pageSession.target}" );
                />
            </sun:dropDown>

            <sun:dropDown id="filter" selected="$pageSession{filterValue}" label="$resource{i18n.common.filterDropDown}" submitForm="#{true}" immediate="#{true}"
                labels="$attribute{filterLabels}" values="$attribute{filterValues}">
                <!beforeCreate
                    getContentOfIntegrationPoints(type="org.glassfish.admingui:resFilterDropdown" labels="#{requestScope.filterLabels}" values="#{requestScope.filterValues}" );
                    listAdd(list="#{requestScope.filterLabels}" value="$resource{i18n.common.showAll}",  index="0");
                    listAdd(list="#{requestScope.filterValues}" value=""  index="0" );
                />
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="filterValue" value="#{click.selected}");
                    gf.redirect(page="$pageSession{listLink}&filterValue=${filterValue}" );
                />
            </sun:dropDown>
       </sun:panelGroup>
 </facet>
