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

<handler id="gfr.prepareLogicalJndiNameForEdit" >
    ## if logicalJndiName is passed in, we don't need to find it.
    getRequestValue(key="logicalJndiName" value="#{pageSession.logicalJndiName}");
    setPageSessionAttribute(key="renderLogic" value="#{false}");
    if("#{pageSession.logicalJndiName}"){
        setPageSessionAttribute(key="renderLogic" value="#{true}");
    }
    if(!#{pageSession.logicalJndiName}){
        gf.restRequest(endpoint="#{sessionScope.REST_URL}/resources/#{pageSession.listCommand}"  method="GET" result="#{requestScope.tmpJMap}");
        mapGet(Map="#{requestScope.tmpJMap.data.extraProperties}", Key="#{pageSession.logicalJndiMapKey}"  Value="#{requestScope.logicalMapList}")
        foreach (var="oneR" list="#{requestScope.logicalMapList}") {
            if("#{oneR['name']} = #{pageSession.Name}" ){
                setPageSessionAttribute(key="logicalJndiName" value="#{oneR['logical-jndi-name']}" );
                if(#{pageSession.logicalJndiName}){
                    setPageSessionAttribute(key="renderLogic" value="#{true}");
                }
            }
        }
    }
</handler>
