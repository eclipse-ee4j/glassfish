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

<!-- grizzly/listenerTabs.inc -->

<sun:tabSet id="listenerTabs" immediate="$boolean{true}" selected="#{sessionScope.listenerTabs}">
    <sun:tab id="generalTab" immediate="$boolean{true}" text="$resource{i18n_web.grizzly.networkListener.listenerTab}" toolTip="$resource{i18n_web.grizzly.networkListener.listenerTab} Tab" >
        <!command
        setSessionAttribute(key="listenerTabs" value="generalTab");
        gf.redirect(page="#{request.contextPath}/web/grizzly/networkListenerEdit.jsf?configName=#{configName}&name=#{pageSession.encodedListenerName}&cancelTo=#{pageSession.cancelTo}");
        />
    </sun:tab>
    <sun:tab id="sslTab" rendered="#{showSSLTab}" immediate="$boolean{true}" text="$resource{i18n_web.grizzly.sslTab}" toolTip="$resource{i18n_web.grizzly.sslTab} Tab" >
        <!command
        setSessionAttribute(key="listenerTabs" value="sslTab");
        gf.redirect(page="#{request.contextPath}/web/grizzly/listenerSSLEdit.jsf?configName=#{configName}&name=#{pageSession.encodedProtocolName}&listenerName=#{pageSession.encodedListenerName}&cancelTo=#{pageSession.cancelTo}");
        />
    </sun:tab>
    <sun:tab id="httpTab" rendered="#{showHttpTab}"  immediate="$boolean{true}" text="$resource{i18n_web.grizzly.httpTab}" toolTip="$resource{i18n_web.grizzly.httpTab} Tab" >
        <!command
            setSessionAttribute(key="listenerTabs" value="httpTab");
            gf.redirect(page="#{request.contextPath}/web/grizzly/listenerHttpEdit.jsf?configName=#{pageSession.configName}&name=#{pageSession.encodedProtocolName}&listenerName=#{pageSession.encodedListenerName}&cancelTo=#{pageSession.cancelTo}");
        />
    </sun:tab>
    <sun:tab id="fileCacheTab" rendered="#{showFileCacheTab}" immediate="$boolean{true}" text="$resource{i18n_web.grizzly.fileCacheTab}" toolTip="$resource{i18n_web.grizzly.fileCacheTab} Tab" >
        <!command
        setSessionAttribute(key="listenerTabs" value="fileCacheTab");
        gf.redirect(page="#{request.contextPath}/web/grizzly/listenerFileCache.jsf?configName=#{pageSession.configName}&name=#{pageSession.encodedProtocolName}&listenerName=#{pageSession.encodedListenerName}&cancelTo=#{pageSession.cancelTo}");
        />
        
    </sun:tab>

</sun:tabSet>
