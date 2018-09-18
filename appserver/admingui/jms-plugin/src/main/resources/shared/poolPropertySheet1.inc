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

    <sun:property id="initSizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.initPoolSize}" helpText="$resource{i18n.jdbcPool.initPoolSizeHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{steadyPoolSize}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.initPoolSize']}" /> 
        <sun:staticText id="con1" style="padding: 8pt" text="$resource{i18n.common.Connections}"/> 
    </sun:property>

    <sun:property id="maxProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.maxPoolSize}" helpText="$resource{i18n.jdbcPool.maxPoolSizeHelp}">
        <sun:textField id="ds2" styleClass="integer" columns="$int{10}" text="#{maxPoolSize}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.maxPoolSize']}" />
        <sun:staticText id="con2" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
    </sun:property>

    <sun:property id="resizeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.poolResize}" helpText="$resource{i18n.jdbcPool.poolResizeHelp}">
        <sun:textField id="ds3" styleClass="integer" columns="$int{10}" text="#{poolResizeQuantity}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.poolResize']}" /> 
        <sun:staticText id="con3" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
    </sun:property>

    <sun:property id="idleProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.idleTimeout}" helpText="$resource{i18n.jdbcPool.idleTimeoutHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{idleTimeoutInSeconds}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.idleTimeout']}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
    </sun:property>

    <sun:property id="maxWaitProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.maxWait}" helpText="$resource{i18n.jdbcPool.maxWaitHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{maxWaitTimeInMillis}" maxLength="#{sessionScope.fieldLengths['maxLength.jdbcPool.maxWait']}" /> 
        <sun:staticText id="mil" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
    </sun:property>



