/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.naming;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.spi.BadConnectionEventListener;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.DynamicallyReconfigurableResource;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.RetryableUnavailableException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;


/**
 * Invocation Handler used by proxy to connection-factory objects<br>
 *
 * @author Jagadish Ramu
 */
public class DynamicResourceReconfigurator implements InvocationHandler, DynamicallyReconfigurableResource {

    private Object actualObject;
    private ResourceInfo resourceInfo;
    private boolean invalid = false;
    private long resourceInfoVersion = 0;

    protected final static Logger _logger = LogDomains.getLogger(DynamicResourceReconfigurator.class,LogDomains.RSR_LOGGER);

    public DynamicResourceReconfigurator(Object actualObject, ResourceInfo resourceInfo){
        this.actualObject = actualObject;
        this.resourceInfo = resourceInfo;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (invalid) {
            throw new ResourceException("Resource ["+ resourceInfo +"] instance is not valid any more");
        }

        if (method.getName().equals(DynamicallyReconfigurableResource.SET_DELEGATE_METHOD_NAME) && args.length == 1) {
            setDelegate(args[0]);
        } else if (method.getName().equals(DynamicallyReconfigurableResource.SET_INVALID_METHOD_NAME)
                && args.length == 0) {
            setInvalid();
        } else {
            long version = ConnectorRegistry.getInstance().getResourceInfoVersion(resourceInfo);
            if (version == -1L) {
                // since we're not keeping the list of proxies, we need to invalidate as soon we are aware of the fact
                setInvalid();
                invoke(proxy,method,args); // just to trigger the exception
            }

            boolean status = resourceInfoVersion >= version;
            resourceInfoVersion = version;
            if (!status) {
                debug("status is outdated: " + this);
                Hashtable env = new Hashtable();
                env.put(ConnectorConstants.DYNAMIC_RECONFIGURATION_PROXY_CALL, "TRUE");
                //TODO ASR : resource-naming-service need to support "env" for module/app scope also
                ResourceNamingService namingService = ConnectorRuntime.getRuntime().getResourceNamingService();
                actualObject = namingService.lookup(resourceInfo, resourceInfo.getName(), env);
                debug("actualObject : " + actualObject);
            }else{
                debug("status is true: " + this);
            }

            debug("DynamicResourceReconfigurator : method : " + method.getName());
            try {
                return method.invoke(actualObject, args);
            } catch (InvocationTargetException ite) {
                debug("exception [ " + ite + " ] in method : " + method.getName());
                if (ite.getCause() != null && ite.getCause().getCause() != null){
                    return retryIfNeeded(proxy, method, args, ite.getCause().getCause());
                }else if(ite.getCause() != null){
                    return retryIfNeeded(proxy, method, args, ite.getCause());
                }else{
                    throw ite;
                }
            }
        }
        return null;
    }

    /**
     * retry the operation if it is of expected exception type.
     * @param proxy Proxy object
     * @param method Method to be invoked
     * @param args arguments to method
     * @param actualException ActualException thrown by the method
     * @return Result of invoking the method
     * @throws Throwable when calling the method fails.
     */
    private Object retryIfNeeded(Object proxy, Method method, Object[] args, Throwable actualException)
            throws Throwable {
        if ((actualException instanceof RetryableUnavailableException)) {
            RetryableUnavailableException rue =
                    (RetryableUnavailableException) actualException;
            if (BadConnectionEventListener.POOL_RECONFIGURED_ERROR_CODE.equals(rue.getErrorCode())) {
                debug(" DynamicResourceReconfigurator : retryable-exception in method, retrying : " +
                        method.getName());
                return invoke(proxy, method, args);
            }
        }
            throw actualException;
    }

    public void setDelegate(Object o) {
        actualObject = o;
    }

    public void setInvalid() {
        invalid = true;
    }

    private void debug(String message){
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("[DRC] : " + message);
        }
    }
}
