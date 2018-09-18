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

<!-- jca/connectorSecurityMapAttr.inc -->

<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">
    <sun:propertySheetSection id="propertSectionTextField">
        <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{sheetId});
        />
        <sun:property id="poolName" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.poolName}">
            <sun:staticText id="poolName" text="#{pageSession.Name}"/>
        </sun:property>

        <sun:property id="mapName" rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.securityMapName}">
            <sun:staticText id="mapName" text="#{pageSession.mapName}"/>
        </sun:property>

        <sun:property id="mapNameNew" rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.securityMapName}" >
            <sun:textField id="mapName"   maxLength="#{sessionScope.fieldLengths['maxLength.connectorSecurityMap.securityMapName']}" styleClass="required" text="#{pageSession.valueMap['name']}" required="#{true}">
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{resCompId});
                />
            </sun:textField>
        </sun:property>

        <sun:property id="groupProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.userGroups}"  helpText="$resource{i18njca.connectorSecurityMap.userGroupsHelp}">
            <sun:radioButton id="optA"  name="usersOption" selected="#{requestScope.usersOptionU}" selectedValue="users"
                onClick="enableSecurityMapFields('users'); "
            />
            <sun:textField id="group" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.connectorSecurityMap.userGroups']}" text="#{pageSession.valueMap['userGroupCommaStr']}" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{userGroupsId});
                    />
            </sun:textField>
            <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{grpPropId});
                />
        </sun:property>

        <sun:property id="principalsProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.principals}"  helpText="$resource{i18njca.connectorSecurityMap.principalsHelp}">
                   <sun:radioButton id="optB"  name="usersOption" selected="#{requestScope.usersOptionP}" selectedValue="principals"
                    onClick="javascript: enableSecurityMapFields('principals');"
                />
            <sun:textField id="principals" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.connectorSecurityMap.principals']}" text="#{pageSession.valueMap['principalCommaStr']}" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{principalsId});
                    />
            </sun:textField>
            <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{pPropId});
                />
        </sun:property>
        "<br /><br />
    </sun:propertySheetSection>

    <sun:propertySheetSection id="propertSectionTextField2" label="$resource{i18njca.connectorSecurityMap.backendPrincipal}">
        <sun:property id="userNameEdit" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.userName}">
            <sun:textField id="userNameEdit" disabled="#{edit}" styleClass="required" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.connectorSecurityMap.userName']}" text="#{pageSession.valueMap['mappedUserName']}" required="#{true}" />
        </sun:property>
        <sun:property id="passwordEdit" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.password}">
            <sun:passwordField id="passwordEdit" name="password" columns="$int{55}" text="#{pageSession.valueMap['mappedPassword']}"/>
        </sun:property>
    </sun:propertySheetSection>

</sun:propertySheet>
