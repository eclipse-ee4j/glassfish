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

<!-- grizzly/networkListenerAttr.inc -->


<sun:propertySheet id="propertySheet">
#include "/common/shared/configNameSection.inc"
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">

        <sun:property id="nameNew"   labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.common.name}" helpText="$resource{i18n_web.common.nameHelp}">
            <sun:textField id="name"  required="#{true}" columns="$int{50}" maxLength="#{sessionScope.fieldLengths['maxLength.common.Name']}" text="#{pageSession.valueMap['name']}"
                onChange="setProtocolName()" >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{listenerId});
                />
            </sun:textField>
        </sun:property>


    <sun:property id="prop1" labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.protocol}">
    <sun:radioButton id="rdBtn" name="rdBtn" selected="#{pageSession.rdBtn}" selectedValue="create" onClick="radioBtnAction('create');setProtocolName();"  >
      <!beforeCreate
            setPageSessionAttribute(key="rdBtn", value="create");
      />
   </sun:radioButton>
   <sun:staticText text="$resource{i18n_web.grizzly.networkListener.createNewProtocol}" />
   <sun:textField id="pName" required="#{false}" text="#{pageSession.valueMap['newProtocolName']}" submitForm="false" style="margin-left: 4pt" columns="$int{50}" maxLength="#{sessionScope.fieldLengths['maxLength.grizzly.networkListener.newProtocolName']}"  >
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{pNameId});
        />
    </sun:textField>

        "<br/><br/>
        <sun:staticText text="$resource{i18n_web.http.defVirtualServerLabel}" style="margin-left: 12pt" />
        <sun:dropDown id="vs" selected="#{pageSession.valueMap['defaultVirtualServer']}" labels="$pageSession{vsList}"  values="$pageSession{vsList}" style="margin-left: 4pt" >
        <!beforeCreate
            gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/http-service/virtual-server" result="#{pageSession.vsList}");
            mapPut(map="#{pageSession.valueMap}" key="defaultVirtualServer"  value="server")
        />
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{newVsId});
        />
       </sun:dropDown>

   "<br/><br/><br/>

    <sun:radioButton id="existingRdBtn" name="rdBtn"
        selected="#{pageSession.rdBtn}"  selectedValue="existing"  onClick="radioBtnAction('existing')"/>

        <sun:staticText text="$resource{i18n_web.grizzly.networkListener.useExistingProtocol}" />
        <sun:dropDown id="protocoldw" selected="#{pageSession.valueMap['existingProtocolName']}"  disabled="true" style="margin-left: 4pt" labels="$pageSession{protocolListLabel}" >
            <!beforeCreate
                gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/network-config/protocols/protocol" result="#{pageSession.protocolListLabel}");
            />
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{PdropDownId});
            />
        </sun:dropDown>

        <sun:textField  id="protocolChoice" visible="$boolean{false}" text="#{pageSession.valueMap['protocolChoice']}">
            <!beforeCreate
                mapPut(map="#{pageSession.valueMap}" value="create"  key="protocolChoice")
            />
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{protocolChoiceId});
            />
        </sun:textField>
        "<br/><br/><br/>
        </sun:property>

        <sun:property id="port"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.port}" helpText="$resource{i18n_web.grizzly.networkListener.portHelp}">
            <sun:textField id="port" columns="$int{40}" maxLength="#{sessionScope.fieldLengths['maxLength.grizzly.networkListener.port']}" required="#{true}" styleClass="required port" text="#{pageSession.valueMap['port']}"  />
        </sun:property>

        <sun:property id="addr"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.address}" helpText="$resource{i18n_web.grizzly.networkListener.addressHelp}">
            <sun:textField id="addr" columns="$int{40}" maxLength="#{sessionScope.fieldLengths['maxLength.grizzly.networkListener.address']}" text="#{pageSession.valueMap['address']}"  />
        </sun:property>

        <sun:property id="status"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.common.status}"  helpText="$resource{i18n_web.grizzly.networkListener.statusHelp}" >
            <sun:checkbox  selected="#{pageSession.valueMap['enabled']}" selectedValue="true" />
        </sun:property>

        <sun:property id="security"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.security}" helpText="$resource{i18n_web.grizzly.networkListener.securityHelp}" >
            <sun:checkbox  selected="#{pageSession.valueMap['securityEnabled']}" selectedValue="true" />
        </sun:property>

        <sun:property id="JkEnabled"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.JkEnabled}" helpText="$resource{i18n_web.grizzly.networkListener.JkEnabledHelp}">
            <sun:checkbox  selected="#{pageSession.valueMap['jkenabled']}" selectedValue="true" />
        </sun:property>

        <sun:property id="threadpool"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.threadPool}" helpText="$resource{i18n_web.grizzly.networkListener.threadPoolHelp}">
            <sun:dropDown id="threadpool" selected="#{pageSession.valueMap['threadpool']}" labels="$pageSession{threadPoolList}"  values="$pageSession{threadPoolList}" >
            <!beforeCreate
                gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/thread-pools/thread-pool" result="#{pageSession.threadPoolList}");
            />
           </sun:dropDown>
        </sun:property>

        <sun:property id="transport"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.transport}" helpText="$resource{i18n_web.grizzly.networkListener.transportHelp}">
            <sun:dropDown id="transport" selected="#{pageSession.valueMap['transport']}" labels="$pageSession{transportList}"  values="$pageSession{transportList}"  >
            <!beforeCreate
                gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/network-config/transports/transport" result="#{pageSession.transportList}");
            />
           </sun:dropDown>
        </sun:property>

<!--
        <sun:property id="protocol"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.networkListener.protocol}" helpText="$resource{i18n_web.grizzly.networkListener.protocolHelp}">
            <sun:dropDown id="protocolChoice" selected="#{pageSession.valueMap['protocol']}"
                labels="$pageSession{protocolListLabel}"  values="$pageSession{protocolListValue}"
                onChange="javascript: disableProtocolNameField();" >
            <!beforeCreate
                gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/network-config/protocols/protocol" result="#{pageSession.protocolListLabel}");
                gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/network-config/protocols/protocol" result="#{pageSession.protocolListValue}");
                listAdd(list="#{pageSession.protocolListLabel}" value="$resource{i18n_web.grizzly.createNewProtocol}" index="0")
                listAdd(list="#{pageSession.protocolListValue}" value="__create__" index="0")
            />
            <!afterCreate
                getClientId(component="$this{component}" clientId="#{pageSession.protocolChoice}");
            />
           </sun:dropDown>
        </sun:property>

        <sun:property id="protocolNm"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18_web.grizzly.protocol.protocolName}" helpText="$resource{i18n_web.grizzly.protocol.protocolNameHelp}">
            <sun:textField id="protocolNm"  columns="$int{55}" text="#{pageSession.valueMap['newProtocolName']}" >
            <!afterCreate
                getClientId(component="$this{component}" clientId="#{pageSession.protocolNmField}");
            />
            </sun:textField>
        </sun:property>
-->

    </sun:propertySheetSection>
</sun:propertySheet>
