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

<!-- cluster/clusterInstanceTabs.inc -->

<sun:tabSet id="clusterInstanceTabs" immediate="$boolean{true}" selected="#{sessionScope.clusterInstanceTabs}">

    <sun:tab id="general" immediate="$boolean{true}" text="$resource{i18nc.GeneralTab}" >
        <!command
            setSessionAttribute(key="clusterInstanceTabs" value="general");
            gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterInstanceEdit.jsf?clusterName=#{pageSession.encodedClusterName}&instanceName=#{pageSession.encodedInstanceName}");
        />       
    </sun:tab>

    <sun:tab id="clusterProp" immediate="$boolean{true}" text="$resource{i18nc.PropertiesTab}">
        <!command
            setSessionAttribute(key="clusterInstanceTabs" value="configProps");
            gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterInstanceConfigProperties.jsf?clusterName=#{pageSession.encodedClusterName}&instanceName=#{pageSession.encodedInstanceName}");
        />
        <sun:tab id="configProps" immediate="$boolean{true}" text="$resource{i18ncs.cluster.SystemPropertiesTab}" >
            <!command
                setSessionAttribute(key="clusterInstanceTabs" value="configProps");
                gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterInstanceConfigProperties.jsf?clusterName=#{pageSession.encodedClusterName}&instanceName=#{pageSession.encodedInstanceName}");
            />
	</sun:tab>
        <sun:tab id="instanceProps" immediate="$boolean{true}" text="$resource{i18n.inst.tab.InstanceProperties}" >
	    <!command
                setSessionAttribute(key="clusterInstanceTabs" value="instanceProps");
                gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterInstanceProperties.jsf?clusterName=#{pageSession.encodedClusterName}&instanceName=#{pageSession.encodedInstanceName}");
	    />
	</sun:tab>
    </sun:tab>
</sun:tabSet>
<event>
    <!afterCreate
	includeIntegrations(type="org.glassfish.admingui:clusterInstTab");
    />
</event>
