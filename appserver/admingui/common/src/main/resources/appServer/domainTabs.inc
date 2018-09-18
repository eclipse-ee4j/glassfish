<!--

    Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!-- appServer/domainTabs.jsf -->

<sun:tabSet id="domainTabs" immediate="true" selected="#{sessionScope.domainTabs}">
    <sun:tab id="domainAttrs" immediate="true" text="$resource{i18n.headings.DomainAttributes}" toolTip="$resource{i18n.headings.DomainAttributesTab}">
        <!command
            setSessionAttribute(key="domainTabs" value="domainAttrs");
            gf.redirect(page="#{request.contextPath}/common/appServer/serverInstDomainAttrs.jsf");
        />
    </sun:tab>
    <sun:tab id="appConfig" immediate="true" text="$resource{i18n.headings.AppConfig}" toolTip="$resource{i18n.headings.AppConfigTab}" >
        <!command
            setSessionAttribute(key="domainTabs" value="appConfig");
            gf.redirect(page="#{request.contextPath}/common/appServer/serverInstAppsConfig.jsf");
        />
    </sun:tab>
    <sun:tab id="adminPassword" immediate="true" text="$resource{i18n.headings.AdminPassword}" toolTip="$resource{i18n.headings.AdminPasswordTab}" >
        <!command
            setSessionAttribute(key="domainTabs" value="adminPassword");
            gf.redirect(page="#{request.contextPath}/common/appServer/serverInstAdminPassword.jsf");
        />
    </sun:tab>
    <sun:tab id="pswdAliases" immediate="true" text="$resource{i18nc.pswdAlias.tab}" toolTip="$resource{i18nc.pswdAlias.tabTip}" >
    <!command
        setSessionAttribute(key="domainTabs" value="pswdAliases");
        gf.redirect(page="#{request.contextPath}/common/appServer/pswdAliases.jsf");
    />
    </sun:tab>
    <sun:tab id="domainLogs" immediate="true" text="$resource{i18nc.domainLogs.tab}" toolTip="$resource{i18nc.domainLogs.tabTip}" >
    <!command
        setSessionAttribute(key="domainTabs" value="domainLogs");
        gf.redirect(page="#{request.contextPath}/common/appServer/domainLogs.jsf");
    />
    </sun:tab>
</sun:tabSet>
<event>
    <!afterCreate
        includeIntegrations(type="org.glassfish.admingui:domainTab");
    />
</event>

