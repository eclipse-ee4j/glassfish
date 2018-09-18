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

<!-- grizzly/protocolNewButtons.jsf -->

<!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                urlencode(value="#{pageSession.valueMap['Name']}" result="#{requestScope.encodedProtocolToCheck}");
                gf.checkIfEndPointExist(endpoint="#{pageSession.selfUrl}/#{requestScope.encodedProtocolToCheck}", exists="#{requestScope.pExist}");
                if (#{requestScope.pExist}){
                    gf.handleError(detail="$resource{i18n_web.grizzly.protocol.alreadyExist, #{requestScope.encodedProtocolToCheck}}");
                }
                gf.createEntity(endpoint="#{pageSession.selfUrl}"
                        attrs="#{pageSession.valueMap}"
                        convertToFalse={"securityEnabled"},
                        result => $attribute{newProxy});

                setAttribute(key="emptyStr" value="");
                if("#{pageSession.skipAttrsList}=#{null}") {
                    createList(size="0", result="#{pageSession.skipAttrsList}");
                }
                if ("#{pageSession.httpMap['redirectPort']} = #{emptyStr}") {
                    listAdd(list="#{pageSession.skipAttrsList}" value="redirectPort");
                }
                if ("#{pageSession.httpMap['redirectPort']} = #{null}") {
                    listAdd(list="#{pageSession.skipAttrsList}" value="redirectPort");
                }
                if ("#{pageSession.httpMap['noCompressionUserAgents']} = #{null}") {
                    listAdd(list="#{pageSession.skipAttrsList}" value="noCompressionUserAgents");
                }
                mapPut(map="#{pageSession.httpMap}" key="target"  value="#{pageSession.valueMap['target']}")
                gf.createEntity(endpoint="${newProxy}/#{pageSession.valueMap['Name']}/create-http"
                        attrs="#{pageSession.httpMap}"
                        onlyUseAttrs={"target", "defaultVirtualServer"});
                listAdd(list="#{pageSession.skipAttrsList}" value="target");
                gf.createEntity(endpoint="${newProxy}/#{pageSession.valueMap['Name']}/http"
                        attrs="#{pageSession.httpMap}"
                        convertToFalse={"uploadTimeoutEnabled", "cometSupportEnabled", "dnsLookupEnabled", "rcmSupportEnabled", "traceEnabled", "authPassThroughEnabled", 
                                "chunkingEnabled",  "encodedSlashEnabled", "websocketsSupport", "xpoweredBy"},
                        skipAttrs="#{pageSession.skipAttrsList}"
                        result => $attribute{newHttp});

                gf.createEntity(endpoint="${newHttp}/#{pageSession.childType3}"
                        attrs="#{pageSession.fileMap}"
                        convertToFalse={"enabled"},
                );

                gf.redirect(page="#{parentPage}?configName=#{configName}")
            />
        </sun:button>
        <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Cancel}" >
            <!command
                gf.redirect(page="#{parentPage}?configName=#{configName}");
            />
        </sun:button>
    </sun:panelGroup>
</facet>
