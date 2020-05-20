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

import com.sun.enterprise.deployment.annotation.context.RarBundleContext;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.AuthMechanism;
import com.sun.enterprise.deployment.PoolManagerConstants;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.resource.spi.AuthenticationMechanism;
import jakarta.resource.spi.Connector;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import org.glassfish.apf.*;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;
import org.jvnet.hk2.annotations.Service;


/**
 * @author Jagadish Ramu
 */
@Service
@AnnotationHandlerFor(AuthenticationMechanism.class)
public class AuthenticationMechanismHandler extends AbstractHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(AuthenticationMechanismHandler.class);

    public HandlerProcessingResult processAnnotation(AnnotationInfo element) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        AuthenticationMechanism authMechanism = (AuthenticationMechanism) element.getAnnotation();

        if (aeHandler instanceof RarBundleContext) {
            boolean isConnectionDefinition = hasConnectorAnnotation(element);
            if (isConnectionDefinition) {
                RarBundleContext rarContext = (RarBundleContext) aeHandler;
                ConnectorDescriptor desc = rarContext.getDescriptor();
                if (!desc.getOutBoundDefined()) {
                    OutboundResourceAdapter ora = new OutboundResourceAdapter();
                    desc.setOutboundResourceAdapter(ora);
                }
                OutboundResourceAdapter ora = desc.getOutboundResourceAdapter();
                String[] description = authMechanism.description();
                int authMechanismValue = getAuthMechVal(authMechanism.authMechanism());
                AuthenticationMechanism.CredentialInterface ci = authMechanism.credentialInterface();
                String credentialInterface = ora.getCredentialInterfaceName(ci);
                //XXX: Siva: For now use the first description
                String firstDesc = "";
                if(description.length > 0) {
                    firstDesc = description[0];
                }
                AuthMechanism auth = new AuthMechanism(firstDesc, authMechanismValue, credentialInterface);
                ora.addAuthMechanism(auth);
            } else {
                getFailureResult(element, "Not a @Connector annotation : @AuthenticationMechanism must " +
                        "be specified along with @Connector annotation", true);
            }
        } else {
            getFailureResult(element, "Not a rar bundle context", true);
        }
        return getDefaultProcessedResult();
    }

    private boolean hasConnectorAnnotation(AnnotationInfo element) {
        Class c = (Class) element.getAnnotatedElement();
        return c.getAnnotation(Connector.class) != null;
    }

    public Class<? extends Annotation>[] getTypeDependencies() {
        return getConnectorAnnotationTypes();
    }

    /**
     * @return a default processed result
     */
    protected HandlerProcessingResult getDefaultProcessedResult() {
        return HandlerProcessingResultImpl.getDefaultResult(
                getAnnotationType(), ResultType.PROCESSED);
    }

    /**
     * Set the authentication mechanism value.
     */
    public int getAuthMechVal(String value) {
        int authMechVal;
        if ((value.trim()).equals(ConnectorTagNames.DD_BASIC_PASSWORD))
            authMechVal = PoolManagerConstants.BASIC_PASSWORD;
        else if ((value.trim()).equals(ConnectorTagNames.DD_KERBEROS))
            authMechVal = PoolManagerConstants.KERBV5;
        else throw new IllegalArgumentException("Invalid auth-mech-type");// put this in localStrings...
        return authMechVal;
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
