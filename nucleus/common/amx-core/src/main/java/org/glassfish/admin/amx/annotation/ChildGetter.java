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

package org.glassfish.admin.amx.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
    Used on an AMXProxy sub-interface only.  Indicates that the method
    is a proxy-based method for getting a child or List/Set/Map of children;
    <em>it doesn’t actually exist as an MBean attribute or method</em>.  This annotation
    should not be applied when the method or Attribute actually does exist. This
    annotation is generally needed only when there are arbitrary types of children that
    are not known in advance and/or methods do not exist for them (eg because of derivation
    such as with config MBeans).
    <p>
    The proxy method to which the annotation is applied must be of one of the following forms,
    where the interface FooBar is a sub-interface of {@link org.glassfish.admin.amx.core.AMXProxy}.
    <code>
    <ul>
    <li>FooBar           getFooBar();        // gets a singleton child </li>
    <li>Set&lt;FooBar>   getFooBar();        // gets all FooBar </li>
    <li>List&lt;FooBar>  getFooBar();        // gets all FooBar </li>
    <li>Map&lt;String,FooBar>  getFooBar();  // gets all FooBar </li>
    <li>FooBar[]         getFooBar();        // gets all FooBar </li>
    </ul>
    </code>
    The child type is derived from the method name, but if the standard derivation would result
    in the incorrect type then the annotation must include <code>type="child-type"></code>, where
    "child-type" is the appropriate type.
   @author Lloyd Chambers
 */
@Retention(RUNTIME)
@Documented
@Target({METHOD})
@Taxonomy(stability = Stability.NOT_AN_INTERFACE)
public @interface ChildGetter {
    /** child type, derived automatically by default for the normal naming pattern */
    String type() default "";
}







