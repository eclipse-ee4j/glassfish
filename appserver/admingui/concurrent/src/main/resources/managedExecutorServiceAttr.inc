<!--

    Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
    Copyright (c) 2021 Contributors to the Eclipse Foundation

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

<!-- concurrent/managedExecutorService.inc -->

<sun:propertySheet id="propertySheet">
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
                gfr.prepareLogicalJndiNameForEdit();
            />
        </sun:property>

#include "/concurrent/contextInfo.inc"
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
                
       <sun:property id="threadPriorityProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.threadPriorityLabel}"  helpText="$resource{i18ncon.threadPriorityLabelHelp}">
            <sun:textField id="threadPriorityProp" styleClass="integer" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['threadPriority']}" />
       </sun:property>

       <sun:property id="useVirtualThreads"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.usevirtualthreads}">
            <sun:checkbox  selected="#{pageSession.valueMap['useVirtualThreads']}" selectedValue="true"  />
       </sun:property>

       <sun:property id="longrunningtasks"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.longRunningTasks}">
            <sun:checkbox  selected="#{pageSession.valueMap['longRunningTasks']}" selectedValue="true"  />
       </sun:property>
       
       <sun:property id="hungafter" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.hungAfterSeconds}"  helpText="$resource{i18ncon.hungAfterSecondsHelp}">
            <sun:textField id="hungafter" styleClass="integer" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['hungAfterSeconds']}" />
            <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
       </sun:property>
       <sun:property id="hungLoggerPrintOnce"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.hungLoggerPrintOnce}">
            <sun:checkbox  selected="#{pageSession.valueMap['hungLoggerPrintOnce']}" selectedValue="true"  />
       </sun:property>
       <sun:property id="hungLoggerInitialDelay" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.hungLoggerInitialDelaySeconds}"  helpText="$resource{i18ncon.hungLoggerInitialDelaySecondsHelp}">
            <sun:textField id="hungLoggerInitialDelay" styleClass="integer" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['hungLoggerInitialDelaySeconds']}" />
            <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
       </sun:property>
       <sun:property id="hungLoggerInterval" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.hungLoggerIntervalSeconds}"  helpText="$resource{i18ncon.hungLoggerIntervalSecondsHelp}">
            <sun:textField id="hungLoggerInterval" styleClass="integer" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['hungLoggerIntervalSeconds']}" />
            <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
       </sun:property>
       <sun:property id="deploymentOrder" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" rendered="#{edit}" label="$resource{i18n.common.resource.deploymentOrder}"  helpText="$resource{i18n.common.resource.deploymentOrderHelp}">
          <sun:textField id="deploymentOrder" styleClass="integer" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.common.deploymentOrder']}" text="#{pageSession.valueMap['deploymentOrder']}" />
      </sun:property>
      <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
           <sun:textField id="desc" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['description']}" />
      </sun:property>
   </sun:propertySheetSection>
                
   <sun:propertySheetSection id="poolSettingsPropertySheet" label="$resource{i18ncon.propertySheetSection.poolSettings}">         
       <sun:property id="coresize"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.corePoolSize}"  helpText="$resource{i18ncon.corePoolSizeHelp}">
            <sun:textField id="coresize" styleClass="integer" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['corePoolSize']}" />
       </sun:property>
       <sun:property id="maximumPoolSize" rendered="#{pageSession.showMaxPoolSize}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.maximumPoolSize}"  helpText="$resource{i18ncon.maximumPoolSizeHelp}">
            <sun:textField id="maximumSize" styleClass="integer" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['maximumPoolSize']}" />
       </sun:property>
       <sun:property id="keepAlive" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.keepAliveSeconds}"  helpText="$resource{i18ncon.keepAliveSecondsHelp}">
            <sun:textField id="keepAlive" styleClass="integer" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['keepAliveSeconds']}" />
            <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
       </sun:property>
       <sun:property id="threadLifetime" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.threadLifetimeSeconds}"  helpText="$resource{i18ncon.threadLifetimeSecondsHelp}">
            <sun:textField id="threadLifetime" styleClass="integer" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['threadLifetimeSeconds']}" />
            <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
       </sun:property>
       <sun:property id="taskQueueCapacity"  rendered="#{pageSession.showTaskQueue}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.taskQueueCapacity}"  helpText="$resource{i18ncon.taskQueueCapacityHelp}">
            <sun:textField id="taskQueueCapacity" styleClass="integer" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.common.description']}" text="#{pageSession.valueMap['taskQueueCapacity']}" />
       </sun:property>

    </sun:propertySheetSection>
    "</br/></br>
</sun:propertySheet>
