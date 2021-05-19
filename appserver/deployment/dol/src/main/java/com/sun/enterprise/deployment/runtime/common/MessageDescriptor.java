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

package com.sun.enterprise.deployment.runtime.common;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDescriptor extends RuntimeDescriptor {
    public static final String JAVA_METHOD = "JavaMethod";
    public static final String OPERATION_NAME = "OperationName";

    private static final String ALL_METHODS = "*";

    private String operationName = null;
    private MethodDescriptor methodDescriptor = null;
    private ArrayList convertedMethodDescs = new ArrayList();

    // when this message is defined from client side
    private ServiceRefPortInfo portInfo = null;

    // when this message is defined from server side
    private WebServiceEndpoint endPoint = null;

    private BundleDescriptor bundleDesc = null;

    private boolean isConverted = false;

    public MessageDescriptor() {}

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setMethodDescriptor(MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    public MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    public void setServiceRefPortInfo(ServiceRefPortInfo portInfo) {
        this.portInfo = portInfo;
    }

    public ServiceRefPortInfo getServiceRefPortInfo() {
        return portInfo;
    }

    public void setWebServiceEndpoint(WebServiceEndpoint endPoint){
        this.endPoint = endPoint;
    }

    public WebServiceEndpoint getWebServiceEndpoint() {
        return endPoint;
    }

    public void setBundleDescriptor(BundleDescriptor bundleDesc){
        this.bundleDesc = bundleDesc;
    }

    public BundleDescriptor getBundleDescriptor() {
        return bundleDesc;
    }

    /**
     * Return all methods defined in this message.
     *
     * In the case of an empty message, it will return all methods
     * defined in the SEI.
     *
     * In the case of methods overloading, it will return all methods
     * defined in the SEI that match with the specified method name.
     *
     * In the case of DII, i.e the client doesn't have the SEI info,
     * it will return an empty list for client side defined message.
     *
     **/
    public ArrayList getAllDefinedMethodsInMessage() {
       // only do the conversion if it hasn't done it yet
       if (!isConverted) {
           doStyleConversion();
       }
       return convertedMethodDescs;
    }

    private void doStyleConversion() {
        if (operationName == null && methodDescriptor == null) {
            // this is the empty message case
            // and we need to expand to all methods
            convertedMethodDescs =  getAllSEIMethodsOf(ALL_METHODS);
        } else if (methodDescriptor != null) {
            if (methodDescriptor.getName() != null  &&
                methodDescriptor.getParameterClassNames() != null) {
                // this is the exact case, so no conversion needed
                convertedMethodDescs.add(methodDescriptor);
            } else if (methodDescriptor.getName() != null  &&
                methodDescriptor.getParameterClassNames() == null) {
                // we need to check for overloading methods
                convertedMethodDescs =
                    getAllSEIMethodsOf(methodDescriptor.getName());
            }
        }
        isConverted = true;
    }

    private ArrayList getAllSEIMethodsOf(String methodName) {
        String serviceInterfaceName = null;
        ArrayList allMethodsInSEI = new ArrayList();

        // this is a server side message
        if (endPoint != null) {
            serviceInterfaceName = endPoint.getServiceEndpointInterface();
        // this is a client side message
        } else if (portInfo != null) {
            serviceInterfaceName = portInfo.getServiceEndpointInterface();
        }

        // In the case of DII, client doesn't know the SEI name
        // return an empty list
        if (serviceInterfaceName == null) {
            return allMethodsInSEI;
        }

        ClassLoader classLoader = null;
        if (bundleDesc != null) {
            classLoader = bundleDesc.getClassLoader();
        }

        // return an empty list if class loader is not set
        if (classLoader == null) {
            return allMethodsInSEI;
        }

        try {
            Class c = classLoader.loadClass(serviceInterfaceName);
            Method[] methods = c.getMethods();
            for (int i = 0; i < methods.length; i++) {
                // empty message or message name is *
                if (methodName.equals(ALL_METHODS)) {
                    allMethodsInSEI.add(new MethodDescriptor(methods[i]));
                // overloading methods with same method name
                } else if (methodName.equals(methods[i].getName())) {
                    allMethodsInSEI.add(new MethodDescriptor(methods[i]));
                }
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Error occurred", e);
            // if there is exception in the class loading
            // then we just return the empty list
        }
        return allMethodsInSEI;
    }
}
