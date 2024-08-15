/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.inbound;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.logging.LogDomains;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.ejb.api.MessageBeanListener;
import org.glassfish.ejb.api.MessageBeanProtocolManager;

/**
 * This class handles the implementation of two interfaces:
 * 1) jakarta.resource.spi.endpoint.MessageEndpoint;
 * 2) any message listener type (e.g. jakarta.jms.MessageListener,
 * OR javax.xml.messaging.OnewayListener)
 *
 * @author Qingqing Ouyang
 */

public final class MessageEndpointInvocationHandler
        implements InvocationHandler {

    private MessageBeanListener listener_;
    private boolean beforeDeliveryCalled = false;

    private boolean throwTransactedExceptions_ = true;
    private MessageBeanProtocolManager messageBeanPM_;

    private static final String MESSAGE_ENDPOINT
            = "jakarta.resource.spi.endpoint.MessageEndpoint";

    private static final String THROW_TRANSACTED_EXCEPTIONS_PROP
            = "resourceadapter.throw.transacted.exceptions";

    private static final Logger logger =
            LogDomains.getLogger(MessageEndpointInvocationHandler.class, LogDomains.RSR_LOGGER);

    //private final Agent callFlowAgent = Switch.getSwitch().getCallFlowAgent();

    /**
     * Constructs a MessageEndpointInvocationHandler.
     *
     * @param listener <code>MessageBeanListener</code> to deliver messages.
     * @param pm       Protocol object to MDB container.
     */
    public MessageEndpointInvocationHandler(MessageBeanListener listener,
                                            MessageBeanProtocolManager pm) {
        this.listener_ = listener;
        this.messageBeanPM_ = pm;

        throwTransactedExceptions_ =
                ConnectorConstants.THROW_TRANSACTED_EXCEPTIONS;

        if (throwTransactedExceptions_ != true) {
            logger.info(ConnectorConstants.THROW_TRANSACTED_EXCEPTIONS_PROP +
                    " set to false");
        }

    }

    /**
     * Invokes the method
     *
     * @param proxy  Object
     * @param method <code>Method</code> to be executed.
     * @param args   Arguments
     * @throws Throwable.
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {

        // NOTE : be careful with "args" parameter.  It is null
        //        if method signature has 0 arguments.

        String methodClassName = method.getDeclaringClass().getName();
        String methodName = method.getName();
        Object returnValue = null;

        if (logger.isLoggable(Level.FINEST)) {

            String msg = "Invoking method [" + methodName
                    + "] from class [" + methodClassName + "]";
            logger.log(Level.FINEST, msg);
        }

        // delegate the beforeDelivery and afterDelivery calls
        // to the MDB container
        if (MESSAGE_ENDPOINT.equals(methodClassName)) {

            if ("beforeDelivery".equals(methodName)) {
                Method onMessageMethod = (Method) args[0];
                beforeDeliveryCalled = true;
                listener_.beforeMessageDelivery(onMessageMethod, false);

            } else if ("afterDelivery".equals(methodName)) {
                beforeDeliveryCalled = false; //reset
                listener_.afterMessageDelivery();

            } else if ("release".equals(methodName)) {
                messageBeanPM_.destroyMessageBeanListener(listener_);
            } else {
                logger.log(Level.SEVERE, "endpointfactory.method_not_defined",
                        new Object[]{methodName, MESSAGE_ENDPOINT});
                throw new RuntimeException(methodName);
            }

        } else if ("java.lang.Object".equals(methodClassName)) {
            returnValue = invokeJavaObjectMethod(this, method, args);

        } else { //the rest are considered methods for message delivery

            //RA did not call beforeDelivery, handle it here
            if (!beforeDeliveryCalled) {
                JavaEETransactionManager txManager =
                        ConnectorRuntime.getRuntime().getTransactionManager();
                boolean txImported = (txManager.getTransaction() != null);
                listener_.beforeMessageDelivery(method, txImported);
            }

            try {
                //returnValue = listener_.deliverMessage(method, args);
                //TODO startCallFlowAgent();


                returnValue = listener_.deliverMessage(args);
            } catch (Throwable ex) {
                if (messageBeanPM_.isDeliveryTransacted(method)) {
                    if (throwTransactedExceptions_) {
                        throw ex;
                    } else {
                        logger.log(Level.INFO, "Resource adapter eating " +
                                " transacted exception", ex);
                    }
                } else {
                    throw ex;
                }
            } finally {
                //TODO stopCallFlowAgent();

                //assume that if the RA didn't call beforeDelivery, it
                //would not call afterDelivery.  o.w. it will be hard to
                //to determine when to pair the afterDelivery call.
                if (!beforeDeliveryCalled) {
                    listener_.afterMessageDelivery();
                }
                beforeDeliveryCalled = false;
            }
        }
        return returnValue;
    }

/*
    private void stopCallFlowAgent() {
        // Send end notification to call flow agent
        try{
                    callFlowAgent.endTime();
                    callFlowAgent.requestEnd();
                } catch (Exception ex){
            logger.log(Level.WARNING, "Call Flow Agent threw exception" + ex);
        }
    }

    private void startCallFlowAgent() {
        // Notify Call Flow Agent.
        try{
                    callFlowAgent.requestStart(
                            RequestType.REMOTE_ASYNC_MESSAGE);
                    callFlowAgent.startTime(
                            ContainerTypeOrApplicationType.EJB_CONTAINER);
                    // This is an opportunity to provide callerIPAddress and
            // remote user name. But since this information is not
            // currently available. So, we don't call
            // callFlowAgent.setRequestInfo().
        } catch (Exception ex){
            logger.log(Level.WARNING, "Call Flow Agent threw exception" + ex);
        }
    }
*/

    /**
     * This is the same implementation in
     * com.sun.ejb.container.InvocationHandlerUtil
     * Need to abstract out at some point.
     */
    private Object invokeJavaObjectMethod(
            InvocationHandler handler, Method method, Object[] args)
            throws RuntimeException {

        Object returnValue = null;

        // Can only be one of :
        //     boolean java.lang.Object.equals(Object)
        //     int     java.lang.Object.hashCode()
        //     String  java.lang.Object.toString()
        //
        // Optimize by comparing as few characters as possible.

        switch (method.getName().charAt(0)) {
            case 'e':
                Object other = Proxy.isProxyClass(args[0].getClass()) ?
                        Proxy.getInvocationHandler(args[0]) : args[0];
                returnValue = Boolean.valueOf(handler.equals(other));
                break;
            case 'h':
                returnValue = Integer.valueOf(handler.hashCode());
                break;
            case 't':
                returnValue = handler.toString();
                break;
            default:
                throw new RuntimeException(method.getName());
        }

        return returnValue;
    }
}

