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

<!-- configuration/webContainerTabs.jsf -->

<sun:tabSet id="webContainerTabs" immediate="$boolean{true}" selected="#{sessionScope.webContainerTabs}">

<!-- comment out till we convert the general and Manager page -->
    <sun:tab id="general" immediate="$boolean{true}" text="$resource{i18n.common.General}" toolTip="$resource{i18n.common.General} Tab" >
        <!command
        setSessionAttribute(key="webContainerTabs" value="general");
        gf.redirect(page="#{request.contextPath}/web/configuration/webContainerGeneral.jsf?configName=#{configName}");
        />       
    </sun:tab>
    <sun:tab id="sessionTab" immediate="$boolean{true}" text="$resource{i18n_web.webContainer.sessionProp}" toolTip="$resource{i18n_web.webContainer.sessionProp} Tab" >
        <!command
        setSessionAttribute(key="webContainerTabs" value="sessionTab");
        gf.redirect(page="#{request.contextPath}/web/configuration/webContainerSession.jsf?configName=#{configName}");
        />
    </sun:tab>
    <sun:tab id="managerTab" immediate="$boolean{true}" text="$resource{i18n_web.webContainer.managerProp}" toolTip="$resource{i18n_web.webContainer.managerProp} Tab" >
        <!command
        setSessionAttribute(key="webContainerTabs" value="managerTab");
        gf.redirect(page="#{request.contextPath}/web/configuration/webContainerManager.jsf?configName=#{configName}");
        />
    </sun:tab>
    <sun:tab id="storeTab" immediate="$boolean{true}" text="$resource{i18n_web.webContainer.storeProp}" toolTip="$resource{i18n_web.webContainer.storeProp} Tab" >
        <!command
        setSessionAttribute(key="webContainerTabs" value="storeTab");
        gf.redirect(page="#{request.contextPath}/web/configuration/webContainerStore.jsf?configName=#{configName}");
        />
    </sun:tab>    
    
</sun:tabSet>
