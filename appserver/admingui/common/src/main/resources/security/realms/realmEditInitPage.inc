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

<!--  security/realms/realmEditInitPage.inc -->

<!-- 
   # set the component required to false so that when ok button is pressed, it won't enforce the required field
   # there are some hidden fields which may/may not required depending on the class selected.
   # use own JS to check requird field.
-->
<!initPage
    setResourceBundle(key="i18nc" bundle="org.glassfish.common.admingui.Strings")
    setResourceBundle(key="help_common" bundle="org.glassfish.common.admingui.Helplinks");
    setComponentRequired(id="form1:fileSection:jaax:jaax" required="$boolean{false}" );
    setComponentRequired(id="form1:fileSection:keyFile:keyFile" required="$boolean{false}" );
    setComponentRequired(id="form1:ldapSection:jaax:jaax" required="$boolean{false}" );
    setComponentRequired(id="form1:ldapSection:directory:directory" required="$boolean{false}" );
    setComponentRequired(id="form1:ldapSection:baseDn:baseDn" required="$boolean{false}" );
    setComponentRequired(id="form1:solarisSection:jaax:jaax" required="$boolean{false}" );
    setComponentRequired(id="form1:pamSection:jaax:jaax" required="$boolean{false}" );
    setComponentRequired(id="form1:jdbcSection:jaax:jaax" required="$boolean{false}" );
    setComponentRequired(id="form1:jdbcSection:jndi:jndi" required="$boolean{false}" );
    setComponentRequired(id="form1:jdbcSection:userTable:userTable" required="$boolean{false}" );
    setComponentRequired(id="form1:jdbcSection:userName:userName" required="$boolean{false}" );
    setComponentRequired(id="form1:jdbcSection:pswd:pswd" required="$boolean{false}" );
    setComponentRequired(id="form1:jdbcSection:groupTable:groupTable" required="$boolean{false}" );
    setComponentRequired(id="form1:jdbcSection:groupName:groupName" required="$boolean{false}" );
    setComponentRequired(id="form1:jdbcSection:pswdEnc:pswdEnc" required="$boolean{false}" );
/>
    
