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

<sun:html id="html2">
    <sun:head id="propertyhead" title="#{pageSession.pageTitle}" />
    <sun:body id="body3">
        <sun:form id="propertyForm">
            <sun:title id="propertyContentPage" title="#{pageSession.pageTitle}" helpText="#{pageSession.pageTitleHelp}" />
                <sun:propertySheet id="propertySheet">
                    <sun:propertySheetSection id="propertySectionTextField">
                        <sun:property id="nameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.ApplicationName}">
                            <sun:staticText id="name" text="#{pageSession.appName}" />
                        </sun:property>
                        <sun:property id="linkProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Links}">

                            "<table>
                            <foreach key="list" list="#{pageSession.urlList}">
                               "<tr>
                                   "<td>
                                       <sun:staticText id="linkTarget" text="[#{list.target}] " />
                                   "</td>
                                   "<td>
                                        <sun:hyperlink text="#{list.url}" url="#{list.url}"/>
                                   "</td>
                                "</tr>
                            </foreach>
                            "</table>

                        </sun:property>
                    </sun:propertySheetSection>
                </sun:propertySheet>
            <sun:title id="title2">
                <!facet pageButtonsBottom>
                <sun:panelGroup id="topButtons">
                    <sun:button id="closeButton"
                        onClick="javascript: window.close(); return false;"
                        primary="#{true}"
                        text="$resource{i18n.button.Close}">
                    </sun:button>
                </sun:panelGroup>
                </facet>
            </sun:title>
        <sun:hidden id="helpKey" value="#{pageSession.helpKey}" />
        </sun:form>
    </sun:body>
</sun:html>
