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

<!-- jca/connectorConnectionPoolAdvance.jsf -->
<!-- used by connectorConnectionPool.jsf -->
   

<!-- PropertySheet .... -->

<sun:propertySheetSection id="connectionPropertySheet" label="$resource{i18njca.jcaPool.connectionSettings}">

    <sun:property id="name"   labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.poolName}">
            <sun:staticText escape="true" id="name" text="#{pageSession.Name}"  />
        </sun:property>

    <sun:property id="p1"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.validateAtmostOnceInSeconds}" helpText="$resource{i18njca.jcaPool.validateAtmostOnceInSecondsHelp}">
        <sun:textField id="va" styleClass="integer" columns="$int{30}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.validateAtmostOnceInSeconds']}" text="#{pageSession.valueMap['validateAtmostOncePeriodInSeconds']}" /> 
        <sun:staticText id="vasec" text="$resource{i18n.common.Seconds}"/>
    </sun:property>

    <sun:property id="p2"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.connectionLeakTimeoutInSeconds}" helpText="$resource{i18njca.jcaPool.connectionLeakTimeoutInSecondsHelp}">
        <sun:textField id="p2" styleClass="integer" columns="$int{30}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.connectionLeakTimeoutInSeconds']}" text="#{pageSession.valueMap['connectionLeakTimeoutInSeconds']}"/> 
        <sun:staticText id="vasec" text="$resource{i18n.common.Seconds}"/>
   </sun:property>   

    <sun:property id="p3"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.connectionLeakReclaim}" helpText="$resource{i18njca.jcaPool.connectionLeakReclaimHelp}">
        <sun:checkbox  selected="#{pageSession.valueMap['connectionLeakReclaim']}" label=" " selectedValue="true"/>
    </sun:property> 

    <sun:property id="p4"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.connectionCreationRetryAttempts}" helpText="$resource{i18njca.jcaPool.connectionCreationRetryAttemptsHelp}">
        <sun:textField id="p4" styleClass="integer" columns="$int{30}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.connectionCreationRetryAttempts']}" text="#{pageSession.valueMap['connectionCreationRetryAttempts']}"/> 
   </sun:property>   

    <sun:property id="p5"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.connectionCreationRetryIntervalInSeconds}" helpText="$resource{i18njca.jcaPool.connectionCreationRetryIntervalInSecondsHelp}">
        <sun:textField id="p5" styleClass="integer" columns="$int{30}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.connectionCreationRetryIntervalInSeconds']}" text="#{pageSession.valueMap['connectionCreationRetryIntervalInSeconds']}"/> 
        <sun:staticText id="p51" text="$resource{i18n.common.Seconds}"/>
   </sun:property>

   <sun:property id="poolingProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.pooling}" helpText="$resource{i18njca.jcaPool.poolingHelp}">
            <sun:checkbox  selected="#{pageSession.valueMap['pooling']}" selectedValue="true" />
       </sun:property>

    <sun:property id="p7"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.lazyConnectionAssociation}" helpText="$resource{i18njca.jcaPool.lazyConnectionAssociationHelp}">
        <sun:checkbox id="associate"  selected="#{pageSession.valueMap['lazyConnectionAssociation']}"  onClick="enableDisableLazyConnection('#{associateId}');" selectedValue="true" >  
           <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{associateId});
            />
        </sun:checkbox>
   </sun:property>

    <sun:property id="p6"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.lazyConnectionEnlistment}" helpText="$resource{i18njca.jcaPool.lazyConnectionEnlistmentHelp}">
        <sun:checkbox id="enlist"  selected="#{pageSession.valueMap['lazyConnectionEnlistment']}"   selectedValue="true">  
            <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{enlistId});
            />
        </sun:checkbox>
    </sun:property>

    <sun:property id="p8"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.associationWithThread}" helpText="$resource{i18njca.jcaPool.associationWithThreadHelp}">
        <sun:checkbox  selected="#{pageSession.valueMap['associateWithThread']}"  selectedValue="true"/>  
   </sun:property>

    <sun:property id="p9"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.matchConnections}" helpText="$resource{i18njca.jcaPool.matchConnectionsHelp}">
        <sun:checkbox  selected="#{pageSession.valueMap['matchConnections']}"  selectedValue="true"/>  
   </sun:property>

    <sun:property id="p10"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.maxConnectionUsageCount}" helpText="$resource{i18njca.jcaPool.maxConnectionUsageCountHelp}">
        <sun:textField id="p5" styleClass="integer" columns="$int{30}" maxLength="#{sessionScope.fieldLengths['maxLength.jcaPool.maxConnectionUsageCount']}" text="#{pageSession.valueMap['maxConnectionUsageCount']}"/> 
   </sun:property>

</sun:propertySheetSection>
