<!--

    Copyright (c) 2023 Contributors to the Eclipse Foundation
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

<!-- sslAttrs.inc -->

<sun:propertySheet id="propertySheet">
#include "/common/shared/configNameSection.inc"
<sun:propertySheetSection id="propertySheetSection">
    <!beforeCreate
        getRequestValue(key="configName" value=>$page{configName}  default="server-config")
        gf.restRequest(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/security-service/list-supported-cipher-suites.json"
                       method="GET"
                       result="#{requestScope.result}");
        gf.getMessageProps(messageListProps="#{requestScope.result.data.children}"
                           id="message"
                           keyList="#{requestScope.modList}");
        convertListToArray(list="#{requestScope.modList}" array="#{requestScope.supportedCiphers}");
        convertToDifferentCiphersGroup(ciphers="#{requestScope.supportedCiphers}"
                CommonCiphersList="#{requestScope.availableCommon}"
                EphemeralCiphersList="#{requestScope.availableEphemeral}"
                OtherCiphersList="#{requestScope.availableOther}"
                EccCiphersList="#{requestScope.availableEcc}")
    />
     <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{sheetId});
     />
     
     <sun:property id="SSL3Prop"  visible="#{false}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.ssl3Label}" >
        <sun:checkbox id="SSL3"  selected="#{pageSession.valueMap['ssl3Enabled']}" selectedValue="true" />
     </sun:property>

     <sun:property id="TLSProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.tlsLabel}" >
        <sun:checkbox id="TLS"  selected="#{pageSession.valueMap['tlsEnabled']}" selectedValue="true"/>
     </sun:property>

     <sun:property id="TLS11Prop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.tls11Label}" >
        <sun:checkbox id="TLS11"  selected="#{pageSession.valueMap['tls11Enabled']}" selectedValue="true"/>
     </sun:property>
 
     <sun:property id="TLS12Prop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.tls12Label}" >
        <sun:checkbox id="TLS12"  selected="#{pageSession.valueMap['tls12Enabled']}" selectedValue="true"/>
     </sun:property>

     <sun:property id="TLS13Prop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.tls13Label}" >
        <sun:checkbox id="TLS13"  selected="#{pageSession.valueMap['tls13Enabled']}" selectedValue="true"/>
     </sun:property>

     <sun:property id="ClientAuthProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.clientAuthLabel}" helpText="$resource{i18n.ssl.clientAuthHelp}" >
        <sun:checkbox id="ClientAuth"  selected="#{pageSession.valueMap['clientAuthEnabled']}" selectedValue="true" />
     </sun:property>
     <sun:property id="CertNicknameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.certNicknameLabel}" helpText="$resource{i18n.ssl.certNicknameHelp}" >
        <sun:textField id="CertNickname" styleClass="required" required="#{true}" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.ssl.certNickname']}" text="#{pageSession.valueMap['certNickname']}" />
     </sun:property>
     <sun:property id="keystore"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.keyStore}" helpText="$resource{i18n.ssl.keyStoreHelp}" >
        <sun:textField id="keystore" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.ssl.keyStore']}" text="#{pageSession.valueMap['keyStore']}" />
     </sun:property>
     <sun:property id="trustedAlg"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.TrustAlgorithm}" helpText="$resource{i18n.ssl.TrustAlgorithmHelp}" >
        <sun:textField id="trustedAlg" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.ssl.TrustAlgorithm']}" text="#{pageSession.valueMap['trustAlgorithm']}" />
     </sun:property>
     <sun:property id="maxCertLength"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.trustMaxCert}" helpText="$resource{i18n.ssl.trustMaxCertHelp}" >
        <sun:textField id="maxCertLength" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.ssl.trustMaxCert']}" text="#{pageSession.valueMap['trustMaxCertLength']}" />
     </sun:property>
     <sun:property id="trustStore"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.trustStore}" helpText="$resource{i18n.ssl.trustStoreHelp}" >
        <sun:textField id="trustStore" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.ssl.trustStore']}" text="#{pageSession.valueMap['trustStore']}" />
     </sun:property>


    </sun:propertySheetSection>


    <sun:propertySheetSection label="$resource{i18n.ssl.cipherssuites}"  helpText="$resource{i18n.ssl.ciphersHelp}">
    <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{cipherId});
     />
     <sun:property id="ciphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
       <sun:staticText id="ciphers2" text="$resource{i18n.ssl.ciphersHelp}"/>
     </sun:property>

       <sun:property id="CommonCiphersProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{true}">
            <sun:addRemove id="commonAddRemove"
                selectAll="$boolean{true}"
                items="#{pageSession.commonCiphers}"
                selected="#{pageSession.selectedCommon}"
                availableItemsLabel="$resource{i18n.ssl.availableCommonCiphersLabel}"
                selectedItemsLabel="$resource{i18n.ssl.selectedCommonCiphersLabel}">
            </sun:addRemove>
            <!beforeCreate
                gf.stringArrayToSelectItemArray(stringArray="#{requestScope.availableCommon}" item="#{pageSession.commonCiphers}")
            />
        </sun:property>

        <sun:property id="EphemeralCiphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
            <sun:addRemove id="ephemeralAddRemove"
                selectAll="$boolean{true}"
                items="#{pageSession.ephCiphers}"
                selected="#{pageSession.selectedEphemeral}"
                availableItemsLabel="$resource{i18n.ssl.availableEphCiphersLabel}"
                selectedItemsLabel="$resource{i18n.ssl.selectedEphCiphersLabel}">
            </sun:addRemove>
            <!beforeCreate
                gf.stringArrayToSelectItemArray(stringArray="#{requestScope.availableEphemeral}" item="#{pageSession.ephCiphers}")
            />
         </sun:property>

     <sun:property id="OtherCiphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
        <sun:addRemove id="otherAddRemove"
            selectAll="$boolean{true}"
            items="#{pageSession.other}"
            selected="#{pageSession.selectedOther}"
            availableItemsLabel="$resource{i18n.ssl.availableOtherCiphersLabel}"
            selectedItemsLabel="$resource{i18n.ssl.selectedOtherCiphersLabel}">
        </sun:addRemove>
        <!beforeCreate
                gf.stringArrayToSelectItemArray(stringArray="#{requestScope.availableOther}" item="#{pageSession.other}")
            />
     </sun:property>
     <sun:property id="EccCiphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
        <sun:addRemove id="eccAddRemove"
            selectAll="$boolean{true}"
            items="#{pageSession.eccCiphers}"
            selected="#{pageSession.selectedEcc}"
            availableItemsLabel="$resource{i18n.ssl.availableEccCiphersLabel}"
            selectedItemsLabel="$resource{i18n.ssl.selectedEccCiphersLabel}">
        </sun:addRemove>
        <!beforeCreate
                gf.stringArrayToSelectItemArray(stringArray="#{requestScope.availableEcc}" item="#{pageSession.eccCiphers}")
            />
     </sun:property>
</sun:propertySheetSection>
</sun:propertySheet>
