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

<!-- configuration/providerConfig.inc -->    
<!-- Provider Configuration section -->               
<sun:propertySheetSection id="providerConfSection" label="#{pageSession.providerSectionLabel}" >

    <sun:property id="AuthLayerProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecurity.authLayerLabel}" >
        <sun:staticText id="AuthLayer" text="#{pageSession.msgSecurityName}" />
    </sun:property>

    <sun:property id="ProviderIdProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecProvider.ProviderIdLabel}" >
        <sun:staticText id="ProviderId" text="#{pageSession.valueMap['Name']}" />
    </sun:property>
    <sun:property id="ProviderIdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecProvider.ProviderIdLabel}">
        <sun:textField id="ProviderIdText" styleClass="required" text="#{pageSession.valueMap['Name']}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.msgSecProvider.ProviderId']}" required="#{true}" />
        <sun:staticText text=""/>
    </sun:property>

    <sun:property >
        <sun:staticText text=" " />
    </sun:property>
    <!--  the defaultProvider attr is not in the <provider-config> element, User should set default provider
          in the msg security page itself during edit.
          During creation, the create command takes this option, so we can pass that in.
    -->
    <sun:property id="DefaultProviderProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecProvider.defaultProviderLabel}"
                   helpText="$resource{i18nc.msgSecProvider.defaultProviderLabeHelp}">
        <sun:checkbox id="def" selected="#{pageSession.valueMap['defaultProvider']}" selectedValue="true" />
    </sun:property>

    <sun:property id="ProviderTypeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecProvider.providerTypeLabel}" helpText="$resource{i18nc.msgSecProvider.providerTypeHelp}">
        <sun:dropDown id="ProviderType" selected="#{pageSession.valueMap['ProviderType']}" labels={"client", "server", "client-server"}  required="#{true}" />
    </sun:property>                          
    
    <sun:property id="ClassNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecProvider.classNameLabel}" helpText="$resource{i18nc.msgSecProvider.classNameHelp}">
        <sun:textField id="ClassName" styleClass="required" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.msgSecProvider.className']}" text="#{pageSession.valueMap['ClassName']}" required="#{true}" />
    </sun:property>

</sun:propertySheetSection>                            


