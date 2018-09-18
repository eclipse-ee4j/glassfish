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

<!-- grizzly/protocolAttr.inc -->

<sun:propertySheetSection id="propertSectionTextField">

        <sun:property id="nameNew"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.common.name}" helpText="$resource{i18n_web.common.nameHelp}">
            <sun:textField id="name"  styleClass="required" required="#{true}" columns="$int{50}" maxLength="#{sessionScope.fieldLengths['maxLength.common.Name']}" text="#{pageSession.valueMap['Name']}" />
        </sun:property>

        <sun:property id="name"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.common.name}">
            <sun:staticText id="name" text="#{pageSession.Name}"  rendered="#{Edit}" />
        </sun:property>

        <sun:property id="status"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.protocol.securityEnabled}" >
            <sun:checkbox  selected="#{pageSession.valueMap['securityEnabled']}" selectedValue="true" />
        </sun:property>
</sun:propertySheetSection>
