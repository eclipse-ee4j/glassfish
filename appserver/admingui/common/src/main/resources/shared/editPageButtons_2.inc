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

<!-- editPageButtons_2.inc -->

    <!-- To use this general include files for page buttons,  the following page Session attributes should be set:
        setPageSessionAttribute(key="edit" value="#{false}" );
        setPageSessionAttribute(key="showDefaultButton" value="#{false}" );  //true is default
        setPageSessionAttribute(key="showCancelButton" value="#{false}" );   // true is default
        setPageSessionAttribute(key="hasPropertySheet" value="#{true}" );    //false is default
        setPageSessionAttribute(key="hasPropertyTable" value="#{true}" );   //false is default
        
        setPageSessionAttribute(key="has2Objects" value="#{true}" );        //false is default
        For creating new objects, if #{pageSession.parentObjectNameStr2} is specified, it will be used as the parent for creating the 2nd config.
        otherwise, it means the 2nd config is the sub-element, and the newly created object will be used as the parent to create
        the 2nd object.

    -->

    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    <!--
                    // no page defines hasRadioButtons yet, so we commen this out.
                    // when need to use this, we have to ensure that we don't refer to #{param , should go through getRequestValue
                    //if(#{pageSession.hasRadioButtons}){
                    //    forLoop(end="#{pageSession.radioButtonCount}" varName="i"){
                    //        setValueExpression(expression="#{pageSession.valueMap[requestScope.attrNames[i]]}"   value="#{param[requestScope.RadioButtonNames[i]]}");
                    //    }
                    //}
                    -->

                   if (#{pageSession['rest-api']}) {
                        if(#{pageSession.hasPropertySheet}){
                            gf.createEntity(endpoint="#{pageSession.selfUrl}"
                                attrs="#{pageSession.valueMap}"
                                skipAttrs="#{pageSession.skipAttrsList}"
                                convertToFalse="#{pageSession.convertToFalseList}"
                                onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                            );
                        if(#{pageSession.has2Objects}){
                                gf.createEntity(endpoint="#{pageSession.selfUrl2}"
                                    attrs="#{pageSession.valueMap2}"
                                    skipAttrs="#{pageSession.skipAttrsList2}"
                                    convertToFalse="#{pageSession.convertToFalseList2}"
                                    onlyUseAttrs="#{pageSession.onlyUseAttrs2}"
                                );
                            }
                        }
                   }

                    // Save properties...
                    if (#{pageSession.hasPropertyTable}) {
                        removeEmptyProps(props="#{pageSession.tableList}" modifiedProps="#{pageSession.tableList}");
                        javaToJSON(obj="#{pageSession.tableList}" json="#{requestScope.tmpJSON}");
                        gf.restRequest(endpoint="#{pageSession.selfUrl}/property.json", method="POST", data="#{requestScope.tmpJSON}", result="#{requestScope.restResponse}");
                    }

                    if (#{pageSession.additionalSaveHandler}) {
                        gf.invokeHandler(handler="#{pageSession.additionalSaveHandler}", args="#{pageSession.additionalSaveHandlerArgs}" );
                    }
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>


            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command

                    if (#{pageSession['rest-api']}){
                    gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        result => $attribute{objStr});
                    if(#{pageSession.has2Objects}){
                        if (#{pageSession.parentObjectNameStr2}){
                            gf.createEntity(endpoint="#{pageSession.parentUrl2}/#{pageSession.childType2}"
                                attrs="#{pageSession.valueMap2}"
                                skipAttrs="#{pageSession.skipAttrsList2}"
                                convertToFalse="#{pageSession.convertToFalseList2}"
                                result => $attribute{resultEndpoint});
                        }
                        if (!#{pageSession.parentObjectNameStr2}){
                            //create as a sub-element
                            gf.createEntity(endpoint="$attribute{objStr}"
                                attrs="#{pageSession.valueMap2}"
                                convertToFalse="#{pageSession.convertToFalseList2}"
                                result => $attribute{resultEndpoint2});
                        }
                    }
                    }


                    if (#{pageSession.hasPropertyTable}){
                        if (#{pageSession['rest-api']}) {
                            removeEmptyProps(props="#{pageSession.tableList}" modifiedProps="#{pageSession.tableList}");
                            javaToJSON(obj="#{pageSession.tableList}" json="#{requestScope.tmpJSON}");
                            //println("payload -------------------------------> #{requestScope.tmpJSON}");
                            urlencode(value="#{pageSession.valueMap['name']}" encoding="UTF-8" result="#{pageSession.encodeName}");
                            gf.restRequest(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}/#{pageSession.encodeName}/property.json"
                                           method="POST"
                                           data="#{requestScope.tmpJSON}"
                                           result="#{requestScope.restResponse}");
                        }
                    }

                    if (#{pageSession.additionalSaveHandler}){
                        gf.invokeHandler(handler="#{pageSession.additionalSaveHandler}", args="#{pageSession.additionalSaveHandlerArgs}" );
                    }
                    if (#{pageSession.showSuccessfulMsg}){
                        prepareSuccessfulMsg();
                        gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                    }
                    gf.redirect(page="#{pageSession.parentPage}?configName=#{configName}")
                />
            </sun:button>


            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}?configName=#{configName}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
