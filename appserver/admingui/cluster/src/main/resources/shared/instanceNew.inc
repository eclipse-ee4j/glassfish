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

<sun:form id="propertyForm">
#include "/cluster/shared/handlers.inc"
#include "/common/shared/alertMsg.inc"
    <sun:title id="propertyContentPage" title="#{pageSession.pageTitle}" helpText="#{pageSession.pageTitleHelp}">
        <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="newButton" text="$resource{i18n.button.OK}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                setAttribute(key="nameToCheck" value="#{pageSession.name}");
                gfj.checkNameExist();
                createMap(result="#{requestScope.attrs}")
                mapPut(map="#{requestScope.attrs}" key="name"  value="#{pageSession.name}" )
                mapPut(map="#{requestScope.attrs}" key="node"  value="#{pageSession.node}" )
                if (#{forStandalone}){
                    if ('#{configOptionGrp}=copy'){
                        if ("! (#{pageSession.config}=default-config)"){
                            setAttribute(key="nameToCheck" value="#{pageSession.name}-config");
                            gfj.checkNameExist();
                            createMap(result="#{requestScope.copyAttrs}")
                            mapPut(map="#{requestScope.copyAttrs}" key="id"  value={"$pageSession{config}" , "$pageSession{name}-config" } );
                            gf.restRequest(endpoint="#{sessionScope.REST_URL}/configs/copy-config"  attrs="#{requestScope.copyAttrs}" );
                            mapPut(map="#{requestScope.attrs}" key="config"  value="#{pageSession.name}-config" )
                        }
                    }
                    if ("!(#{configOptionGrp}=copy)"){
                        mapPut(map="#{requestScope.attrs}" key="config"  value="#{pageSession.config}" )
                    }
                }
                if (!#{forStandalone}){
                    mapPut(map="#{requestScope.attrs}" key="cluster"  value="#{pageSession.clusterName}" )
                }
                gf.restRequest(endpoint="#{sessionScope.REST_URL}/create-instance"  attrs="#{requestScope.attrs}" method="post"  result="#{requestScope.result}");
                gf.redirect(page="#{pageSession.listLink}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
             />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}" >
                <!command
                    gf.redirect(page="#{pageSession.listLink}");
                />
            </sun:button>

        </sun:panelGroup>
        </facet>
       
    </sun:title>

    <sun:propertySheet id="propertySheet">

        <sun:propertySheetSection id="propertSectionTextField">

            <sun:property id="clusterNameProp" rendered="#{!forStandalone}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.cluster.ClusterName}" >
                <sun:staticText id="clusterName" text="#{encodedClusterName}" />
            </sun:property>

            <sun:property id="NameTextProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.instanceName}" >
                <sun:textField id="NameText" text="#{pageSession.name}" styleClass="required" columns="$int{55}" maxLength="#{sessionScope.fieldLengths['maxLength.cluster.instanceName']}" required="#{true}" />
            </sun:property>
            <sun:property id="node"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.clusterNew.Node}" helpText="$resource{i18ncs.clusterNew.NodeHelp}">
                <sun:dropDown id="node" labels="#{pageSession.nodesList}" selected="#{pageSession.node}" >
                </sun:dropDown>
            </sun:property>
            <sun:property id="configProp"  rendered="#{pageSession.forStandalone}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.clusterNew.Configuration}" helpText="$resource{i18ncs.clusterNew.ConfigurationHelp}">
                <sun:dropDown id="Config" labels="#{pageSession.configsList}" selected="#{pageSession.config}" >
                </sun:dropDown>
            </sun:property>

            <sun:property id="configOptionProp" rendered="#{pageSession.forStandalone}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}">
                <sun:radioButton id="optC"  name="configOptionGrp" label="$resource{i18ncs.clusterNew.CopyConfig}" selected="#{configOption}" selectedValue="copy" />
                "<br />
                <sun:radioButton id="optR"  name="configOptionGrp" label="$resource{i18ncs.clusterNew.ReferenceConfig}" selected="#{configOption}" selectedValue="ref" />
                "<br />
            </sun:property>
        </sun:propertySheetSection>
    </sun:propertySheet>

    <sun:hidden id="helpKey" value="#{pageSession.helpId}" />
</sun:form>
