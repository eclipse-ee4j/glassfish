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

<!facet pageButtonsTop>
<sun:panelGroup id="topButtons">
     <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}" 
        onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            gf.mapValueNullToFalse(map="#{pageSession.valueMap}"  keys={"enabled", "failurefatal" } result="#{requestScope.attrMap}")
            setPageSessionAttribute(key="valueMap" value="#{requestScope.attrMap}");

            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});

            setAttribute(key="tmp" value="#{pageSession.valueMap.classname}");
            gf.createAttributeMap(keys={"name" , "value" } ,values={"class-name", "$attribute{tmp}"} map="#{requestScope.map}");
            listAdd(list="#{requestScope.newList}" value="#{requestScope.map}");

            if (#{pageSession.valueMap.classpath}){
                setAttribute(key="tmp" value="#{pageSession.valueMap.classpath}");
                gf.createAttributeMap( keys={"name" , "value" } , values={"classpath", "$attribute{tmp}"}  map="#{requestScope.map}");
                listAdd(list="#{requestScope.newList}" value="#{requestScope.map}");
            }
            if (#{pageSession.valueMap.loadorder}){
                setAttribute(key="tmp" value="#{pageSession.valueMap.loadorder}");
                gf.createAttributeMap(keys={"name" , "value" } ,values={"load-order","$attribute{tmp}"}  map="#{requestScope.map}");
                listAdd(list="#{requestScope.newList}" value="#{requestScope.map}");
            }

            setAttribute(key="tmp" value="#{pageSession.valueMap.failurefatal}");
            gf.createAttributeMap(keys={"name" , "value" }, values={"is-failure-fatal", "$attribute{tmp}"}  map="#{requestScope.map}");
            listAdd(list="#{requestScope.newList}" value="#{requestScope.map}");

            gf.createAttributeMap(keys={"name" , "value" }, values={ "isLifecycle", "true"}  map="#{requestScope.map}");
            listAdd(list="#{requestScope.newList}" value="#{requestScope.map}");

            javaToJSON(obj="#{requestScope.newList}" json="#{requestScope.tmpJSON}");
            gf.restRequest(endpoint="#{pageSession.selfUrl}/property.json" method="POST" data="#{requestScope.tmpJSON}" result="#{requestScope.restResponse}");

            createMap(result="#{requestScope.aMap}")
            mapPut(map="#{requestScope.aMap}" key="description" value="#{pageSession.valueMap.description}");
            gf.restRequest(endpoint="#{pageSession.selfUrl}" attrs="#{requestScope.aMap}" method="POST" );

            if (#{pageSession.onlyDASExist}){
                createMap(result="#{requestScope.aMap}")
                mapPut(map="#{requestScope.aMap}" key="enabled" value="#{pageSession.valueMap.enabled}");
                gf.restRequest(endpoint="#{pageSession.appRefUrl}" attrs="#{requestScope.aMap}" method="POST" );
            }

            prepareSuccessfulMsg();
            gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
        />
    </sun:button>

    <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
            onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            if (!#{pageSession.onlyDASExist}){
              // mapPut(map="#{pageSession.valueMap}" key="target" value="domain");
            }
            gf.mapValueNullToFalse(map="#{pageSession.valueMap}"  keys={"enabled", "failurefatal" } result="#{requestScope.attrMap}")
            gf.restRequest(
                endpoint="#{sessionScope.REST_URL}/applications/application/create-lifecycle-module"
                attrs="#{requestScope.attrMap}"
                method="POST");
            if (!#{pageSession.onlyDASExist}){
                gf.changeAppTargets(appName="#{pageSession.valueMap['name']}" targets="#{pageSession.selectedTargets}"  status="#{requestScope.attrMap['enabled']}");
            }
            gf.redirect(page="#{pageSession.listPageLink}")
        />
    </sun:button>
    <sun:button id="cancelButton" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
        <!command
            gf.redirect(page="#{listPageLink}")
        />
  </sun:button>
</sun:panelGroup>
</facet>
