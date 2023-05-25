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

<!-- appserver/instancesTable.inc -->

<sun:table id="instancesTable" title="#{tableTitle}"
    deselectMultipleButton="$boolean{true}"
    deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
    selectMultipleButton="$boolean{true}"
    selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
     <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{tableId});
    />

#include "/common/shared/listTableButtons.inc"

<sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
    <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
    />

    <sun:tableColumn headerText="$resource{i18n.common.SelectHeader}" selectId="select" rowHeader="$boolean{false}" id="col0">
        <sun:checkbox id="select"
            toolTip="$resource{i18n.common.select}"
            selected="#{td.value.selected}"
            selectedValue="$boolean{true}"
            onClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0);"
        />
    </sun:tableColumn>
    <sun:tableColumn headerText="$resource{i18n.common.name.header}" sort="name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" url="#{pageSession.editLink}&instanceName=#{td.value.name}" />
    </sun:tableColumn>
    <sun:tableColumn headerText="$resource{i18ncs.standalone.ColWeight}" sort="lbWeight" rowHeader="$boolean{false}" id="col2">
        <sun:textField id="weight" columns="$int{10}" maxLength="#{sessionScope.fieldLengths['maxLength.standalone.lbWeight']}" text="#{td.value.lbWeight}" />
    </sun:tableColumn>
    <sun:tableColumn headerText="$resource{i18ncs.common.ConfigurationCol}" sort="configRef" rowHeader="$boolean{false}" id="col3">
        <sun:hyperlink id="configlink" text="#{td.value.configRef}" url="/configuration/configuration.jsf?configName=#{td.value.configRef}" />
    </sun:tableColumn>
    <sun:tableColumn headerText="$resource{i18ncs.common.NodeCol}" sort="node" rowHeader="$boolean{false}" id="col5">
        <sun:hyperlink id="nodeAgentlink" text="#{td.value.nodeRef}"  url="#{request.contextPath}/cluster/node/nodeEdit.jsf?nodeName=#{td.value.nodeRef}" />
    </sun:tableColumn>
    <sun:tableColumn headerText="$resource{i18n.common.Status}"  rowHeader="$boolean{false}" id="col6" styleClass="status">
        <staticText id="statusCol" value="#{requestScope.statusString}">
            <!beforeEncode
                mapGet(Map="#{pageSession.instanceStatusMap}" Key="#{td.value.name}" Value="#{requestScope.status}")
                setAttribute(key="statusString" value="$resource{i18nc.status.image.UNKNOWN}  &nbsp; $resource{i18nc.status.UNKNOWN}");
                if ( #{not empty requestScope.status} ) {
                    setAttribute(key="statusString" value="$resource{i18nc.status.image.$attribute{status}}  &nbsp; $resource{i18nc.status.$attribute{status}}");
                }
            />
        </staticText>
        <f:verbatim>
            <span class="instance-name" data-instancename="#{td.value.name}"/>
        </f:verbatim>
    </sun:tableColumn>
</sun:tableRowGroup>

</sun:table>

<sun:script>
    <f:verbatim>
        function fetchStatusesOfInstances(tableIdPrefix) {
            const statusCells = document.querySelectorAll("[id='" + tableIdPrefix + "instancesTable'] tr td.status");
            statusCells.forEach(e => {
                const instName = e.querySelector("span.instance-name").attributes["data-instancename"].value;
                admingui.ajax.get("#{request.contextPath}/shared/instanceStatus.jsf?instanceName=" + instName, "", (req) => {
                e.innerHTML = req.responseText;
                });
            });
        }
    </f:verbatim>

</sun:script>