<!--

    Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.

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

<sun:property id="ctxInfoEnabled"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncon.contextInfoLabel}">
            <sun:checkbox id="ctxInfoEnabled"   selected="#{pageSession.valueMap['contextInfoEnabled']}" selectedValue="true"
                 //onChange="javascript:   webui.suntheme.dropDown.setDisabled('#{pageSession.ctxInfoCompId}', !this.checked); "
             />
        </sun:property>
        <sun:property id="contextInfo"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}"  helpText="$resource{i18ncon.contextInfoLabelHelp}">
            ### the beforeEncode is needed for 'loadDefault' since beforeCreate will not be called.
            <event>
                <!beforeEncode
                    getListBoxOptions(
                        availableList={ "Classloader", "JNDI", "Security", "WorkArea" },
                        selectedCommaString="#{pageSession.valueMap['contextInfo']}",
                        availableListResult="#{pageSession.ctxInfoOptions}",
                        selectedOptions="#{pageSession.contextInfo}"
                        addEmptyFirstChoice="false");
                />
            </event>
            <sun:listbox id="contextInfo" immediate="#{true}"  rows="$int{6}" multiple="#{true}"
                labels="#{pageSession.ctxInfoOptions}" selected="#{pageSession.contextInfo}" >
                <!beforeCreate
                    getListBoxOptions(
                        availableList={ "Classloader", "JNDI", "Security", "WorkArea" },
                        selectedCommaString="#{pageSession.valueMap['contextInfo']}",
                        availableListResult="#{pageSession.ctxInfoOptions}",
                        selectedOptions="#{pageSession.contextInfo}"
                        addEmptyFirstChoice="false");
                />
                <!afterCreate
                    getClientId(component="$this{component}" clientId="#{pageSession.ctxInfoCompId}");
                />
            </sun:listbox>
            <sun:staticText id="select" text="$resource{i18ncon.contextInfoSelection}" />
        </sun:property>
