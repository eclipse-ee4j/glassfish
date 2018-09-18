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
             <sun:title id="propertyContentPage" title="$resource{i18nf.batch.jobListingTitle}" helpText="$resource{i18nf.batch.jobListingTitleHelp}" />
#include "/common/shared/nameSection.inc"
        <sun:table id="configs" title="#{tableTitle}"
            deselectMultipleButton="$boolean{false}"
            selectMultipleButton="$boolean{false}"
            paginationControls="$boolean{true}"
            paginateButton="$boolean{true}">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{tableId});
            />

   <sun:tableRowGroup id="rowGroup1"  data={"$attribute{listOfRows}"} sourceVar="td">
     <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
        />

    <sun:tableColumn headerText="$resource{i18nf.batch.executionIdCol}" sort="executionId" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.executionId}" url="#{pageSession.editLink}&executionId=#{td.value.executionId}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18nf.batch.jobNameCol}"  sort="jobName"  rowHeader="$boolean{false}" id="jobname">
        <staticText id="max" value="#{td.value.jobName}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18nf.batch.batchStatusCol}"  sort="batchStatus"  rowHeader="$boolean{false}" id="batchStatus">
        <staticText id="max" value="#{td.value.batchStatus}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18nf.batch.exitStatusCol}"  sort="exitStatus"  rowHeader="$boolean{false}" id="exitStatus">
        <staticText id="max" value="#{td.value.exitStatus}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18nf.batch.instanceIdCol}"  sort="instanceId"  rowHeader="$boolean{false}" id="instanceId">
        <staticText id="max" value="#{td.value.instanceId}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18nf.batch.startTimeCol}"  sort="startTime"  rowHeader="$boolean{false}" id="startTime">
        <staticText id="st" value="#{requestScope.st}" >
        <!beforeEncode
            gf.convertDateTime(dateTime="#{td.value.startTime}"  result="#{requestScope.st}");
        />
        </staticText>
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18nf.batch.endTimeCol}"  sort="endTime"  rowHeader="$boolean{false}" id="endTime">
        <staticText id="endTime" value="#{requestScope.endTime}" >
            <!beforeEncode
                gf.convertDateTime(dateTime="#{td.value.endTime}"  result="#{requestScope.endTime}");
            />
        </staticText>
    </sun:tableColumn>
  </sun:tableRowGroup>
</sun:table>

<sun:hidden id="helpKey" value="$resource{help_full.batchJobs}" />
            </sun:form>
</define>
</composition>
