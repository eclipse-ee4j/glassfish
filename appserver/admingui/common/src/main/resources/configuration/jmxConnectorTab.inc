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

<!-- configuration/jmxConnectorTab.inc -->

<sun:tabSet id="jmxConnectorTab" immediate="true" selected="#{sessionScope.jmxConnectorTab}">
    <sun:tab id="jmxConnectorEdit" immediate="true" text="$resource{i18n.edJmxConnector.jmxConnectorLabel}" toolTip="$resource{i18n.edJmxConnector.jmxConnectorLabelTab}" >
        <!command
            setSessionAttribute(key="jmxConnectorTab" value="jmxConnectorEdit");
            gf.redirect(page="#{request.contextPath}/common/configuration/jmxConnectorEdit.jsf?configName=#{pageSession.configName}");
        /> 
    </sun:tab>

    <sun:tab id="jmxSSLEdit" immediate="true" text="$resource{i18n.edJmxConnector.sslSettingsLabel}" toolTip="$resource{i18n.edJmxConnector.sslSettingsLabelTab}" >
        <!command
            setSessionAttribute(key="jmxConnectorTab" value="jmxSSLEdit");
            gf.redirect(page="#{request.contextPath}/common/configuration/jmxSSLEdit.jsf?configName=#{pageSession.configName}");
        /> 
    </sun:tab>
</sun:tabSet>
