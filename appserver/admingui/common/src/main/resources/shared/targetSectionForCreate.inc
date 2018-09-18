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

<!-- shared/targetSectionForCreate.inc -->

<sun:propertySheet id="targetSection">
    <!beforeCreate
        gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/clusters/cluster", result="#{requestScope.clusterList}");
        gf.listInstances(optionKeys={"standaloneonly"} optionValues={"true"}, instances="#{pageSession.targetList}" );
        gf.listCombine(list="#{pageSession.targetList}",  list2="#{requestScope.clusterList}");
        listAdd(list="#{pageSession.targetList}", value="server" index="0");

        setAttribute(key="one" value="$int{1}");
	    setAttribute(key="targetSize" value="#{pageSession.targetList.size()}");
        //println("=========== #{requestScope.targetSize}");
        //println("===========#{pageSession.targetList}");

        if( #{requestScope.targetSize}=#{requestScope.one} ){
            setPageSessionAttribute(key="showTargetSection",  value="#{false}");
            if (#{pageSession.setDefaultTarget}){
                setPageSessionAttribute(key="selectedTargets" value=["server"] );
            }
        }
        if( #{requestScope.targetSize}>#{requestScope.one} ){
            setPageSessionAttribute(key="showTargetSection",  value="#{true}");
            //when issue#12596 is fixed.  we should not preselect target.
            //Because for the edit case, it has been set previously.
            //for create case, we should leave it blank and warn user to select one.
            if (#{pageSession.setDefaultTarget}){
                gf.containedIn(list="#{pageSession.targetList}" testStr="#{pageSession.targetValue}" contain=>$attribute{selectedTargetExists});
                if (!#{requestScope.selectedTargetExists}) {
		    convertListToArray(list={}, array="#{pageSession.selectedTargets}");
                }
                if (#{selectedTargetExists}) {
                    listAdd(value="#{pageSession.targetValue}" result=>$attribute{selectedTargetsList});
                    convertListToArray(list="#{selectedTargetsList}", array="#{pageSession.selectedTargets}");
                }
            }
        }
        setPageSessionAttribute(key="useCheckBox" value="#{true}");
        setPageSessionAttribute(key="useString" value="#{false}");

        gf.convertListToOptionArray(list="#{pageSession.targetList}", optionArray="#{pageSession.availableTargets}");
    />
    <sun:propertySheetSection id="targetSectionId" visible="#{pageSession.showTargetSection}" label="$resource{i18n.common.TargetTableTitle}"  >
        <sun:property id="addRemoveProp" noWrap="#{true}" overlapLabel="#{false}">
#include "/common/shared/commonAddRemove.inc"
        </sun:property>
        <!afterCreate
            getClientId(component="$this{component}" clientId="#{pageSession.targetSectionId}");
        />
    </sun:propertySheetSection>

</sun:propertySheet>

