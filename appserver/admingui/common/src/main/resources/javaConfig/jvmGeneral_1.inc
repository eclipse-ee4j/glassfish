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

<!-- jvmGeneral_1.inc -->
<!-- when including this page, should followed by including jvmGeneral_2.inc to make a complete page -->

<!initPage
    setResourceBundle(key="i18nc" bundle="org.glassfish.common.admingui.Strings")
    setResourceBundle(key="help_common" bundle="org.glassfish.common.admingui.Helplinks");
/>

<!composition template="/templates/default.layout"  guiTitle="$resource{i18nc.jvm.GeneralTitle}" >
<!define name="content">
    <event>
    <!beforeCreate
            # for EE, the following session attribute will just be ignored.
            setSessionAttribute(key="eeJvmSettingsTab" value="eeJvmGeneral");
            setSessionAttribute(key="javaConfigTab" value="jvmGeneral");
            getRequestValue(key="configName" value=>$page{configName} default="server-config");
            setPageSessionAttribute(key="parentUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}");
            setPageSessionAttribute(key="selfUrl", value="#{pageSession.parentUrl}/java-config");
            setPageSessionAttribute(key="rest-api" value="true");
            gf.getEntityAttrs(endpoint="#{pageSession.selfUrl}.json", valueMap="#{pageSession.valueMap}");
            
            setPageSessionAttribute(key="convertToFalseList" value={"debugEnabled"});
            setPageSessionAttribute(key="childType" value="java-config");
            gf.restRequest(endpoint="#{pageSession.selfUrl}/property.json" method="GET" result="#{requestScope.propTable}");
            setPageSessionAttribute(key="tableList" value="#{requestScope.propTable.data.extraProperties.properties}");
            setPageSessionAttribute(key="selfPage" value="#{request.contextPath}/javaConfig/serverInstJvmGeneral.jsf?configName=#{pageSession.configName}");
            setPageSessionAttribute(key="skipAttrsList", value={"envClasspathIgnored" "serverClasspath" "systemClasspath" "classpathPrefix" "classpathSuffix" "nativeLibraryPathPrefix" "nativeLibraryPathSuffix"});
             //set the following for including buttons.inc
            setPageSessionAttribute(key="edit" value="#{true}" );
            setPageSessionAttribute(key="showDefaultButton" value="#{true}" );
            setPageSessionAttribute(key="showCancelButton" value="#{false}" );
            setPageSessionAttribute(key="hasPropertySheet" value="#{true}" );
             setPageSessionAttribute(key="hasPropertyTable" value="#{true}" );
    />
    </event>
            <sun:form id="propertyForm">
