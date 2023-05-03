/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is put on user-supplied annotations in order to cause
 * the hk2-inhabitant-generator tool to create descriptors with certain
 * properties.
 * <p>
 * The user-supplied annotation must have the following properties:<UL>
 * <LI>Must only apply to methods (or classes also annotated with {@link Decorator})</LI>
 * <LI>Must be available at runtime</LI>
 * <LI>Must only be applied to interfaces marked with {@link Configured}</LI>
 * <LI>May have one or zero String fields marked with {@link GeneratedServiceName}</LI>
 * </UL>
 * Only methods of type java.util.List with a parameterized type (e.g. List<Config>) or
 * which take a single parameter and a void return type may be
 * annotated with the user-supplied annotation.  The parameterized actual type (or the type of
 * the parameter) will end up being used
 * as a field in the descriptor.  A single method may have multiple user-supplied annotations
 * marked with this annotation, in which case a different descriptor will be generated for each
 * user-supplied annotation.
 * <p>
 * When a method on an {@link Configured} interface has a user-supplied annotation
 * that is annotated with this interface the hk2-inhabitant-generator will generate
 * a service descriptor with the following properties:<UL>
 * <LI>The implementation class will be as specified in this annotation</LI>
 * <LI>The available contracts will be as specified in this annotation</LI>
 * <LI>The scope will be as specified in this annotation</LI>
 * <LI>If the user-supplied annotation has a field marked with {@link GeneratedServiceName} it will be the name in the descriptor</LI>
 * <LI>It will have a metadata entry with the name of the actual type of the List parameterized return type (or the single parametere type) with key METHOD_ACTUAL</LI>
 * <LI>It will have a metadata entry with the name of the method with key METHOD_NAME</LI>
 * <LI>It will have a metadata entry with the name of the parent {@link Configured} class with key PARENT_CONFGIURED</LI>
 * </UL>
 * <p>
 * If a class is annotated with a user-supplied annotation marked with this annotation then that
 * class *must* also be annotated with the {@link Decorate} annotation.  The {@link Decorate} annotation
 * will provide the values for several of the fields as described below.
 * <p>
 * When a class on an {@link Configured} interface has a user-supplied annotation
 * that is annotated with this interface the hk2-inhabitant-generator will generate
 * a service descriptor with the following properties:<UL>
 * <LI>The implementation class will be as specified in this annotation</LI>
 * <LI>The available contracts will be as specified in this annotation</LI>
 * <LI>The scope will be as specified in this annotation</LI>
 * <LI>The name will come from the field annotated with {@link GeneratedServiceName} from the user-supplied annotation defined by
 * the {@link Decorate#with()} method</LI>
 * <LI>It will have a metadata entry with the {@link Decorate#targetType()} value with key METHOD_ACTUAL</LI>
 * <LI>It will have a metadata entry with the {@link Decorate#methodName()} value with key METHOD_NAME</LI>
 * <LI>It will have a metadata entry with the name of the parent {@link Configured} class with key PARENT_CONFGIURED</LI>
 * </UL>
 *
 * @author jwells
 */
@Documented
@Retention(RUNTIME)
@Target( ANNOTATION_TYPE )
public @interface GenerateServiceFromMethod {
    /**
     * This is the key in the metadata that will contain the actual type of the List return type of the
     * method where the user-supplied annotation has been placed
     */
    public final static String METHOD_ACTUAL = "MethodListActual";

    /**
     * This is the key in the metadata that will contain the name of the method where the user-supplied
     * annotation has been placed
     */
    public final static String METHOD_NAME = "MethodName";

    /**
     * This is the key in the metadata that will contain the fully qualified class name of the class marked
     * {@link Configured} that contains this annotation
     */
    public final static String PARENT_CONFIGURED = "ParentConfigured";

    /**
     * This must have the fully qualified class name of the implementation that is to be used in the
     * generated descriptor
     *
     * @return The fully qualified class name of the implementation
     */
    public String implementation();

    /**
     * The set of fully qualified class names of the advertised contracts that are to be used in
     * the generated descriptor.  Note that the implementation class is not automatically added
     * to this list
     *
     * @return The fully qualified class names of the advertised contracts the generated descriptor
     * should take
     */
    public String[] advertisedContracts();

    /**
     * The scope that the descriptor should take.  Defaults to PerLookup
     *
     * @return The fully qualified class names of the scope the descriptor should take
     */
    public String scope() default "org.glassfish.hk2.api.PerLookup";
}
