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

<!-- grizzly/transportAttr.inc -->

<sun:propertySheet id="propertySheet">
#include "/common/shared/configNameSection.inc"
    <sun:propertySheetSection id="propertSectionTextField">

    <sun:property id="name"  rendered="#{edit}"   labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.transportName}">
        <sun:staticText id="name" text="#{pageSession.Name}" />
    </sun:property>

    <sun:property id="IdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.transportName}">
        <sun:textField id="IdText" styleClass="required" text="#{pageSession.valueMap['name']}" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.transportName']}" required="#{true}" />
    </sun:property>

    <sun:property id="Classname"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.Classname}"  helpText="$resource{i18n_web.transport.ClassnameHelp}">
        <sun:textField id="Classname" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.Classname']}" text="#{pageSession.valueMap['classname']}" />
    </sun:property>


    <sun:property id="SelectionKeyHandler"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.SelectionKeyHandler}"  helpText="$resource{i18n_web.transport.SelectionKeyHandlerHelp}">
        <sun:textField id="SelectionKeyHandler"  columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.SelectionKeyHandler']}" text="#{pageSession.valueMap['selectionKeyHandler']}" />
    </sun:property>

    <sun:property id="ByteBufferType"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.ByteBufferType}"  helpText="$resource{i18n_web.transport.ByteBufferTypeHelp}">
        <sun:dropDown id="ByteBufferType" selected="#{pageSession.valueMap['byteBufferType']}" labels={"HEAP", "DIRECT"}  />
    </sun:property>

    <sun:property id="BufferSizeBytes"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.BufferSizeBytes}"  helpText="$resource{i18n_web.transport.BufferSizeBytesHelp}">
        <sun:textField id="BufferSizeBytes" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.BufferSizeBytes']}" styleClass="integer" text="#{pageSession.valueMap['bufferSizeBytes']}" />
        <sun:staticText id="bytes" style="padding: 8pt" text="$resource{i18n.common.Bytes}"/>
    </sun:property>

    <sun:property id="AcceptorThreads"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.AcceptorThreads}" helpText="$resource{i18n_web.transport.AcceptorThreadsHelp}">
        <sun:textField id="AcceptorThreads" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.AcceptorThreads']}" styleClass="intAllowMinusOne" text="#{pageSession.valueMap['acceptorThreads']}" />
    </sun:property>

    <sun:property id="MaxConnectionsCount"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.MaxConnectionsCount}"  helpText="$resource{i18n_web.transport.MaxConnectionsCountHelp}">
        <sun:textField id="MaxConnectionsCount" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.MaxConnectionsCount']}" styleClass="integer" text="#{pageSession.valueMap['maxConnectionsCount']}" />
    </sun:property>

    <sun:property id="IdleKeyTimeoutSeconds"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.IdleKeyTimeoutSeconds}"  helpText="$resource{i18n_web.transport.IdleKeyTimeoutSecondsHelp}">
        <sun:textField id="IdleKeyTimeoutSeconds" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.IdleKeyTimeoutSeconds']}" styleClass="integer" text="#{pageSession.valueMap['idleKeyTimeoutSeconds']}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
    </sun:property>

    <sun:property id="ReadTimeoutMillis"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.ReadTimeoutMillis}" helpText="$resource{i18n_web.transport.ReadTimeoutMillisHelp}">
        <sun:textField id="ReadTimeoutMillis" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.ReadTimeoutMillis']}" styleClass="integer" text="#{pageSession.valueMap['readTimeoutMillis']}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
    </sun:property>

    <sun:property id="SelectorPollTimeoutMillis"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.SelectorPollTimeoutMillis}" helpText="$resource{i18n_web.transport.SelectorPollTimeoutMillisHelp}">
        <sun:textField id="SelectorPollTimeoutMillis" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.SelectorPollTimeoutMillis']}" styleClass="integer" text="#{pageSession.valueMap['selectorPollTimeoutMillis']}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
    </sun:property>


    <sun:property id="WriteTimeoutMillis"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.WriteTimeoutMillis}"  helpText="$resource{i18n_web.transport.WriteTimeoutMillisHelp}">
        <sun:textField id="WriteTimeoutMillis" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.transport.WriteTimeoutMillis']}" styleClass="integer" text="#{pageSession.valueMap['writeTimeoutMillis']}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
    </sun:property>

    <sun:property id="DisplayConfiguration"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.DisplayConfiguration}"  helpText="$resource{i18n_web.transport.DisplayConfigurationHelp}" >
        <sun:checkbox  selected="#{pageSession.valueMap['displayConfiguration']}" selectedValue="true" />
    </sun:property>

    <sun:property id="EnableSnoop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.EnableSnoop}"  helpText="$resource{i18n_web.transport.EnableSnoopHelp}" >
        <sun:checkbox  selected="#{pageSession.valueMap['enableSnoop']}" selectedValue="true" />
    </sun:property>

     <sun:property id="TcpNoDelay"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.transport.TcpNoDelay}" helpText="$resource{i18n_web.transport.TcpNoDelayHelp}">
        <sun:checkbox  selected="#{pageSession.valueMap['tcpNoDelay']}" selectedValue="true" />
    </sun:property>

    </sun:propertySheetSection>
</sun:propertySheet>
	
