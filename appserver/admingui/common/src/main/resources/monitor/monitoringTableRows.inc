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

<!-- web/monitor/monitoringTableRows.inc -->
<!-- used in web/monitor/monitoringStatsPage.jsf -->
        <sun:tableColumn headerText="$resource{i18n.common.PropertyName}" rowHeader="$boolean{false}" id="col2">
            <staticText id="col1Name" value="#{td.value.name}" />
        </sun:tableColumn>
        <sun:tableColumn headerText="$resource{i18n.common.PropertyValue}" rowHeader="$boolean{false}" id="col3">
            <staticText  id="col2Value" value="#{td.value.value}" />
        </sun:tableColumn>
        <sun:tableColumn headerText="$resource{i18nc.monitoring.startTimeCol}" nowrap="$boolean{true}" rowHeader="$boolean{false}" id="col4">
            <staticText  id="col3Value" value="#{td.value.startTime}" />
        </sun:tableColumn>
        <sun:tableColumn headerText="$resource{i18nc.monitoring.lastTimeCol}" nowrap="$boolean{true}" rowHeader="$boolean{false}" id="col5">
            <staticText  id="col4Value" value="#{td.value.lastTime}" />
        </sun:tableColumn>
        <sun:tableColumn headerText="$resource{i18nc.monitoring.DetailsCol}" rowHeader="$boolean{false}" id="col6">
            <staticText  id="col5Value" value="#{td.value.details}" />
        </sun:tableColumn>
        <sun:tableColumn headerText="$resource{i18nc.monitoring.DescCol}" nowrap="$boolean{true}" rowHeader="$boolean{false}" id="col7">
            <staticText  id="col5Value" value="#{td.value.description}" />
        </sun:tableColumn>
