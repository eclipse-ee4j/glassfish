<!--

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

<!-- jca/connectorConnectionPoolAttr.inc -->
<!-- used by connectorConnectionPool.jsf -->


<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
    <sun:propertySheetSection id="generalPropertySheet"  label="$resource{i18n.common.GeneralSettings}">
        
        <sun:property id="name"   labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.poolName}">
            <sun:staticText escape="true" id="name" text="#{pageSession.Name}"  />
        </sun:property>

        <sun:property id="resAdapterProp"   labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.common.resourceAdapter}"  >
            <sun:dropDown id="db"  selected="#{pageSession.valueMap.resourceAdapterName}" submitForm="#{true}" immediate="#{true}"
                labels="$pageSession{modList}"  values="$pageSession{modList}" required="#{true}" >
                <!command
                    //getUIComponent(clientId="#{jndiNameId}", component=>$attribute{nameComponent});
                    //getUIComponentProperty(component="$attribute{nameComponent}", name="submittedValue", value=>$attribute{poolName});
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="name" value="#{pageSession.Name}")
                    setAttribute(key="name" value="#{pageSession.Name}")
                    setAttribute(key="name" value="#{pageSession.Name}")
                    setAttribute(key="name" value="#{pageSession.Name}")
                    setAttribute(key="resAdapterChoosen" value="#{click.selected}")
                    gf.navigate(page="#{request.contextPath}/jca/connectorConnectionPoolEdit.jsf");
                />
            </sun:dropDown>
        </sun:property>

        <sun:property id="connectionDefProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorConnectionPool.connectionDefinitionName}" >
            <sun:dropDown id="connectionDef"  selected="#{pageSession.valueMap.connectionDefinitionName}"
                labels="$pageSession{cdList}"  values="$pageSession{cdList}" required="#{true}" >
            </sun:dropDown>
        </sun:property>

        <sun:property id="pingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.ping}" helpText="$resource{i18njca.jcaPool.pingHelp}">
            <sun:checkbox  selected="#{pageSession.valueMap.ping}" selectedValue="true" />
        </sun:property>
        <sun:property id="deploymentOrder" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resource.deploymentOrder}"  helpText="$resource{i18n.common.resource.deploymentOrderHelp}">
            <sun:textField id="deploymentOrder" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.common.deploymentOrder']}" text="#{pageSession.valueMap.deploymentOrder}" />
        </sun:property>
        <sun:property id="descProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.common.description}" >
            <sun:textField id="desc" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap.description}" />
        </sun:property>

        "<br /><br />
    </sun:propertySheetSection>

    <!-- ======================================== -->

    <sun:propertySheetSection id="poolSettingsPropertySheet" label="$resource{i18njca.jcaPool.poolSettings}">
        <sun:property id="initSizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.initPoolSize}" helpText="$resource{i18njca.jcaPool.initPoolSizeHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.initPoolSize']}" text="#{pageSession.valueMap.steadyPoolSize}" />
            <sun:staticText escape="true" id="con1" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>

        <sun:property id="maxProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.maxPoolSize}" helpText="$resource{i18njca.jcaPool.maxPoolSizeHelp}">
            <sun:textField id="ds2" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.maxPoolSize']}" text="#{pageSession.valueMap.maxPoolSize}" />
            <sun:staticText escape="true" id="con2" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>

        <sun:property id="resizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.poolResize}" helpText="$resource{i18njca.jcaPool.poolResizeHelp}">
            <sun:textField id="ds3" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.poolResize']}" text="#{pageSession.valueMap.poolResizeQuantity}" />
            <sun:staticText escape="true" id="con3" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>

        <sun:property id="idleProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.idleTimeout}" helpText="$resource{i18njca.jcaPool.idleTimeoutHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.idleTimeout']}" text="#{pageSession.valueMap.idleTimeoutInSeconds}" />
            <sun:staticText escape="true" id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
        </sun:property>

        <sun:property id="maxWaitProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.maxWait}" helpText="$resource{i18njca.jcaPool.maxWaitHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.maxWait']}" text="#{pageSession.valueMap.maxWaitTimeInMillis}" />
            <sun:staticText escape="true" id="mil" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
        </sun:property>
    </sun:propertySheetSection>

    <!-- ======================================== -->

    <sun:propertySheetSection id="poolPropertySheet" label="$resource{i18njca.jcaPool.Validation}">
        <sun:property id="conValidationProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.conValidation}" helpText="$resource{i18njca.connectorConnectionPool.conValidationHelp}">
            <sun:checkbox label="$resource{i18n.common.Required}" selected="#{pageSession.valueMap.isConnectionValidationRequired}" selectedValue="true" />
        </sun:property>

        <sun:property id="failProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorConnectionPool.onFail}" helpText="$resource{i18njca.connectorConnectionPool.onFailHelp}">
            <sun:checkbox label="$resource{i18njca.connectorConnectionPool.closeAll}" selected="#{pageSession.valueMap.failAllConnections}" selectedValue="true" />
        </sun:property>

        <sun:property>
            "<br /><br />
        </sun:property>

        <sun:property id="transprop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorConnectionPool.transSupport}" helpText="$resource{i18njca.connectorConnectionPool.transSupportHelp}" >
            <sun:dropDown id="trans" selected="#{pageSession.valueMap.transactionSupport}" labels={"" "XATransaction"  "LocalTransaction"  "NoTransaction"}/>
        </sun:property>

        <sun:property>
            "<br /><br />
        </sun:property>
    </sun:propertySheetSection>
</sun:propertySheet>  
