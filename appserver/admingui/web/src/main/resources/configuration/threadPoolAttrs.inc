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

<!-- configuration/threadPoolAttrs.inc -->

<sun:propertySheet id="propertySheet">
#include "/common/shared/configNameSection.inc"
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">

        <sun:property id="name"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
            <sun:staticText id="name" text="#{pageSession.Name}"/>
        </sun:property> 
        <sun:property id="nameProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" helpText="$resource{i18n_web.threadPool.nameHelp}">
            <sun:textField id="nameText" text="#{pageSession.valueMap['name']}" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.Name']}" styleClass="required" required="#{true}" />
        </sun:property>

        <sun:property id="classname"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.classname}" helpText="$resource{i18n_web.threadPool.classnameHelp}">
        <sun:textField id="classname" styleClass="required" columns="$int{50}" maxLength="#{sessionScope.fieldLengths['maxLength.threadPool.classname']}" text="#{pageSession.valueMap['classname']}" />
        </sun:property>

        <sun:property id="max"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.maxQueue}" helpText="$resource{i18n_web.threadPool.maxQueueHelp}">
        <sun:textField id="max" styleClass="intAllowMinusOne" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.threadPool.maxQueue']}" text="#{pageSession.valueMap['maxQueueSize']}" />
        </sun:property>

        <sun:property id="maxThread"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.maxThread}" helpText="$resource{i18n_web.threadPool.maxThreadHelp}">
        <sun:textField id="maxThread" styleClass="intAllowMinusOne" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.threadPool.maxThread']}" text="#{pageSession.valueMap['maxThreadPoolSize']}" />
        </sun:property>

        <sun:property id="minThread"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.minThread}" helpText="$resource{i18n_web.threadPool.minThreadHelp}">
        <sun:textField id="minThread" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.threadPool.minThread']}" text="#{pageSession.valueMap['minThreadPoolSize']}" />
        </sun:property>

        <sun:property id="timeout"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.timeout}" helpText="$resource{i18n_web.threadPool.timeoutHelp}">
        <sun:textField id="timeout" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.threadPool.timeout']}" text="#{pageSession.valueMap['idleThreadTimeoutSeconds']}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
        </sun:property>

    </sun:propertySheetSection>
    </sun:propertySheet>
