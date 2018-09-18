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

<!-- appServer/serverInstTabs.jsf -->

<sun:tabSet id="serverInstTabs" immediate="true" selected="#{sessionScope.serverInstTabs}">
    <sun:tab id="general" immediate="true" text="$resource{i18n.common.General}" toolTip="$resource{i18n.common.GeneralTab}" >
        <!command
            setSessionAttribute(key="serverInstTabs" value="general");
            gf.redirect(page="#{request.contextPath}/common/appServer/serverInstGeneralPe.jsf");
        />
    </sun:tab>
    <sun:tab id="resources" immediate="true" text="$resource{i18nc.resources}" toolTip="$resource{i18nc.resourcesTab}">
        <!command
            setSessionAttribute(key="serverInstTabs" value="resources");
            gf.redirect(page="#{request.contextPath}/common/appServer/serverInstResources.jsf?instanceName=server");
        />
    </sun:tab>

    <sun:tab id="serverInstProps" immediate="$boolean{true}" text="$resource{i18nc.PropertiesTab}">
        <!command
            setSessionAttribute(key="serverInstTabs" value="configProps");
            gf.redirect(page="#{request.contextPath}/common/appServer/instanceConfigProperties.jsf?instanceName=server");
        />
        <sun:tab id="configProps" immediate="$boolean{true}" text="$resource{i18nc.instanceProperties.SystemPropertiesTitle}" >
            <!command
                setSessionAttribute(key="serverInstTabs" value="configProps");
                gf.redirect(page="#{request.contextPath}/common/appServer/instanceConfigProperties.jsf?instanceName=server");
            />
	</sun:tab>
        <sun:tab id="instanceProps" immediate="$boolean{true}" text="$resource{i18nc.instanceProperties.PropertiesTitle}" >
	    <!command
                setSessionAttribute(key="serverInsTabs" value="instanceProps");
                gf.redirect(page="#{request.contextPath}/common/appServer/instanceProperties.jsf?instanceName=server");
	    />
	</sun:tab>
    </sun:tab>

</sun:tabSet>
<event>
    <!afterCreate
        includeIntegrations(type="org.glassfish.admingui:serverInstTab");
    />
</event>
