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

<!--  security/realms/realm.inc -->

<event>
    <!beforeEncode
        # need to put back the required image if there is any error in processing and the page is refreshed.
        # we had turned off the requirement in initPage before.
        setComponentRequired(id="form1:fileSection:jaax:jaax" required="$boolean{true}" );
        setComponentRequired(id="form1:fileSection:keyFile:keyFile" required="$boolean{true}" );
        setComponentRequired(id="form1:ldapSection:jaax:jaax" required="$boolean{true}" );
        setComponentRequired(id="form1:ldapSection:directory:directory" required="$boolean{true}" );
        setComponentRequired(id="form1:ldapSection:baseDn:baseDn" required="$boolean{true}" );
        setComponentRequired(id="form1:solarisSection:jaax:jaax" required="$boolean{true}" );
        setComponentRequired(id="form1:pamSection:jaax:jaax" required="$boolean{true}" );
        setComponentRequired(id="form1:jdbcSection:jaax:jaax" required="$boolean{true}" );
        setComponentRequired(id="form1:jdbcSection:jndi:jndi" required="$boolean{true}" );
        setComponentRequired(id="form1:jdbcSection:userTable:userTable" required="$boolean{true}" );
        setComponentRequired(id="form1:jdbcSection:userName:userName" required="$boolean{true}" );
        setComponentRequired(id="form1:jdbcSection:pswd:pswd" required="$boolean{true}" );
        setComponentRequired(id="form1:jdbcSection:groupTable:groupTable" required="$boolean{true}" );
        setComponentRequired(id="form1:jdbcSection:groupName:groupName" required="$boolean{true}" );
        setComponentRequired(id="form1:jdbcSection:pswdEnc:pswdEnc" required="$boolean{true}" );
    />
</event>

        <sun:form id="form1">
#include "/common/shared/alertMsg.inc"
        <!-- there seems to be a bug with the radio button component where if we switch from 'input' to 'predefine',
             the selected value will not get updated.  It will always stay as 'input'.
             So i work around the problem by using a hidden field
          -->
        <sun:hidden id="option"  value="#{myOption}" />

        <sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
            <sun:button id="manageUsersButton" rendered="#{showUserButton}" immediate="#{true}" style="margin-left: 8pt"  text="$resource{i18n.button.ManageUsers}" >
                <!command
                    gf.redirect(page="#{request.contextPath}/common/security/realms/manageUsers.jsf?name=#{pageSession.encodedName}&configName=#{pageSession.encodedConfigName}");
                />
            </sun:button>

            <!facet pageButtonsTop>
#include "/security/realms/realmButtons.inc"            
            </facet>   
            </sun:title>

#include "/security/realms/realmAttrs.inc" 
#include "/common/shared/propertyDescTable.inc"

        <sun:title id="title2">
            <!facet pageButtonsBottom>
#include "/security/realms/realmButtons.inc" 
            </facet> 
        </sun:title>    

        <sun:hidden id="helpKey" value="#{helpKey}" />
    </sun:form>
    "<br/><br/>
#include "/common/security/realms/realmjs.inc"
#include "/common/shared/changeButtonsJS.inc"
