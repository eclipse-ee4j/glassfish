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
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">

    <sun:property id="IdStaticProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
        <sun:staticText id="IdStatic" text="#{pageSession.valueMap['name']}"/>
    </sun:property>

    <sun:property id="IdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
        <sun:textField id="IdText" styleClass="required" text="#{pageSession.valueMap['name']}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.Name']}" required="#{true}" />
    </sun:property>

    <sun:property id="classNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.lifecycleModule.classname}" helpText="$resource{i18nc.lifecycleModule.classnameHelp}" >
        <sun:textField id="classname" styleClass="required" columns="$int{70}" maxLength="#{sessionScope.fieldLengths['maxLength.lifecycleModule.classname']}" text="#{pageSession.valueMap['classname']}"  required="#{true}" />
    </sun:property> 

    <sun:property id="pathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}"  label="$resource{i18nc.lifecycleModule.classPath}" helpText="$resource{i18nc.lifecycleModule.classPathHelp}" >
        <sun:textField id="classPath" columns="$int{70}" maxLength="#{sessionScope.fieldLengths['maxLength.lifecycleModule.classPath']}" text="#{pageSession.valueMap['classpath']}" />
    </sun:property> 

    <sun:property id="loadOrderProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.lifecycleModule.loadOrder}"  helpText="$resource{i18nc.lifecycleModule.loadOrderHelp}" >
        <sun:textField id="loadOrder" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.lifecycleModule.loadOrder']}" text="#{pageSession.valueMap['loadorder']}" />
    </sun:property> 

    <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
        <sun:textField id="desc" columns="$int{70}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['description']}" />
    </sun:property>

    <!-- Virtual Server doesn't apply to lifecycle module. refer to issue# 13620 -->

    <sun:property id="statusNew" rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
        <sun:checkbox id="status"   selected="#{pageSession.valueMap['enabled']}" selectedValue="true" />
    </sun:property>

    <sun:property id="statusEdit"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
        <sun:checkbox id="status" rendered="#{pageSession.onlyDASExist}"  selected="#{pageSession.valueMap['enabled']}" selectedValue="true" />
        <sun:staticText id="statusText" rendered="#{!pageSession.onlyDASExist}" text="#{pageSession.status}" />
    </sun:property>

    <sun:property id="failProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.lifecycleModule.onLoadFailure}" helpText="$resource{i18nc.lifecycleModule.onLoadFailureHelp}" >
        <sun:checkbox label="$resource{i18nc.lifecycleModule.preventStart}" selected="#{pageSession.valueMap['failurefatal']}"  selectedValue="true" />
   </sun:property>

    </sun:propertySheetSection>
</sun:propertySheet>

    "<br /><br />

