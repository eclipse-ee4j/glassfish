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

<sun:property  rendered="#{pageSession.gmsShowIt}">
    <sun:hyperlink  rendered="#{pageSession.gmsShowIt}"
        toolTip="$resource{i18ncs.tree.gms.tooltip}"
        url="/cluster/configuration/gms.jsf?configName=#{pageSession.configName}"
        >
        
        <sun:image url="/resource/cluster/images/management_services.gif" />
        <sun:staticText text="$resource{i18ncs.tree.gms}" />
    </sun:hyperlink>
    <!beforeCreate
            setResourceBundle(key="i18ncs" bundle="org.glassfish.cluster.admingui.Strings");
            setPageSessionAttribute(key="gmsShowIt" value="$boolean{false}");
            if ("!(server-config=#{pageSession.configName})") {
                setPageSessionAttribute(key="gmsShowIt" value="$boolean{true}");
            }
    />
</sun:property>
