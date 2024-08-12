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
import com.sun.enterprise.deployment.util.TypeUtil;

import jakarta.ejb.Init;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbInitInfo;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the jakarta.ejb.Init attribute
 *
 */
@Service
@AnnotationHandlerFor(Init.class)
public class InitHandler extends AbstractAttributeHandler {

    public InitHandler() {
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbContext[] ejbContexts) throws AnnotationProcessorException {

        Init init = (Init) ainfo.getAnnotation();

        for(EjbContext next : ejbContexts) {

            EjbSessionDescriptor sessionDescriptor =
                (EjbSessionDescriptor) next.getDescriptor();

            Method m = (Method) ainfo.getAnnotatedElement();

            // Check for matching method on home and/or local home interface.

            int numMatches = 0;


            String adaptedCreateMethodName = init.value();

            try {
                if( sessionDescriptor.isRemoteInterfacesSupported() ) {
                    addInitMethod(sessionDescriptor, m,
                                  adaptedCreateMethodName, false);
                    numMatches++;
                }
            } catch(Exception e) {
            }

            try {
                if( sessionDescriptor.isLocalInterfacesSupported() ) {
                    addInitMethod(sessionDescriptor, m,
                                  adaptedCreateMethodName, true);
                    numMatches++;
                }
            } catch(Exception e) {
            }

            if( numMatches == 0 ) {
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.notmatchcreate",
                    "Unable to find matching Home create method for Init method {0} on bean {1}.",
                    new Object[] { m, sessionDescriptor.getName() }));
                return getDefaultFailedResult();
            }
        }

        return getDefaultProcessedResult();
    }

    private void addInitMethod(EjbSessionDescriptor descriptor,
                               Method beanMethod,
                               String adaptedCreateMethodName, boolean local)
        throws Exception {

        String homeIntfName = local ? descriptor.getLocalHomeClassName() :
            descriptor.getHomeClassName();

        ClassLoader cl = descriptor.getEjbBundleDescriptor().getClassLoader();

        Class homeIntf = cl.loadClass(homeIntfName);

        Method createMethod = null;
        if( (adaptedCreateMethodName == null) ||
            (adaptedCreateMethodName.equals("")) ) {
            // Can't make any assumptions about matching method name.  Could
            // be "create" or some form of create<METHOD>, so match based on
            // signature.
            for(Method next : homeIntf.getMethods()) {
                if( next.getName().startsWith("create") &&
                    TypeUtil.sameParamTypes(next, beanMethod) ) {
                    createMethod = next;
                    break;
                }
            }
            if( createMethod == null ) {
                throw new NoSuchMethodException("No matching adapted home " +
                                                "method found for @Init " +
                                                " method " + beanMethod);
            }
        } else {
            createMethod = homeIntf.getMethod(adaptedCreateMethodName,
                                              beanMethod.getParameterTypes());
        }

        MethodDescriptor beanMethodDescriptor =
            new MethodDescriptor(beanMethod, MethodDescriptor.EJB_BEAN);

        MethodDescriptor createMethodDescriptor =
            new MethodDescriptor(createMethod,
                                 ( local ?
                                   MethodDescriptor.EJB_HOME :
                                   MethodDescriptor.EJB_LOCALHOME ));

        EjbInitInfo initInfo = new EjbInitInfo();

        initInfo.setBeanMethod(beanMethodDescriptor);
        initInfo.setCreateMethod(createMethodDescriptor);

        descriptor.addInitMethod(initInfo);

    }

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAnnotationTypes();
    }
}
