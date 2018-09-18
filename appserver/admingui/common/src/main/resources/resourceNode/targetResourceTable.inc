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

<!-- resourceNode/instanceResourceTable.inc -->

<sun:table id="resourcesTable" title="$resource{i18nc.resources}"
        deselectMultipleButton="$boolean{true}"
        deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
        selectMultipleButton="$boolean{true}"
        selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />

#include "/common/resourceNode/targetResourceTableButtons.inc"
        <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
            <!beforeCreate
                //To get the resources table for a target
                setAttribute(key="listOfRows" value={});
                setPageSessionAttribute(key="resTypes" value={"jdbc-resource", "mail-resource", "custom-resource",
                       "external-jndi-resource", "connector-resource", "admin-object-resource",
                       "context-service", "managed-thread-factory", "managed-executor-service", "managed-scheduled-executor-service"});
                setPageSessionAttribute(key="resLabels" value={
                        "$resource{i18n.tree.jdbcResources}",
                        "$resource{i18n.tree.javaMailSessions}",
                        "$resource{i18n.tree.customResources}",
                        "$resource{i18n.tree.externalResources}",
                        "$resource{i18n.tree.connectorResources}",
                        "$resource{i18n.tree.adminObjectResources}",
                        "$resource{i18n.tree.ContextServices}",
                        "$resource{i18n.tree.ManagedThreadFactories}",
                        "$resource{i18n.tree.ManagedExecutorServices}",
                        "$resource{i18n.tree.ManagedScheduledExecutorServices}"
                        });
                setPageSessionAttribute(key="editResUrls" value={
                        "/jdbc/jdbcResourceEdit.jsf",
                        "/full/mailResourceEdit.jsf",
                        "/full/customResourceEdit.jsf",
                        "/full/externalResourceEdit.jsf",
                        "/jca/connectorResourceEdit.jsf",
                        "/jca/adminObjectEdit.jsf",
                        "/concurrent/contextServiceEdit.jsf",
                        "/concurrent/managedThreadFactoryEdit.jsf",
                        "/concurrent/managedExecutorServiceEdit.jsf",
                        "/concurrent/managedScheduledExecutorServiceEdit.jsf"
                        });
                gf.createAttributeMap(keys="$pageSession{resTypes}" values="$pageSession{resLabels}" map="#{requestScope.typeMap}");
                gf.createAttributeMap(keys="$pageSession{resTypes}" values="$pageSession{editResUrls}" map="#{requestScope.editMap}");
                gf.getChildrenNamesList(endpoint="#{pageSession.restURL}" result="#{requestScope.resourceNames}");
                foreach (var="resName" list="#{requestScope.resourceNames}") {
                    urlencode(value="#{requestScope.resName}" encoding="UTF-8" result="#{requestScope.encodedResName}");
                    foreach (var="resType" list="#{pageSession.resTypes}") {
                        //To verify the given resource is of resType or not.
                        gf.checkIfEndPointExist(endpoint="#{sessionScope.REST_URL}/resources/#{requestScope.resType}/#{requestScope.encodedResName}", exists="#{requestScope.isResourceLinkExists}");
                        if (#{requestScope.isResourceLinkExists}) {
                            if ("!#{pageSession.filterValue} | (#{pageSession.filterValue}=#{requestScope.resType})") {
                                gf.restRequest(
                                    endpoint="#{pageSession.restURL}/#{requestScope.encodedResName}"
                                    method="GET"
                                    result=>$page{resourceRefAttrs});
                                if ("#{resourceRefAttrs.data.extraProperties.entity.enabled}=true") {
                                    setAttribute(key="status" value="/resource/images/enabled.png");
                                }
                                if ("#{resourceRefAttrs.data.extraProperties.entity.enabled}=false") {
                                    setAttribute(key="status" value="/resource/images/disabled.png");
                                }
                                mapGet(Map="#{requestScope.typeMap}" Key="#{requestScope.resType}" Value=>$attribute{type});
                                mapGet(Map="#{requestScope.editMap}" Key="#{requestScope.resType}" Value=>$attribute{link});
                                gf.createAttributeMap(keys={"Enabled", "selected", "Name", "link", "Type"}
                                    values={"$attribute{status}",
                                            "$boolean{false}",
                                            "$attribute{resName}",
                                            "$attribute{link}?name=$attribute{resName}",
                                            "$attribute{type}"}
                                    map="#{requestScope.oneRowMap}");
                                listAdd(list="$attribute{listOfRows}" value="#{requestScope.oneRowMap}");
                            }
                        }
                    }
                }
            />
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
            <sun:tableColumn headerText="$resource{i18n.common.ResourceName}" rowHeader="$boolean{true}" id="col1">
                <sun:hyperlink id="link" text="#{td.value.Name}" url="#{td.value.link}" />
            </sun:tableColumn>
            <sun:tableColumn headerText="$resource{i18n.common.Enabled}" rowHeader="$boolean{false}" id="col2">
                <sun:image id="enablecol" value="#{td.value.Enabled}" />
            </sun:tableColumn>
            <sun:tableColumn headerText="$resource{i18n.common.type.header}" rowHeader="$boolean{false}" id="col3">
                <sun:staticText escape="true" id="type" value="#{td.value.Type}" />
            </sun:tableColumn>
       </sun:tableRowGroup>
    </sun:table>
