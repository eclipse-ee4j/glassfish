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

#include "/common/shared/alertMsg_1.inc"
    <sun:title id="propertyContentPage" title="$resource{i18nf.batch.configurationTitle}" helpText="$resource{i18nf.batch.configurationTitleHelp}" >
    <!facet pageButtonsTop>
            <sun:panelGroup id="topButtons">
                <sun:button id="saveButton"  text="$resource{i18n.button.Save}"
                        onClick="submitAndDisable(this, '$resource{i18n.button.Processing}'); return false;" >
                    <!command
                        prepareSuccessfulMsg();
                        gf.restRequest(endpoint="#{sessionScope.REST_URL}/set-batch-runtime-configuration" method="POST" attrs="#{pageSession.valueMap}");
                    />
                </sun:button>
            </sun:panelGroup>
        </facet>
    </sun:title>
#include "/common/shared/nameSection.inc"
            <sun:propertySheetSection >
                <sun:property id="executorServiceLookupName"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nf.batch.configuration.executorServiceLookupName}" helpText="$resource{i18nf.batch.configuration.executorServiceLookupNameHelp}">
                    <sun:dropDown id="executorServiceLookupName" selected="#{pageSession.valueMap['executorServiceLookupName']}" labels="$pageSession{mesList}"  values="$pageSession{mesList}"  >
                        <!beforeCreate
                            gf.getChildrenNameFromListCmd(endpoint="#{sessionScope.REST_URL}/resources/list-managed-executor-services", attrs="#{pageSession.attrsMap}"  result="#{pageSession.mesList}");
                        />
                    </sun:dropDown>
                </sun:property>

                <sun:property id="poolNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nf.batch.configuration.dataSourceLookupName}"  helpText="$resource{i18nf.batch.configuration.dataSourceLookupNameHelp}">
                    <sun:dropDown id="PoolName" selected="#{pageSession.valueMap['dataSourceLookupName']}" labels="$pageSession{jdbcList}"  values="$pageSession{jdbcList}" >
                    <!beforeCreate
                        gf.getChildrenNameFromListCmd(endpoint="#{sessionScope.REST_URL}/resources/list-jdbc-resources", attrs="#{pageSession.attrsMap}" result="#{pageSession.jdbcList}");
                    />
                   </sun:dropDown>
               </sun:property>
            </sun:propertySheetSection>

    <sun:hidden id="helpKey" value="$resource{help_full.batchConfiguration}" />
</sun:form>
</define>
</composition>
