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

<sun:tabSet id="lcGeneralTabs" immediate="true" selected="#{sessionScope.lcGeneralTabs}">
    <sun:tab id="general" immediate="true" text="$resource{i18n.common.General}" toolTip="$resource{i18n.common.GeneralTab}" >
        <!command
            setSessionAttribute(key="lcGeneralTabs" value="general");
            gf.redirect(page="#{request.contextPath}/common/applications/lifecycleEdit.jsf?appName=#{pageSession.encodedName}&listPageLink=#{pageSession.listPageLink}");
        />       
    </sun:tab>
    
     <sun:tab id="targetTab" rendered="#{!pageSession.onlyDASExist}" immediate="true" text="$resource{i18n.common.TargetTab}" toolTip="$resource{i18n.common.TargetTab} Tab">
        <!command
            setSessionAttribute(key="lcGeneralTabs" value="targetTab");
            gf.redirect(page="#{request.contextPath}/common/applications/lifecycleTargetListing.jsf?appName=#{pageSession.encodedName}&listPageLink=#{pageSession.listPageLink}");
        />
    </sun:tab>
</sun:tabSet>
