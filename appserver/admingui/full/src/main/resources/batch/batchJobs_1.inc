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
    setResourceBundle(key="i18nf" bundle="org.glassfish.full.admingui.Strings");
    setResourceBundle(key="i18nc" bundle="org.glassfish.common.admingui.Strings");
    setResourceBundle(key="i18ncs" bundle="org.glassfish.cluster.admingui.Strings")
	setResourceBundle(key="help_full" bundle="org.glassfish.full.admingui.Helplinks");
/>
<!composition template="/templates/default.layout"  guiTitle="$resource{i18nf.batch.jobListingTitle}"  >

<!define name="content">
    <event>
    <!beforeCreate
#include "/full/batch/batchRequestParam.inc"
        setSessionAttribute(key="#{pageSession.tabSetName}" value="batchJobs");
        createMap(result="#{requestScope.attrsMap}");
        mapPut(map="#{requestScope.attrsMap}" key="target" value="#{pageSession.encodedTarget}");
        mapPut(map="#{requestScope.attrsMap}"  key="long" value="true");
        gf.restRequest(endpoint="#{sessionScope.REST_URL}/list-batch-jobs" attrs="#{requestScope.attrsMap}" method="GET" result="#{requestScope.resp}");
        setAttribute(key="listOfRows", value="#{requestScope.resp.data.extraProperties.listBatchJobs}");
        setPageSessionAttribute(key="tableTitle" value="$resource{i18nf.batch.batchJobsTableTitle}");
        setPageSessionAttribute(key="editLink" value="#{request.contextPath}/full/batch/batchJobExecution.jsf?target=#{pageSession.target}&isCluster=#{pageSession.isCluster}&tabSetName=#{pageSession.encodedTabSetName}");
    />
    </event>
        <sun:form id="propertyForm">
