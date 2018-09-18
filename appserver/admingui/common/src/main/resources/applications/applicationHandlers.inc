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

<!-- applications/applicationHandlers.inc -->

<!-- Input : snifferType     - required
     Input : isFullName
     Output: snifferTypeApplicationsList
-->
<handler id="gfr.getApplicationsBySnifferType">
    setPageSessionAttribute(key="snifferTypeApplicationsList" value={});
    if ("#{pageSession.isFullName}=#{null}") {
        setPageSessionAttribute(key="isFullName" value="#{true}");
    }
    setAttribute(key="applicationsURL" value="#{sessionScope.REST_URL}/applications/application");
    setAttribute(key="sniffersHideList" value={"security"});
    gf.getChildrenNamesList(endpoint="#{applicationsURL}" result="#{requestScope.applicationsList}");
    foreach (var="app" list="#{applicationsList}") {
        urlencode(value="#{app}" encoding="UTF-8" result="#{requestScope.encodedAppName}");
        setAttribute(key="moduleURL" value="#{applicationsURL}/#{encodedAppName}/module");
        gf.getChildrenNamesList(endpoint="#{moduleURL}" result="#{requestScope.modulesList}");
        foreach (var="module" list="#{modulesList}") {
            urlencode(value="#{module}" encoding="UTF-8" result="#{requestScope.encodedModuleName}");
            gf.getChildrenNamesList(endpoint="#{moduleURL}/#{encodedModuleName}/engine" result="#{requestScope.sniffersList}");
            gf.containedIn(list="#{requestScope.sniffersList}" testStr="#{pageSession.snifferType}" contain="#{requestScope.isSnifferType}");
            gf.containedIn(list="#{sniffersHideList}" testStr="#{pageSession.snifferType}" contain="#{requestScope.isSnifferHideType}");
            if(!#{requestScope.isSnifferHideType}&#{requestScope.isSnifferType}) {
                if(#{pageSession.isFullName}) {
                    gf.checkIfEndPointExist(endpoint="#{applicationsURL}/#{encodedAppName}/engine/ear" exists="#{requestScope.isEarSniffer}");
                    if(!#{isEarSniffer}) {
                        listAdd(list="#{snifferTypeApplicationsList}" value="#{app}");
                    }
                    if(#{isEarSniffer}) {
                        listAdd(list="#{snifferTypeApplicationsList}" value="#{app}##{module}");
                    }
                }
                if(!#{pageSession.isFullName}) {
                    listAdd(list="#{snifferTypeApplicationsList}" value="#{app}");
                }
            }
        }
    }
</handler>

<handler id="gfr.listAppScopedResources">
    mapPut(map="#{pageSession.argsMap}" key="appname" value="#{pageSession.encodedAppName}");
    gf.restRequest(endpoint="#{sessionScope.REST_URL}/_list-resources"
                    attrs="#{pageSession.argsMap}"
                    method="get"
                    throwException="$boolean{false}"
                    result="#{requestScope.appScopedResourcesTable}");
    if("#{requestScope.appScopedResourcesTable.data.properties}") {
        gf.addToAppScopedResourcesTable(appName="#{pageSession.appName}"
            moduleName="#{pageSession.moduleName}"
            resources="#{requestScope.appScopedResourcesTable.data.properties}"
            listOfRows="#{pageSession.listOfRows}"
            result="#{pageSession.listOfRows}");
    }
</handler>

<handler id="gfr.getAppScopedResourcesTable">
    //Application scoped Resources
    createMap(result="#{pageSession.argsMap}");
    gfr.listAppScopedResources();
    //Modules list of an application
    createMap(result="#{pageSession.argsMap}");
    //mapPut(map="#{pageSession.argsMap}" key="id" value="#{pageSession.encodedAppName}");
    //mapPut(map="#{pageSession.argsMap}" key="resources" value="true");
    gf.restRequest(endpoint="#{sessionScope.REST_URL}/applications/application/#{pageSession.encodedAppName}/list-sub-components"
                   attrs="#{pageSession.argsMap}"
                   method="get"
                   result="#{requestScope.result}");
    //Module scoped Resources
    if("#{requestScope.result.data.properties}") {
        gf.getMapKeys(Map="#{requestScope.result.data.properties}" Keys=>$attribute{moduleKeys});
        foreach(var="module" list="#{moduleKeys}") {
            setPageSessionAttribute(key="moduleName" value="#{module}");
            urlencode(value="#{module}" encoding="UTF-8" result="#{encodedModuleName}");
            createMap(result="#{pageSession.argsMap}");
            mapPut(map="#{pageSession.argsMap}" key="modulename" value="#{encodedModuleName}");
            gfr.listAppScopedResources();
        }
    }   
    if(!#{pageSession.listOfRows}) {
        setPageSessionAttribute(key="appScopedResLength" value="$int{0}");
    }
    if(#{pageSession.listOfRows}) {
        setPageSessionAttribute(key="appScopedResLength" value="#{pageSession.listOfRows.size()}");
    }
    setPageSessionAttribute(key="appScopedResExists" value="$boolean{false}");
    if(#{pageSession.appScopedResLength} > $int{0}) {
        setPageSessionAttribute(key="appScopedResExists" value="$boolean{true}");
    }
</handler>
