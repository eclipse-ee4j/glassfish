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

<!-- jca/connectorConnectionPoolAttr.inc -->
<!-- used by connectorConnectionPoolNew2.jsf -->


<!-- PropertySheet .... -->
//"<h1>#{wizardPoolExtra}</h1>
<sun:propertySheet id="propertySheet">
    <sun:propertySheetSection id="generalPropertySheet"  label="$resource{i18n.common.GeneralSettings}">
        <sun:property id="jndiProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.poolName}">
            <sun:staticText escape="true" id="Jndi" text="#{wizardPoolExtra.name}" />
        </sun:property>

        <sun:property id="resAdapterNewProp" rendered="#{!edit}"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.common.resourceAdapter}"  >
            <sun:staticText escape="true" id="resAdapter" text="#{wizardPoolExtra.resourceAdapterName}"  />
        </sun:property>

        <sun:property id="connectionDefNewProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorConnectionPool.connectionDefinitionName}" >
            <sun:staticText escape="true" id="connectionDef" text="#{wizardPoolExtra.connectiondefinitionname}"  />
        </sun:property>

        <sun:property id="pingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.ping}" helpText="$resource{i18njca.jcaPool.pingHelp}">
            <sun:checkbox  selected="#{sessionScope.wizardMap.ping}" selectedValue="true" />
        </sun:property>

        <sun:property id="descProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.common.description}" >
            <sun:textField id="desc" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{sessionScope.wizardMap.description}" />
        </sun:property>

        "<br /><br />
    </sun:propertySheetSection>

    <sun:propertySheetSection id="poolSettingsPropertySheet" label="$resource{i18njca.jcaPool.poolSettings}">
        <sun:property id="initSizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}"
            label="$resource{i18njca.jcaPool.initPoolSize}" helpText="$resource{i18njca.jcaPool.initPoolSizeHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.initPoolSize']}" text="#{sessionScope.wizardMap.steadyPoolSize}" />
            <sun:staticText escape="true" id="con1" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>

        <sun:property id="maxProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.maxPoolSize}" helpText="$resource{i18njca.jcaPool.maxPoolSizeHelp}">
            <sun:textField id="ds2" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.maxPoolSize']}" text="#{sessionScope.wizardMap.maxPoolSize}" />
            <sun:staticText escape="true" id="con2" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>

        <sun:property id="resizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.poolResize}" helpText="$resource{i18njca.jcaPool.poolResizeHelp}">
            <sun:textField id="ds3" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.poolResize']}" text="#{sessionScope.wizardMap.poolResizeQuantity}" />
            <sun:staticText escape="true" id="con3" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>

        <sun:property id="idleProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.idleTimeout}" helpText="$resource{i18njca.jcaPool.idleTimeoutHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.idleTimeout']}" text="#{sessionScope.wizardMap.idleTimeoutInSeconds}" />
            <sun:staticText escape="true" id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
        </sun:property>

        <sun:property id="maxWaitProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.maxWait}" helpText="$resource{i18njca.jcaPool.maxWaitHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.maxWait']}" text="#{sessionScope.wizardMap.maxWaitTimeInMillis}" />
            <sun:staticText escape="true" id="mil" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
        </sun:property>
    </sun:propertySheetSection>

    <sun:propertySheetSection id="poolPropertySheet" label="$resource{i18njca.jcaPool.Validation}">
        <sun:property id="conValidationProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.conValidation}" helpText="$resource{i18njca.connectorConnectionPool.conValidationHelp}">
            <sun:checkbox label="$resource{i18n.common.Required}" selected="#{sessionScope.wizardMap.isConnectionValidationRequired}" selectedValue="true" />
        </sun:property>

        <sun:property id="failProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorConnectionPool.onFail}" helpText="$resource{i18njca.connectorConnectionPool.onFailHelp}">
            <sun:checkbox label="$resource{i18njca.connectorConnectionPool.closeAll}" selected="#{sessionScope.wizardMap.failAllConnections}" selectedValue="true" />
        </sun:property>

        <sun:property>
            "<br /><br />
        </sun:property>

        <sun:property id="transprop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorConnectionPool.transSupport}" helpText="$resource{i18njca.connectorConnectionPool.transSupportHelp}" >
            <sun:dropDown id="trans" selected="#{sessionScope.wizardMap.transactionSupport}" labels={"" "XATransaction"  "LocalTransaction"  "NoTransaction"}/>
        </sun:property>

        <sun:property>
            "<br /><br />
        </sun:property>
    </sun:propertySheetSection>
</sun:propertySheet>  
