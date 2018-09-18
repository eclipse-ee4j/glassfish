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

<!-- securityAttrs.inc -->

<sun:propertySheet id="propertySheet">
#include "/common/shared/configNameSection.inc"
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="securityManagerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.SecurityManager}"  helpText="$resource{i18nc.security.SecurityManagerHelp}">
            <sun:checkbox  selected="#{pageSession.securityManagerStatus}" selectedValue="true"  >
            <!beforeCreate
                setPageSessionAttribute(key="jvmOptionsUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/java-config/jvm-options");
                getSecurityManagerValue(endpoint="#{pageSession.jvmOptionsUrl}" value="#{pageSession.securityManagerStatus}")
            />
            </sun:checkbox>
        </sun:property>
        <sun:property id="auditLoggingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.AuditLogging}" helpText="$resource{i18nc.security.AuditLoggingHelp}">
            <sun:checkbox  selected="#{pageSession.valueMap['auditEnabled']}" selectedValue="true" />
        </sun:property>

      <sun:property id="defaultRealmProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.DefaultRealm}" helpText="$resource{i18nc.security.DefaultRealmHelp}">
            <sun:dropDown id="DefaultRealm" labels="$pageSession{realms}" selected="#{pageSession.valueMap['defaultRealm']}" />
            <!beforeCreate
                gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/security-service/auth-realm"
                    result="#{pageSession.realms}");
            />
        </sun:property>
        <sun:property id="defaultPrincipalProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.DefaultPrincipal}" helpText="$resource{i18nc.security.DefaultPrincipalHelp}">
            <sun:textField id="DefaultPrincipal" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.security.DefaultPrincipal']}" text="#{requestScope.principal"/>
        </sun:property>
        <sun:property id="defaultPrincipalPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.DefaultPrincipalPassword}" helpText="$resource{i18nc.security.DefaultPrincipalPasswordHelp}">
            <sun:passwordField id="DefaultPrincipalPassword" name="password" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.security.DefaultPrincipalPassword']}" text="#{requestScope.password"/>
        </sun:property>
      <sun:property id="jaccProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.Jacc}" helpText="$resource{i18nc.security.JaccHelp}">
            <sun:dropDown id="Jacc" labels="$pageSession{jaccs}" selected="#{pageSession.valueMap['jacc']}" />
            <!beforeCreate
                gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/security-service/jacc-provider"
                    result="#{pageSession.jaccs}");
            />
        </sun:property>
     <sun:property id="auditModulesProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.AuditModules}" helpText="$resource{i18nc.security.AuditModulesHelp}">
            <sun:listbox id="AuditModules" immediate="#{true}" multiple="#{true}"  rows="$int{4}"
                labels="$pageSession{availableAudits}"
                selected="#{pageSession.selectedAuditModules}" >
                <!beforeCreate
                    gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/security-service/audit-module"
                        result="#{pageSession.audits}");
                    getListBoxOptions(
                        availableList="#{pageSession.audits}",
                        selectedCommaString="#{pageSession.valueMap['auditModules']}",
                        availableListResult="#{pageSession.availableAudits}",
                        selectedOptions="#{pageSession.selectedAuditModules}"
                        addEmptyFirstChoice="true");
                />
            </sun:listbox>
        </sun:property>
       <sun:property id="roleMappingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.RoleMapping}" helpText="$resource{i18nc.security.RoleMappingHelp}">
            <sun:checkbox  selected="#{pageSession.valueMap['activateDefaultPrincipalToRoleMapping']}" selectedValue="true" />
        </sun:property>
        <sun:property id="mappedPrincipalClassProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.MappedPrincipalClass}" helpText="$resource{i18nc.security.MappedPrincipalClassHelp}">
            <sun:textField id="MappedPrincipalClass" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.security.MappedPrincipalClass']}" text="#{pageSession.valueMap['mappedPrincipalClass']}"/>
        </sun:property>

        "<br /><br />
    </sun:propertySheetSection>

</sun:propertySheet>
