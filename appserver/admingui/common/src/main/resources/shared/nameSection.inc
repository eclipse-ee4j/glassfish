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

    <sun:propertySheet id="nameSheet">
        <!beforeCreate
            setPageSessionAttribute(key="showClusterName" value="#{true}");
            if("#{pageSession.clusterName}=${null}"){
                setPageSessionAttribute(key="showClusterName" value="#{false}");
            }
            setPageSessionAttribute(key="showInstanceName" value="#{true}");
            if("#{pageSession.instanceName}=${null}"){
                setPageSessionAttribute(key="showInstanceName" value="#{false}");
            }
        />
        <!-- Text Field section -->
        <sun:propertySheetSection id="propertSectionTextField">
            <sun:property id="clusterNameProp" rendered="#{pageSession.showClusterName}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.cluster.ClusterName}" >
                <sun:staticText id="clusterName"text="#{pageSession.clusterName}" />
            </sun:property>
            <sun:property id="instanceName" rendered="#{pageSession.showInstanceName}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.instanceName}" >
                <sun:staticText id="instanceName" text="#{pageSession.instanceName}" />
            </sun:property>
        </sun:propertySheetSection>
    </sun:propertySheet>
    "<br/>
