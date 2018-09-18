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

<!-- resourceNode/resourceEditTabs.inc -->

<sun:tabSet id="resEditTabs" rendered="$pageSession{showTargetSection}" immediate="$boolean{true}" selected="#{sessionScope.resEditTabs}">
    <!beforeCreate
        //To set the showTargetSection        
#include "/common/shared/targetsList.inc"
        setPageSessionAttribute(key="rest-api" value="true");
        if(#{pageSession.isAppScopedRes}){
            setPageSessionAttribute(key="showTargetSection" value="#{false}");
            setPageSessionAttribute(key="useString" value="#{false}");
            setPageSessionAttribute(key="useCheckBox" value="#{false}");
            setPageSessionAttribute(key="has2Objects" value="#{false}");
            setPageSessionAttribute(key="showDefaultButton" value="#{false}" );
        }
        if(!#{pageSession.isAppScopedRes}){
            if(#{pageSession.showTargetSection}) {
                setPageSessionAttribute(key="useString" value="#{true}");
                setPageSessionAttribute(key="useCheckBox" value="#{false}");
                setPageSessionAttribute(key="has2Objects" value="#{false}");
                setPageSessionAttribute(key="showDefaultButton" value="#{true}" );
            }
            if(!#{pageSession.showTargetSection}) {
                setPageSessionAttribute(key="useString" value="#{false}");
                setPageSessionAttribute(key="useCheckBox" value="#{true}");
                setPageSessionAttribute(key="parentUrl2", value="#{sessionScope.REST_URL}/servers/server/server");
                setPageSessionAttribute(key="childType2", value="resource-ref");
                if ("#{pageSession.resChildType} = connector-resource") {
                    setPageSessionAttribute(key="resourceRefUrl", value="#{pageSession.parentUrl2}/#{pageSession.childType2}/#{pageSession.encodedName}");
                    gf.checkIfEndPointExist(endpoint="#{pageSession.resourceRefUrl}" exists="#{requestScope.hasAssociatedTarget}");
                    if (#{hasAssociatedTarget}) {
                        gf.getEntityAttrs(endpoint="#{pageSession.resourceRefUrl}",  valueMap="#{pageSession.resourceRef}");
                        // if the resource itself is set to disabled, the real status should be disabled.
                        if( "#{pageSession.valueMap.enabled} = false"){
                            mapPut(map="#{pageSession.resourceRef}", key="enabled", value="#{pageSession.valueMap.enabled}");
                        }
                    }
                    if (!#{hasAssociatedTarget}) {
                        createMap(result="#{pageSession.resourceRef}");
                        gf.getEntityAttrs(endpoint="#{pageSession.parentUrl}/#{pageSession.resChildType}/#{pageSession.encodedName}",  valueMap="#{requestScope.resValueMap}");
                        mapPut(map="#{pageSession.resourceRef}", key="enabled", value="#{resValueMap.enabled}");
                    }
                }
                if ("!(#{pageSession.resChildType} = connector-resource)") {
                    setPageSessionAttribute(key="has2Objects" value="#{true}");
                    setPageSessionAttribute(key="showDefaultButton" value="#{true}" );
                    setPageSessionAttribute(key="selfUrl2", value="#{pageSession.parentUrl2}/#{pageSession.childType2}/#{pageSession.encodedName}");
                    gf.checkIfEndPointExist(endpoint="#{pageSession.selfUrl2}" exists="#{requestScope.hasAssociatedTarget}");
                    if (#{hasAssociatedTarget}) {
                        gf.getEntityAttrs(endpoint="#{pageSession.selfUrl2}", valueMap="#{pageSession.valueMap2}");
                        // if the resource itself is set to disabled, the real status should show disabled.
                        if( "#{pageSession.valueMap.enabled} = false"){
                            mapPut(map="#{pageSession.valueMap2}", key="enabled", value="#{pageSession.valueMap.enabled}");
                        }
                    }
                    if (!#{hasAssociatedTarget}) {
                        createMap(result="#{pageSession.valueMap2}");
                        mapPut(map="#{pageSession.valueMap2}", key="enabled", value="#{pageSession.valueMap.enabled}");
                    }
                    if(#{reload}) {
                        mapPut(map="#{pageSession.valueMap2}", key="enabled", value="#{status}");
                    }
                }
            }
        }
    />
    <sun:tab id="general" immediate="$boolean{true}" text="$resource{i18n.common.General}" >
        <!command
            setSessionAttribute(key="resEditTabs" value="general");
            gf.redirect(page="#{generalPage}");
        />
    </sun:tab>
    <sun:tab id="targetTab" immediate="$boolean{true}" text="$resource{i18n.common.TargetTab}" >
        <!command
            setSessionAttribute(key="resEditTabs" value="targetTab");
            gf.redirect(page="#{targetPage}&generalPage=#{generalPage}");
        />
    </sun:tab>
</sun:tabSet>
