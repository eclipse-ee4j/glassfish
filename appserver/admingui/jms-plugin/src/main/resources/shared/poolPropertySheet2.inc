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

    <sun:property id="failProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorConnectionPool.onFail}" helpText="$resource{i18n.connectorConnectionPool.onFailHelp}">
        <sun:checkbox label="$resource{i18n.connectorConnectionPool.closeAll}" selected="#{failAllConnections}" />  
    </sun:property>   

    <sun:property id="transprop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorConnectionPool.transSupport}" helpText="$resource{i18n.connectorConnectionPool.transSupportHelp}" >
        <sun:dropDown id="trans" selected="#{transactionSupport}" labels={"" "XATransaction"  "LocalTransaction"  "NoTransaction"} />
    </sun:property>   

    <sun:property id="conValidationProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.conValidation}" helpText="$resource{i18n.connectorConnectionPool.conValidationHelp}">
        <sun:checkbox label="$resource{i18n.common.Required}" selected="#{isConnectionValidationRequired}" />  
    </sun:property>   



