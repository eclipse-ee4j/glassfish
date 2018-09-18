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

<!-- grizzly/transportsTable.inc -->

<sun:table id="configs" title="#{tableTitle}">

#include "/common/shared/listTableButtons.inc"
    
   <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
     <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
        />
    <sun:tableColumn headerText="$resource{i18n.common.SelectHeader}" selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select" selected="#{td.value.selected}" selectedValue="$boolean{true}" />
    </sun:tableColumn>    

    <sun:tableColumn headerText="$resource{i18n_web.grizzly.transport.nameCol}" sort="name"  rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" 
            url="#{pageSession.editLink}&name=#{td.value.name}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n_web.grizzly.transport.byteBufferCol}" sort="byteBuffer"   rowHeader="$boolean{true}" id="col3">
        <staticText id="byteBuffer" value="#{td.value.byteBuffer}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n_web.grizzly.transport.classnameCol}"  sort="classname" rowHeader="$boolean{true}" id="pro">
        <staticText id="classname" value="#{td.value.classname}" />
    </sun:tableColumn>

  </sun:tableRowGroup>
  
</sun:table>
