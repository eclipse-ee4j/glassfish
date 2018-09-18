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

<!-- sslButtons.inc -->
   
<!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                 onClick="if (checkSSLRequiredElements() && guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                convertCiphersItemsToStr(common="#{pageSession.selectedCommon}" ephemeral="#{pageSession.selectedEphemeral}" other="#{pageSession.selectedOther}" ecc="#{pageSession.selectedEcc}" ciphers="#{requestScope.ciphers}")
                mapPut(map="#{pageSession.valueMap}" key="ssl3TlsCiphers" value="#{requestScope.ciphers}")
                gf.createEntity(endpoint="#{pageSession.selfUrl}"
                            attrs="#{pageSession.valueMap}"
                            skipAttrs="#{pageSession.skipAttrsList}"
                            convertToFalse="#{pageSession.convertToFalseList}"
                );
                prepareSuccessfulMsg();
                gf.checkIfEndPointExist(endpoint="#{pageSession.selfUrl}" exists="#{requestScope.exist}")

                gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
        </sun:button>

        <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                    onClick="if (checkSSLRequiredElements()  && guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') ) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                convertCiphersItemsToStr(common="#{pageSession.selectedCommon}" ephemeral="#{pageSession.selectedEphemeral}" other="#{pageSession.selectedOther}" ecc="#{pageSession.selectedEcc}" ciphers="#{requestScope.ciphers}")
                mapPut(map="#{pageSession.valueMap}" key="ssl3TlsCiphers" value="#{requestScope.ciphers}")

                <!-- since create-ssl doesn't take all the options, we create it first just with cert name, and then edit it with all the attributes -->
                mapPut(map="#{pageSession.createMap}", key="certNickname",  value="#{pageSession.valueMap['certNickname']}")
                gf.createEntity(endpoint="#{pageSession.createSslUrl}" attrs="#{pageSession.createMap}" skipAttrs="#{pageSession.skipAttrsList}" convertToFalse="#{pageSession.convertToFalseList}");
                gf.createEntity(endpoint="#{pageSession.selfUrl}" attrs="#{pageSession.valueMap}" skipAttrs="#{pageSession.skipAttrsList}" convertToFalse="#{pageSession.convertToFalseList}");
                prepareSuccessfulMsg();
                gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            />
        </sun:button>
        <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
            <!command
                gf.redirect(page="#{parentPage}?configName=#{configName}");
            />
        </sun:button>
    </sun:panelGroup>
</facet>
