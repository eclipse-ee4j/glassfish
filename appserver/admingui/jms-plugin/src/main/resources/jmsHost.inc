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

<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
    <sun:button id="def" rendered="#{edit}" primary="#{false}"  text="$resource{i18n.button.LoadDefaults}" >
        <!command
            gf.getDefaultValues(endpoint="#{pageSession.selfUrl}", valueMap="#{pageSession.valueMap}");
        />
    </sun:button>
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') && checkPSW()) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    gf.createEntity(endpoint="#{pageSession.selfUrl}" attrs="#{pageSession.valueMap}");
                    prepareSuccessfulMsg();
                    gf.redirect(page="jmsHosts.jsf?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&configName=#{configName}")
                />
            </sun:button>

            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}" 
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') && checkPSW()) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    mapPut(map="#{pageSession.valueMap}", key="target", value="#{pageSession.configName}");
                    gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}" attrs="#{pageSession.valueMap}"
                        convertToFalse="#{pageSession.convertToFalseList}");
                    prepareSuccessfulMsg();
                    gf.redirect(page="jmsHosts.jsf?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&configName=#{configName}")
                    />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
                <!command
                    gf.redirect(page="jmsHosts.jsf?configName=#{configName}");
                    />
            </sun:button>

        </sun:panelGroup>
    </facet>
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">
#include "/common/shared/configNameSection.inc"
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="JmsHostStaticProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.jmsHostNameLabel}" >
            <sun:staticText escape="true" id="JmsHostStatic" text="#{pageSession.valueMap['name']}"/>
        </sun:property>

        <sun:property id="JmsHostTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.jmsHostNameLabel}">
            <sun:textField id="JmsHostText" text="#{pageSession.valueMap['name']}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.edJmsHost.jmsHostName']}" styleClass="required" required="#{true}" />
        </sun:property>

        <sun:property id="HostProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.hostLabel}" helpText="$resource{i18njms.edJmsHost.hostHelp}" >
            <sun:textField id="Host" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.edJmsHost.host']}"  text="#{pageSession.valueMap['host']}" />
        </sun:property>
        <sun:property id="PortProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.portLabel}" helpText="$resource{i18njms.edJmsHost.portHelp}" >
            <sun:textField id="Port" columns="$int{25}" maxLength="#{sessionScope.fieldLengths['maxLength.edJmsHost.port']}"  styleClass="port"text="#{pageSession.valueMap['port']}" />
        </sun:property>
        <sun:property id="AdminUserProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.adminUserLabel}" helpText="$resource{i18njms.edJmsHost.adminUserHelp}" >
            <sun:textField id="AdminUser" columns="$int{25}" maxLength="#{sessionScope.fieldLengths['maxLength.edJmsHost.adminUser']}"  text="#{pageSession.valueMap['adminUserName']}" styleClass="required"  required="#{true}" />
        </sun:property>
        <sun:property id="newPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.adminPwdLabel}" helpText="$resource{i18njms.edJmsHost.adminPwdHelp}">
        <sun:passwordField id="NewPassword" name="password" columns="$int{25}" maxLength="#{sessionScope.fieldLengths['maxLength.edJmsHost.adminPwd']}"  text="#{pageSession.valueMap['adminPassword']}" styleClass="required"  required="#{true}">
             <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{ps1});
            />
        </sun:passwordField>
        </sun:property>
        <sun:property id="confirmPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.ConfirmNewPassword}">
            <sun:passwordField id="ConfirmPassword" name="password" columns="$int{25}" maxLength="#{sessionScope.fieldLengths['maxLength.server.ConfirmNewPassword']}"  text="#{pageSession.valueMap['adminPassword']}" styleClass="required" required="#{true}"  >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{ps2});
                />
            </sun:passwordField>
        </sun:property>

        "<br /><br />
    </sun:propertySheetSection>
</sun:propertySheet>    
