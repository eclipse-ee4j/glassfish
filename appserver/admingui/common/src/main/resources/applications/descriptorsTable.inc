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

<!-- applications/descriptorsTable.inc -->

<!-- Descriptos Table .... -->
               
<sun:table id="basicTable" style="padding: 10pt" deselectMultipleButton="$boolean{false}" selectMultipleButton="$boolean{false}" title="$resource{i18n.deploy.descriptors}">

    <sun:tableRowGroup id="rowGroup1" data={"$attribute{descriptors}"} sourceVar="td">      
        <sun:tableColumn headerText="$resource{i18n.deploy.ApplicationName}" rowHeader="$boolean{false}" id="col1">
            <staticText id="name" value="#{pageSession.appName}" />
        </sun:tableColumn>
        <sun:tableColumn  headerText="$resource{i18n.deploy.subCompName}" rowHeader="$boolean{false}" id="col2">
            <staticText id="name" value="#{td.value.moduleName}" />
        </sun:tableColumn>
        <sun:tableColumn headerText="$resource{i18n.deploy.descriptorFileName}" rowHeader="$boolean{false}" id="col3">
            <sun:hyperlink id="link" text="#{td.value.ddPath}" >
                <!command
                    //setSessionAttribute(key="descriptorForDisplay" value="#{td.value.ddContent}")
                    urlencode(value="#{td.value.moduleName}" encoding="UTF-8" result="#{pageSession.encodedModName}");
                    urlencode(value="#{td.value.ddPath}" encoding="UTF-8" result="#{pageSession.encodedDdPath}");
                    gf.redirect(page="#{request.contextPath}/common/applications/viewDD.jsf?appName=#{pageSession.encodedAppName}&moduleName=#{pageSession.encodedModName}&ddPath=#{pageSession.encodedDdPath}");
                />
            </sun:hyperlink>
        </sun:tableColumn>
    </sun:tableRowGroup>
</sun:table>    
