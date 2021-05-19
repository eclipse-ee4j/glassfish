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
import com.sun.enterprise.deployment.annotation.handlers.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.AdminObject;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.resource.spi.AdministeredObject;
import jakarta.resource.spi.ResourceAdapterAssociation;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.logging.Level;
import java.io.Externalizable;
import java.io.Serializable;

import org.glassfish.apf.*;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * Jagadish Ramu
 */
@Service
@AnnotationHandlerFor(AdministeredObject.class)
public class AdministeredObjectHandler extends AbstractHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(AdministeredObjectHandler.class);

    public HandlerProcessingResult processAnnotation(AnnotationInfo element) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        AdministeredObject adminObject = (AdministeredObject) element.getAnnotation();

        if (aeHandler instanceof RarBundleContext) {
            RarBundleContext rarContext = (RarBundleContext) aeHandler;
            ConnectorDescriptor desc = rarContext.getDescriptor();

            Class c = (Class) element.getAnnotatedElement();
            String adminObjectClassName = c.getName();

            Class[] adminObjectInterfaceClasses = adminObject.adminObjectInterfaces();
            //When "adminObjectInterfaces()" is specified, add one admin-object entry per interface
            if (adminObjectInterfaceClasses != null && adminObjectInterfaceClasses.length > 0) {
                for (Class adminObjectInterface : adminObjectInterfaceClasses) {
                    processAdminObjectInterface(adminObjectClassName, adminObjectInterface.getName(), desc);
                }
            } else {
                List<Class> interfacesList = deriveAdminObjectInterfacesFromHierarchy(c);

                if (interfacesList.size() == 1) {
                    Class intf = interfacesList.get(0);
                    String intfName = intf.getName();
                    processAdminObjectInterface(adminObjectClassName, intfName, desc);
                } else {
                    //TODO V3 this case is, multiple interfaces implemented, no "adminObjectInterfaces()" attribute defined,
                    // should we check the DD whether this Impl class is already specified in any of "admin-object" elements ?
                    // If present, return. If not present, throw exception ?
                }
            }
        } else {
            getFailureResult(element, "not a rar bundle context", true);
        }
        return getDefaultProcessedResult();
    }

    public static List<Class> deriveAdminObjectInterfacesFromHierarchy(Class c) {
        Class interfaces[] = c.getInterfaces();

        List<Class> interfacesList = new ArrayList<Class>(Arrays.asList(interfaces));
        interfacesList.remove(Serializable.class);
        interfacesList.remove(Externalizable.class);
        interfacesList.remove(ResourceAdapterAssociation.class);
        return interfacesList;
    }

    private void processAdminObjectInterface(String adminObjectClassName, String adminObjectInterfaceName,
                                             ConnectorDescriptor desc) {
        Set ddAdminObjects = desc.getAdminObjects();
        //merge DD and annotation values of admin-objects
        //merge involves simple union
        boolean ignore = false;
        for (Object o : ddAdminObjects) {
            AdminObject ddAdminObject = (AdminObject) o;
            if (ddAdminObject.getAdminObjectInterface().equals(adminObjectInterfaceName) &&
                    ddAdminObject.getAdminObjectClass().equals(adminObjectClassName)) {
                ignore = true;
                break;
            }
        }
        if (!ignore) {
            AdminObject ao = new AdminObject(adminObjectInterfaceName, adminObjectClassName);
            desc.addAdminObject(ao);
        }else{
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST,"Ignoring administered object annotation " +
                        "[ "+adminObjectInterfaceName+"," + adminObjectClassName + "] as it is already defined ");
            }
        }
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
