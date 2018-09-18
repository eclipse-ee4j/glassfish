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

<!facet actionsTop>
     <sun:panelGroup id="topActionsGroup1">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />
        <sun:button id="newButton" text="$resource{i18n.button.New}" >
            <!command gf.redirect(page="#{pageSession.createLink}") />
        </sun:button>

        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
            onClick="if (getConfirm(this,'$pageSession{confirmDeleteMsg}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$pageSession{selectedRows});
            gfr.deleteSelectedResources();
            gf.redirect(page="#{pageSession.listLink}");
        />
        </sun:button>
        <sun:button id="button2" text="$resource{i18n.button.Enable}" disabled="#{true}" primary="#{false}" >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$pageSession{selectedRows});
                gfr.enableSelectedResources();
                setAttribute(key="selectedRows" value="$pageSession{selectedRows}");
                prepareAlertMsg(summary="$resource{i18n.msg.enableResourceSuccessful}", type="success");
                gf.redirect(page="#{pageSession.listLink}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
        </sun:button>
        <sun:button id="button3" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}" >
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
