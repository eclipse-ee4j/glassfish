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

<!-- iiopListener.inc  -->

<sun:propertySheet id="propertySheet">
#include "/common/shared/configNameSection.inc"
    <!-- General Settings section -->
    <sun:propertySheetSection id="generalSettingsSetion">
        <sun:property id="IiopNameProp"  rendered="#{pageSession.edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
            <sun:staticText id="IiopName" text="#{pageSession.valueMap['id']}" />
        </sun:property>
        <sun:property id="IiopNameTextProp"  rendered="#{!pageSession.edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
            <sun:textField id="IiopNameText" styleClass="required" text="#{pageSession.valueMap['id']}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.Name']}" required="#{true}" />
        </sun:property>
        <sun:property id="NetwkAddrProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_corba.iiopListener.netwkAddrLabel}" helpText="$resource{i18n_corba.iiopListener.netwkAddrHelp}" >
            <sun:textField id="NetwkAddr" styleClass="required"  columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.iiopListener.netwkAddr']}" text="#{pageSession.valueMap['address']}" required="#{true}" />
        </sun:property>
        <sun:property id="ListenerPortProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_corba.iiopListener.listPortLabel}" helpText="$resource{i18n_corba.iiopListener.listPortHelp}" >
            <sun:textField id="ListenerPort" styleClass="required port" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.iiopListener.listPort']}" text="#{pageSession.valueMap['port']}" required="#{true}" />
        </sun:property>
        <sun:property id="ListenerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_corba.iiopListener.listenerLabel}" helpText="$resource{i18n_corba.iiopListener.listenerHelp}" >
            <sun:checkbox  selected="#{pageSession.valueMap['enabled']}" selectedValue="true" />
        </sun:property>
        <sun:property id="SecurityProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_corba.iiopListener.securityEnable}" >
            <sun:checkbox id="Security"  selected="#{pageSession.valueMap['securityEnabled']}" selectedValue="true" />
        </sun:property>
    </sun:propertySheetSection>
</sun:propertySheet>    

