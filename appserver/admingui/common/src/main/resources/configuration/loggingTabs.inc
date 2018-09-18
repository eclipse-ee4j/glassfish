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

<!-- common/configuration/loggingTabs.inc -->


<sun:tabSet id="loggingTabs" immediate="true" selected="#{sessionScope.loggingTabs}" >
    <sun:tab id="loggerGeneral" immediate="true" text="$resource{i18nc.log.LoggingGeneralTab}" toolTip="$resource{i18n.common.GeneralTab}">
	<!command
            setSessionAttribute(key="loggingTabs" value="loggerGeneral");
            gf.redirect(page="#{request.contextPath}/common/configuration/loggerGeneral.jsf?configName=#{configName}");
	/>
    </sun:tab>

    <sun:tab id="loggerLevels" immediate="true" text="$resource{i18nc.log.LogLevels}" toolTip="$resource{i18nc.log.LogLevelsTab}">
	<!command
            setSessionAttribute(key="loggingTabs" value="loggerLevels");
            gf.redirect(page="#{request.contextPath}/common/configuration/loggerLevels.jsf?configName=#{configName}");
	/>
    </sun:tab>
</sun:tabSet>
