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

        <!insert name="listTableDeleteButton">
        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
            onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
            { return submitAndDisable(this, '$resource{i18n.button.Processing}');}
            else {return false;} "
        >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});

            if (#{pageSession['rest-api']}){
                urlencode(value="#{pageSession.valueMap['target']}" encoding="UTF-8" result="#{encodedTargetName}");
                gf.deleteConfigCascade(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}", target="#{encodedTargetName}", selectedRows="${selectedRows}");
            }
            //REST-TODO: need to ensure page that has additionalDeleteHandler to change to use rest-api
            if (#{pageSession.additionalDeleteHandler}){
                gf.invokeHandler(handler="#{pageSession.additionalDeleteHandler}", args="#{pageSession.additionalDeleteHandlerArgs}" );
            }

            gf.redirect(page="#{pageSession.listLink}");
        />

        </sun:button>
        </insert>

        <!insert name="extraButton2">
        </insert>

        <!insert name="extraButton3">
        </insert>


     </sun:panelGroup>
 </facet>
