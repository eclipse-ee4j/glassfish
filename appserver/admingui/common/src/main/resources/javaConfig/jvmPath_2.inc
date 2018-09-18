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

<!-- shared/jvmPath_2.inc -->

#include "/common/shared/alertMsg.inc"      
                <!-- Page Title -->
<sun:title id="propertyContentPage" title="$resource{i18nc.jvm.PathSettingsTitle}" helpText="$resource{i18nc.jvm.PathSettingsPageHelp}" >
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" text="$resource{i18n.button.Save}">
                <!command
                  <!-- commented out. refer to issue# 10967
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['serverClasspath']}"  formattedString="#{requestScope.scp}");
                    mapPut(map="#{pageSession.valueMap}" key="serverClasspath" value="$attribute{scp}");
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['systemClasspath']}"  formattedString="#{requestScope.syscp}");
                    mapPut(map="#{pageSession.valueMap}" key="systemClasspath" value="$attribute{syscp}");
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['classpathPrefix']}"  formattedString="#{requestScope.cpp}");
                    mapPut(map="#{pageSession.valueMap}" key="classpathPrefix" value="$attribute{cpp}");
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['classpathSuffix']}"  formattedString="#{requestScope.cps}");
                    mapPut(map="#{pageSession.valueMap}" key="classpathSuffix" value="$attribute{cps}");
                  -->

                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['nativeLibraryPathPrefix']}"  formattedString="#{requestScope.npp}");
                    mapPut(map="#{pageSession.valueMap}" key="nativeLibraryPathPrefix" value="$attribute{npp}");
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['nativeLibraryPathSuffix']}"  formattedString="#{requestScope.nps}");
                    mapPut(map="#{pageSession.valueMap}" key="nativeLibraryPathSuffix" value="$attribute{nps}");
                    gf.createEntity(endpoint="#{pageSession.selfUrl}"
                            attrs="#{pageSession.valueMap}"
                            skipAttrs="#{pageSession.skipAttrsList}"
                            convertToFalse="#{pageSession.convertToFalseList}"
                        );
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
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
            <sun:property id="NativeLibraryPathPrefixProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.NativeLibraryPathPrefix}" helpText="$resource{i18nc.jvm.NativeLibraryPathPrefixHelp}">
                <sun:textArea id="NativeLibraryPathPrefix" rows="$int{4}" columns="$int{75}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.NativeLibraryPathPrefix']}" text="#{pageSession.valueMap['nativeLibraryPathPrefix']}"/>
            </sun:property>
            <sun:property id="NativeLibraryPathSuffixProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.NativeLibraryPathSuffix}" helpText="$resource{i18nc.jvm.NativeLibraryPathSuffixHelp}">
                <sun:textArea id="NativeLibraryPathSuffix" rows="$int{4}" columns="$int{75}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.NativeLibraryPathSuffix']}" text="#{pageSession.valueMap['nativeLibraryPathSuffix']}"/>
            </sun:property>
            <sun:property  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" >
                <sun:staticText text=" " />
            </sun:property>
        </sun:propertySheetSection>

            
        <sun:propertySheetSection label="">
        <!-- disable the following. refer to issue# 10967
             Woodstock has issue of setting readOnly for textArea, we need to use disable instead
         -->

            <sun:property id=""  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.unsupportedSectionHelp}" >
                <sun:staticText text=""/>
                "<br/>
            </sun:property>

            <sun:property id="EnvironmentClasspathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.EnvironmentClasspath}"  helpText="$resource{i18nc.jvm.EnvironmentClasspathHelp}">
                <sun:checkbox disabled="true" label="$resource{i18n.common.Ignore}" selected="#{pageSession.valueMap['envClasspathIgnored']}" selectedValue="true" />
            </sun:property>

            <sun:property id="ServerClasspathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ServerClasspath}" helpText="$resource{i18nc.jvm.ServerClasspathHelp}">
                <sun:staticText id="ServerClasspath"  text="#{pageSession.valueMap['serverClasspath']}"/>
            </sun:property>
            
            <sun:property id="SystemClasspathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.SystemClasspath}" helpText="$resource{i18nc.jvm.SystemClasspathHelp}">
                <sun:textArea disabled="true"  id="systemClasspath" rows="$int{4}" columns="$int{75}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.SystemClasspath']}" text="#{pageSession.valueMap['systemClasspath']}"/>
            </sun:property>
            <sun:property id="ClasspathPrefixProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ClasspathPrefix}" helpText="$resource{i18nc.jvm.ClasspathPrefixHelp}">
                <sun:textArea disabled="true"  id="ClasspathPrefix" rows="$int{4}" columns="$int{75}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.ClasspathPrefix']}" text="#{pageSession.valueMap['classpathPrefix']}"/>
            </sun:property>
            <sun:property id="ClasspathSuffixProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ClasspathSuffix}" helpText="$resource{i18nc.jvm.ClasspathSuffixHelp}">
                <sun:textArea disabled="true"  id="ClasspathSuffix" rows="$int{4}" columns="$int{75}" maxLength="#{sessionScope.fieldLengths['maxLength.jvm.ClasspathSuffix']}" text="#{pageSession.valueMap['classpathSuffix']}"/>
            </sun:property>
            
        </sun:propertySheetSection>
    </sun:propertySheet>
"<br/><br/>
	<sun:hidden id="helpKey" value="$resource{help_common.serverInstJvmPathSettings}" />
            </sun:form>
</define>
</composition>
