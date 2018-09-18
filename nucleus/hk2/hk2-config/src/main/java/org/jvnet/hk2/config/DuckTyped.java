/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * Indicates that this method on {@link ConfigBeanProxy} is a duck-typed method
 * that are implemented by the static method on the nested static {@code Duck} class.
 *
 * <h2>Usage</h2>
 * <p>
 * Often it's convenient if one can define some convenience methods on the config bean proxy.
 * This mechanism allows you to do that by putting this annotation on a convenience method,
 * then write the nested static {@code Duck} class and places the actual implementation there.
 *
 * <pre>
 * interface MyConfigBean extends ConfigBeanProxy {
 *    &#64;Element
 *    List&lt;Property> getProperties();
 *
 *    &lt;DuckTyped
 *    String getPropertyValue(String name);
 *
 *    class Duck {
 *        public static String getPropertyValue(MyConfigBean me, String name) {
 *            for( Property p : me.getProperties() )
 *                if(p.getName().equals(name))
 *                    return p.getValue();
 *            return null;
 *        }
 *    }
 * }
 * </pre>
 *
 * <p>
 * The invocation of the <tt>getPropertyValue</tt> above will cause HK2 to in turn invoke
 * the corresponding static method on {@code Duck}, where the first argument will be the
 * original {@code this} value.
 *
 * @author Kohsuke Kawaguchi
 * @see ConfigBeanProxy
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface DuckTyped {
}
