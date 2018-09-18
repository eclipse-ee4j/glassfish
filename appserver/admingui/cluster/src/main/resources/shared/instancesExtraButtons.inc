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

<!-- cluster/shared/instancesExtraButtons.inc -->

<!define name="listTableDeleteButton">
    <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
            onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
                { return submitAndDisable(this, '$resource{i18n.button.Processing}');}  else {return false;} " >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component="#{requestScope.tableRowGroup}");
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows="#{requestScope.selectedRows}");
            gf.instanceAction(rows="#{requestScope.selectedRows}", action="delete-instance" );
            gf.redirect(page="#{pageSession.listLink}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
        />
    </sun:button>
</define>

<!define name="extraButton2">
   <sun:button id="button2" text="$resource{i18n.button.Start}" disabled="#{true}" primary="#{false}"
        onClick="if ( getConfirm(this,'$resource{i18ncs.msg.JS.confirmStartInstances}') )
            { return submitAndDisable(this, '$resource{i18n.button.Processing}');}  else {return false;} " >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component="#{requestScope.tableRowGroup}");
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows="#{requestScope.selectedRows}");
            gf.instanceAction(rows="#{requestScope.selectedRows}", action="start-instance");
            gf.redirect(page="#{pageSession.listLink}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
        />
    </sun:button>
</define>

<!define name="extraButton3">
    <sun:button id="button3" text="$resource{i18n.button.Stop}" disabled="#{true}" primary="#{false}"
            onClick="if ( getConfirm(this,'$resource{i18ncs.msg.JS.confirmStopInstances}') )
                    { return submitAndDisable(this, '$resource{i18n.button.Processing}');}  else {return false;} " >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component="#{requestScope.tableRowGroup}");
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows="#{requestScope.selectedRows}");
            gf.instanceAction(rows="#{requestScope.selectedRows}", action="stop-instance");
            gf.redirect(page="#{pageSession.listLink}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
         />
    </sun:button>
</define>

<!define name="extraDropDown">
    <sun:dropDown id="dropdown1" rendered="#{pageSession.clusterInstancesPage}" submitForm="#{true}"  disabled="#{false}" toolTip="$resource{i18n.common.moreActionDropDown}"
            labels={ "$resource{i18n.common.moreActionDropDown}"
                    "$resource{i18n.button.Enable}"  "$resource{i18n.button.Disable}"
                    "$resource{i18n.targetListing.table.lbEnable}"  "$resource{i18n.targetListing.table.lbDisable}"
                 }
            values={"" "enable" "disable" "lb-enable" "lb-disable" } >
            <!command
                getUIComponent(clientId="#{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                setAttribute(key="click" value="$this{component}");
                setAttribute(key="actionValue" value="#{click.selected}")

                if( ${actionValue}=enable){
                    gf.changeClusterStatus(selectedRows="${selectedRows}"  clusterName="#{pageSession.clusterName}" Enabled="#{true}" forLB="$boolean{false}")
                }
                if( ${actionValue}=disable){
                    gf.changeClusterStatus(selectedRows="${selectedRows}"  clusterName="#{pageSession.clusterName}" Enabled="#{false}" forLB="$boolean{false}")
                }
                if( ${actionValue}=lb-enable){
                    gf.changeClusterStatus(selectedRows="${selectedRows}" clusterName="#{pageSession.clusterName}" Enabled="#{true}" forLB="$boolean{true}")
                }
                if( ${actionValue}=lb-disable){
                    gf.changeClusterStatus(selectedRows="${selectedRows}"  clusterName="#{pageSession.clusterName}" Enabled="#{false}" forLB="$boolean{true}")
                }
                gf.redirect(page="#{listLink}");
            />
    </sun:dropDown>
</define>
