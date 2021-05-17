/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * AgentAdapter.java
 */
package    com.sun.enterprise.admin.monitor.callflow;

/**
 * This    class provides a fallback implementation.
 */
public class AgentAdapter implements Agent {

    public void    requestStart(RequestType requestType) {}
    public void    addRequestInfo(RequestInfo requestInfo,    String value) {}
    public void    requestEnd() {}
    public void    startTime(ContainerTypeOrApplicationType type) {}
    public void    endTime() {}

    public void    ejbMethodStart(CallFlowInfo info) {}
    public void    ejbMethodEnd(CallFlowInfo info)    {}

    public void    webMethodStart(
        String methodName, String applicationName, String moduleName,
        String componentName, ComponentType    componentType,
        String callerPrincipal) {}
    public void    webMethodEnd(Throwable exception) {}

    public void    entityManagerQueryStart(EntityManagerQueryMethod queryMethod) {}
    public void    entityManagerQueryEnd()    {}

    public void    entityManagerMethodStart(EntityManagerMethod entityManagerMethod) {}
    public void    entityManagerMethodEnd() {}

    public void    registerListener(Listener listener) {}
    public void    unregisterListener(Listener listener) {}

    public ThreadLocalData getThreadLocalData()    {
    return null;
    }

    public void    setEnable(boolean enable) {}

    public boolean isEnabled() {return false;}

    public void    setCallerIPFilter(String ipAddress) {}

    public void    setCallerPrincipalFilter(String    callerPrincipal) {}

    public String getCallerPrincipalFilter() {
    return null;
    }

    public String getCallerIPFilter() {
    return null;
    }

    public void    clearData() {}

    public boolean deleteRequestIds (String[] requestIds){
    return true;
    }

    public java.util.List<java.util.Map<String,    String>>
        getRequestInformation() {
    return null;
    }

    public java.util.List<java.util.Map<String,    String>>
        getCallStackForRequest(String requestId) {
    return null;
    }

    public java.util.Map<String, String> getPieInformation (String requestID) {
    return null;
    }

}
