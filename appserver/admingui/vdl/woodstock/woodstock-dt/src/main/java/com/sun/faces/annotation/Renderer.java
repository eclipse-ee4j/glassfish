/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that identifies a class as being a JSF renderer. The annotated
 * type type must be a public class.
 *
 * <p>
 * A renderer annotation should contain one or more rendering declarations,
 * using the {@link Renderer.Renders} annotation. Each such entry corresponds to
 * one renderer registration in the Faces application.
 *
 * <p>
 * In the simplest case, a renderer is used for just one component family, and
 * the renderer type is defaulted to the component family name:
 * <pre>
 *    &#64;Renderer(&#64;Renders(componentFamily="org.example.input"))
 * </pre> A renderer annotation may be used to declare more than one rendering
 * type and component family combination.
 * <pre>
 *    &#64;Renderer({
 *        &#64;Renders(componentFamily="org.example.input"),
 *        &#64;Renders(componentFamily="org.example.output")
 *    })
 * </pre> If a single rendering type is used with more than one component
 * family, a value for the renderer type must be supplied explicitly:
 * <pre>
 *    &#64;Renderer(&#64;Renders(
 *            renderType="org.example.renderAll",
 *            componentFamily={"org.example.input", "org.example.output"})
 *    )
 * </pre>
 *
 * @see Renderer.Renders
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Renderer {

    /**
     * Zero or more rendering declarations. Each declaration corresponds to an
     * entry in the Faces application.
     *
     * @return Renders[]
     */
    Renders[] value() default {};

    /**
     * Annotation that identifies a single component-renderer combination. The
     * value of {@code componentFamily} must corresponds to a component family
     * declared in the current compilation unit (see {@link Component#family}).
     *
     * <p>
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.ANNOTATION_TYPE)
    @interface Renders {

        /**
         * The renderer type for this component and renderer combination. If
         * this annotation contains a single component family, and a renderer
         * type is not provided, it will default to the component family.
         *
         * @return String
         */
        String rendererType() default "";

        /**
         * The component family or families to which this rendering type
         * applies. The values given must correspond to component families
         * declared in the current compilation unit (see
         * {@link Component#family}).
         *
         * @return String[]
         */
        String[] componentFamily();
    }

}
