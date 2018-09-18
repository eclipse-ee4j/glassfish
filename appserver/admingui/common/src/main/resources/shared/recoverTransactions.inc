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

<sun:form id="propertyForm">
#include "/common/shared/alertMsg.inc"
    <sun:title id="propertyContentPage" title="$resource{i18nc.recoverTransactionsTitle}"   helpText="$resource{i18nc.recoverTransactionsTitleHelp}">
        <!facet pageButtonsTop>
            <sun:panelGroup id="topButtons">
                <sun:button id="saveButton" text="$resource{i18n.button.OK}"
                     onClick=" if (checkTxDir('#{pageSession.targetId}', '#{pageSession.instanceName}', '#{pageSession.logDirId}' , '$resource{i18nc.msg.JS.recoverTransactions.enterLogDir}'))
                                {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;"  >
                    <!command
                        createMap(result="#{requestScope.attrs}");
                        mapPut(map="#{requestScope.attrs}", key="server_name", value="#{pageSession.instanceName}");
                        if("!#{useSelf}"){
                            mapPut(map="#{requestScope.attrs}", key="transactionlogdir", value="#{pageSession.txnLogDir}");
                            mapPut(map="#{requestScope.attrs}", key="target", value="#{pageSession.target}");
                        }
                        gf.restRequest(endpoint="#{pageSession.resourceUrl}/recover-transactions", attrs="#{requestScope.attrs}", method="post", result="#{requestScope.results}");
                        prepareAlertMsg(type="success", summary="$resource{i18nc.msg.recoverTransactionsSuccessful}");
                        gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                    />
                </sun:button>
                <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Cancel}" >
                    <!command
                        gf.redirect(page="#{parentPage}");
                    />
                </sun:button>
            </sun:panelGroup>
        </facet>
    </sun:title>


    <sun:propertySheet id="propertySheet">

        <sun:propertySheetSection id="propertSectionTextField" >
            <sun:property id="selfRecovery" rendered="#{useSelf}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.recoverTransactions.selfRecover}" >
                <sun:staticText id="descTarget" text="#{pageSession.instanceName}" />
            </sun:property>

            <sun:property id="sourceName" rendered="#{!useSelf}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.recoverTransactions.instanceName}" >
                <sun:staticText id="sourceName" text="#{pageSession.instanceName}" />
            </sun:property>

            <sun:property id="instanceDestProp" rendered="#{!useSelf}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.destServer}"
                helpText="$resource{i18nc.destServerHelp}">
                <sun:dropDown id="target" selected="#{pageSession.target}" required="#{true}" labels="#{pageSession.runningInstances}"
                      onChange="javascript:showTxnLogDir(this.value, '#{pageSession.instanceName}', '#{pageSession.logDirId}');"
                 >
                <!afterCreate
                    getClientId(component="$this{component}" clientId="#{pageSession.targetId}");
                    setPageSessionAttribute(key='target', value="#{pageSession.runningInstances.get(0)}");
                />
                </sun:dropDown>
            </sun:property>
            <sun:property id="txnLogProp" rendered="#{!useSelf}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.txnLogDir}"
                    helpText="$resource{i18nc.txnLogDirHelp}">
                <sun:textField id="txnLogDir" text="#{pageSession.txnLogDir}" required="#{true}" styleClass="required"  columns="$int{60}"  maxLength="#{sessionScope.fieldLengths['maxLength.recoverTransaction.transactionlogdir']}" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId="#{pageSession.logDirId}");
                    />
                </sun:textField>
            </sun:property>
        </sun:propertySheetSection>
    </sun:propertySheet>

 <sun:hidden id="helpKey" value="#{pageSession.helpKey}" />

</sun:form>
<f:verbatim>
    <script type="text/javascript">
    function checkTxDir(targetId, instanceName, logDirId, errorMsg){
        var el = document.getElementById(targetId);
        if (el == null) {
            return true;
        }

        var targetName = el.value;
        var logDir = document.getElementById(logDirId);
        var logDirValue = logDir.value;
        if(targetName == instanceName){
            disableComponent(logDirId, 'text');
            return true;     //logDir is not required.
        }else{
            if (logDirValue==null || logDirValue==''){
                logDir.select();
                logDir.focus();
                alert(errorMsg);
                return false;
            }
            return true;
       }
    }

    function showTxnLogDir(targetName, instanceName, logDirId){
        if (logDirId==null || logDirId==''){
            return;
        }
        if (targetName == instanceName){
            disableComponent(logDirId, 'text');
        }else{
            enableComponent(logDirId, 'text');
        }

    }
    </script>
</f:verbatim>
