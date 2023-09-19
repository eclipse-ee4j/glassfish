<!--

    Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
    Copyright (c) 2023 Contributors to the Eclipse Foundation.

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
        <sun:button id="deployButton" text="$resource{i18n.button.Deploy}" primary="#{true}">
                <!command
		    gf.redirect(page="#{pageSession.nextPageLink}") />
        </sun:button>
        <sun:button id="button1" text="$resource{i18n.button.Undeploy}" disabled="#{true}" primary="#{false}"
                onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
                    { return submitAndDisable(this, '$resource{i18n.button.Processing}');}
                    else {return false;} " >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                gf.undeploy(selectedRows="${selectedRows}" );
                gf.navigate(page="#{listPageLink}");
            />
         </sun:button>
        <sun:button id="button2" text="$resource{i18n.button.Enable}" rendered="#{pageSession.onlyDASExist}" disabled="#{true}" primary="#{false}"
                onClick="return submitAndDisable(this, '$resource{i18n.button.Processing}');" >
        <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                prepareAlertMsg(type="success" summary="$resource{i18n.msg.enableSuccessfulPE}")
                foreach (var="row" list="#{requestScope.selectedRows}") {
                   urlencode(value="#{row['name']}" result="#{requestScope.encodedName}");
                   createMap(result="#{requestScope.attrMap}");
                   mapPut(map="#{requestScope.attrMap}" key="target" value="server");
                   gf.restRequest(
                        endpoint="#{sessionScope.REST_URL}/applications/application/#{requestScope.encodedName}/enable"
                        attrs="#{requestScope.attrMap}"
                        method="POST");
                }
                gf.navigate(page="#{listPageLink}");
            />
            </sun:button>
        <sun:button id="button3" text="$resource{i18n.button.Disable}" rendered="#{pageSession.onlyDASExist}" disabled="#{true}" primary="#{false}"
            onClick="return submitAndDisable(this, '$resource{i18n.button.Processing}');" >
        <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                prepareAlertMsg(type="success" summary="$resource{i18n.msg.disableSuccessfulPE}")
                foreach (var="row" list="#{requestScope.selectedRows}") {
                   urlencode(value="#{row['name']}" result="#{requestScope.encodedName}");
                   createMap(result="#{requestScope.attrMap}");
                   mapPut(map="#{requestScope.attrMap}" key="target" value="server");
                   gf.restRequest(
                        endpoint="#{sessionScope.REST_URL}/applications/application/#{requestScope.encodedName}/disable"
                        attrs="#{requestScope.attrMap}"
                        method="POST");
                }
                gf.navigate(page="#{listPageLink}");
            />
        </sun:button>

        <sun:image id="actionTopSeparator1" 
           align="top"
           height="$int{18}"
           width="$int{18}"
           url="#{request.contextPath}/theme/com/sun/webui/jsf/suntheme/images/table/actions_separator.gif"
        />

       <sun:dropDown id="filter"  selected="#{filterValue}" label="$resource{i18n.common.filterDropDown}" submitForm="true" immediate="true" values="${filters}" labels="${filters}" onChange="return admingui.woodstock.dropDownChanged(this);">
            <!command
                setAttribute(key="click" value="$this{component}");
                setAttribute(key="filterValue" value="#{click.selected}");
                gf.redirect(page="#{pageSession.listPageLink}?filterValue=${filterValue}" );
            />
        </sun:dropDown>

     </sun:panelGroup>
 </facet>
