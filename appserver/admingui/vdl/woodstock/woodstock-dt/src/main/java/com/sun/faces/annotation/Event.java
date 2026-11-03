/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * Annotation that identifies a component event. An event is a logical entity
 * that consists typically of:
 * <ul>
 * <li>an event class or interface
 * <li>an event listener class or interface, with methods that are invoked when
 * an event occurs, and passed an instance of the event class or interface
 * <li>a component "add" method for adding event listeners
 * <li>a component "remove" method for removing event listeners
 * <li>optionally, a component "get" method for obtaining an array of all
 * registered event listeners.
 * </ul>
 * An event annotation is placed on either of the required listener add or
 * remove methods. It is preferable to annotate the "add" method.
 *
 * <p>
 * The annotation for a given event should occur only once per component. An
 * event is uniquely identified by its {@link #name}.
 *
 * <p>
 * An event may be annotated in a component class, a non-component class, or an
 * interface. It is generally a good idea to define common events once, in an
 * abstract base class or interface. Inherited event metadata may be overridden,
 * by providing an annotation on the overriding add or remove method. Annotation
 * element values from the super class will be inherited unless overridden by
 * annotation element values in the child class.
 *
 * <p>
 * If names for the listener add and remove methods are not supplied, they may
 * be generated following the standard naming conventions. For example, for an
 * event "blam", if the annotated method has a single parameter, and the method
 * name begins with the prefix "add", and the remaining portion of the name
 * equals the unqualified class name of the singleton parameter, then this
 * method is assumed to be an "add" method for an event listener, and the
 * singleton parameter type is assumed to be the event listener class. The same
 * logic is used to imply the name of the "remove" method.
 *
 * <p>
 * The name of an event may also be defaulted, if, in addition to the naming
 * conventions for listener add and remove methods, the event listener class
 * name ends with the suffix "Listener". Hence for an event listener class named
 * {@code BlamListener}, the default event name would be "blam".
 *
 * <p>
 * If all standard naming conventions are followed, a minimal event annotation
 * for the "blam" event would look like:
 *
 * <pre>
 *    &#64;Event
 *    public void addBlamListener(BlamListener blamListener) {...}
 *    public void removeBlamListener(BlamListener blamListener) {...}
 * </pre>
 *
 * <p>
 * If the event is to be mapped to a property on the component, then the
 * listener class must define extactly one handler method, e.g.:
 *
 * <pre>
 *    public interface BlamListener {
 *        public void blamHappened(BlamEvent blamEvent);
 *    }
 * </pre>
 *
 * <p>
 * Note that since add and remove listener methods have the same signature, if
 * default naming conventions are not followed, it is necessary to provide both
 * method names explicitly, e.g.
 *
 * <pre>
 *    &#64;Event(
 *        name="blam",
 *        addListenerMethodName="insertBlamListener",
 *        removeListenerMethodName="deleteBlamListener"
 *    )
 *    public void insertBlamListener(KerBlamListener blamListener) {...}
 *    public void deleteBlamListener(KerBlamListener blamListener) {...}
 * </pre>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Event {

    /**
     * This event's unique name. The name must be unique within the scope of the
     * event's class.
     *
     * @return String
     */
    String name() default "";

    /**
     * The display name, typically used at design-time. If no value is provided,
     * the {@link #name} will be used.
     *
     * @return String
     */
    @Localizable
    String displayName() default "";

    /**
     * An optional short description of this event, typically used as a tool tip
     * at design-time.
     *
     * @return String
     */
    @Localizable
    String shortDescription() default "";

    /**
     * The name of the "add" method for this event. A value need be specified
     * only if the annotation is placed elsewhere than the "add" method.
     *
     * @return String
     */
    String addListenerMethodName() default "";

    /**
     * The name of the "remove" method for this event. A value need be specified
     * only if the annotation is placed elsewhere than the "remove" method, or
     * if the name of the method does not follow standard naming conventions.
     *
     * @return String
     */
    String removeListenerMethodName() default "";

    /**
     * The name of the "get" method for this event. A value need be specified
     * only if the annotation is placed elsewhere than the "remove" method, or
     * if the name of the method does not follow standard naming conventions.
     *
     * @return String
     */
    String getListenersMethodName() default "";

    /**
     * Indicates whether this event is the default event for its component. Only
     * one event may be declared to be the default event for a component. If a
     * component does not declare a default event and does not inherit a default
     * event, and implements {@link jakarta.faces.component.ActionSource}, the
     * {@code action} event will be made the default, if it exists; if it
     * implements {@link jakarta.faces.component.EditableValueHolder}, the
     * {@code valueChange} event will be made the default, if it exists.
     *
     * @return {@code true} if default, {@code false} otherwise
     */
    boolean isDefault() default false;
}
