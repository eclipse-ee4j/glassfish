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

package com.sun.ejb;

import java.lang.reflect.Method;

import com.sun.ejb.containers.interceptors.InterceptorManager;
import com.sun.enterprise.security.authorize.cache.CachedPermission;
import org.glassfish.ejb.deployment.descriptor.EjbRemovalInfo;

/**
 * InvocationInfo caches various attributes of the method that
 * is currently held in the invocation object (that is currently executed)
 * This avoids some of the expensive operations like (for example)
 *      method.getName().startsWith("findByPrimaryKey")
 *
 * Every container maintains a HashMap of method VS invocationInfo that
 *  is populated during container initialization
 *
 * @author Mahesh Kannan
 */

public class InvocationInfo {

    public String     ejbName;
    public Method     method;
    public String     methodIntf;


    public int        txAttr;
    public CachedPermission cachedPermission;

    public boolean    isBusinessMethod;
    public boolean    isHomeFinder;
    public boolean    isCreateHomeFinder;
    public boolean    startsWithCreate;
    public boolean    startsWithFind;
    public boolean    startsWithRemove;
    public boolean    startsWithFindByPrimaryKey;

    // Used by InvocationHandlers to cache bean class methods that
    // correspond to ejb interface methods.
    public Method     targetMethod1;
    public Method     targetMethod2;
    public boolean    ejbIntfOverride;

    public boolean    flushEnabled;
    public boolean    checkpointEnabled;

    public InterceptorManager.InterceptorChain interceptorChain;

    // method associated with @AroundInvoke or @AroundTimeout
    public Method aroundMethod;
    public boolean isEjbTimeout;


    // Only applies to EJB 3.0 SFSBs
    public EjbRemovalInfo     removalInfo;

    public boolean isTxRequiredLocalCMPField = false;

    public MethodLockInfo methodLockInfo;

    private boolean asyncMethodFlag;

    // Stringified method signature to be used for monitoring
    public String str_method_sig;

    public InvocationInfo() {}

    public InvocationInfo(Method method) {
        this.method = method;
    }

    public void setIsAsynchronous(boolean val) {
        this.asyncMethodFlag = val;
    }

    public boolean isAsynchronous() {
        return asyncMethodFlag;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Invocation Info for ejb " + ejbName + "\t");
        sb.append("method=" + method + "\t");
        sb.append("methodIntf = " + methodIntf + "\t");
        if (txAttr != -1) {
            sb.append("tx attr = " + Container.txAttrStrings[txAttr] + "\t");
        } else {
            sb.append("tx attr = -1\t");
        }
        sb.append("Cached permission = " + cachedPermission + "\t");
        sb.append("target method 1 = " + targetMethod1 + "\t");
        sb.append("target method 2 = " + targetMethod2 + "\t");
        sb.append("ejbIntfOverride = " + ejbIntfOverride + "\t");
        sb.append("flushenabled = " + flushEnabled + "\t");
        sb.append("checkpointenabled = " + checkpointEnabled + "\t");
        sb.append("removalInfo = " + removalInfo + "\t");
        sb.append("lockInfo = " + methodLockInfo + "\t");
        sb.append("async = " + asyncMethodFlag + "\t");
        sb.append("\n");
        return sb.toString();
    }
}
