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

<!-- Request Policy section -->               
<sun:propertySheetSection id="requestPolicySection" label="$resource{i18nc.msgSecProvider.requestPolicyLabel}">

    <sun:property id="AuthSourceProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecProvider.authSourceLabel}" helpText="$resource{i18nc.msgSecProvider.authSourceRequestHelp}">
        <sun:dropDown id="AuthSource" selected="#{pageSession.valueMap['Request-AuthSource']}" labels={"", "sender", "content"}/>
    </sun:property>
    <sun:property id="AuthRecipientProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecProvider.authRecepientLabel}" helpText="$resource{i18nc.msgSecProvider.authRecepientHelp}">
        <sun:dropDown id="AuthRecipient" selected="#{pageSession.valueMap['Request-AuthRecipient']}" labels={"", "before-content", "after-content"} />
    </sun:property>
</sun:propertySheetSection> 

                           
<!-- Response Policy section -->               
<sun:propertySheetSection id="responsePolicySection" label="$resource{i18nc.msgSecProvider.responsePolicyLabel}">
    <sun:property id="AuthSourceProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecProvider.authSourceLabel}" helpText="$resource{i18nc.msgSecProvider.authSourceResponseHelp}">
        <sun:dropDown id="AuthSource" selected="#{pageSession.valueMap['Response-AuthSource']}" labels={"", "sender", "content"} />
    </sun:property>
    <sun:property id="AuthRecipientProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.msgSecProvider.authRecepientLabel}" helpText="$resource{i18nc.msgSecProvider.authRecepientHelp}">
        <sun:dropDown id="AuthRecipient" selected="#{pageSession.valueMap['Response-AuthRecipient']}" labels={"", "before-content", "after-content"} />
    </sun:property>
</sun:propertySheetSection>                            

