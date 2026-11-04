/*
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
 * Annotation that identifies a class as being a JSF component class. The
 * annotated type must be a public, non-abstract class. A minimal component
 * annotation need contain no element values, e.g.
 * <pre>
 *    &#64;Component
 *    public class MyComponent extends UIComponent {
 *        // ....
 *    }
 * </pre> It is generally a good idea to provide, at a minimum, a suitable
 * display name and component family, e.g.
 * <pre>
 *    &#64;Component(
 *        displayName="My Component",
 *        family="org.example.Mine")
 *    public class MyComponent extends UIComponent {
 *        // ....
 *    }
 * </pre>
 * <p>
 * The javadoc formatted comment found with the annotated class declaration is
 * also considered part of this property's metadata. Typically, the first
 * sentence of the comment will be used as the component's default short
 * description.
 *
 * <p>
 * A component's properties should be annotated with the {@link Property},
 * annotation, and its events should be annotated with the {@link Event}
 * annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Component {

    /**
     * Constraints that determine how a component may be resized in the IDE.
     */
    enum ResizeConstraints {
        /**
         * None.
         */
        NONE,
        /**
         * Any.
         */
        ANY,
        /**
         * Top.
         */
        TOP,
        /**
         * Bottom.
         */
        BOTTOM,
        /**
         * Left.
         */
        LEFT,
        /**
         * Right.
         */
        RIGHT,
        /**
         * Horizontal.
         */
        HORIZONTAL,
        /**
         * Vertical.
         */
        VERTICAL,
        /**
         * Maintain aspect ratio.
         */
        MAINTAIN_ASPECT_RATIO
    }

    /**
     * This component's type, which acts as a unique identifier. The value of
     * type must be unique within the scope of the current compilation unit. If
     * no value is supplied, the component class's fully qualified name will be
     * used.
     *
     * @return String
     */
    String type() default "";

    /**
     * This component's family. The value should be the same as is returned by a
     * call to <code>UIComponent.getFamily()</code>. If no value is supplied,
     * defaults to the value of {@link #type}.
     *
     * @return String
     */
    String family() default "";

    /**
     * This component's display name, typically used at design-time. If no value
     * is supplied, defaults to the component's simple class name.
     *
     * @return String
     */
    @Localizable
    String displayName() default "";

    /**
     * The basis of variable names generated for this component, typically at
     * design-time. If no value is provided, defaults to the component's simple
     * class name, with the first letter having been translated from upper to
     * lower case.
     *
     * @return String
     */
    String instanceName() default "";

    /**
     * If true, this component is intended to be used only in Java code, and no
     * JSP tag should be generated for it.
     *
     * @see Property#isAttribute
     * @return {@code true} if should be used only java Java code, {@code false}
     * otherwise
     */
    boolean isTag() default true;

    /**
     * The name of the JSP tag that corresponds to this component. If no value
     * is provided, the {@link #instanceName} will be used.
     *
     * <p>
     * <b>Note:</b> If this component does not correspond to a tag
     * ({@link #isTag} is set to false), then providing a value for this element
     * has no effect.
     *
     * @return String
     */
    String tagName() default "";

    /**
     * The renderer type for this component's tag. The value specified must
     * correspond to a renderer type specified for a renderer class in the
     * current compilation unit (see {@link Renderer.Renders#rendererType}). If
     * not specified, and exactly one renderer type has been associated with
     * this components {@link #family}, this value will default to that
     * renderer type. If not specified, and more than one renderer type has been
     * associated with this component's {@link #family}, the default value is
     * undefined.
     *
     * <p>
     * <b>Note:</b> If this component does not correspond to a tag
     * ({@link #isTag} is set to false), then providing a value for this element
     * has no effect.
     *
     * @return String
     */
    String tagRendererType() default "";

    /**
     * An optional short description of this component, typically used as a tool
     * tip at design-time. If no value is provided, the first sentence of the
     * javadoc-formatted comment for this component's class definition will be
     * used.
     *
     * @return String
     */
    @Localizable
    String shortDescription() default "";

    /**
     * Indicates whether this component may act as a container for other
     * components.
     *
     * @return {@code true} if container, {@code false} otherwise
     */
    boolean isContainer() default true;

    /**
     * Controls the manner in which a component may be resized at design-time.
     *
     * @return ResizeConstraints[]
     */
    ResizeConstraints[] resizeConstraints() default {ResizeConstraints.ANY};

    /**
     * The name of a unique key used to associate help with this component at
     * design-time. If no key is provided, a suitable default will be generated.
     *
     * @return String
     */
    String helpKey() default "";

    /**
     * The name of a unique key used to associate help with this component at
     * design-time. If no key is provided, a suitable default will be generated.
     *
     * @return String
     */
    String propertiesHelpKey() default "";

}
