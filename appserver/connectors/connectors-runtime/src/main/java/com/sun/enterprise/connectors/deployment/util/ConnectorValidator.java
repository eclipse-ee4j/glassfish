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

package com.sun.enterprise.connectors.deployment.util;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.connectors.deployment.annotation.handlers.ConnectorAnnotationHandler;
import com.sun.enterprise.connectors.deployment.annotation.handlers.ConfigPropertyHandler;
import com.sun.enterprise.deployment.util.ConnectorVisitor;
import com.sun.enterprise.deployment.util.DefaultDOLVisitor;
import com.sun.logging.LogDomains;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.Connector;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@PerLookup
public class ConnectorValidator extends DefaultDOLVisitor implements ConnectorVisitor {

    private Logger _logger = LogDomains.getLogger(ConnectorValidator.class, LogDomains.RSR_LOGGER);

    public void accept (BundleDescriptor descriptor) {
        if (descriptor instanceof ConnectorDescriptor) {
            ConnectorDescriptor connectorDesc = (ConnectorDescriptor)descriptor;
            accept(connectorDesc);
        }
    }

    public void accept(ConnectorDescriptor descriptor) {

        //make sure that the ActivationSpec class implement ActivationSpec interface.
        validateActivationSpec(descriptor);

        //validate & process annotations if a valid connector annotation is not already processed
        if (!descriptor.getValidConnectorAnnotationProcessed()) {
            Set<AnnotationInfo> annotations = descriptor.getConnectorAnnotations();
            String raClass = descriptor.getResourceAdapterClass();

            if (annotations.size() == 0) {
                return;
            }

            //only one annotation is present
            if (annotations.size() == 1) {
                Iterator<AnnotationInfo> it = annotations.iterator();
                AnnotationInfo annotationInfo = it.next();
                Class claz = (Class) annotationInfo.getAnnotatedElement();
                Connector connector = (Connector) annotationInfo.getAnnotation();
                ConnectorAnnotationHandler.processDescriptor(claz, connector, descriptor);
                Collection<AnnotationInfo> configProperties = descriptor.getConfigPropertyAnnotations(claz.getName());
                if (configProperties != null) {
                    for (AnnotationInfo ai : configProperties) {
                        ConfigPropertyHandler handler = new ConfigPropertyHandler();
                        try {
                            handler.processAnnotation(ai);
                        } catch (AnnotationProcessorException e) {
                            RuntimeException re = new RuntimeException("Unable to process ConfigProperty " +
                                    "annotation in class ["+claz.getName()+"] : " + e.getMessage());
                            re.initCause(e);
                            throw re;
                        }
                    }
                }
            } else {

                // if raClass is specified in the descriptor and multiple annotations not matching the raClass
                // are present, ignore them.
                if (raClass == null || raClass.equals("")) {
                    //all the cases below are unacceptable, fail deployment
                    if (annotations.size() > 1) {
                        throw new RuntimeException("cannot determine appropriate @Connector annotation as multiple " +
                                "annotations are present");
                    }
                }
            }
        }

        //check whether outbound is defined, if so, atleast one connection-definition must be present
        if(descriptor.getOutBoundDefined()){
            Set connectionDefinitions = descriptor.getOutboundResourceAdapter().getConnectionDefs();
            if(connectionDefinitions.size() == 0){
                throw new RuntimeException("Invalid connector descriptor for RAR [ "+descriptor.getName()+" ], when " +
                        "outbound-resource-adapter is specified," +
                        "atleast one connection-definition must be specified either via annotation or via descriptor");
            }
        }
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, descriptor.toString());
        }

        processConfigProperties(descriptor);

        //processed all annotations, clear from book-keeping
        descriptor.getConnectorAnnotations().clear();
        descriptor.getAllConfigPropertyAnnotations().clear();
        descriptor.getConfigPropertyProcessedClasses().clear();
    }

    private void validateActivationSpec(ConnectorDescriptor descriptor) {
        if (descriptor.getInBoundDefined()) {
            InboundResourceAdapter ira = descriptor.getInboundResourceAdapter();
            Set messageListeners = ira.getMessageListeners();
            Iterator it = messageListeners.iterator();
            while (it.hasNext()) {
                MessageListener ml = (MessageListener) it.next();
                String activationSpecClass = ml.getActivationSpecClass();
                if (activationSpecClass != null && !activationSpecClass.equals("")) {
                    Class clazz = getClass(descriptor, activationSpecClass);
                    boolean validClass =  false;
                    if(clazz != null){
                        if(ActivationSpec.class.isAssignableFrom(clazz)){
                            validClass = true;
                        }
                    }
                    if(!validClass){
                        throw new IllegalArgumentException("Class ["+activationSpecClass+"] does not " +
                                "implement jakarta.resource.spi.ActivationSpec interface, but " +
                                "defined in MessageListener ["+ml.getMessageListenerType()+"] of RAR ["+ descriptor.getName() + "]");
                    }
                }else{
                    throw new RuntimeException("ActivationSpec class cannot be null or empty for message-listener" +
                            "["+ml.getMessageListenerType()+"] of RAR ["+descriptor.getName()+"]");
                }
            }
        }
    }

    /**
     * Process for ConfigProperty annotation for rar artifact classes where @ConfigProperty is
     * not defined in them, but their superclasses as we would have ignored
     * ConfigProperty annotations in non-concrete rar artifacts during
     * annotation processing phase
     * @param desc ConnectorDescriptor
     */
    private void processConfigProperties(ConnectorDescriptor desc)  {

        String raClass = desc.getResourceAdapterClass();
        if (raClass != null && !raClass.equals("")) {
            if (!desc.getConfigPropertyProcessedClasses().contains(raClass)) {
                Class claz = getClass(desc, raClass);
                ConfigPropertyHandler.processParent(claz, desc.getConfigProperties());
            }
        }
        if (desc.getOutBoundDefined()) {
            OutboundResourceAdapter ora = desc.getOutboundResourceAdapter();
            Set connectionDefs = ora.getConnectionDefs();
            Iterator it = connectionDefs.iterator();
            while (it.hasNext()) {
                ConnectionDefDescriptor connectionDef = (ConnectionDefDescriptor) it.next();
                //connection-factory class is the unique identifier.
                String connectionFactoryClass = connectionDef.getConnectionFactoryIntf();
                if (connectionFactoryClass != null && !connectionFactoryClass.equals("")) {
                    if (!desc.getConfigPropertyProcessedClasses().contains(connectionFactoryClass)) {
                        Class claz = getClass(desc, connectionDef.getManagedConnectionFactoryImpl());
                        ConfigPropertyHandler.processParent(claz, connectionDef.getConfigProperties());
                    }
                }
            }
        }

        if (desc.getInBoundDefined()) {
            InboundResourceAdapter ira = desc.getInboundResourceAdapter();
            Set messageListeners = ira.getMessageListeners();
            Iterator it = messageListeners.iterator();
            while (it.hasNext()) {
                MessageListener ml = (MessageListener) it.next();
                String activationSpecClass = ml.getActivationSpecClass();
                if (activationSpecClass != null && !activationSpecClass.equals("")) {
                    if (!desc.getConfigPropertyProcessedClasses().contains(activationSpecClass)) {
                        Class claz = getClass(desc, activationSpecClass);
                        ConfigPropertyHandler.processParent(claz, ml.getConfigProperties());
                    }
                }
            }
        }

        Set adminObjects = desc.getAdminObjects();
        Iterator it = adminObjects.iterator();
        while (it.hasNext()) {
            AdminObject ao = (AdminObject) it.next();
            String uniqueName = ao.getAdminObjectInterface() + "_" + ao.getAdminObjectClass();
            if (!desc.getConfigPropertyProcessedClasses().contains(uniqueName)) {
                Class claz = getClass(desc, ao.getAdminObjectClass());
                ConfigPropertyHandler.processParent(claz, ao.getConfigProperties());
            }
        }
    }

    private Class getClass(ConnectorDescriptor desc, String className){
        Class claz = null;
            try {
                if (desc.getClassLoader() != null) {
                    //Use the descriptor's ClassLoader as the TCL in DAS will not have a
                    // resource adapter that is not targetted to DAS
                    claz = desc.getClassLoader().loadClass(className);
                } else {
                    claz = Thread.currentThread().getContextClassLoader().loadClass(className);
                }
            } catch (ClassNotFoundException e) {
                _logger.log(Level.WARNING, "Unable to load class [ " + className + " ]", e.getMessage());
                throw new RuntimeException("Unable to load class [ " + className + " ]");
            }
        return claz;
    }
}
