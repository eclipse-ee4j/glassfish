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

<!-- jdbc/poolPropertyEdit.inc -->

<!-- PropertySheet .... -->
<sun:propertySheet id="sheet" requiredFields="true">
<sun:propertySheetSection id="generalSheet" label="$resource{i18njdbc.jdbcPool.general}">
    <sun:property id="name"   labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.poolName}">
        <sun:staticText id="name" text="#{pageSession.Name}"/>
    </sun:property>

    <sun:property id="resTypeEditProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resType}" helpText="$resource{i18njdbc.jdbcPool.resTypeHelp}" >
        <sun:dropDown id="resTypeVal" selected="#{pageSession.valueMap.resType}"
            labels={"" "javax.sql.DataSource"  "javax.sql.XADataSource"  "javax.sql.ConnectionPoolDataSource" "java.sql.Driver"}
            onChange="javascript: disableFields();" >
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{resTypeVal});
            />
        </sun:dropDown>
    </sun:property>

    <sun:property id="dSProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.datasource}" helpText="$resource{i18njdbc.jdbcPool.datasourceHelp}">
        <sun:textField id="dsTextField" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.datasource']}" text="#{pageSession.valueMap.datasourceClassname}"  >
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{dsTextField});
        />
        </sun:textField>
    </sun:property>

    <sun:property id="dDSProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.driver}" helpText="$resource{i18njdbc.jdbcPool.driverHelp}">
        <sun:textField id="ddsTextField"  columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.driver']}" text="#{pageSession.valueMap.driverClassname}"  >
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{ddsTextField});
        />
        </sun:textField>
    </sun:property>
    <sun:property id="pingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.ping}" helpText="$resource{i18njdbc.jdbcPool.pingHelp}">
        <sun:checkbox  selected="#{pageSession.valueMap.ping}" selectedValue="true" />
   </sun:property>
   <sun:property id="deploymentOrder" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resource.deploymentOrder}"  helpText="$resource{i18n.common.resource.deploymentOrderHelp}" >
       <sun:textField id="deploymentOrder" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.common.deploymentOrder']}" text="#{pageSession.valueMap['deploymentOrder']}" />
   </sun:property>
    <sun:property id="descProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.common.description}" >
        <sun:textField id="desc" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap.description}" />
    </sun:property>
    "<br /><br />
</sun:propertySheetSection>

			<!-- -------------------- Pool Settings ------------ -->
<sun:propertySheetSection id="poolSettingsPropertySheet" label="$resource{i18njdbc.jdbcPool.poolSettings}">
    <sun:property id="initSizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.initPoolSize}" helpText="$resource{i18njdbc.jdbcPool.initPoolSizeHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.initPoolSize']}" text="#{pageSession.valueMap.steadyPoolSize}" />
        <sun:staticText id="con1" style="padding: 8pt" text="$resource{i18n.common.Connections}"/> 
    </sun:property>

    <sun:property id="maxProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.maxPoolSize}" helpText="$resource{i18njdbc.jdbcPool.maxPoolSizeHelp}">
        <sun:textField id="ds2" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.maxPoolSize']}" text="#{pageSession.valueMap.maxPoolSize}" />
        <sun:staticText id="con2" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
    </sun:property>

    <sun:property id="resizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.poolResize}" helpText="$resource{i18njdbc.jdbcPool.poolResizeHelp}">
        <sun:textField id="ds3" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.poolResize']}" text="#{pageSession.valueMap.poolResizeQuantity}" />
        <sun:staticText id="con3" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
    </sun:property>

    <sun:property id="idleProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.idleTimeout}" helpText="$resource{i18njdbc.jdbcPool.idleTimeoutHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.idleTimeout']}" text="#{pageSession.valueMap.idleTimeoutInSeconds}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
    </sun:property>

    <sun:property id="maxWaitProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.maxWait}" helpText="$resource{i18njdbc.jdbcPool.maxWaitHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.maxWait']}"  text="#{pageSession.valueMap.maxWaitTimeInMillis}" />
        <sun:staticText id="mil" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
    </sun:property>

    "<br /><br />
</sun:propertySheetSection>

			
    <!---------------------- Transaction Isolation ------------ -->

<sun:propertySheetSection id="transactionPropertySheet" label="$resource{i18njdbc.jdbcPool.transIsolationSection}">

    <sun:property id="nonTransProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.nonTransConnection}" helpText="$resource{i18njdbc.jdbcPool.nonTransConnectionHelp}">
        <sun:checkbox  selected="#{valueMap.nonTransactionalConnections}" selectedValue="true" />
   </sun:property>

    <sun:property id="transProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.transIsolation}" helpText="$resource{i18njdbc.jdbcPool.transIsolationHelp}" >
        <sun:dropDown id="transIso" selected="#{valueMap.transactionIsolationLevel}"
            labels={"" "read-uncommitted" "read-committed" "repeatable-read" "serializable" }
            
            />
    </sun:property>

    <sun:property id="levelProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.isoLevel}" helpText="$resource{i18njdbc.jdbcPool.isoLevelHelp}">
        <sun:checkbox label="$resource{i18njdbc.jdbcPool.Guaranteed}" selected="#{valueMap.isIsolationLevelGuaranteed}" selectedValue="true" />
   </sun:property>
   "<br /><br />
</sun:propertySheetSection>
</sun:propertySheet>
