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

<!initPage
    setResourceBundle(key="theme" bundle="org.glassfish.admingui.community-theme.Strings");
/>

<sun:masthead id="Masthead" productImageURL="#{request.contextPath}/resource/community-theme/images/masthead-product_name_open.png" 
              style="border-width: 0px"
              productImageDescription="$resource{theme.productName}"
              productImageWidth="327"
              productImageHeight="23"
              userInfo="#{userName}"  userInfoLabel="$resource{i18n.masthead.userInfoLabel}"
              roleInfo="#{domainName}" roleInfoLabel="$resource{i18n.masthead.domainInfoLabel}"
              serverInfo="#{hostName}" serverInfoLabel="$resource{i18n.masthead.serverInfoLabel}"
              dateTime="$boolean{false}" notificationMsg="$attribute{null}">



    <!facet consoleLink>
        <sun:hyperlink id="homeLink" toolTip="$resource{i18n.homeLinkTooltip}" target="_top" text="$resource{i18n.masthead.Home}" url="#{request.contextPath}/common/index.jsf" style="background: rgb(84, 123, 150);" />
    </facet>
    <!facet versionLink>
        <sun:hyperlink id="versionLink" toolTip="$resource{i18n.versionTooltip}" text="$resource{i18n.masthead.Version}"
            onClick="javascript: var versionWin = window.open('#{request.contextPath}/common/version.jsf','VersionWindow','scrollbars,resizable,width=800,height=740,top='+((screen.height - (screen.height/1.618)) - (500/2))+',left='+((screen.width-650)/2) ); versionWin.focus(); return false;" onKeyPress="javascript: return true;" style="background: rgb(84, 123, 150);" />
    </facet>
    <!facet helpLink>
        <sun:button id="helpLink" rendered="true" toolTip="$resource{i18n.helpWindowTooltip}" target="help_window" text="$resource{i18n.masthead.Help}" onClick="admingui.help.launchHelp(this); return false;" style="background: rgb(84, 123, 150);" />
    </facet>
    <!facet logoutLink>
        <sun:button id="logoutLink" rendered="#{showLogoutButton}" toolTip="$resource{i18n.logoutTooltip}" target="_top" text="$resource{i18n.masthead.Logout}"
            onClick="if (!confirm('$resource{i18n.msg.JS.confirmLogout}')) return false;" onKeyPress="javascript: return true;" url="/" >
                <!command
                    logout();
                    redirect("#{request.contextPath}/common/index.jsf");
                />
        </sun:button>
    </facet>
    <!facet statusArea>
        <sun:panelGroup id="statusPanel" separator="">
            "<span style="margin: 5px 5px 0px 5px">
                <sun:image id="alertImage" rendered="#{sessionScope.restartRequired}" alt="$resource{i18n.masthead.restartRequired}" icon="ALERT_WARNING_MEDIUM"/>
                <sun:hyperlink id="restartLink" rendered="#{sessionScope.restartRequired}"
                               style="color:white; margin-left: 5px;"
                               text="$resource{i18n.masthead.restartRequired}"
                               toolTip="$resource{i18n.masthead.restartStatusMsg}"
                               onMouseOver="window.status='$resource{i18n.masthead.restartStatusMsg}'; return true"
                               onMouseOut="window.status=''; return true"
                               onBlur="window.status=''; return true"
                               onFocus="window.status='$resource{i18n.masthead.restartStatusMsg}'; return true"
                               url="#{request.contextPath}/common/appServer/restart.jsf">
                </sun:hyperlink>
                <f:verbatim>
                    <iframe src="/commandrecorder/menu.xhtml" style="overflow:auto; width:600px; height:18px; border:0px;">
                    </iframe>
                </f:verbatim>
                "</span>
            <!afterCreate
                includeIntegrations(type="org.glassfish.admingui:mastheadStatusArea" root="$this{component}");
            />
        </sun:panelGroup>
    </facet>
</sun:masthead>
