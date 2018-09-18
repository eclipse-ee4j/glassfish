<!--

    Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!-- applications/appGeneralTabs.inc -->

<sun:tabSet id="appGeneralTabs" immediate="true" selected="#{sessionScope.appGeneralTabs}">
    <sun:tab id="general" immediate="true" text="$resource{i18n.common.General}" toolTip="$resource{i18n.common.GeneralTab}" >
        <!command
            setSessionAttribute(key="appGeneralTabs" value="general");
            gf.redirect(page="#{request.contextPath}/common/applications/applicationEdit.jsf?appName=#{pageSession.encodedAppName}&listPageLink=#{pageSession.listPageLink}");
        />       
    </sun:tab>
    
    <sun:tab id="descriptorTab" immediate="true" text="$resource{i18n.common.DescriptorTab}" toolTip="$resource{i18n.common.DescriptorTab} Tab">
        <!command
            setSessionAttribute(key="appGeneralTabs" value="descriptorTab");
            gf.redirect(page="#{request.contextPath}/common/applications/webDDEdit.jsf?appName=#{pageSession.encodedAppName}&listPageLink=#{pageSession.listPageLink}");
        />
    </sun:tab>

    <sun:tab id="resourcesTab" rendered="#{pageSession.appScopedResExists}" immediate="true" text="$resource{i18n.common.Resources}" toolTip="$resource{i18n.common.Resources} Tab">
        <!command
            setSessionAttribute(key="appGeneralTabs" value="resourcesTab");
            gf.redirect(page="#{request.contextPath}/common/applications/resources/appScopedResources.jsf?appName=#{pageSession.encodedAppName}&listPageLink=/common/applications/applications.jsf");
        />
    </sun:tab>

     <sun:tab id="targetTab" rendered="#{!pageSession.onlyDASExist}" immediate="true" text="$resource{i18n.common.TargetTab}" toolTip="$resource{i18n.common.TargetTab} Tab">
        <!command
            setSessionAttribute(key="appGeneralTabs" value="targetTab");
            gf.redirect(page="#{request.contextPath}/common/applications/targetListing.jsf?appName=#{pageSession.encodedAppName}&listPageLink=#{pageSession.listPageLink}");
        />
    </sun:tab>
</sun:tabSet>
