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

<!-- shared/instanceActionButtons.inc -->

<!facet actionsTop>
    <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{topActionGroup});
        />

        <sun:button id="newButton" text="$resource{i18n.button.New}" >
            <!command redirect(page="#{newPageLink}"); />
        </sun:button>

        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
            onClick="if (getConfirm(this, '$resource{i18n.msg.JS.confirmDeleteInstance}') )
                { return submitAndDisable(this, '$resource{i18n.button.Processing}');} 
                else {return false;} " >
            <!command 
            getUIComponent(clientId="$pageSession{instancesTableRowGroupId}", component=>$attribute{tableRowGroup});      
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            if ($pageSession{clusterList}){
                deleteSelectedInstances(selectedRows="${selectedRows}");
                redirect(page="clusterInstances.jsf?clusterName=#{clusterName}");
            }
            if (!$pageSession{clusterList}){
                deleteStandaloneInstance(selectedRows="${selectedRows}");
                redirect(page="standaloneInstances.jsf");
            }
            />            
        </sun:button>
       
        <sun:button id="button2" text="$resource{i18n.button.Start}" disabled="#{true}" primary="#{false}" 
            onClick="if (getConfirm(this, '$resource{i18n.msg.JS.confirmStartInstances}') )
                { return submitAndDisable(this, '$resource{i18n.button.Processing}');} 
                else {return false;} " >
            
            <!command 
            getUIComponent(clientId="$pageSession{instancesTableRowGroupId}", component=>$attribute{tableRowGroup});      
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            if ($pageSession{clusterList}){
                clusterInstanceAction(clusterName="#{clusterName}", start="$boolean{true}", selectedRows="${selectedRows}");
                setAttribute(key="selectedRows" value="${selectedRows}");
                redirect(page="clusterInstances.jsf?clusterName=#{clusterName}");
            }
            if (!$pageSession{clusterList}){
                startSelectedInstance(selectedRows="${selectedRows}");
                setAttribute(key="selectedRows" value="${selectedRows}");
                redirect(page="standaloneInstances.jsf");
            }
            />            
        </sun:button>
        <sun:button id="button3" text="$resource{i18n.button.Stop}" disabled="#{true}" primary="#{false}" 
            onClick="if (getConfirm(this, '$resource{i18n.msg.JS.confirmStopInstances}') )
                { return submitAndDisable(this, '$resource{i18n.button.Processing}');} 
                else {return false;} " >
            <!command 
            getUIComponent(clientId="$pageSession{instancesTableRowGroupId}", component=>$attribute{tableRowGroup});      
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            
            if ($pageSession{clusterList}){
                clusterInstanceAction(clusterName="#{clusterName}", start="$boolean{false}", selectedRows="${selectedRows}");
                setAttribute(key="selectedRows" value="${selectedRows}");
                redirect(page="clusterInstances.jsf?clusterName=#{clusterName}");
            }
            if (!$pageSession{clusterList}){
                stopSelectedInstance(selectedRows="${selectedRows}");
                setAttribute(key="selectedRows" value="${selectedRows}");
                redirect(page="standaloneInstances.jsf");
            }
            />            
        </sun:button>

        <!--
            <sun:dropDown rendered="false" id="dropdown1" submitForm="#{true}"  disabled="#{true}"
                labels="$attribute{actionLabels}" values="$attribute{actionValues}" >
                <!beforeCreate
                    setAttribute(key="actionLabels" value={ "$resource{i18n.common.LBActionDropDown}"
                        "$resource{i18n.cluster.startLB}"  "$resource{i18n.cluster.quiesce}" });
                    setAttribute(key="actionValues" value={"" "lb-enable" "lb-disable" });
                />
                <!command
                    getUIComponent(clientId="$pageSession{instancesTableRowGroupId}", component=>$attribute{tableRowGroup});   
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="actionValue" value="#{click.selected}")
                    if( ${actionValue}=lb-enable){
                        changeServerRefLB(clusterName="#{clusterName}" selectedRows="${selectedRows}" enabled="$boolean{true}" );
                    }
                    if( ${actionValue}=lb-disable){
                        changeServerRefLB(clusterName="#{clusterName}" selectedRows="${selectedRows}" enabled="$boolean{false}");
                    }
                    setAttribute(key="selectedRows" value="${selectedRows}");
                    redirect(page="clusterInstances.jsf?clusterName=#{clusterName}");
                />
            </sun:dropDown>
            -->

    </sun:panelGroup>
</facet>
