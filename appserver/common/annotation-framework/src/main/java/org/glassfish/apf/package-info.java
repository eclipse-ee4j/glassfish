/*
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

/**
 * Provides the classes necessary to process J2SE 1.5 annotations in the context 
 * of the J2EE application server. 
 * 
 * <p>
 * Annotations are defined by their annotation type. This tool assumes that 
 * annotation handlers will be registered to it to process a particular annotation 
 * type. These annotation handlers have no particular knowledge outside of the 
 * annotation they process and the annoted element on which the annotation was
 * defined. 
 * </p>
 * <p>
 * The AnnotationProcessor tool implementation is responsible for maintening a 
 * list of annotations handlers per annotation type. AnnotationHandler are added
 * to the tool through the pushAnnotationHandler and can be removed through the 
 * popAnnotationHandler. Alternatively, the Factory singleton can be used to get
 * an initialized AnnotationProcessor with all the default AnnotationHandler. 
 * </p>
 * <p>
 * The tool uses the ProcessingContext to have access to Class instances. Each 
 * class instance will be processed in order, and if annotations are present, the 
 * tool will also process Field, Constructor and Methods elements. Each time the 
 * annotation processor switches for one particular AnnotatedElement to another,
 * it will send start and stop events to any AnnotatedElementHandler interface 
 * implementation registered within the ProcessingContext. This allow client 
 * code to keep context information about the AnnotatedElements being 
 * processed since AnnotationHandler only know about the AnnotatedElement the 
 * annotation was defined on.
 * </p>
 * @since 9.0
 * @auther Jerome Dochez
 */
package org.glassfish.apf;
