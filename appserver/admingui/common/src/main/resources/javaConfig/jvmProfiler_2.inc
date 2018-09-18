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

<!-- javaConfig/jvmProfiler_2.inc -->

#include "/common/shared/alertMsg.inc"
                <!-- Page Title -->
                <sun:title id="propertyContentPage" title="$resource{i18nc.jvm.ProfilerPageName}"
                    helpText="$resource{i18nc.jvm.ProfilerPageHelp}">
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18nc.button.CreateProfiler}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')){
                        disableAllButtons();
                        return submitAndDisable(this, '$resource{i18n.button.Processing}');}
                        else {return false;}" >
                <!command
                    mapPut(map="#{pageSession.valueMap}" key="target" value="#{pageSession.configName}");
                    getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                    getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    foreach(var="oneRow" list="#{requestScope.newList}"){
                        startsWith(testStr="#{requestScope.oneRow.value}" pattern="-" result="#{requestScope.valid}")
                        if ("!#{requestScope.valid}"){
                            gf.handleError( detail="$resource{i18nc.msg.jvmOptionInvalid, #{requestScope.oneRow.value}}");
                        }
                    }
                    
                    gf.createEntity(endpoint="#{pageSession.createProfilerUrl}"
                            attrs="#{pageSession.valueMap}"
                            convertToFalse="#{pageSession.convertToFalseList}"
                            onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                            result => $attribute{objStr});
                    prepareAlertMsg(type="success", summary="$resource{i18nc.jvm.ProfilerCreated}");
                    saveJvmOptionValues(endpoint="#{pageSession.profilerUrl}/jvm-options", target="#{pageSession.configName}", options="$attribute{newList}" ,
                        deleteProfileEndpoint="#{pageSession.deleteProfilerUrl}" profiler="true");
                    gf.redirect(page="#{selfPage}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&name=#{pageSession.Name}&configName=#{pageSession.configName}");
                />
            </sun:button>
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')){
                        disableAllButtons();
                        return submitAndDisable(this, '$resource{i18n.button.Processing}');}
                        else {return false;}" >
                <!command
                    gf.createEntity(endpoint="#{pageSession.profilerUrl}" attrs="#{pageSession.valueMap}"
                            skipAttrs="#{pageSession.skipAttrsList}"
                            convertToFalse="#{pageSession.convertToFalseList}"
                            onlyUseAttrs="#{pageSession.onlyUseAttrs}");
                    getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                    getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    saveJvmOptionValues(endpoint="#{pageSession.profilerUrl}/jvm-options", target="#{pageSession.configName}", options="$attribute{newList}" , origList="#{pageSession.origList}" profiler="true");
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&name=#{pageSession.Name}&configName=#{pageSession.configName}");
                />
            </sun:button>

            <sun:button id="deleteButton" rendered="#{edit}" text="$resource{i18nc.button.DeleteProfiler}"
                onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
                    { disableAllButtons(); return submitAndDisable(this, '$resource{i18n.button.Processing}');}
                    else {return false;} " >
                <!command
                    createMap(result="#{pageSession.attrMap}");
                    mapPut(map="#{pageSession.attrMap}" key="target" value="#{pageSession.configName}");
                    gf.restRequest(endpoint="#{pageSession.deleteProfilerUrl}", attrs="#{pageSession.attrMap}", method="delete");
                    prepareAlertMsg(type="success", summary="$resource{i18nc.jvm.ProfilerDeleted}");
                    gf.redirect(page="#{selfPage}?configName=#{pageSession.configName}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>

        </sun:panelGroup>
    </facet>
        </sun:title>
        <!-- PropertySheet .... -->
        <sun:propertySheet id="propertySheet">
            <!-- Text Field section -->
#include "/common/shared/configNameSection.inc"

            <sun:propertySheetSection id="propertSectionTextField">
                <sun:property id="profilerNameProp" rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ProfilerName}">
                    <sun:textField id="ProfilerName"  styleClass="required" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.ProfilerName']}" text="#{pageSession.valueMap['name']}" required="#{true}"/>
                </sun:property>
                <sun:property id="profilerNamePropEdit" rendered="#{edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ProfilerName}">
                    <sun:staticText id="ProfilerName2" text="#{pageSession.valueMap['name']}"/>
                </sun:property>
               <sun:property id="profilerEnabledProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
                    <sun:checkbox  selected="#{pageSession.valueMap['enabled']}" selectedValue="true" />
               </sun:property>
                <sun:property id="ClasspathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.Classpath}" helpText="$resource{i18nc.jvm.ClasspathHelp}">
                    <sun:textField id="ClasspathLabel" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.Classpath']}"  text="#{pageSession.valueMap['classpath']}" />
                </sun:property>
                <sun:property id="NativeLibraryProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.NativeLibraryPath}" helpText="$resource{i18nc.jvm.NativeLibraryPathHelp}">
                    <sun:textField id="NativeLibrary" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.NativeLibraryPath']}" text="#{pageSession.valueMap['nativeLibraryPath']}"/>
                </sun:property>
                "<br /><br />
            </sun:propertySheetSection>
        </sun:propertySheet>
"<br/>
#include "/common/javaConfig/jvmOptionsTable.inc"
                
	<sun:hidden id="helpKey" value="$resource{help_common.serverInstProfiler}" />
            </sun:form>
#include "/common/shared/changeButtonsJS.inc"
</define>
</composition>
