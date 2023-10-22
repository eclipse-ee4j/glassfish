<!--

    Copyright (c) 2023 Contributors to the Eclipse Foundation
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

<!-- jca/resourceAdapterConfigAttr.inc -->
<!-- used by resourceAdapterConfigNew.jsf and resourceAdapterConfigEdit.jsf -->

#include "/common/applications/applicationHandlers.inc"
<sun:propertySheet id="propertySheet">
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="name"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.resourceAdapterConfig.Name}">
            <sun:staticText id="name" text="#{pageSession.Name}"  rendered="#{Edit}" />
        </sun:property>

        <sun:property id="nameNewDropDown"  rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.resourceAdapterConfig.Name}" helpText="$resource{i18njca.resourceAdapterConfig.NameHelp}" >
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{propId});
            />
            <sun:hidden id="option" value="#{pageSession.myOption}">
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{optionId});
                />
            </sun:hidden>
            <sun:radioButton id="optA"  rendered="#{!edit}" name="dropDownOption" onClick="navigateToPage('#{pageSession.nameDropDown}', 'dropdown');"/>
            <sun:dropDown id="resAdaptorProp" selected="#{pageSession.nameDropDown}" labels="$pageSession{connectorList}"
                    submitForm="#{true}" immediate="#{true}" >
                <!beforeCreate
                     setPageSessionAttribute(key="snifferType" value="connector");
                     gfr.getApplicationsBySnifferType();
                     setPageSessionAttribute(key="connectorList" value="#{pageSession.snifferTypeApplicationsList}");
                     filterOutRarExtension(inList="#{pageSession.connectorList}" convertedList="#{pageSession.connectorList}");
                     if (#{sessionScope._jms_exist}){
                        listAdd(list="#{pageSession.connectorList}" value="jmsra" index="$int{0}")
                    }
                />
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="name" value="#{click.selected}");
                    getUIComponent(clientId="#{pageSession.textId}", component=>$attribute{textIdComp});
                    getUIComponentProperty(component="$attribute{textIdComp}", name="value", value=>$attribute{nameText});
                    setAttribute(key="reload" value="#{true}" );
                    gf.navigate(page="#{pageSession.selfPage}");
                />
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{dropdownTextId});
                />
            </sun:dropDown>
            "<br />
            <sun:radioButton id="optB"  rendered="#{!edit}" name="textOption" onClick="navigateToPage('#{pageSession.nameDropDown}', 'text');" />
             <sun:textField id="nameNewText" rendered="#{!edit}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.resourceAdapterConfig.Name']}" text="#{pageSession.nameText}" >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{textId});
                />
            </sun:textField>
        </sun:property>

        <sun:property id="threadPoolsIdProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.resourceAdapterConfig.threadPoolId}"
                helpText="$resource{i18njca.resourceAdapterConfig.ThreadPoolIdsHelp}" >
            //<sun:textField id="threadpoolsid" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.resourceAdapterConfig.ThreadPoolId']}" text="#{pageSession.valueMap['threadPoolIds']}" />
            <sun:dropDown id="threadpoolsid" selected="#{pageSession.valueMap['threadPoolIds']}" labels="#{pageSession.threadPoolList}" >
                <!beforeCreate
                    gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/server-config/thread-pools/thread-pool"
                        result="#{requestScope.tmpList}");
                    addEmptyFirstElement(in="#{requestScope.tmpList}" out="#{pageSession.threadPoolList}")
                />
                </sun:dropDown>
        </sun:property>

        <sun:property id="deploymentOrder" labelAlign="left" noWrap="#{true}" rendered="#{edit}" overlapLabel="#{false}"  rendered="#{edit}" label="$resource{i18n.common.resource.deploymentOrder}" helpText="$resource{i18n.common.resource.deploymentOrderHelp}">
            <sun:textField id="deploymentOrder" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.common.deploymentOrder']}" text="#{pageSession.valueMap['deploymentOrder']}" />
        </sun:property>

        "<br /><br />
    </sun:propertySheetSection>
</sun:propertySheet> 
