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

<!-- shared/jvmGeneral_2.jsf -->

#include "/common/shared/alertMsg.inc"
                <!-- Page Title -->
                <sun:title id="propertyContentPage" title="$resource{i18nc.jvm.GeneralTitle}" 
                    helpText="$resource{i18nc.jvm.GeneralPageHelp}">
#include "/common/shared/editPageButtons.inc"             
                </sun:title>
                    <!-- PropertySheet .... -->
                    <sun:propertySheet id="propertySheet" requiredFields="true">	
                        <!-- Text Field section -->
#include "/common/shared/configNameSection.inc"
                        <sun:propertySheetSection id="propertSectionTextField">
                            <sun:property id="javaHomeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.JavaHomeLabel}" helpText="$resource{i18nc.jvm.JavaHomeHelp}">
                                <sun:textField id="JavaHome"  styleClass="required" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.JavaHome']}" text="#{pageSession.valueMap['javaHome']}" required="#{true}"/> 
                            </sun:property>                       
                            <sun:property id="JavacOptionsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.JavacOptionsLabel}" helpText="$resource{i18nc.jvm.JavacOptionsHelp}">                           
                                <sun:textField id="JavacOptions" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.JavacOptions']}" text="#{pageSession.valueMap['javacOptions']}"/>     
                            </sun:property>                        
                          <sun:property id="debugEnabledProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.DebugLabel}" helpText="$resource{i18nc.jvm.DebugHelp}">
                                <sun:checkbox id="debug"  selected="#{pageSession.valueMap['debugEnabled']}" selectedValue="true" />      
                            </sun:property>
                            <sun:property id="DebugOptionsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.DebugOptionsLabel}" helpText="$resource{i18nc.jvm.DebugOptionsHelp}">
                                <sun:textField id="DebugOptions" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.DebugOptions']}" text="#{pageSession.valueMap['debugOptions']}" />      
                            </sun:property>
                            <sun:property id="RmicOptionsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.RMICompileOptionsLabel}" helpText="$resource{i18nc.jvm.RMICompileOptionsHelp}">
                                <sun:textField id="RmicOptions" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.RMICompileOptions']}" text="#{pageSession.valueMap['rmicOptions']}"/>      
                            </sun:property> 
                            <sun:property id="bytecodePreprocessorProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.BytecodePreprocessorLabel}" helpText="$resource{i18nc.jvm.BytecodePreprocessorHelp}">
                                <sun:textArea id="bytecodePreprocessor" rows="$int{8}" columns="$int{55}" text="#{pageSession.valueMap['bytecodePreprocessors']}"/>      
                            </sun:property>                  
                            "<br /><br />
                        </sun:propertySheetSection>

                    </sun:propertySheet>    
#include "/common/shared/propertyDescTable.inc"
                    <sun:hidden id="helpKey" value="$resource{help_common.serverInstJvmGeneral}" />
            </sun:form>
</define>
</composition>
