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

package org.glassfish.ejb.deployment.annotation.handlers;

import jakarta.ejb.Stateful;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the jakarta.ejb.Stateful
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(Stateful.class)
public class StatefulHandler extends AbstractEjbHandler {

    /** Creates a new instance of StatefulHandler */
    public StatefulHandler() {
    }

    /**
     * Return the name attribute of given annotation.
     * @param annotation
     * @return name
     */
    protected String getAnnotatedName(Annotation annotation) {
        Stateful sfAn = (Stateful)annotation;
        return sfAn.name();
    }

    /**
     * Check if the given EjbDescriptor matches the given Annotation.
     * @param ejbDesc
     * @param annotation
     * @return boolean check for validity of EjbDescriptor
     */
    protected boolean isValidEjbDescriptor(EjbDescriptor ejbDesc,
            Annotation annotation) {
        boolean isValid = EjbSessionDescriptor.TYPE.equals(ejbDesc.getType());

        if( isValid ) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;
            // Only check specific session-bean type if it's set in the descriptor.
            // Otherwise it was probably populated with a sparse ejb-jar.xml and
            // we'll set the type later.
            if( sessionDesc.isSessionTypeSet() && !sessionDesc.isStateful() ) {
                isValid = false;
            }
        }

        return  isValid;
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
        Class ejbClass = (Class)ae;
        EjbSessionDescriptor newDescriptor = new EjbSessionDescriptor();
        newDescriptor.setName(elementName);
        newDescriptor.setEjbClassName(ejbClass.getName());
        newDescriptor.setSessionType(EjbSessionDescriptor.STATEFUL);
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

        EjbSessionDescriptor ejbSessionDesc = (EjbSessionDescriptor)ejbDesc;

         // set session bean type in case it wasn't set in a sparse ejb-jar.xml.
        if( !ejbSessionDesc.isSessionTypeSet() ) {
            ejbSessionDesc.setSessionType(EjbSessionDescriptor.STATEFUL);
        }


        Stateful sful = (Stateful) ainfo.getAnnotation();
        doDescriptionProcessing(sful.description(), ejbDesc);
        doMappedNameProcessing(sful.mappedName(), ejbDesc);
        // set passivation capable property in case it wasn't set in ejb-jar.xml
        if( !ejbSessionDesc.isPassivationCapableSet()) {
            ejbSessionDesc.setPassivationCapable(sful.passivationCapable());
        }

        return setBusinessAndHomeInterfaces(ejbDesc, ainfo);
    }
}
