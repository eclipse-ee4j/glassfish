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

<!-- jca/workSecurityMapAttr.inc -->

#include "/common/applications/applicationHandlers.inc"
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">
    <sun:propertySheetSection id="propertSectionTextField">
        <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{sheetId});
        />

        <sun:property id="mapName" rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.securityMapName}">
            <sun:staticText id="mapName" text="#{pageSession.Name}"/>
        </sun:property>

        <sun:property id="mapNameNew" rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.securityMapName}" >
            <sun:textField id="mapName"   styleClass="required" maxLength="#{sessionScope.fieldLengths['maxLength.connectorSecurityMap.securityMapName']}" text="#{pageSession.valueMap['id']}" required="#{true}">
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{resCompId});
                />
            </sun:textField>
        </sun:property>
                <sun:property id="groupProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.workSecurityMap.Group}">

            <sun:radioButton id="optA"  name="usersOption" label="$resource{i18njca.workSecurityMap.GroupMapping}" selected="#{requestScope.usersOptionG}" selectedValue="users"
                    disabled="#{disableGroups}"  onClick="enableWorkSecurityMapFields('users'); "
                />
            "<br /><br />
            <sun:textField id="eisgrouptext" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.workSecurityMap.Group']}" text="#{pageSession.valueMap2['groupMapping']}" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{eisuserGroupsId});
                    />
            </sun:textField>
            "<br />
            <sun:helpInline id="eisgroupHelp"text="$resource{i18njca.workSecurityMap.EISGroupInlineHelp}"/>
            "<br />
                    <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{grpPropId});
                />
        </sun:property>

        <sun:property id="principalsProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.workSecurityMap.Principal}">
                   <sun:radioButton id="optB"  name="usersOption" label="$resource{i18njca.workSecurityMap.PrincipalMapping}" selected="#{requestScope.usersOptionP}" selectedValue="principals"
                     disabled="#{disablePrincipals}" onClick="javascript: enableWorkSecurityMapFields('principals');"
                />
            "<br /><br />
            <sun:textField id="eisprincipals" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.workSecurityMap.GroupMapping']}" text="#{pageSession.valueMap2['principalMapping']}" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{eisprincipalsId});
                    />
            </sun:textField>
            "<br />
                    <sun:helpInline id="eisprincipalsHelp"text="$resource{i18njca.workSecurityMap.EISPrincipalInlineHelp}"/>
            "<br />
                    <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{pPropId});
                />
        </sun:property>
        <sun:property id="resAdaptorProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resourceAdapter}" helpText="$resource{i18njca.adminObjectResource.resourceAdapterHelp}">
            <sun:dropDown id="resAdaptorProp" selected="#{pageSession.valueMap['raname']}" labels="$pageSession{connectorList}"  >
                <!beforeCreate
                     setPageSessionAttribute(key="snifferType" value="connector");                     
                     gfr.getApplicationsBySnifferType();
                     setPageSessionAttribute(key="connectorList" value="#{pageSession.snifferTypeApplicationsList}");
                     if (#{sessionScope._jms_exist}){
                        listAdd(list="#{pageSession.connectorList}" value="jmsra")
                    }
                />
           </sun:dropDown>
        </sun:property>
        <sun:property id="deploymentOrder" labelAlign="left" noWrap="#{true}" rendered="#{edit}" overlapLabel="#{false}"  rendered="#{edit}" label="$resource{i18n.common.resource.deploymentOrder}"  helpText="$resource{i18n.common.resource.deploymentOrderHelp}" >
            <sun:textField id="deploymentOrder" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.common.deploymentOrder']}" text="#{pageSession.valueMap['deploymentOrder']}" />
        </sun:property>
     <sun:property id="descriptionProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
            <sun:textField id="descAdaptor" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['description']}" />
        </sun:property>
        <!--<sun:property id="status"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}" >
            <sun:checkbox  selected="#{pageSession.valueMap['enabled']}" selectedValue="true" />
       </sun:property>-->

</sun:propertySheetSection>
</sun:propertySheet>
