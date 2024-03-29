/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.apf.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessor;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.ComponentInfo;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.ProcessingContext;
import org.glassfish.apf.ProcessingResult;
import org.glassfish.apf.ResultType;
import org.glassfish.apf.Scanner;

import static org.glassfish.apf.impl.HandlerProcessingResultImpl.getDefaultResult;


/**
 * @author dochez
 */
public class AnnotationProcessorImpl implements AnnotationProcessor {

    private static final Logger LOG = AnnotationUtils.getLogger();

    private final Map<String, List<AnnotationHandler>> handlers = new HashMap<>();
    private final Stack<StackElement> annotatedElements = new Stack<>();
    private final Set<Package> visitedPackages = new HashSet<>();

    private AnnotationProcessorImpl delegate;
    private int errorCount;

    public void setDelegate(AnnotationProcessorImpl delegate) {
        this.delegate = delegate;
    }


    @Override
    public ProcessingContext createContext() {
        ProcessingContext ctx = new ProcessingContextImpl(this);
        ctx.setErrorHandler(new DefaultErrorHandler());
        return ctx;
    }


    /**
     * Log a message on the default LOG
     */
    @Override
    public void log(Level level, AnnotationInfo locator, String localizedMessage) {
        if (LOG.isLoggable(level)) {
            if (locator == null) {
                LOG.log(level, localizedMessage);
            } else {
                LOG.log(level,
                    MessageFormat.format("{2}\n symbol: {0}\n location: {1}",
                        new Object[] {
                            locator.getAnnotation().annotationType().getName(),
                            locator.getAnnotatedElement(),
                            localizedMessage,
                        }));
            }
        }
    }


    /**
     * Starts the annotation processing tool passing the processing context which
     * encapuslate all information necessary for the configuration of the tool.
     *
     * @param ctx is the initialized processing context
     * @return the result of the annoations processing
     */
    @Override
    public ProcessingResult process(ProcessingContext ctx) throws AnnotationProcessorException {
        Scanner scanner = ctx.getProcessingInput();
        ProcessingResultImpl result = new ProcessingResultImpl();
        errorCount = 0;
        for (Class<?> c : scanner.getElements()) {
            result.add(process(ctx, c));
        }
        return result;
    }


    /**
     * Process a set of classes from the parameter list rather than from the
     * processing context. This allow the annotation handlers to call be the
     * annotation processing tool when classes need to be processed in a
     * particular context rather than when they are picked up by the scanner.
     *
     * @param ctx the processing context
     * @param classes the list of classes to process
     * @return the processing result for such classes
     * @throws AnnotationProcessorException if handlers fail to process an annotation
     */
    @Override
    public ProcessingResult process(ProcessingContext ctx, Class<?>[] classes) throws AnnotationProcessorException {
        ProcessingResultImpl result = new ProcessingResultImpl();
        for (Class<?> c : classes) {
            result.add(process(ctx, c));
        }
        return result;
    }


    private ProcessingResult process(ProcessingContext ctx, Class<?> c) throws AnnotationProcessorException {
        Scanner scanner = ctx.getProcessingInput();
        ProcessingResultImpl result = new ProcessingResultImpl();

        // let's see first if this package is new to us and annotated.
        Package classPackage = c.getPackage();
        if (classPackage != null && visitedPackages.add(classPackage)) {
            // new package
            result.add(classPackage, processAnnotations(ctx, ElementType.PACKAGE, classPackage));
        }

        ComponentInfo info = null;
        try {
            info = scanner.getComponentInfo(c);
        } catch (NoClassDefFoundError err) {
            // issue 456: allow verifier to report this issue
            AnnotationProcessorException ape = new AnnotationProcessorException(
                MessageFormat.format("Class [ {0} ] not found. Error while loading [ {1} ]", err.getMessage(), c));
            ctx.getErrorHandler().error(ape);
            // let's continue to the next class instead of aborting the whole
            // annotation processing
            return result;
        }

        // process the class itself.
        AnnotatedElementHandler handler = ctx.getHandler();
        logStart(handler, ElementType.TYPE, c);
        result.add(c, processAnnotations(ctx, c));

        // now dive into the fields.
        for (Field field : info.getFields()) {
            result.add(field, processAnnotations(ctx, ElementType.FIELD, field));
        }

        // constructors...
        for (Constructor<?> constructor : info.getConstructors()) {
            logStart(ctx.getHandler(), ElementType.CONSTRUCTOR, constructor);
            result.add(constructor, processAnnotations(ctx, constructor));

            // parameters
            processParameters(ctx, constructor.getParameterAnnotations());

            logEnd(ctx.getHandler(), ElementType.CONSTRUCTOR, constructor);

        }

        // methods...
        for (Method method : info.getMethods()) {
            logStart(ctx.getHandler(), ElementType.METHOD, method);
            result.add(method, processAnnotations(ctx, method));

            // parameters
            processParameters(ctx, method.getParameterAnnotations());

            logEnd(ctx.getHandler(), ElementType.METHOD, method);
        }

        // Because of annotation inheritance, we need to to travel to
        // the superclasses to ensure that annotations defined at the
        // TYPE level are processed at this component level.
        // Note : so far, I am ignoring the implemented interfaces
        Class<?> currentClass = c.getSuperclass();
        while (currentClass != null && !currentClass.equals(Object.class)) {
            // the trick is to add the results for this class, not
            // for the ones they are defined in...
            result.add(c, processAnnotations(ctx, currentClass));
            currentClass = currentClass.getSuperclass();
        }

        // end of class processing, we need to get the top handler
        // since it may have changed during the annotation processing
        logEnd(ctx.getHandler(), ElementType.TYPE, c);

        return result;
    }


    private HandlerProcessingResult processParameters(ProcessingContext ctx, Annotation[][] parametersAnnotations)
        throws AnnotationProcessorException {
        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();

        // process the method parameters...
        for (Annotation[] parameterAnnotations : parametersAnnotations) {
            logStart(ctx.getHandler(), ElementType.PARAMETER, null);
            if (parameterAnnotations != null) {
                for (Annotation annotation : parameterAnnotations) {
                    AnnotationInfo info = new AnnotationInfo(ctx, null, annotation, ElementType.PARAMETER);
                    process(ctx, info, result);
                    dumpProcessingResult(result);
                }
            }
            logEnd(ctx.getHandler(), ElementType.PARAMETER, null);
        }
        return result;
    }


    private HandlerProcessingResult processAnnotations(ProcessingContext ctx, ElementType type, AnnotatedElement element)
        throws AnnotationProcessorException {
        AnnotatedElementHandler handler = ctx.getHandler();
        logStart(handler, type, element);
        HandlerProcessingResult result = processAnnotations(ctx, element);
        logEnd(handler, type, element);

        dumpProcessingResult(result);

        return result;
    }


    private HandlerProcessingResult processAnnotations(ProcessingContext ctx, AnnotatedElement element)
        throws AnnotationProcessorException {
        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();
        try {
            for (Annotation annotation : element.getAnnotations()) {
                // initialize the result...
                AnnotationInfo subElement = new AnnotationInfo(ctx, element, annotation, getTopElementType());
                if (result.processedAnnotations().containsKey(annotation.annotationType())) {
                    LOG.log(Level.FINER, "Annotation {0} already processed.", annotation.annotationType());
                } else {
                    process(ctx, subElement, result);
                }
            }
        } catch (ArrayStoreException e) {
            LOG.info("Exception " + e + " encountered while processing annotaton for element " + element
                + ". Message is: " + e.getMessage() + ". Ignoring annotations and proceeding.");
        }
        return result;
    }


    private void process(ProcessingContext ctx, AnnotationInfo element, HandlerProcessingResultImpl result)
        throws AnnotationProcessorException {
        Annotation annotation = element.getAnnotation();
        LOG.log(Level.FINER, "Processing annotation: {0} delegate = {1}",
            new Object[] {element, delegate});
        result.addResult(annotation.annotationType(), ResultType.UNPROCESSED);

        // we ignore all java.* annotations
        Package annPackage = annotation.annotationType().getPackage();
        if (annPackage != null && annPackage.getName().startsWith("java.lang")) {
            return;
        }

        List<AnnotationHandler> annotationHandlers = handlers.get(annotation.annotationType().getName());
        if (annotationHandlers == null) {
            if (delegate == null) {
                LOG.log(Level.FINER, "No handler defined for {0}", annotation);
            } else {
                delegate.process(ctx, element, result);
            }
            return;
        }
        for (AnnotationHandler handler : annotationHandlers) {
            // here we need to be careful, we are ready to invoke a handler
            // to process a particular annotation type. However, this handler
            // may have defined a list of annotations that should be processed
            // (if present on the annotated element) before itself.
            // do this check and process those annotations first.
            Class<? extends Annotation>[] dependencies = handler.getTypeDependencies();
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "Dependencies of handler " + handler + ", " + Arrays.toString(dependencies));
            }
            if (dependencies != null) {
                AnnotatedElement ae = element.getAnnotatedElement();
                for (Class<? extends Annotation> annotationType : dependencies) {
                    Annotation depAnnotation = ae.getAnnotation(annotationType);
                    if (depAnnotation != null) {
                        ResultType resultType = result.processedAnnotations().get(annotationType);
                        if (resultType == null || resultType == ResultType.UNPROCESSED) {
                            // annotation is present, process it.
                            AnnotationInfo info = new AnnotationInfo(ctx, ae, depAnnotation, getTopElementType());
                            process(ctx, info, result);
                        }
                    }
                }
            }

            // at this point, all annotation that I declared depending on are processed
            HandlerProcessingResult processingResult = null;
            try {
                processingResult = handler.processAnnotation(element);
            } catch (AnnotationProcessorException ape) {
                // I am logging this exception
                log(Level.SEVERE, ape.getAnnotationInfo(), ape.getMessage());

                // I am not throwing the exception unless it is fatal so annotation
                // processing can continue and we have a chance to report all
                // errors.
                if (ape.isFatal()) {
                    throw ape;
                }

                if (++errorCount > 100) {
                    throw new AnnotationProcessorException("Too many errors, annotation processing abandoned.");
                }

                processingResult = getDefaultResult(annotation.annotationType(), ResultType.FAILED);
            } catch (Throwable e) {
                throw new AnnotationProcessorException(e.getMessage(), element, e);
            }
            result.addAll(processingResult);
        }
    }

    private void dumpProcessingResult(HandlerProcessingResult result) {
        if (result == null || !LOG.isLoggable(Level.FINER)) {
            return;
        }
        Map<Class<? extends Annotation>, ResultType> annotationResults = result.processedAnnotations();
        for (Map.Entry<Class<? extends Annotation>, ResultType> element : annotationResults.entrySet()) {
            LOG.finer("Annotation " + element.getKey() + " : " + element.getValue());
        }
    }


    @Override
    public void pushAnnotationHandler(AnnotationHandler handler) {
        String type = handler.getAnnotationType().getName();
        pushAnnotationHandler(type, handler);
    }

    /**
     * This method is similar to {@link #pushAnnotationHandler(AnnotationHandler)} except that
     * it takes an additional String type argument which allows us to avoid extracting the information from the
     * AnnotationHandler. Calling the AnnotationHandler can lead to its instantiation where as the annotation
     * that a handler is responsible for handling is a metadata that can be statically extracted. This allows us to
     * build more lazy systems.
     *
     * @param type
     * @param handler
     */
    public void pushAnnotationHandler(String type, AnnotationHandler handler) {
        List<AnnotationHandler> currentHandlers = handlers.get(type);
        if (currentHandlers==null) {
            currentHandlers = new ArrayList<>();
            handlers.put(type, currentHandlers);
        }
        currentHandlers.add(handler);
    }

    @Override
    public void popAnnotationHandler(Class<? extends Annotation> type) {
        List<AnnotationHandler> currentHandlers = handlers.get(type.getName());
        if (currentHandlers!=null) {
            currentHandlers.remove(currentHandlers.size()-1);
        }
    }

    @Override
    public AnnotationHandler getAnnotationHandler(Class<? extends Annotation> type) {
        List<AnnotationHandler> currentHandlers = handlers.get(type.getName());
        if (currentHandlers!=null && currentHandlers.size()>0) {
            return currentHandlers.get(0);
        }
        return null;
    }

    /**
     * @return the last element pushed on the stack which ElementType was
     * the one passed or null if no stack element is of the given type.
     */
    @Override
    public AnnotatedElement getLastAnnotatedElement(ElementType type) {
        for (int i = annotatedElements.size(); i != 0; i--) {
            StackElement e = annotatedElements.get(i - 1);
            if (e.getElementType().equals(type)) {
                return e.getAnnotatedElement();
            }
        }
        return null;
    }


    public Stack<StackElement> getStack() {
        return annotatedElements;
    }


    private void logStart(AnnotatedElementHandler handler, ElementType type, AnnotatedElement c)
        throws AnnotationProcessorException {
        LOG.log(Level.FINEST, "{0} START : {1}", new Object[] {type, c});

        // push it to our annotated element stack
        annotatedElements.push(new StackElement(type, c));
        if (delegate != null) {
            delegate.getStack().push(new StackElement(type, c));
        }

        if (handler != null) {
            handler.startElement(type, c);
        }
    }


    private void logEnd(AnnotatedElementHandler handler, ElementType type, AnnotatedElement c)
        throws AnnotationProcessorException {
        LOG.log(Level.FINEST, "{0} END : {1}", new Object[] {type, c});

        // pop it from our annotated element stack
        annotatedElements.pop();
        if (delegate != null) {
            delegate.getStack().pop();
        }

        if (handler != null) {
            handler.endElement(type, c);
        }
    }


    /**
     * @return the top annotated elements stack element type
     */
    private ElementType getTopElementType() {
        try {
            StackElement top = annotatedElements.peek();
            return top.getElementType();
        } catch (EmptyStackException ex) {
            return null;
        }
    }
}
