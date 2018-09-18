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

getRequestValue(key="target" value="#{pageSession.target}");
urlencode(value="#{pageSession.target}" encoding="UTF-8" result="#{pageSession.encodedTarget}");
getRequestValue(key="isCluster" value="#{pageSession.isCluster}" default="false");
getRequestValue(key="tabSetName" value="#{pageSession.tabSetName}");
urlencode(value="#{pageSession.tabSetName}" encoding="UTF-8" result="#{pageSession.encodedTabSetName}");

if(#{pageSession.isCluster}=true){
    setPageSessionAttribute(key="clusterName", value="#{pageSession.target}");
    setPageSessionAttribute(key="encodedClusterName", value="#{pageSession.encodedTarget}");
    setPageSessionAttribute(key="listJobsLink" value="#{request.contextPath}/full/batch/batchJobsCluster.jsf?target=#{pageSession.encodedTarget}&isCluster=true&tabSetName=#{pageSession.encodedTabSetName}");
}
if(#{pageSession.isCluster}=false){
    setPageSessionAttribute(key="isCluster", value="false");
    setPageSessionAttribute(key="instanceName", value="#{pageSession.target}");
    setPageSessionAttribute(key="encodedInstanceName", value="#{pageSession.encodedTarget}");
    if(#{pageSession.target}=server){
        setPageSessionAttribute(key="listJobsLink" value="#{request.contextPath}/full/batch/batchJobsServer.jsf?target=server&isCluster=false&tabSetName=#{pageSession.encodedTabSetName}");
    }
    if("!(#{pageSession.target}=server)"){
        setPageSessionAttribute(key="listJobsLink" value="#{request.contextPath}/full/batch/batchJobsStandalone.jsf?target=#{pageSession.encodedTarget}&isCluster=false&tabSetName=#{pageSession.encodedTabSetName}");
    }
}

