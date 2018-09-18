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

<!-- jvmProfiler_1.inc -->
<!-- when including this page, should followed by including jvmProfiler_2.inc to make a complete page -->
<!initPage
    setResourceBundle(key="i18nc" bundle="org.glassfish.common.admingui.Strings")
    setResourceBundle(key="help_common" bundle="org.glassfish.common.admingui.Helplinks");
/>
<!composition template="/templates/default.layout"  guiTitle="$resource{i18nc.jvm.ProfilerPageName}" >
<!define name="content">
    <event>
    <!beforeCreate
        setSessionAttribute(key="javaConfigTab" value="profiler");
        getRequestValue(key="configName" value=>$page{configName}  );
    
        urlencode(value="#{pageSession.configName}" encoding="UTF-8" result="#{pageSession.encodedConfigName}");
        setPageSessionAttribute(key="parentUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.encodedConfigName}");
        setPageSessionAttribute(key="selfUrl", value="#{pageSession.parentUrl}/java-config");
        setPageSessionAttribute(key="rest-api" value="true");
        setPageSessionAttribute(key="selfPage" value="#{request.contextPath}/javaConfig/serverInstProfiler.jsf");
        setPageSessionAttribute(key="childType" value="profiler");
        setPageSessionAttribute(key="confirmDeleteMsg" value="$resource{i18nc.msg.JS.confirmDeleteProfiler}");
        setPageSessionAttribute(key="profilerUrl", value="#{pageSession.parentUrl}/java-config/profiler");
        setPageSessionAttribute(key="createProfilerUrl", value="#{pageSession.parentUrl}/java-config/create-profiler");
        setPageSessionAttribute(key="deleteProfilerUrl", value="#{pageSession.parentUrl}/java-config/profiler/delete-profiler");
        gf.checkIfEndPointExist(endpoint="#{pageSession.profilerUrl}/jvm-options" exists="#{pageSession.edit}");

        if(#{edit}) {
            gf.getEntityAttrs(endpoint="#{pageSession.parentUrl}/java-config/profiler.json", valueMap="#{pageSession.valueMap}");
            getJvmOptionsValues(endpoint="#{pageSession.profilerUrl}/jvm-options.json"  result=>$attribute{tableList});
            gf.listCombine(list="#{null}" list2="$attribute{tableList}", result="#{pageSession.origList}");
        }
        //there is no way to get the default value of profiler when one doesn't exist.  just hard code 'enabled' to true
        if ("#{edit}=false"){
            createMap(result="#{pageSession.valueMap}");
            mapPut(map="#{pageSession.valueMap}" key="enabled" value="true");
        }
        setPageSessionAttribute(key="onlyUseAttrs" value={"name", "target", "classpath", "enabled","nativeLibraryPath"})
        setPageSessionAttribute(key="convertToFalseList" value={"enabled"});
    />
    </event>
            <sun:form id="propertyForm">
