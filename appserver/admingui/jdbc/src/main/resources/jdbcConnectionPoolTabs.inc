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

<!-- jdbc/jdbcConnectionPoolTabs.inc -->

<sun:tabSet id="jdbcPoolSet" immediate="$boolean{true}" selected="#{sessionScope.jdbcPoolTabs}">
        <sun:tab id="general" immediate="$boolean{true}" text="$resource{i18n.common.General}" toolTip="$resource{i18n.common.General} Tab" >
                <!command
                    setSessionAttribute(key="jdbcPoolTabs" value="general");
                    gf.redirect(page="#{request.contextPath}/jdbc/jdbcConnectionPoolEdit.jsf?name=#{pageSession.encodedName}#{pageSession.selfPageArgs}");
                />       
        </sun:tab>
        <sun:tab id="advanceTab" immediate="$boolean{true}" text="$resource{i18n.common.Advanced}" toolTip="$resource{i18n.common.Advanced} Tab">
            <!command
                setSessionAttribute(key="jdbcPoolTabs" value="advanceTab");
                gf.redirect(page="#{request.contextPath}/jdbc/jdbcConnectionPoolAdvance.jsf?name=#{pageSession.encodedName}#{pageSession.selfPageArgs}");
            />
        </sun:tab>
        <sun:tab id="propertyTab" immediate="$boolean{true}" text="$resource{i18n.common.AdditonalProperties}" toolTip="$resource{i18n.common.AdditonalProperties} Tab">
            <!command
                setSessionAttribute(key="jdbcPoolTabs" value="propertyTab");
                gf.redirect(page="#{request.contextPath}/jdbc/jdbcConnectionPoolProperty.jsf?name=#{pageSession.encodedName}#{pageSession.selfPageArgs}");
            />
        </sun:tab>
</sun:tabSet>
