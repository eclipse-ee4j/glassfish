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

<!-- full/jndiResourceAttr.inc -->


<!-- PropertySheet .... -->
<sun:hidden id="option"  value="#{myOption}" />
<sun:propertySheet id="propertySheet">			 
    <sun:propertySheetSection id="propertSectionTextField"> 
        <sun:property id="jndiStaticProp" rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}">
            <sun:staticText id="Jndi" text="#{pageSession.Name}"/> 
        </sun:property>   

        <sun:property id="jndiTextProp" rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}"  >
            <sun:textField id="jnditext" styleClass="required" maxLength="#{sessionScope.fieldLengths['maxLength.common.jndiName']}" text="#{pageSession.valueMap['name']}" required="#{true}">
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{jndiNameId});
                />
            </sun:textField>
        </sun:property> 

        <sun:property id="cp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resType}" helpText="$resource{i18njmail.jndiResource.resTypeHelp}<br /><br />">
                <sun:radioButton id="optA"  name="classnameOption" selected="$pageSession{classnameOption}" selectedValue="predefine"
                    onClick="enableClassnameFields('predefine'); updateFactoryClassOnClick();"
                />
                <sun:dropDown id="Classname" labels="$pageSession{builtInEntries}"  values="$pageSession{builtInEntries}"  selected="#{attrMap.classname}"
                    disabled="#{!attrMap.predefinedClassname}" onChange="updateFactoryClass(this.value);" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{classnameDropdownId});
                    />
                   
                />
                </sun:dropDown>
                "<br /><br />

                <sun:radioButton id="optB"  name="classnameOption" selected="$pageSession{classnameOption}" selectedValue="input"
                    onClick="javascript: enableClassnameFields('input');"
                />
                <sun:textField id="ClassnameText" text="#{attrMap.classnameInput}" columns="$int{70}" maxLength="#{sessionScope.fieldLengths['maxLength.jndiResource.className']}">
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{classnameTextId});
                    />
                </sun:textField>
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{rbPropId});
                />
            </sun:property> 


         <sun:property id="factoryClassProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njmail.jndiResource.factoryClass}" helpText="$resource{i18njmail.jndiResource.factoryClassHelp}">
            <sun:textField id="factoryClass" styleClass="required"  columns="$int{70}" maxLength="#{sessionScope.fieldLengths['maxLength.jndiResource.factoryClass']}" text="#{pageSession.valueMap['factoryClass']}" required="#{true}" >
                <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{factoryClassTextId});
                    />
             </sun:textField>
        </sun:property>
        <sun:property id="jndiLookupProp"  rendered="#{showJndiLookup}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njmail.jndiResource.jndiLookup}" helpText="$resource{i18njmail.jndiResource.jndiLookupHelp}" >
            <sun:textField id="jndiLookup" styleClass="required" columns="$int{70}" maxLength="#{sessionScope.fieldLengths['maxLength.jndiResource.jndiLookup']}" text="#{pageSession.valueMap['jndiLookupName']}" required="#{true}" >
                <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{jndiLookupId});
                    />
             </sun:textField>
        </sun:property>

        <sun:property id="deploymentOrder" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}"  rendered="#{edit}" label="$resource{i18n.common.resource.deploymentOrder}" helpText="$resource{i18n.common.resource.deploymentOrderHelp}" >
            <sun:textField id="deploymentOrder" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.common.deploymentOrder']}" text="#{pageSession.valueMap['deploymentOrder']}" />
        </sun:property>

        <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}" >helpText="$resource{i18n.full.descHelp}">
            <sun:textField id="desc" columns="$int{70}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['description']}" >
            <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{descId});
            />
            </sun:textField>
        </sun:property>

        <sun:property id="statusProp" rendered="#{useCheckBox}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox id="enabled"  selected="#{pageSession.valueMap2['enabled']}" selectedValue="true" >
            <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{statusId});
            />
            </sun:checkbox>
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

