<!--

    Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.

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
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">

        <sun:property id="nameNew"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.common.name}" helpText="$resource{i18n_web.common.nameHelp}">
            <sun:textField id="name"  required="#{true}" columns="$int{50}" maxLength="#{sessionScope.fieldLengths['maxLength.common.Name']}" text="#{pageSession.valueMap['name']}" />
        </sun:property>

        <sun:property id="name"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.common.name}">
            <sun:staticText id="name" text="#{pageSession.Name}"  rendered="#{Edit}" />
        </sun:property>
        
        <sun:property id="port"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.port}" helpText="$resource{i18n_web.grizzly.networkListener.portHelp}">
            <sun:textField id="port" columns="$int{40}" maxLength="#{sessionScope.fieldLengths['maxLength.grizzly.networkListener.port']}" required="#{true}" styleClass="required port" text="#{pageSession.valueMap['port']}"  />
        </sun:property>

        <sun:property id="status"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.common.status}"  helpText="$resource{i18n_web.grizzly.networkListener.statusHelp}" >
            <sun:checkbox  selected="#{pageSession.valueMap['enabled']}" selectedValue="true" />
        </sun:property>

        <sun:property id="security"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.security}" helpText="#{pageSession.secHelpText}" >
            <sun:checkbox disabled="#{readOnly}"   selected="#{pageSession.protocolMap['securityEnabled']}" selectedValue="true" />
        </sun:property>

        <sun:property id="JkEnabled"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.JkEnabled}" helpText="$resource{i18n_web.grizzly.networkListener.JkEnabledHelp}">
            <sun:checkbox  selected="#{pageSession.valueMap['jkEnabled']}" selectedValue="true" />
        </sun:property>
        
        <sun:property id="addr"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.address}" helpText="$resource{i18n_web.grizzly.networkListener.addressHelp}">
            <sun:textField id="addr" columns="$int{40}" maxLength="#{sessionScope.fieldLengths['maxLength.grizzly.networkListener.address']}" text="#{pageSession.valueMap['address']}"  />
        </sun:property>

        <sun:property id="vs"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.http.defVirtualServerLabel}" helpText="$resource{i18n_web.http.defVirtualServerLabelHelp}">
            <sun:dropDown id="vs" selected="#{pageSession.httpMap['defaultVirtualServer']}" labels="$pageSession{vsList}"  values="$pageSession{vsList}" >
            <!beforeCreate
                gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/http-service/virtual-server" result="#{pageSession.vsList}");

            />
           </sun:dropDown>
        </sun:property>
        <sun:property id="threadpool"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.threadPool}" helpText="$resource{i18n_web.grizzly.networkListener.threadPoolHelp}">
            <sun:dropDown id="threadpool" selected="#{pageSession.valueMap['threadPool']}" labels="$pageSession{threadPoolList}"  values="$pageSession{threadPoolList}" >
            <!beforeCreate
                gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/thread-pools/thread-pool" result="#{pageSession.threadPoolList}");
            />
           </sun:dropDown>
        </sun:property>

        <sun:property id="ServerNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.http.serverNameLabel}" helpText="$resource{i18n_web.http.serverNameHelp}" >
            <sun:textField id="ServerName" columns="$int{50}" maxLength="#{sessionScope.fieldLengths['maxLength.http.serverName']}" text="#{pageSession.httpMap['serverName']}" />
        </sun:property>

    </sun:propertySheetSection>
</sun:propertySheet>
