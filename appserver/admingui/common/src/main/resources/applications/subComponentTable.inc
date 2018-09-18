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

<!--
setAttribute(key="sectionTitle", value="#{listOfSection[0].sectionTitle}");
setAttribute(key="sectionDetail", value="#{listOfSection[0].sectionDetail}");
println("++++++ #{requestScope.listOfSection}")
println("sectionTitle =   $attribute{sectionTitle}");
println("sectionDetail =  $attribute{sectionDetail}");
-->

<!-- applications/subComponentTable.inc -->

<sun:table id="subcomponents"  title="$resource{i18n.ComponentTable.tableTitle}" style="padding: 10pt; width:95%" >
    <!beforeCreate
        gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/applications/application/#{pageSession.encodedAppName}/module" result="#{pageSession.moduleList}");
        gf.getSubComponents(appName="#{pageSession.appName}"  moduleList="#{pageSession.moduleList}" result="#{requestScope.listOfRows}");
        />
     <sun:tableRowGroup id="rowGroup1"  data={"$attribute{listOfRows}"} sourceVar="td">
        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colModule}" sort="moduleName" rowHeader="$boolean{true}" id="colModule">
            <staticText id="nameCol" value="#{td.value.moduleName}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.deployTable.ColEngines}"  sort="sniffers"  rowHeader="$boolean{true}" id="colsnif">
            <staticText id="sniffer" value="#{td.value.sniffers}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colName}" sort="name" rowHeader="$boolean{true}" id="col1">
            <staticText id="nameCol" value="#{td.value.name}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colType}" sort="type" rowHeader="$boolean{true}" id="col2">
            <staticText id="typeCol" value="#{td.value.type}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colAction}"  embeddedActions="#{true}" rowHeader="$boolean{true}" id="col3">
            <sun:hyperlink id="endpointLink" rendered="#{td.value.hasEndpoint}" text="$resource{i18n.ComponentTable.viewEndPoint}"
                url="#{request.contextPath}/common/applications/endpoint.jsf?appName=#{appName}&moduleName=#{td.value.moduleName}&compName=#{td.value.name}" />

            <sun:hyperlink id="link" rendered="#{td.value.hasLaunch}" text="$resource{i18n.deployTable.launch}"
                onClick="var win=window.open('#{request.contextPath}/common/applications/webApplicationLinks.jsf?appID=#{appName}&contextRoot=#{td.value.contextRoot}','ViewerWindow','width='+.75*screen.width+',height='+.75*screen.height+',top='+.1*screen.height+',left='+.1*screen.width+',toolbar=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,directories=yes,location=yes');win.focus(); return false;" />

            <sun:hyperlink id="appClientLink" rendered="#{td.value.hasAppClientLaunch}" text="$resource{i18n.deployTable.launch}"
                url="#{request.contextPath}/common/applications/appclientLaunch.jsf?appName=#{appName}&moduleName=#{td.value.moduleName}"/>
            />
            <sun:hyperlink id="downloadStubs" rendered="#{td.value.hasAppClientStub}" text="$resource{i18n.deployTable.downloadStubs}"
                onClick="window.location = '#{request.contextPath}/download/client-stubs.zip?contentSourceId=ClientStubs&appName=#{appName}&restUrl=#{sessionScope.REST_URL}'; return false; "
                           />
        </sun:tableColumn>

    </sun:tableRowGroup>
</sun:table>
"<br /><br />
