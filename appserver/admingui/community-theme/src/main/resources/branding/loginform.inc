<!--

    Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.

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

<f:verbatim>
<div style="height: 435px;background-image: url(#{request.contextPath}/resource/community-theme/images/login-backimage-open.png);
    background-repeat:no-repeat;background-position:left top; width: 720px; margin: auto;">
    <div style="width: 460px; padding-top: 160px; margin-left: 310px;">
</f:verbatim>
        <sun:image url="#{request.contextPath}/resource/community-theme/images/login-product_name_open.png" alt="$resource{theme.productName}" height="42" width="329" >
        <!beforeCreate
                setResourceBundle(key="theme" bundle="org.glassfish.admingui.community-theme.Strings");
            />
        </sun:image>
<f:verbatim>
        <form method="POST" class="form" name="loginform" action="j_security_check" autocomplete="off">
        <table role="presentation">
        <tr>
            <td><label for="Login.username" style="font-weight: bold;">$resource{i18n.common.username}</label></td>
            <td><input type="text" name="j_username" id="Login.username" tabindex="1" value="#{byPassLogin ? pageSession.loginUser : ''}"></td>
        </tr>
        <tr>
            <td><label for="Login.password" style="font-weight: bold;">$resource{i18n.common.password}</label>
            <td><input type="password" name="j_password" id="Login.password" tabindex="2" autocomplete="new-password">
        <tr>
            <td colspan="2" align="center">
                <input type="submit" class="Btn1"
                    value="$resource{i18n.loginPageTitle}"
                    title="#{loginButtonTooltip}" tabindex="3"
                    onmouseover="javascript: if (this.disabled==0) this.className='Btn1Hov'"
                    onmouseout="javascript: if (this.disabled==0) this.className='Btn1'"
                    onblur="javascript: if (this.disabled==0) this.className='Btn1'"
                    onfocus="javascript: if (this.disabled==0) this.className='Btn1Hov'"
                    name="loginButton" id="loginButton">
        	    <input type="hidden" name="loginButton.DisabledHiddenField" value="true" />
    	    </td>
	    </tr>
	    </table>
        </form>
    </div>
</div>
</f:verbatim>
