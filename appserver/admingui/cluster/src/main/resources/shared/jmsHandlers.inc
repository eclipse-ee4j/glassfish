<!--

    Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.

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

<handler id="gf.configureJmsCluster">
    createMap(result="#{requestScope.jmsAttrs}");
    //println("***** #{pageSession}\n\n\n\n\n\n");
    mapPut(map="#{requestScope.jmsAttrs}" key="clustertype" value="$pageSession{clusterType}");
    mapPut(map="#{requestScope.jmsAttrs}" key="property" value="$pageSession{properties}");
    if ('$pageSession{clusterType}=conventional') {
        mapPut(map="#{requestScope.jmsAttrs}" key="configStoreType" value="$pageSession{configStoreType}");
        mapPut(map="#{requestScope.jmsAttrs}" key="messageStoreType" value="$pageSession{messageStoreType}");
    }

    // Save JMS integration type
    createMap(result="#{requestScope.jmsSvcAttrs}");

    mapPut(map="#{requestScope.jmsSvcAttrs}" key="type" value="#{empty pageSession.jmsType ? pageSession.jmsTypeOptionGrp : pageSession.jmsType}");
    gf.restRequest(endpoint="#{sessionScope.REST_URL}/configs/config/#{pageSession.config}/jms-service",
        method="POST", attrs="#{requestScope.jmsSvcAttrs}", result="#{pageSession.resultMap}");
    //println("\n\n\n\n\n**************1needsDB = #{needsDB}\n\n\n\n\n\n\n\n\n");
        
    setAttribute(key="needsDB", value="#{false}");
    
    //println("\n\n\n\n\n**************2needsDB = #{needsDB}\n\n\n\n\n\n\n\n\n");

    if ('($pageSession{clusterType}=enhanced)') {
        setAttribute(key="needsDB" value="#{true}");
    }
    //println("\n\n\n\n\n**************3needsDB = #{needsDB}\n\n\n\n\n\n\n\n\n");
    if ('($pageSession{messageStoreType}=jdbc)') {
        setAttribute(key="needsDB" value="#{true}");
    }
    //println("\n\n\n\n\n**************4needsDB = #{needsDB}\n\n\n\n\n\n\n\n\n");
    if ('($pageSession{configStoreType}=shareddb)') {
        setAttribute(key="needsDB" value="#{true}");
    }
    //println("\n\n\n\n\n**************5needsDB = #{needsDB}\n\n\n\n\n\n\n\n\n");
    if ('(#{needsDB})') {
        if (#{dbVendor}) {
            mapPut(map="#{requestScope.jmsAttrs}" key="dbVendor" value="$pageSession{dbVendor}");
        }
        if (#{dbUser}) {
            mapPut(map="#{requestScope.jmsAttrs}" key="dbUser" value="$pageSession{dbUser}");
        }
        if (#{dbUrl}) {
            mapPut(map="#{requestScope.jmsAttrs}" key="dbUrl" value="$pageSession{dbUrl}");
        }
        if ('$pageSession{authType}=password') {
            mapPut(map="#{requestScope.jmsAttrs}" key="jmsdbpassword" value="#{pageSession.pswdText}")
        }
        if ('$pageSession{authType}=alias') {
            gf.convertToAlias(in="#{pageSession.pswdAlias}" out="#{requestScope.tmpv}");
            mapPut(map="#{requestScope.jmsAttrs}" key="jmsdbpassword" value="#{requestScope.tmpv}")
        }
    }

    if ('($pageSession{jmsType}=LOCAL) || ($pageSession{jmsType}=EMBEDDED)') {  
        gf.restRequest(endpoint="#{sessionScope.REST_URL}/clusters/cluster/#{pageSession.name}/configure-jms-cluster",
            attrs="#{requestScope.jmsAttrs}",
            method="post",
            result="#{pageSession.resultMap}",
            throwException="#{false}");

        if ("#{pageSession.resultMap.data.exit_code}=FAILURE") {
            if (#{pageSession.deleteClusterOnFailure}) {
                prepareAlertMsg(summary="$resource{i18ncs.cluster.jms.ConfigureJmsClusterFailure}", 
                    detail="#{pageSession.resultMap.data.message}"
                    type="error");
                gf.restRequest(endpoint="#{sessionScope.REST_URL}/clusters/cluster/#{pageSession.name}", method="delete");
                gf.redirect(page="#{pageSession.selfPage}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            }
            if (!#{pageSession.deleteClusterOnFailure}) {
                prepareAlertMsg(summary="#{pageSession.resultMap.data.message}", type="error");
                gf.redirect(page="#{pageSession.selfPage}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
            }
        }
    }
</handler>
