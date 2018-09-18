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

<!-- jvmPath_1.inc -->
<!-- when including this page, should followed by including jvmPath_2.inc to make a complete page -->
<!initPage
    setResourceBundle(key="i18nc" bundle="org.glassfish.common.admingui.Strings")
    setResourceBundle(key="help_common" bundle="org.glassfish.common.admingui.Helplinks");
/>
/>

<!composition template="/templates/default.layout"  guiTitle="$resource{i18nc.jvm.PathSettingsTitle}" >
<!define name="content">
    <event>
        <!beforeCreate
            setSessionAttribute(key="javaConfigTab" value="pathSettings");
            getRequestValue(key="configName" value=>$page{configName} default="server-config");
            setPageSessionAttribute(key="parentUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}");
            setPageSessionAttribute(key="selfUrl", value="#{pageSession.parentUrl}/java-config");
            setPageSessionAttribute(key="rest-api" value="true");
            gf.getEntityAttrs(endpoint="#{pageSession.selfUrl}.json", valueMap="#{pageSession.valueMap}");

            formatStringsforDisplay(string="#{pageSession.valueMap['serverClasspath']}"  formattedString="#{requestScope.scp}");
            mapPut(map="#{pageSession.valueMap}" key="serverClasspath" value="$attribute{scp}");
            formatStringsforDisplay(string="#{pageSession.valueMap['systemClasspath']}"  formattedString="#{requestScope.syscp}");
            mapPut(map="#{pageSession.valueMap}" key="systemClasspath" value="$attribute{syscp}");
            formatStringsforDisplay(string="#{pageSession.valueMap['classpathPrefix']}"  formattedString="#{requestScope.cpp}");
            mapPut(map="#{pageSession.valueMap}" key="classpathPrefix" value="$attribute{cpp}");
            formatStringsforDisplay(string="#{pageSession.valueMap['classpathSuffix']}"  formattedString="#{requestScope.cps}");
            mapPut(map="#{pageSession.valueMap}" key="classpathSuffix" value="$attribute{cps}");

            formatStringsforDisplay(string="#{pageSession.valueMap['nativeLibraryPathPrefix']}"  formattedString="#{requestScope.npp}");
            mapPut(map="#{pageSession.valueMap}" key="nativeLibraryPathPrefix" value="$attribute{npp}");
            formatStringsforDisplay(string="#{pageSession.valueMap['nativeLibraryPathSuffix']}"  formattedString="#{requestScope.nps}");
            mapPut(map="#{pageSession.valueMap}" key="nativeLibraryPathSuffix" value="$attribute{nps}");

            setPageSessionAttribute(key="convertToFalseList" value={"envClasspathIgnored"});
            setPageSessionAttribute(key="selfPage" value="#{request.contextPath}/javaConfig/serverInstJvmPathSettings.jsf?name=#{pageSession.Name}&configName=#{pageSession.configName}");
            setPageSessionAttribute(key="skipAttrsList", 
                    value={"envClasspathIgnored" "serverClasspath" "systemClasspath" "classpathPrefix" "classpathSuffix"
                        "rmicOptions" "javacOptions" "javaHome" "debugOptions" "debugEnabled" "bytecodePreprocessors"});
        />   
    </event> 
            <sun:form id="propertyForm">
