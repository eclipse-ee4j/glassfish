/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices.connector.annotation.handlers;

import java.lang.reflect.AnnotatedElement;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

import jakarta.xml.ws.*;
import jakarta.xml.ws.RespectBinding;
import jakarta.xml.ws.spi.WebServiceFeatureAnnotation;
import jakarta.xml.ws.soap.MTOM;
import jakarta.xml.ws.soap.Addressing;

import org.glassfish.apf.*;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;

import com.sun.enterprise.deployment.annotation.context.AppClientContext;
import com.sun.enterprise.deployment.annotation.context.ServiceReferenceContainerContext;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;
import org.jvnet.hk2.annotations.Service;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;

import static com.sun.enterprise.util.StringUtils.ok;

/**
 * This annotation handler is responsible for processing the jakarta.jws.WebServiceRef annotation type.
 *
 * @author Jerome Dochez
 */
@Service
@AnnotationHandlerFor(jakarta.xml.ws.WebServiceRef.class)
public class WebServiceRefHandler extends AbstractHandler  {

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        // it is easier if we return the array of component type. That
        // way, the @WebServiceRef is processed after the component
        // has been added to the DOL and the right EjbContext is
        // on the context stack. It won't hurt when @WebServiceRef
        // is used in appclients or web app since references are
        // declared at the bundle level.
        return getEjbAndWebAnnotationTypes();
    }

    protected HandlerProcessingResult processAWsRef(AnnotationInfo annInfo,
                WebServiceRef annotation) throws AnnotationProcessorException {
        AnnotatedElementHandler annCtx =
            annInfo.getProcessingContext().getHandler();
        AnnotatedElement annElem = annInfo.getAnnotatedElement();

        Class annotatedType = null;
        Class declaringClass = null;
        InjectionTarget target = null;
        String defaultServiceRefName = null;

        if (annInfo.getElementType().equals(ElementType.FIELD)) {
            // this is a field injection
            Field annotatedField = (Field) annElem;

            // check this is a valid field
            if (annCtx instanceof AppClientContext){
                if (!Modifier.isStatic(annotatedField.getModifiers())){
                    throw new AnnotationProcessorException(
                            localStrings.getLocalString(
                            "enterprise.deployment.annotation.handlers.injectionfieldnotstatic",
                            "Injection fields for application clients must be declared STATIC"),
                            annInfo);
                }
            }

            annotatedType = annotatedField.getType();
            declaringClass = annotatedField.getDeclaringClass();
            defaultServiceRefName = declaringClass.getName() + "/" +
                                        annotatedField.getName();
            target = new InjectionTarget();
            target.setFieldName(annotatedField.getName());
            target.setClassName(annotatedField.getDeclaringClass().getName());
        } else if (annInfo.getElementType().equals(ElementType.METHOD)) {
            // this is a method injection
            Method annotatedMethod = (Method) annElem;
            validateInjectionMethod(annotatedMethod, annInfo);

            if (annCtx instanceof AppClientContext){
                if (!Modifier.isStatic(annotatedMethod.getModifiers())){
                    throw new AnnotationProcessorException(
                            localStrings.getLocalString(
                            "enterprise.deployment.annotation.handlers.injectionmethodnotstatic",
                            "Injection methods for application clients must be declared STATIC"),
                            annInfo);
                }
            }

            annotatedType = annotatedMethod.getParameterTypes()[0];
            declaringClass = annotatedMethod.getDeclaringClass();
            // Derive javabean property name.
            String propertyName =
                getInjectionMethodPropertyName(annotatedMethod, annInfo);
            // prefixing with fully qualified type name
            defaultServiceRefName = declaringClass.getName() + "/" +
                                        propertyName;
            target = new InjectionTarget();
            target.setMethodName(annotatedMethod.getName());
            target.setClassName(annotatedMethod.getDeclaringClass().getName());
        } else if (annInfo.getElementType().equals(ElementType.TYPE)) {
            // name must be specified.
            if (!ok(annotation.name())) {
                throw new AnnotationProcessorException(
                        localStrings.getLocalString(
                        "enterprise.deployment.annotation.handlers.nonametypelevel",
                        "TYPE-Level annotation  must specify name member."),  annInfo);
            }
            // this is a dependency declaration, we need the service interface
            // to be specified
            annotatedType = annotation.type();
            if (annotatedType == null || annotatedType == Object.class) {
                throw new AnnotationProcessorException(
                        localStrings.getLocalString(
                        "enterprise.deployment.annotation.handlers.typenotfound",
                        "TYPE-level annotation symbol must specify type member."),
                         annInfo);
            }
            declaringClass = (Class) annElem;
        } else {
            throw new AnnotationProcessorException(
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.invalidtype",
                    "annotation not allowed on this element."),  annInfo);

        }

        MTOM mtom = null;
        Addressing addressing = null;
        RespectBinding respectBinding = null;
        // Other annotations like SchemaValidation etc to be passed on to
        // ServiceReferenceDescriptor
        Map<Class<? extends Annotation>, Annotation> otherAnnotations =
                new HashMap<Class<? extends Annotation>, Annotation>();

        for (Annotation a : annElem.getAnnotations()) {
            if (!(a.annotationType().isAnnotationPresent(
                                        WebServiceFeatureAnnotation.class)))
                continue;
            if (a instanceof MTOM) {
                mtom = (MTOM)a;
            } else if (a instanceof Addressing) {
                addressing = (Addressing)a;
            } else if (a instanceof RespectBinding) {
                respectBinding = (RespectBinding)a;
            } else {
                if (!otherAnnotations.containsKey(a.getClass())) {
                    otherAnnotations.put(a.getClass(), a);
                }
            }
        }

        String serviceRefName = !ok(annotation.name()) ?
                    defaultServiceRefName : annotation.name();
        ServiceReferenceContainer[] containers = null;
        if (annCtx instanceof ServiceReferenceContainerContext) {
            containers = ((ServiceReferenceContainerContext) annCtx).getServiceRefContainers();
        }

        if (containers == null || containers.length == 0) {
            annInfo.getProcessingContext().getErrorHandler().fine(
                    new AnnotationProcessorException(
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.invalidannotationforthisclass",
                    "Illegal annotation symbol for this class will be ignored"),
                    annInfo));
            return HandlerProcessingResultImpl.getDefaultResult(getAnnotationType(), ResultType.PROCESSED);
        }

        // now process the annotation for all the containers.
        for (ServiceReferenceContainer container : containers) {
            ServiceReferenceDescriptor aRef = null;
            try {
                aRef = container.getServiceReferenceByName(serviceRefName);
            } catch(Throwable t) {} // ignore

            if (aRef == null) {
                // time to create it...
                aRef = new ServiceReferenceDescriptor();
                aRef.setName(serviceRefName);
                container.addServiceReferenceDescriptor(aRef);
            }

            // merge other annotations
            Map<Class<? extends Annotation>, Annotation> oa =
                aRef.getOtherAnnotations();
            if (oa == null)
                aRef.setOtherAnnotations(otherAnnotations);
            else {
                for (Map.Entry<Class<? extends Annotation>, Annotation> entry :
                        otherAnnotations.entrySet()) {
                    if (!oa.containsKey(entry.getKey()))
                        oa.put(entry.getKey(), entry.getValue());
                }
            }

            // merge wsdlLocation
            if (!ok(aRef.getWsdlFileUri()) && ok(annotation.wsdlLocation()))
                aRef.setWsdlFileUri(annotation.wsdlLocation());

            if (!aRef.hasMtomEnabled() && mtom != null) {
                aRef.setMtomEnabled(mtom.enabled());
                aRef.setMtomThreshold(mtom.threshold());
            }

            // check Addressing annotation
            if (aRef.getAddressing() == null && addressing != null) {
                aRef.setAddressing(new com.sun.enterprise.deployment.Addressing(
                                        addressing.enabled(),
                                        addressing.required(),
                                        addressing.responses().toString()));
            }

            // check RespectBinding annotation
            if (aRef.getRespectBinding() == null && respectBinding != null) {
                aRef.setRespectBinding(
                        new com.sun.enterprise.deployment.RespectBinding(
                                respectBinding.enabled()));
            }

            // Store mapped name that is specified
            if (!ok(aRef.getMappedName()) && ok(annotation.mappedName()))
                    aRef.setMappedName(annotation.mappedName());

            // Store lookup name that is specified
            if (!aRef.hasLookupName() &&
                    ok(getLookupValue(annotation, annInfo)))
                aRef.setLookupName(getLookupValue(annotation, annInfo));

            aRef.setInjectResourceType("jakarta.jws.WebServiceRef");

            if (target != null)
                aRef.addInjectionTarget(target);

            // Read the WebServiceClient annotation for the service name space
            // uri and wsdl (if required)
            WebServiceClient wsclientAnn;

            // The JAX-WS 2.1 default value was "Object", the JAX-WS 2.2
            // default value is "Service".  Check whether the value is one
            // of these default values.
            if (!Object.class.equals(annotation.value()) &&
                    (!jakarta.xml.ws.Service.class.equals(annotation.value()))) {
                // a value was provided, which should be the Service
                // interface, the requested injection is therefore on the
                // port.
                if (aRef.getServiceInterface() == null) {
                    aRef.setServiceInterface(annotation.value().getName());
                }

                if (aRef.getPortInfoBySEI(annotatedType.getName()) == null) {
                    ServiceRefPortInfo portInfo = new ServiceRefPortInfo();
                    portInfo.setServiceEndpointInterface(annotatedType.getName());
                    aRef.addPortInfo(portInfo);
                }
                // set the port type requested for injection
                if (aRef.getInjectionTargetType() == null) {
                    aRef.setInjectionTargetType(annotatedType.getName());
                }
                wsclientAnn = (WebServiceClient)
                    annotation.value().getAnnotation(WebServiceClient.class);
            } else {
                // no value provided in the annotation
                wsclientAnn = (WebServiceClient)
                    annotatedType.getAnnotation(WebServiceClient.class);
            }
            if (wsclientAnn == null) {
                throw new AnnotationProcessorException(
                        localStrings.getLocalString(
                        "enterprise.deployment.annotation.handlers.classnotannotated",
                        "Class must be annotated with a {1} annotation\n symbol : {1}\n location: {0}",
                        new Object[] { annotatedType.toString(), WebServiceClient.class.toString() }));
            }

            // If wsdl file was not specified in a descriptor and not in the
            // annotation, get it from WebServiceClient annotation
            if (aRef.getWsdlFileUri() == null) {
                aRef.setWsdlFileUri(wsclientAnn.wsdlLocation());
            }

            // Set service name space URI and service local part
            if (aRef.getServiceName() == null) {
                aRef.setServiceNamespaceUri(wsclientAnn.targetNamespace());
                aRef.setServiceLocalPart(wsclientAnn.name());
            }

            if (aRef.getServiceInterface() == null) {
                aRef.setServiceInterface(annotatedType.getName());
            }
        }
        // Now force a HandlerChain annotation processing
        // This is to take care of the case where the client class does not
        // have @HandlerChain but the SEI has one specified through JAXWS customization
        if(annElem.getAnnotation(jakarta.jws.HandlerChain.class) == null) {
            return (new HandlerChainHandler()).processHandlerChainAnnotation(annInfo, annCtx, annotatedType, declaringClass, false);
        }
        return HandlerProcessingResultImpl.getDefaultResult(getAnnotationType(), ResultType.PROCESSED);
    }

    @Override
    public HandlerProcessingResult processAnnotation(AnnotationInfo annInfo)
            throws AnnotationProcessorException {
        WebServiceRef annotation = (WebServiceRef) annInfo.getAnnotation();
        return(processAWsRef(annInfo, annotation));
    }

    /**
     * Return the value of the "lookup" element of the WebServiceRef annotation.
     * This method handles the case where the WebServiceRef class is an older
     * version before the lookup element was added; in that case access to
     * the lookup element will cause a NoSuchMethodError, which is caught
     * and ignored (with a warning message).
     *
     * @return the value of the lookup element
     */
    private String getLookupValue(WebServiceRef annotation,
                                            AnnotationInfo ainfo) {
        String lookupValue = "";
        try {
            lookupValue = annotation.lookup();
        } catch(NoSuchMethodError nsme) {
            // Probably means  an older version of Resource is being picked up from somewhere.
            // Don't treat this as a fatal error.
            try {
                log(Level.WARNING, ainfo,
                    localStrings.getLocalString(
                "enterprise.deployment.annotation.handlers.wrongresourceclass",
                        "Incorrect @Resource annotation class definition - " +
                        "missing lookup attribute"));
            } catch (AnnotationProcessorException ex) { }
        }
        return lookupValue;
    }
}
