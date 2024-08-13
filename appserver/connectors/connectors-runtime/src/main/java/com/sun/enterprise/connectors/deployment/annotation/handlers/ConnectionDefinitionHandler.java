/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.annotation.context.RarBundleContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.resource.spi.ConnectionDefinition;
import jakarta.resource.spi.ManagedConnectionFactory;

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

@Service
@AnnotationHandlerFor(ConnectionDefinition.class)
public class ConnectionDefinitionHandler extends AbstractHandler  {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ConnectionDefinitionHandler.class);

    public void processAnnotation(AnnotationInfo element, ConnectionDefinition defn)
            throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        handleAnnotation(aeHandler, defn, element);
    }

    public HandlerProcessingResult processAnnotation(AnnotationInfo element) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        ConnectionDefinition connDefn = (ConnectionDefinition) element.getAnnotation();

        if (aeHandler instanceof RarBundleContext) {
            handleAnnotation(aeHandler, connDefn, element);
        } else {
            getFailureResult(element, "not a rar bundle context", true);
        }
        return getDefaultProcessedResult();
    }

    private void handleAnnotation(AnnotatedElementHandler aeHandler, ConnectionDefinition connDefn, AnnotationInfo element) {
        RarBundleContext rarContext = (RarBundleContext) aeHandler;
        ConnectorDescriptor desc = rarContext.getDescriptor();

        Class c = (Class) element.getAnnotatedElement();
        String targetClassName = c.getName();
        if (ManagedConnectionFactory.class.isAssignableFrom(c)) {

            if (!desc.getOutBoundDefined()) {
                OutboundResourceAdapter ora = new OutboundResourceAdapter();
                desc.setOutboundResourceAdapter(ora);
            }

            OutboundResourceAdapter ora = desc.getOutboundResourceAdapter();

            if (!ora.hasConnectionDefDescriptor(connDefn.connectionFactory().getName())) {
                ConnectionDefDescriptor cdd = new ConnectionDefDescriptor();
                cdd.setConnectionFactoryImpl(connDefn.connectionFactoryImpl().getName());
                cdd.setConnectionFactoryIntf(connDefn.connectionFactory().getName());
                cdd.setConnectionIntf(connDefn.connection().getName());
                cdd.setConnectionImpl(connDefn.connectionImpl().getName());

                cdd.setManagedConnectionFactoryImpl(targetClassName);

                ora.addConnectionDefDescriptor(cdd);
            }// else {
                // ignore the duplicates
                // duplicates can be via :
                // (i) connection-definition defined in DD
                // (ii) as part of this particular annotation processing,
                // already this connection-definition is defined
                //TODO V3 how to handle (ii)
            //}
        } else {
            getFailureResult(element, "Cant handle ConnectionDefinition annotation as the annotated class does not " +
                    "implement ManagedConnectionFactory", true);
        }
    }

    /**
     * @return a default processed result
     */
    protected HandlerProcessingResult getDefaultProcessedResult() {
        return HandlerProcessingResultImpl.getDefaultResult(
                getAnnotationType(), ResultType.PROCESSED);
    }


    public Class<? extends Annotation>[] getTypeDependencies() {
        return null;
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

