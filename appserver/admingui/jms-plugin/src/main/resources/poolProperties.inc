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

<sun:propertySheet id="propertySheet">
    <sun:propertySheetSection id="generalPropertySheet" label="$resource{i18n.common.GeneralSettings}"   >
        <sun:property id="jndiProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}"  rendered="#{!edit}">
            <sun:textField id="jndiProp" styleClass="required" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.poolName']}" text="#{pageSession.valueMap['name']}" required="#{true}" rendered="#{!edit}" >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{resCompId});
                />
            </sun:textField>
        </sun:property>
                
        <sun:property id="jndiProp2"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}" rendered="#{edit}">
            <sun:staticText text="#{pageSession.Name}"  rendered="#{edit}"/>
        </sun:property>
                
                
        <sun:property id="logic"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.logicalJndiName}"  rendered="#{edit and renderLogic}">
            <sun:staticText  id="logic" text="#{pageSession.logicalJndiName}" />
        </sun:property>
                
                
        <sun:property id="resTyped"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resType}">
            <sun:dropDown id="resType" selected="#{pageSession.valueMap['connectionDefinitionName']}" rendered="#{!edit}"
                labels={"javax.jms.TopicConnectionFactory","javax.jms.QueueConnectionFactory","javax.jms.ConnectionFactory"} />
            <sun:staticText text="#{pageSession.valueMap['connectionDefinitionName']}" rendered="#{edit}"/>
        </sun:property>
                
                
        <sun:property id="descProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
            <sun:textField id="descProp" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['description']}" />
        </sun:property>
        <sun:property id="statusProp" rendered="#{useCheckBox}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox  selected="#{pageSession.resourceRef['enabled']}" selectedValue="true"/>
        </sun:property>
        <sun:property id="statusProp2" rendered="#{useString}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}"
            label="$resource{i18n.common.status}" helpText="$resource{i18n.application.EnableTargetHelp}">
            <sun:staticText  id="enabledStr" text="#{requestScope.enabledString}" >
                <!beforeCreate
                    gf.getTargetEnableInfo(appName="#{pageSession.encodedName}" isApp="#{false}" status="#{requestScope.enabledString}");
                />
           </sun:staticText>
       </sun:property>

        "<br /><br />
    </sun:propertySheetSection>
    <sun:propertySheetSection id="poolPropertySheet" label="$resource{i18njms.connectionFactory.poolSettings}">
        <sun:property id="initSizeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.connectionFactory.initPoolSize}" helpText="$resource{i18njms.connectionFactory.initPoolSizeHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.connectionFactory.initPoolSize']}" text="#{pageSession.valueMap['steadyPoolSize']}" />
            <sun:staticText id="con1" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>
        <sun:property id="maxProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.connectionFactory.maxPoolSize}" helpText="$resource{i18njms.connectionFactory.maxPoolSizeHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.connectionFactory.maxPoolSize']}" text="#{pageSession.valueMap['maxPoolSize']}" />
            <sun:staticText id="con" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>
        <sun:property id="resizeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.connectionFactory.poolResize}" helpText="$resource{i18njms.connectionFactory.poolResizeHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.connectionFactory.poolResize']}" text="#{pageSession.valueMap['poolResizeQuantity']}" />
            <sun:staticText id="con" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>
        <sun:property id="idleProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.connectionFactory.idleTimeout}" helpText="$resource{i18njms.connectionFactory.idleTimeoutHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.connectionFactory.idleTimeout']}" text="#{pageSession.valueMap['idleTimeoutInSeconds']}" />
            <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
        </sun:property>
        <sun:property id="maxWaitProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.connectionFactory.maxWait}" helpText="$resource{i18njms.connectionFactory.maxWaitHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.connectionFactory.maxWait']}" text="#{pageSession.valueMap['maxWaitTimeInMillis']}" />
            <sun:staticText id="mil" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
        </sun:property>


        <sun:property id="failProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.connectionFactory.onFail}" helpText="$resource{i18njms.connectionFactory.onFailHelp}">
            <sun:checkbox label="$resource{i18njms.connectionFactory.closeAll}" selected="#{pageSession.valueMap['failAllConnections']}" selectedValue="true"/>
        </sun:property>
        <sun:property id="transprop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.connectionFactory.transSupport}" helpText="$resource{i18njms.connectionFactory.transSupportHelp}" >
            <sun:dropDown id="trans" selected="#{pageSession.valueMap['transactionSupport']}" labels={"", "XATransaction", "LocalTransaction", "NoTransaction"} />
        </sun:property>
        <sun:property id="conValidationProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.connectionFactory.conValidation}" helpText="$resource{i18njms.connectionFactory.conValidationHelp}">
            <sun:checkbox label="$resource{i18n.common.Required}" selected="#{pageSession.valueMap['isConnectionValidationRequired']}" selectedValue="true" />
        </sun:property>

    </sun:propertySheetSection>
</sun:propertySheet>
