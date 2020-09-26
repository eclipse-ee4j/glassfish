/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.mail.annotation.handler;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.annotation.context.*;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.jvnet.hk2.annotations.Service;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.Interceptors;
import jakarta.mail.MailSessionDefinition;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: naman mehta
 * Date: 18/4/12
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */

@Service
@AnnotationHandlerFor(MailSessionDefinition.class)
public class MailSessionDefinitionHandler extends AbstractResourceHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(MailSessionDefinitionHandler.class);

    public MailSessionDefinitionHandler() {

    }

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts) throws AnnotationProcessorException {
        MailSessionDefinition mailSessionDefnAn =
                (MailSessionDefinition) ainfo.getAnnotation();
        return processAnnotation(mailSessionDefnAn, ainfo, rcContexts);
    }

    protected HandlerProcessingResult processAnnotation(MailSessionDefinition mailSessionDefnAn, AnnotationInfo aiInfo,
                                                        ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {
        Class annotatedClass = (Class) aiInfo.getAnnotatedElement();
        Annotation[] annotations = annotatedClass.getAnnotations();
        boolean warClass = isAWebComponentClass(annotations);
        boolean ejbClass = isAEjbComponentClass(annotations);

        for (ResourceContainerContext context : rcContexts) {
            if (!canProcessAnnotation(annotatedClass, ejbClass, warClass, context)) {
                return getDefaultProcessedResult();
            }

            Set<ResourceDescriptor> mailSessionDescs = context.getResourceDescriptors(JavaEEResourceType.MSD);
            MailSessionDescriptor desc = createDescriptor(mailSessionDefnAn);
            if (isDefinitionAlreadyPresent(mailSessionDescs, desc)) {
                merge(mailSessionDescs, mailSessionDefnAn);
            } else {
                mailSessionDescs.add(desc);
            }
        }
        return getDefaultProcessedResult();
    }

    private boolean canProcessAnnotation(Class annotatedClass, boolean ejbClass, boolean warClass,
                                         ResourceContainerContext context) {
        if (ejbClass) {
            if (!(context instanceof EjbBundleContext ||
                    context instanceof EjbContext ||
                    context instanceof EjbInterceptorContext
            )) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Ignoring @MailSessionDefinition annotation processing as the class is" +
                            "an EJB class and context is not one of EJBContext");
                }
                return false;
            }
        } else if (context instanceof EjbBundleContext) {
            EjbBundleContext ejbContext = (EjbBundleContext) context;
            EjbBundleDescriptor ejbBundleDescriptor = ejbContext.getDescriptor();
            EjbDescriptor[] ejbDescriptor = ejbBundleDescriptor.getEjbByClassName(annotatedClass.getName());
            if (ejbDescriptor == null || ejbDescriptor.length == 0) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Ignoring @MailSessionDefinition annotation processing as the class " +
                            "[ " + annotatedClass + " ] is" +
                            "not an EJB class and the context is EJBContext");
                }
                return false;
            }
        } else if (warClass) {
            if (!(context instanceof WebBundleContext || context instanceof WebComponentsContext
                    || context instanceof WebComponentContext)) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Ignoring @MailSessionDefinition annotation processing as the class is" +
                            "an Web class and context is not one of WebContext");
                }
                return false;
            }
        } else if (context instanceof WebBundleContext) {
            WebBundleContext webBundleContext = (WebBundleContext) context;
            WebBundleDescriptor webBundleDescriptor = webBundleContext.getDescriptor();
            Collection<RootDeploymentDescriptor> extDesc = webBundleDescriptor.getExtensionsDescriptors();
            for (RootDeploymentDescriptor desc : extDesc) {
                if (desc instanceof EjbBundleDescriptor) {
                    EjbBundleDescriptor ejbBundleDesc = (EjbBundleDescriptor) desc;
                    EjbDescriptor[] ejbDescs = ejbBundleDesc.getEjbByClassName(annotatedClass.getName());
                    if (ejbDescs != null && ejbDescs.length > 0) {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.log(Level.FINEST, "Ignoring @MailSessionDefinition annotation processing as the class " +
                                    "[ " + annotatedClass + " ] is" +
                                    "not an Web class and the context is WebContext");
                        }
                        return false;
                    } else if (ejbBundleDesc.getInterceptorByClassName(annotatedClass.getName()) != null) {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.log(Level.FINEST, "Ignoring @MailSessionDefinition annotation processing " +
                                    "as the class " +
                                    "[ " + annotatedClass + " ] is" +
                                    "not an Web class and the context is WebContext");
                        }
                        return false;
                    } else {
                        Method[] methods = annotatedClass.getDeclaredMethods();
                        for (Method method : methods) {
                            Annotation annotations[] = method.getAnnotations();
                            for (Annotation annotation : annotations) {
                                if (annotation.annotationType().equals(AroundInvoke.class) ||
                                        annotation.annotationType().equals(AroundTimeout.class) ||
                                        annotation.annotationType().equals(Interceptors.class)) {
                                    if (logger.isLoggable(Level.FINEST)) {
                                        logger.log(Level.FINEST, "Ignoring @MailSessionDefinition annotation processing " +
                                                "as the class " +
                                                "[ " + annotatedClass + " ] is" +
                                                "not an Web class, an interceptor and the context is WebContext");
                                    }
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private MailSessionDescriptor createDescriptor(MailSessionDefinition defn) {

        MailSessionDescriptor desc = new MailSessionDescriptor();
        desc.setMetadataSource(MetadataSource.ANNOTATION);

        desc.setName(defn.name());

        if (defn.description() != null && !defn.description().equals("")) {
            desc.setDescription(defn.description());
        }

        if (defn.storeProtocol() != null && !defn.storeProtocol().equals("")) {
            desc.setStoreProtocol(defn.storeProtocol());
        }

        if (defn.transportProtocol() != null && !defn.transportProtocol().equals("")) {
            desc.setTransportProtocol(defn.transportProtocol());
        }

        if (defn.host() != null && !defn.host().equals("")) {
            desc.setHost(defn.host());
        }

        if (defn.user() != null && !defn.user().equals("")) {
            desc.setUser(defn.user());
        }

        if (defn.password() != null /*ALLOW EMPTY PASSWORDS && !defn.password().equals("")*/) {
            desc.setPassword(defn.password());
        }

        if (defn.from() != null && !defn.from().equals("")) {
            desc.setFrom(defn.from());
        }

        if (defn.properties() != null) {
            Properties properties = desc.getProperties();

            String[] defnProperties = defn.properties();
            if (defnProperties.length > 0) {
                for (String property : defnProperties) {
                    int index = property.indexOf("=");
                    // found "=" and not at start or end of string
                    if (index > -1 && index != 0 && index < property.length() - 1) {
                        String name = property.substring(0, index);
                        String value = property.substring(index + 1);
                        properties.put(name, value);
                    }
                }
            }
        }

        return desc;
    }

    private boolean isDefinitionAlreadyPresent(Set<ResourceDescriptor> mailSessionDescs,
                                               MailSessionDescriptor mailSessionDesc) {
        boolean result = false;
        for (ResourceDescriptor msDesc : mailSessionDescs) {
            if (msDesc.equals(mailSessionDesc)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void merge(Set<ResourceDescriptor> mailSessionDescs, MailSessionDefinition mailSessionDefn) {

        for (ResourceDescriptor orgDesc : mailSessionDescs) {
            MailSessionDescriptor desc = (MailSessionDescriptor)orgDesc;
            if (desc.getName().equals(mailSessionDefn.name())) {

                if (desc.getDescription() == null) {
                    if (mailSessionDefn.description() != null && !mailSessionDefn.description().equals("")) {
                        desc.setDescription(mailSessionDefn.description());
                    }
                }

                if (desc.getStoreProtocol() == null) {
                    desc.setStoreProtocol(mailSessionDefn.storeProtocol());
                }

                if (desc.getTransportProtocol() == null) {
                    desc.setTransportProtocol(mailSessionDefn.transportProtocol());
                }

                if (desc.getHost() == null) {
                    desc.setHost(mailSessionDefn.host());
                }

                if (desc.getUser() == null) {
                    if (mailSessionDefn.user() != null && !mailSessionDefn.user().equals("")) {
                        desc.setUser(mailSessionDefn.user());
                    }
                }

                if (desc.getPassword() == null) {
                    if (mailSessionDefn.password() != null /*ALLOW EMPTY PASSWORDS && !defn.password().equals("")*/) {
                        desc.setPassword(mailSessionDefn.password());
                    }
                }

                if (desc.getFrom() == null) {
                    desc.setFrom(mailSessionDefn.from());
                }

                Properties properties = desc.getProperties();
                String[] defnProperties = mailSessionDefn.properties();

                if (defnProperties.length > 0) {
                    for (String property : defnProperties) {
                        int index = property.indexOf("=");
                        // found "=" and not at start or end of string
                        if (index > -1 && index != 0 && index < property.length() - 1) {
                            String name = property.substring(0, index);
                            String value = property.substring(index + 1);
                            //add to properties only when not already present
                            if (properties.get(name) == null) {
                                properties.put(name, value);
                            }
                        }
                    }
                }
                break;
            }
        }

    }

}
