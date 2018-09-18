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

<sun:propertySheet id="propertySheet">
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">
        <event>
        <!beforeCreate
            getRequestValue(key="name" value=>$page{jndiName});
            setAttribute(key="enabled" value="#{true}");
        />
        </event>
        <sun:property id="jndiProp" rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}" >
            <sun:textField id="jndi" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.jndiName']}" text="#{pageSession.valueMap['name']}" styleClass="required" required="#{true}" >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{resCompId});
                />
            </sun:textField>
        </sun:property>

        <sun:property rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}" >
            <h:outputText value="#{pageSession.Name}" />
        </sun:property>

        <sun:property id="nameProp" labelAlign="left" noWrap="true" overlapLabel="false" label="$resource{i18njms.jmsDestination.destName}" helpText="$resource{i18njms.jmsDestination.destNameHelp}">
            <sun:textField id="name" columns="55" maxLength="#{sessionScope.fieldLengths['maxLength.jmsDestination.destName']}" text="#{pageSession.physDestName}" styleClass="required" required="true" />
        </sun:property>

        <sun:property id="resTypeProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resType}"  >
            <sun:dropDown id="resType" selected="#{pageSession.valueMap['resType']}" labels={"javax.jms.Topic"  "javax.jms.Queue"}  required="#{true}"/>
        </sun:property>

        <sun:property id="deploymentOrder" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}"  rendered="#{edit}" label="$resource{i18n.common.resource.deploymentOrder}" helpText="$resource{i18n.common.resource.deploymentOrderHelp}">
            <sun:textField id="deploymentOrder" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.common.deploymentOrder']}" text="#{pageSession.valueMap['deploymentOrder']}" />
        </sun:property>

        <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
            <sun:textField id="desc" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['description']}" />
        </sun:property>

        <sun:property id="statusProp"  rendered="#{useCheckBox}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox id="cb"  immediate="#{true}" selected="#{pageSession.valueMap2['enabled']}" selectedValue="true"/>
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
