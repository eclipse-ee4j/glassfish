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

import jakarta.ejb.DependsOn;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

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
 * This handler is responsible for handling the jakarta.ejb.Singleton
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(Singleton.class)
public class SingletonHandler extends AbstractEjbHandler {


    public SingletonHandler() {}

    /**
     * Return the name attribute of given annotation.
     * @param annotation
     * @return name
     */
    protected String getAnnotatedName(Annotation annotation) {
        Singleton slAn = (Singleton) annotation;
        return slAn.name();
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
            if( sessionDesc.isSessionTypeSet() && !sessionDesc.isSingleton() ) {
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
        newDescriptor.setSessionType(EjbSessionDescriptor.SINGLETON);

        doSingletonSpecificProcessing(newDescriptor, ejbClass);

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

        EjbSessionDescriptor ejbSingletonDescriptor = (EjbSessionDescriptor) ejbDesc;

        Class ejbClass = (Class) ainfo.getAnnotatedElement();
        Singleton singleton = (Singleton) ainfo.getAnnotation();

        // set session bean type in case it wasn't set in a sparse ejb-jar.xml.
        if( !ejbSingletonDescriptor.isSessionTypeSet() ) {
            ejbSingletonDescriptor.setSessionType(EjbSessionDescriptor.SINGLETON);
        }

        doDescriptionProcessing(singleton.description(), ejbDesc);
        doMappedNameProcessing(singleton.mappedName(), ejbDesc);

        doSingletonSpecificProcessing(ejbSingletonDescriptor, ejbClass);

        return setBusinessAndHomeInterfaces(ejbDesc, ainfo);
    }

    private void doSingletonSpecificProcessing(EjbSessionDescriptor desc, Class ejbClass) {
        Class clz = ejbClass;

        Startup st = (Startup) clz.getAnnotation(Startup.class);
        if (st != null) {
            // Only set if not explicitly set in .xml
            desc.setInitOnStartupIfNotAlreadySet(true);
        }

        DependsOn dep = (DependsOn) clz.getAnnotation(DependsOn.class);
        if (dep != null) {
            desc.setDependsOnIfNotSet(dep.value());
        }
    }
}
