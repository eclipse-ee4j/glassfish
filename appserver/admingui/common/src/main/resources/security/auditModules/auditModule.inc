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

    <!-- PropertySheet .... -->
    <sun:propertySheet id="propertySheet" requiredFields="true">			 
#include "/common/shared/configNameSection.inc"

        <sun:propertySheetSection id="propertSectionTextField">
            <sun:property id="IdStaticProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
                <sun:staticText id="IdStatic" text="#{pageSession.valueMap['name']}"/>
            </sun:property> 

            <sun:property id="IdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
                <sun:textField id="IdText" styleClass="required" text="#{pageSession.valueMap['name']}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.Name']}" required="#{true}" />
            </sun:property> 

            <sun:property id="classNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.realm.Classname}">                           
                <sun:textField id="ClassName" styleClass="required" text="#{pageSession.valueMap['classname']}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.realm.Classname']}" required="#{true}" />
            </sun:property>

        </sun:propertySheetSection>
    </sun:propertySheet>    


