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

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.handlers.PostProcessor;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateful;
import jakarta.ejb.Stateless;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the jakarta.ejb.Asynchronous
 * annotation on the Bean class.
 *
 * @author Marina Vatkina
 */
@Service
@AnnotationHandlerFor(Asynchronous.class)
public class AsynchronousHandler extends AbstractAttributeHandler
        implements PostProcessor<EjbContext> {

    public AsynchronousHandler() {
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbContext[] ejbContexts) throws AnnotationProcessorException {

        for (EjbContext ejbContext : ejbContexts) {
            EjbDescriptor ejbDesc = (EjbDescriptor) ejbContext.getDescriptor();

            if (ElementType.TYPE.equals(ainfo.getElementType())) {
                ejbContext.addPostProcessInfo(ainfo, this);
            } else {
                Method annMethod = (Method) ainfo.getAnnotatedElement();
                setAsynchronous(annMethod, ejbDesc);
            }
        }

        return getDefaultProcessedResult();
    }

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    public Class<? extends Annotation>[] getTypeDependencies() {

        return new Class[] {
            Local.class, Remote.class, Stateful.class, Stateless.class, Singleton.class};

    }

    protected boolean supportTypeInheritance() {
        return true;
    }

    /**
     * Set the default value (from class type annotation) on all
     * methods that don't have a value.
     * Class type annotation applies to all EJB 3.x Local/Remote/no-interface
     * views in which  that  business method is exposed for that bean.
     */
    public void postProcessAnnotation(AnnotationInfo ainfo, EjbContext ejbContext)
            throws AnnotationProcessorException {
        EjbDescriptor ejbDesc = (EjbDescriptor) ejbContext.getDescriptor();
        Class classAn = (Class)ainfo.getAnnotatedElement();

        Method[] methods = classAn.getDeclaredMethods();
        for (Method m0 : methods) {
            setAsynchronous(m0, ejbDesc);
        }
    }

    private void setAsynchronous(Method m0, EjbDescriptor ejbDesc)
            throws AnnotationProcessorException {

        // All methods processed on bean class / superclass apply to all local/remote
        // business interfaces
        setAsynchronous(m0, ejbDesc, null);
    }

    /**
     * Designate a method as asynchronous in the deployment descriptor
     * @param methodIntf  null if processed on bean class / superclass.  Otherwise,
     *                    set to the remote/local client view of the associated interface
     * @throws AnnotationProcessorException
     */
    private void setAsynchronous(Method m0, EjbDescriptor ejbDesc, String methodIntf)
            throws AnnotationProcessorException {

        if( !ejbDesc.getType().equals(EjbSessionDescriptor.TYPE)) {
            throw new AnnotationProcessorException("Invalid asynchronous method " + m0 +
                 "@Asynchronous is only permitted for session beans");
        }


        EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;

        MethodDescriptor methodDesc = (methodIntf == null) ?
                new MethodDescriptor(m0) : new MethodDescriptor(m0, methodIntf);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Adding asynchronous method " + methodDesc);
        }


        // There is no way to "turn off" the asynchronous designation in the
        // deployment descriptor, so we don't need to do any override checks
        // here.  Just always add any async methods.
        sessionDesc.addAsynchronousMethod(methodDesc);

    }

}
