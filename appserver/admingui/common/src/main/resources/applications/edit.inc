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
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="jndiProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
            <sun:staticText id="name" text="#{pageSession.appName}"/>
        </sun:property>

        

        <sun:property id="statusProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox id="status" rendered="#{pageSession.onlyDASExist}"  selected="#{pageSession.valueMap2['enabled']}" selectedValue="true" />
            <sun:staticText id="statusText" rendered="#{!pageSession.onlyDASExist}" text="#{pageSession.status}" />
        </sun:property>

        <sun:property id="osgi" rendered="#{pageSession.showOsgiType}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.osgiType}" >
                <sun:checkbox id="osgi"  label=" "  disabled="#{true}"  selected="#{requestScope.osgiType}"  />
                <!beforeCreate
                    setAttribute(key="osgiType" value="#{false}");
                    if ("#{pageSession.valueMap['type']"){
                           setAttribute(key="osgiType" value="#{true}");
                    }
                />
        </sun:property>
        <sun:property id="dummy" rendered="#{pageSession.showOsgiType}" >
        </sun:property>
 
        <sun:property id="vsProp"  rendered="#{pageSession.finalShowVS}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.virtualServerLabel}" helpText="$resource{i18n.deploy.vsHelp}">
            <sun:listbox id="vs" immediate="#{true}" multiple="#{true}"  rows="$int{4}"
                labels="$pageSession{availableVS}"
                selected="#{pageSession.selectedVS}" >
                <!beforeCreate
                    getVsForDeployment(result="#{pageSession.vsList}");
                    getListBoxOptions(
                        availableList="#{pageSession.vsList}",
                        selectedCommaString="#{pageSession.valueMap2['virtualServers']}",
                        availableListResult="#{pageSession.availableVS}",
                        selectedOptions="#{pageSession.selectedVS}"
                        addEmptyFirstChoice="true");
                />
                </sun:listbox>
            </sun:property>

        <sun:property id="ctxProp" rendered="#{pageSession.showContextRoot}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.webApp.ContextRoot}" helpText="$resource{i18n.webApp.contextRootHelp}" >
            <sun:textField id="ctx" columns="$int{40}" maxLength="#{sessionScope.fieldLengths['maxLength.webApp.ContextRoot']}" text="#{pageSession.valueMap['contextRoot']}" />
        </sun:property>

        <sun:property id="implicitCdi" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.implicitCdi}" helpText="$resource{i18n.deploy.implicitCdiHelp}">
            <sun:checkbox id="implicitCdi" disabled="true"  selected="#{pageSession.implicitCdiEnabled}" selectedValue="true" />
        </sun:property>

        <sun:property id="availability" rendered="#{pageSession.finalShowAvail}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.availability}" helpText="$resource{i18n.deploy.availabilityHelp}">
            <sun:checkbox id="availability"  selected="#{pageSession.valueMap['availabilityEnabled']}" selectedValue="true" />
        </sun:property>


        <sun:property id="jw" rendered="#{pageSession.showJavaWebStart}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.JavaWebStart}" helpText="$resource{i18n.edit.JavaWebStartHelp}">
            <sun:checkbox id="jwc" disabled="true"  selected="#{pageSession.javaWebStartEnabled}" selectedValue="true" >
            </sun:checkbox>
        </sun:property>

        
        <sun:property id="locationProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.location}">
            <sun:staticText id="location" text="#{pageSession.valueMap['location']}"/>
        </sun:property>
            
        <sun:property id="deploymentOrderProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.deploymentOrder}" helpText="$resource{i18n.deploy.deploymentOrderHelp}">
            <sun:textField id="deploymentOrder" columns="$int{40}" maxLength="#{sessionScope.fieldLengths['maxLength.deploy.deploymentOrder']}" text="#{pageSession.valueMap['deploymentOrder']}" />
        </sun:property>

        <sun:property id="librariesProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Libraries}">
            <sun:staticText id="libraries" text="#{pageSession.valueMap['libraries']}"/>
            
        </sun:property>
            
        <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
            <sun:textField id="desc" columns="$int{40}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['description']}" />
           "<br /><br />
        </sun:property>
        <!beforeCreate
            setPageSessionAttribute(key="javaWebStartEnabled" value="true");
            setPageSessionAttribute(key="implicitCdiEnabled" value="true");
            foreach (var="oneProp" list="#{pageSession.tableList}") {
                if (#{requestScope.oneProp.name}=$constant{org.glassfish.admingui.common.deployment.DFDeploymentProperties.DEPLOY_OPTION_JAVA_WEB_START_ENABLED}){
                    setPageSessionAttribute(key="javaWebStartEnabled" value="#{requestScope.oneProp.value}");
                }
                if (#{requestScope.oneProp.name}=implicitCdiEnabled){
                    setPageSessionAttribute(key="implicitCdiEnabled" value="#{requestScope.oneProp.value}");
                }
            }
        />
    </sun:propertySheetSection>
</sun:propertySheet>


