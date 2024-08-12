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

package com.sun.enterprise.connectors.deployment.annotation.handlers;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.InboundResourceAdapter;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.deployment.annotation.context.RarBundleContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.resource.spi.Activation;

import java.lang.annotation.Annotation;
import java.util.logging.Level;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.ResultType;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;
import org.jvnet.hk2.annotations.Service;


/**
 * @author Jagadish Ramu
 */
@Service
@AnnotationHandlerFor(Activation.class)
public class ActivationHandler extends AbstractHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ActivationHandler.class);

    public HandlerProcessingResult processAnnotation(AnnotationInfo element) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        Activation activation = (Activation) element.getAnnotation();
        if (aeHandler instanceof RarBundleContext) {
            RarBundleContext rarContext = (RarBundleContext) aeHandler;
            ConnectorDescriptor desc = rarContext.getDescriptor();

            //process annotation only if message-listeners are provided
            if (activation.messageListeners().length > 0) {
                //initialize inbound if it was not done already
                if (!desc.getInBoundDefined()) {
                    desc.setInboundResourceAdapter(new InboundResourceAdapter());
                }

                InboundResourceAdapter ira = desc.getInboundResourceAdapter();

                //get the activation-spec implementation class-name
                Class c = (Class) element.getAnnotatedElement();
                String activationSpecClass = c.getName();

                //process all message-listeners, ensure that no duplicate message-listener-types are found
                for (Class mlClass : activation.messageListeners()) {
                    MessageListener ml = new MessageListener();
                    ml.setActivationSpecClass(activationSpecClass);
                    ml.setMessageListenerType(mlClass.getName());

                    if (!ira.hasMessageListenerType(mlClass.getName())) {
                        ira.addMessageListener(ml);
                    }// else {
                        // ignore the duplicates
                        // duplicates can be via :
                        // (i) message listner defined in DD
                        // (ii) as part of this particular annotation processing,
                        // already this message-listener-type is defined
                        //TODO V3 how to handle (ii)
                    //}
                }
            }
        } else {
            getFailureResult(element, "Not a rar bundle context", true);
        }
        return getDefaultProcessedResult();
    }

    public Class<? extends Annotation>[] getTypeDependencies() {
        return null;
    }

    /**
     * @return a default processed result
     */
    protected HandlerProcessingResult getDefaultProcessedResult() {
        return HandlerProcessingResultImpl.getDefaultResult(
                getAnnotationType(), ResultType.PROCESSED);
    }

    private HandlerProcessingResultImpl getFailureResult(AnnotationInfo element, String message, boolean doLog) {
        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();
        result.addResult(getAnnotationType(), ResultType.FAILED);
        if (doLog) {
            Class c = (Class) element.getAnnotatedElement();
            String className = c.getName();
            Object args[] = new Object[]{
                element.getAnnotation(),
                className,
                message,
            };
            String localString = localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.connectorannotationfailure",
                    "failed to handle annotation [ {0} ] on class [ {1} ], reason : {2}", args);
            logger.log(Level.WARNING, localString);
        }
        return result;
    }
}
