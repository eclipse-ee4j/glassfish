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

<!-- httpSaveButtons.inc -->

#include "/common/shared/editPageLoadDefaultButton.inc"
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command

                    setAttribute(key="emptyStr" value="");
                    if("#{pageSession.skipAttrsList}=#{null}") {
                        createList(size="0", result="#{pageSession.skipAttrsList}");
                    }
                    if ("#{pageSession.valueMap['redirectPort']} = #{emptyStr}") {
                        listAdd(list="#{pageSession.skipAttrsList}" value="redirectPort");
                    }
                    if ("#{pageSession.valueMap['noCompressionUserAgents']} = #{emptyStr}") {
                        listAdd(list="#{pageSession.skipAttrsList}" value="noCompressionUserAgents");
                    }
                    gf.createEntity(endpoint="#{pageSession.selfUrl}"
                                    attrs="#{pageSession.valueMap}"
                                    skipAttrs="#{pageSession.skipAttrsList}"
                                    convertToFalse="#{pageSession.convertToFalseList}"
                                    onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                    );
                    setPageSessionAttribute(key="protocolHttpUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/network-config/protocols/protocol/#{pageSession.Name}/http");
                    setPageSessionAttribute(key="networkListenerUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/network-config/network-listeners/network-listener");
                    setPageSessionAttribute(key="virtualServerUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/http-service/virtual-server");
                    gf.getChildrenNamesList(endpoint="#{pageSession.networkListenerUrl}",
                                            id="name",
                                            result="#{pageSession.networkListeners}");
                    gf.restRequest(endpoint="#{pageSession.parentUrl}.json",
                                   method="get",
                                   result="#{pageSession.protocolAttr}");
                    //gf.checkIfEndPointExist(endpoint="#{pageSession.protocolHttpUrl}",
                    //exists="#{pageSession.protocolHttpExist}");
                    if (#{pageSession.httpEndPointExist}) {
                        gf.restRequest(endpoint="#{pageSession.protocolHttpUrl}.json",
                                       method="get",
                                       result="#{pageSession.protocolAttrHttp}");
                        if ("!(#{pageSession.protocolAttrHttp.data.extraProperties.entity['defaultVirtualServer']}=#{pageSession.previousVSName})") {
                            foreach (var="listener" list="#{pageSession.networkListeners}") {
                                gf.restRequest(endpoint="#{pageSession.networkListenerUrl}/#{listener}.json",
                                               method="get",
                                               result="#{pageSession.networkListAttrs}");
                                if ("#{pageSession.networkListAttrs.data.extraProperties.entity['protocol']}=#{pageSession.protocolAttr.data.extraProperties.entity['name']}") {
                                    gf.restRequest(endpoint="#{pageSession.virtualServerUrl}/#{pageSession.previousVSName}.json",
                                                   method="get",
                                                   result="#{pageSession.vsAttrs}");
                                    setPageSessionAttribute(key="additionalSaveHandlerArgs" 
                                                            value="vsAttrs:%23{pageSession.vsAttrs.data.extraProperties.entity},
                                                                   listenerName:%23{pageSession.networkListAttrs.data.extraProperties.entity['name']},
                                                                   addFlag:{%23false},
                                                                   result:%23{pageSession.vsAttrModified}");
                                    if (#{pageSession.additionalSaveHandler}){
                                        gf.invokeHandler(handler="#{pageSession.additionalSaveHandler}", args="#{pageSession.additionalSaveHandlerArgs}" );
                                    }
                                    gf.restRequest(endpoint="#{pageSession.virtualServerUrl}/#{pageSession.previousVSName}",
                                                   attrs="#{pageSession.vsAttrModified}",
                                                   method="post");
                                    // Add the Network Listener in the current VS
                                    gf.restRequest(endpoint="#{pageSession.virtualServerUrl}/#{pageSession.protocolAttrHttp.data.extraProperties.entity['defaultVirtualServer']}.json",
                                                   method="get",
                                                   result="#{pageSession.vsAttrs}");
                                    setPageSessionAttribute(key="additionalSaveHandlerArgs"
                                                            value="vsAttrs:%23{pageSession.vsAttrs.data.extraProperties.entity},
                                                            listenerName:%23{pageSession.networkListAttrs.data.extraProperties.entity['name']},
                                                            addFlag:{%23false},
                                                            result:%23{pageSession.vsAttrModified}");
                                    if (#{pageSession.additionalSaveHandler}){
                                        gf.invokeHandler(handler="#{pageSession.additionalSaveHandler}", args="#{pageSession.additionalSaveHandlerArgs}" );
                                    }
                                    gf.restRequest(endpoint="#{pageSession.virtualServerUrl}/#{pageSession.protocolAttrHttp.data.extraProperties.entity['defaultVirtualServer']}",
                                                   attrs="#{pageSession.vsAttrModified}",
                                                   method="post");
                                }
                            }
                        }
                    }
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />

                prepareSuccessfulMsg();
                gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>

            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}?configName=#{configName}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
