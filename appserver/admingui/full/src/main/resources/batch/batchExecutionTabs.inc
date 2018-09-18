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

<sun:tabSet id="batchExecutionTabs" immediate="$boolean{true}" selected="#{sessionScope.batchExecutionTabs}">
    <!beforeCreate
            setResourceBundle(key="i18nf" bundle="org.glassfish.full.admingui.Strings");
            setResourceBundle(key="i18nc" bundle="org.glassfish.common.admingui.Strings");
            setResourceBundle(key="i18ncs" bundle="org.glassfish.cluster.admingui.Strings")
        />
    <sun:tab id="jobParameters" immediate="$boolean{true}" text="$resource{i18nf.batch.jobParametersTab}" toolTip="$resource{i18nf.batch.jobParametersTabTip}" >
        <!command
        setSessionAttribute(key="batchExecutionTabs" value="jobParameters");
        gf.redirect(page="#{request.contextPath}/full/batch/batchJobExecution.jsf?executionId=#{pageSession.executionId}&target=#{pageSession.target}&isCluster=#{pageSession.isCluster}&tabSetName=#{pageSession.tabSetName}");
        />
    </sun:tab>
    <sun:tab id="steps" immediate="$boolean{true}" text="$resource{i18nf.batch.stepTab}" toolTip="$resource{i18nf.batch.stepTabTip}" >
        <!command
        setSessionAttribute(key="batchExecutionTabs" value="steps");
        gf.redirect(page="#{request.contextPath}/full/batch/batchStepListing.jsf?executionId=#{pageSession.executionId}&target=#{pageSession.target}&isCluster=#{pageSession.isCluster}&tabSetName=#{pageSession.tabSetName}");
        />
    </sun:tab>
</sun:tabSet>
