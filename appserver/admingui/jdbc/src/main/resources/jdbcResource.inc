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

<!-- jdbc/jdbcResource.inc -->
<!-- used by jdbcResourceEdit.jsf and jdbcResourceNew.jsf -->
 

<sun:propertySheet id="propertySheet">
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="nameNew"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}">
            <sun:textField id="name" styleClass="required"  required="#{true}" columns="$int{30}" maxLength="#{sessionScope.fieldLengths['maxLength.common.jndiName']}" text="#{pageSession.valueMap['name']}" >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{resCompId});
                />
            </sun:textField>
        </sun:property>

        <sun:property id="name"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}">
            <sun:staticText id="name" text="#{pageSession.Name}"  rendered="#{Edit}" />
        </sun:property>

#include "/common/resourceNode/logicalName.inc"
        <sun:property id="logicalName"  rendered="#{edit and renderLogic}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.logicalJndiName}">
            <sun:staticText id="lName" text="#{pageSession.logicalJndiName}"  rendered="#{Edit}" />
            <!beforeCreate
                setPageSessionAttribute(key="listCommand" value="list-jdbc-resources");
                setPageSessionAttribute(key="logicalJndiMapKey" value="jdbcResources");
                gfr.prepareLogicalJndiNameForEdit();
            />
        </sun:property>

        <sun:property id="poolNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.poolName}"  helpText="$resource{i18njdbc.jdbcResource.poolHelp}">
            <sun:dropDown id="PoolName" selected="#{pageSession.valueMap['poolName']}" labels="$pageSession{jdbcList}"  values="$pageSession{jdbcList}" >
            <!beforeCreate
                gf.getChildrenNamesList(endpoint="#{pageSession.parentUrl}/jdbc-connection-pool", result="#{pageSession.jdbcList}");
            />
           </sun:dropDown>
        </sun:property>   
        <sun:property id="deploymentOrder" rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resource.deploymentOrder}" helpText="$resource{i18n.common.resource.deploymentOrderHelp}" >
            <sun:textField id="deploymentOrder" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.common.deploymentOrder']}" text="#{pageSession.valueMap['deploymentOrder']}" />
        </sun:property>
        <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
            <sun:textField id="desc" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['description']}" />      
        </sun:property>

        <sun:property id="statusProp"  rendered="#{useCheckBox}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox id="enabled"  selected="#{pageSession.valueMap2['enabled']}" selectedValue="true" />
        </sun:property>
        <sun:property id="statusProp2" rendered="#{useString}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}"
            label="$resource{i18n.common.status}" helpText="$resource{i18n.application.EnableTargetHelp}">
            <sun:staticText  id="enabledStr" text="#{requestScope.enabledString}" >
                <!beforeCreate
                    gf.getTargetEnableInfo(appName="#{pageSession.encodedName}" isApp="#{false}" status="#{requestScope.enabledString}");
                />
           </sun:staticText>
       </sun:property>
        "<br /><br />
    </sun:propertySheetSection>
</sun:propertySheet>    


