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
#include "/common/shared/configNameSection.inc"
		 
        <sun:propertySheetSection id="propertSectionTextField">
            <sun:property id="IdStaticProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
                <sun:staticText id="IdStatic" text="#{pageSession.valueMap['name']}"/>
            </sun:property> 
            <sun:property id="IdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" helpText="$resource{i18nc.jacc.NameHelp}">
                <sun:textField id="IdText" text="#{pageSession.valueMap['name']}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.Name']}" styleClass="required" required="#{true}" />
            </sun:property> 
            <sun:property id="policyConfigProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jacc.PolicyConfig}" helpText="$resource{i18nc.jacc.PolicyConfigHelp}">
                <sun:textField id="PolicyConfig" columns="$int{75}" maxLength="#{sessionScope.fieldLengths['maxLength.jacc.PolicyConfig']}" text="#{pageSession.valueMap['policyConfigurationFactoryProvider']}" styleClass="required" required="#{true}"/>
            </sun:property>      
            <sun:property id="policyProviderProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jacc.PolicyProvider}" helpText="$resource{i18nc.jacc.PolicyProviderHelp}">
                <sun:textField id="PolicyProvider" columns="$int{75}" maxLength="#{sessionScope.fieldLengths['maxLength.jacc.PolicyProvider']}" text="#{pageSession.valueMap['policyProvider']}" styleClass="required" required="#{true}"/>
            </sun:property>      
        </sun:propertySheetSection>
</sun:propertySheet>    

