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

<!-- shared/saveInstanceWtButton.inc -->

<!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="saveButton" text="$resource{i18n.button.Save}"
             onClick=" return submitAndDisable(this, '$resource{i18n.button.Processing}'); " >
            <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows="#{requestScope.selectedRows}");
            gf.saveInstanceWeight(rows="#{requestScope.selectedRows}" );
            setAttribute(key="selectedRows" value="${selectedRows}");
            gf.isClusterName(clusterName="#{pageSession.clusterName}" );
            
            prepareSuccessfulMsg();
            gf.redirect(page="#{pageSession.listLink}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");

            />
        </sun:button>
    </sun:panelGroup>
</facet>


