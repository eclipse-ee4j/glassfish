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

<!initPage
    setResourceBundle(key="i18nc" bundle="org.glassfish.common.admingui.Strings");
    setResourceBundle(key="i18nf" bundle="org.glassfish.full.admingui.Strings");
    setResourceBundle(key="i18ncs" bundle="org.glassfish.cluster.admingui.Strings")
	setResourceBundle(key="help_full" bundle="org.glassfish.full.admingui.Helplinks");
/>
<!composition template="/templates/default.layout"  guiTitle="$resource{i18nf.batch.configurationTitle}"  >

<!define name="content">
    <event>
    <!beforeCreate
#include "/full/batch/batchRequestParam.inc"
        setSessionAttribute(key="#{pageSession.tabSetName}" value="batchConfig");
        createMap(result="#{pageSession.attrsMap}")
        mapPut(map="#{pageSession.attrsMap}" key="target" value="#{pageSession.encodedTarget}");
        gf.restRequest(endpoint="#{sessionScope.REST_URL}/list-batch-runtime-configuration?target=#{pageSession.encodedTarget}"  method="GET" result="#{requestScope.resp}");
        setPageSessionAttribute(key="valueMap", value="#{requestScope.resp.data.extraProperties.listBatchRuntimeConfiguration}");
        mapPut(map="#{pageSession.valueMap}" key="target" value="#{pageSession.encodedTarget}");
    />
    </event>
<sun:form id="propertyForm">
