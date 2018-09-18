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

<!-- shared/jvmOptions_2.jsf -->

#include "/common/shared/alertMsg.inc"
    <sun:title id="propertyContentPage" title="$resource{i18nc.jvmOptions.Title}" helpText="$resource{i18nc.jvmOptions.PageHelp}">
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}" >
                <!command
                    getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                    getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    prepareSuccessfulMsg();
                    saveJvmOptionValues(endpoint="#{pageSession.selfUrl}", target="#{pageSession.configName}", options="$attribute{newList}" , origList="#{pageSession.origList}", profiler="false");
                    gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
    </sun:title>
#include "/common/shared/configNameSection.inc"

#include "/common/javaConfig/jvmOptionsTable.inc"

	<sun:hidden id="helpKey" value="$resource{help_common.serverJvmOptions}" />
         </sun:form>
</define>
</composition>

