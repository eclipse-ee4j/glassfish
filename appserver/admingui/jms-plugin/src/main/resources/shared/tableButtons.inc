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

<!facet actionsTop>
    <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{topActionGroup});
        />
        <sun:button id="newButton" text="$resource{i18n.button.New}" >
            <!command gf.redirect(page="#{pageSession.createLink}") />
        </sun:button>

        <sun:button id="deleteConnButton" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}" rendered="#{isConnPage}"
            onClick="if (getConfirm(this,'$pageSession{confirmDeleteMsg}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                gf.onlyDASExist(onlyDAS="#{pageSession.onlyDASExist}");
                setPageSessionAttribute(key="target", value="server");
                foreach (var="row1" list="#{requestScope.selectedRows}") {
                    if ("#{row1['objectType']}=system-all-req") {
                        gf.handleError( detail="$resource{i18njms.msg.jms.cannotDeleteDefault, #{row1['name']}}");
                    }
                }
                foreach (var="row" list="#{requestScope.selectedRows}") {
                    urlencode(value="#{row['name']}" result="#{requestScope.encodedResName}");
                    if (!#{pageSession.onlyDASExist}){
                        setAttribute(key="targetUrlsList" value="#{row['targetUrls']}");
                        foreach (var="targetUrl" list="#{requestScope.targetUrlsList}") {
                            createMap(result="#{requestScope.attrMap}");
                            mapGet(Map="#{row['targetsMap']}" Key="#{requestScope.targetUrl}" Value="#{requestScope.targetVal}");
                            mapPut(map="#{requestScope.attrMap}" key="target" value="#{targetVal}");
                            gf.restRequest(
                                endpoint="#{requestScope.targetUrl}/resource-ref/#{requestScope.encodedResName}"
                                attrs="#{requestScope.attrMap}"
                                method="DELETE"
                                result="#{requestScope.foo}");
                        }
                        setPageSessionAttribute(key="target", value="domain");
                    }
                    createMap(result="#{requestScope.delMap}");
                    mapPut(map="#{requestScope.delMap}" key="id" value="#{row['name']}");
                    mapPut(map="#{requestScope.delMap}" key="target" value="#{pageSession.target}");
                    gf.restRequest(
                        endpoint="#{sessionScope.REST_URL}/resources/delete-jms-resource"
                        attrs="#{requestScope.delMap}"
                        method="DELETE"
                        result="#{requestScope.foo}");
                    
                }
                setAttribute(key="selectedRows" value="${selectedRows}");
                gf.redirect(page="#{pageSession.listLink}");
            />
        </sun:button>

        <sun:button id="deleteDestButton" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}" rendered="#{!isConnPage}"
            onClick="if (getConfirm(this,'$pageSession{confirmDeleteMsg}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$pageSession{selectedRows});
                gfr.deleteSelectedResources();
                gf.redirect(page="#{pageSession.listLink}");
            />
        </sun:button>

        <sun:button id="enableButton" text="$resource{i18n.button.Enable}" disabled="#{true}" primary="#{false}">
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$pageSession{selectedRows});
                gfr.enableSelectedResources();
                setAttribute(key="selectedRows" value="$pageSession{selectedRows}");
                prepareAlertMsg(summary="$resource{i18n.msg.enableResourceSuccessful}", type="success");
                gf.redirect(page="#{pageSession.listLink}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
        </sun:button>

        <sun:button id="disableButton" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}">
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$pageSession{selectedRows});
                gfr.disableSelectedResources();
                setAttribute(key="selectedRows" value="$pageSession{selectedRows}");
                prepareAlertMsg(summary="$resource{i18n.msg.disableResourceSuccessful}", type="success");
                gf.redirect(page="#{pageSession.listLink}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
        </sun:button>
     </sun:panelGroup>
 </facet>
