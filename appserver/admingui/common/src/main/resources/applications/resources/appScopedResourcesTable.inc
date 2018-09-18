<!--

    Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!-- applications/resources/appScopedResourcesTable.inc -->

#include "/applications/applicationHandlers.inc"
<sun:table id="appScopedResources"  title="$resource{i18n.appScopedResourcesTable.tableTitle}" style="padding: 10pt; width:95%" >
    <!beforeCreate
       gfr.getAppScopedResourcesTable();
    />
     <sun:tableRowGroup id="rowGroup1"  data={"$pageSession{listOfRows}"} sourceVar="td">

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colApp}" sort="appName" rowHeader="$boolean{false}" id="colAppName">
            <staticText id="name" value="#{td.value.appName}" />
        </sun:tableColumn>
         <sun:tableColumn headerText="$resource{i18n.ComponentTable.colModule}" sort="moduleName" rowHeader="$boolean{true}" id="colModule">
            <staticText id="nameCol" value="#{td.value.moduleName}" />
        </sun:tableColumn>
        <sun:tableColumn headerText="$resource{i18n.appScopedResourcesTable.colResourceName}" sort="resName" rowHeader="$boolean{true}" id="resName">
            <sun:hyperlink id="resNameCol" text="#{td.value.resName}" url="#{request.contextPath}/#{td.value.link}&isAppScopedRes=#{true}" />
           </sun:tableColumn>
        <sun:tableColumn headerText="Resource Type" sort="resType" rowHeader="$boolean{true}" id="colResType">
            <staticText id="resTypeCol" value="#{td.value.resType}" />
        </sun:tableColumn>

    </sun:tableRowGroup>
</sun:table>
"<br /><br />
