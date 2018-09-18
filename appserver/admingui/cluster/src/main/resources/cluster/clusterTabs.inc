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

<!-- cluster/clusterTabs.jsf -->

<sun:tabSet id="clusterTabs" immediate="$boolean{true}" selected="#{sessionScope.clusterTabs}">
    <!beforeCreate
        setResourceBundle(key="i18nc" bundle="org.glassfish.common.admingui.Strings");
        setResourceBundle(key="i18ncs" bundle="org.glassfish.cluster.admingui.Strings");
    />
    <sun:tab id="general" immediate="$boolean{true}" text="$resource{i18ncs.cluster.GeneralTab}" >
        <!command
            setSessionAttribute(key="clusterTabs" value="general");
            gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterGeneral.jsf?clusterName=#{pageSession.encodedClusterName}");
        />       
    </sun:tab>

    <sun:tab id="clusterApp" immediate="$boolean{true}" text="$resource{i18ncs.cluster.ApplicationsTab}">
        <!command
            setSessionAttribute(key="clusterTabs" value="clusterApp");
            gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterApplications.jsf?clusterName=#{encodedClusterName}");
        />
    </sun:tab>

    <sun:tab id="clusterInst" immediate="$boolean{true}" text="$resource{i18ncs.cluster.InstancesTab}">
        <!command
            setSessionAttribute(key="clusterTabs" value="clusterInst");
            gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterInstances.jsf?clusterName=#{pageSession.encodedClusterName}");
        />
    </sun:tab>

     <sun:tab id="clusterResources" immediate="$boolean{true}" text="$resource{i18ncs.cluster.ResourcesTab}">
        <!command
            setSessionAttribute(key="clusterTabs" value="clusterResources");
            gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterResources.jsf?clusterName=#{encodedClusterName}");
        />
    </sun:tab>

        <!-- Should be from the jms plugin
    <sun:tab id="clusterPhysicalDest" immediate="$boolean{true}" text="$resource{i18ncs.cluster.PhysicalDestTab}">
        <!command
            setSessionAttribute(key="clusterTabs" value="clusterPhysicalDest");
            redirect(page="../cluster/clusterPhysicalDest.jsf?name=#{encodedClusterName}");
        />
    </sun:tab>
        -->

    <sun:tab id="clusterProps" immediate="$boolean{true}" text="$resource{i18ncs.cluster.PropertiesTab}">
        <!command
            setSessionAttribute(key="clusterTabs" value="clusterSystemProps");
            gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterSystemProperties.jsf?clusterName=#{encodedClusterName}");
        />
        <sun:tab id="clusterSystemProps" immediate="$boolean{true}" text="$resource{i18ncs.cluster.SystemPropertiesTab}">
            <!command
                setSessionAttribute(key="clusterTabs" value="clusterSystemProps");
                gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterSystemProperties.jsf?clusterName=#{encodedClusterName}");
            />
        </sun:tab>
        <sun:tab id="clusterInstanceProps" immediate="$boolean{true}" text="$resource{i18ncs.cluster.ClusterProperties}">
            <!command
                setSessionAttribute(key="clusterTabs" value="clusterInstanceProps");
                gf.redirect(page="#{request.contextPath}/cluster/cluster/clusterProperties.jsf?clusterName=#{encodedClusterName}");
            />
        </sun:tab>    
    </sun:tab>
</sun:tabSet>
<event>
    <!afterCreate
        includeIntegrations(type="org.glassfish.admingui:clusterTab");
    />
</event>
