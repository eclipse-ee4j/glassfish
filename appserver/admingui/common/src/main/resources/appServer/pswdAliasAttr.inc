<!--

    Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.

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

    <sun:propertySheet id="propertySheet" requiredFields="true">

        <sun:propertySheetSection id="propertSectionTextField">
            <sun:property id="aliasName"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.pswdAlias.aliasName}" >
                <sun:staticText id="aliasName" text="#{pageSession.name}"/>
            </sun:property> 

            <sun:property id="aliasNameNew"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.pswdAlias.aliasName}"  helpText="$resource{i18nc.pswdAlias.aliasNameHelp}">
                <sun:textField id="aliasNameNew" styleClass="required" required="#{true}" text="#{pageSession.valueMap['id']}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.Name']}" />
            </sun:property>

            <sun:property id="newPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.pswdAlias.password}"  helpText="$resource{i18nc.pswdAlias.passwordHelp}">
            <sun:passwordField id="NewPassword"  name="password" styleClass="required" required="#{true}" columns="$int{25}" maxLength="#{sessionScope.fieldLengths['maxLength.server.NewPassword']}" text="#{pageSession.valueMap['aliaspassword']}" >
                 <!afterCreate
                    getClientId(component="$this{component}" clientId="#{pageSession.ps1}");
                />
            </sun:passwordField>
            </sun:property>
            <sun:property id="confirmPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.pswdAlias.confirmPassword}"  helpText="$resource{i18nc.pswdAlias.confirmPasswordHelp}">>
                <sun:passwordField id="ConfirmPassword"  name="password" styleClass="required" required="#{true}" columns="$int{25}" maxLength="#{sessionScope.fieldLengths['maxLength.server.ConfirmNewPassword']}" text="#{pageSession.confirmPassword}"   >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId="#{pageSession.ps2}");
                    />
                </sun:passwordField>
            </sun:property>

        </sun:propertySheetSection>
    </sun:propertySheet>


