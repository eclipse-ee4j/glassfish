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

package org.glassfish.ejb.mdb.deployment.annotation.handlers;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.logging.Level;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.annotation.handlers.AbstractEjbHandler;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the jakarta.ejb.MessageDriven
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(MessageDriven.class)
public class MessageDrivenHandler extends AbstractEjbHandler {

    /** Creates a new instance of MessageDrivenHandler */
    public MessageDrivenHandler() {
    }

    /**
     * Return the name attribute of given annotation.
     * @param annotation
     * @return name
     */
    protected String getAnnotatedName(Annotation annotation) {
        MessageDriven mdAn = (MessageDriven)annotation;
        return mdAn.name();
    }

    /**
     * Check if the given EjbDescriptor matches the given Annotation.
     * @param ejbDesc
     * @param annotation
     * @return boolean check for validity of EjbDescriptor
     */
    protected boolean isValidEjbDescriptor(EjbDescriptor ejbDesc,
            Annotation annotation) {
        return EjbMessageBeanDescriptor.TYPE.equals(ejbDesc.getType());
    }

    /**
     * Create a new EjbDescriptor for a given elementName and AnnotationInfo.
     * @param elementName
     * @param ainfo
     * @return a new EjbDescriptor
     */
    protected EjbDescriptor createEjbDescriptor(String elementName,
            AnnotationInfo ainfo) throws AnnotationProcessorException {

        AnnotatedElement ae = ainfo.getAnnotatedElement();
        EjbMessageBeanDescriptor newDescriptor = new EjbMessageBeanDescriptor();
        Class ejbClass = (Class)ae;
        newDescriptor.setName(elementName);
        newDescriptor.setEjbClassName(ejbClass.getName());
        return newDescriptor;
    }

    /**
     * Set Annotation information to Descriptor.
     * This method will also be invoked for an existing descriptor with
     * annotation as user may not specific a complete xml.
     * @param ejbDesc
     * @param ainfo
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult setEjbDescriptorInfo(
            EjbDescriptor ejbDesc, AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        MessageDriven mdAn = (MessageDriven)ainfo.getAnnotation();
        Class ejbClass = (Class)ainfo.getAnnotatedElement();
        EjbMessageBeanDescriptor ejbMsgBeanDesc =
                (EjbMessageBeanDescriptor)ejbDesc;

        HandlerProcessingResult procResult =
            setMessageListenerInterface(
                    mdAn, ejbMsgBeanDesc, ejbClass, ainfo);

        doDescriptionProcessing(mdAn.description(), ejbMsgBeanDesc);
        doMappedNameProcessing(mdAn.mappedName(), ejbMsgBeanDesc);

        for (ActivationConfigProperty acProp : mdAn.activationConfig()) {
            EnvironmentProperty envProp = new EnvironmentProperty(
                    acProp.propertyName(), acProp.propertyValue(), "");
                                                // with empty description
            // xml override
            if (ejbMsgBeanDesc.getActivationConfigValue(envProp.getName()) == null) {
                ejbMsgBeanDesc.putActivationConfigProperty(envProp);
            }
        }

        return procResult;
    }

    private HandlerProcessingResult setMessageListenerInterface(
            MessageDriven mdAn, EjbMessageBeanDescriptor msgEjbDesc,
            Class ejbClass, AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        String intfName = null;

        // If @MessageDriven contains message listener interface, that takes
        // precedence.  Otherwise, the message listener interface is derived
        // from the implements clause.

        if( mdAn.messageListenerInterface() != Object.class ) {
            intfName = mdAn.messageListenerInterface().getName();
        } else {
            for(Class next : ejbClass.getInterfaces()) {
                if( !excludedFromImplementsClause(next) ) {
                    if( intfName == null ) {
                        intfName = next.getName();
                    } else {
                        EjbBundleDescriptorImpl currentBundle = (EjbBundleDescriptorImpl)
                        ((EjbBundleContext)ainfo.getProcessingContext().getHandler()).getDescriptor();
                        log(Level.SEVERE, ainfo,
                            localStrings.getLocalString(
                            "enterprise.deployment.annotation.handlers.ambiguousimplementsclausemdb",
                            "Implements clause for 3.x message driven bean class {0} in {1} declares more than one potential message-listener interface.  In this case, the @MessageDriven.messageListenerInterface() attribute must be used to specify the message listener interface.",
                             new Object[] { ejbClass,
                             currentBundle.getModuleDescriptor().getArchiveUri() }));
                        return getDefaultFailedResult();
                    }
                }
            }
        }

        // if it's still null, check whether it's defined through
        // deployment descriptor
        // note: the descriptor class has a default value
        // for the interface: jakarta.jms.MessageListener
        // so intfName after this set, will never be null
        if (intfName == null) {
            intfName = msgEjbDesc.getMessageListenerType();
        }

        msgEjbDesc.setMessageListenerType(intfName);

        return getDefaultProcessedResult();
    }
}
