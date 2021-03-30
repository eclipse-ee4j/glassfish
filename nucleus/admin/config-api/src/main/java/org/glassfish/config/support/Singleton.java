/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.config.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that qualifies a configuration element that can only exist as a singleton in the configuration tree.
 *
 * When configuration elements are added to their parents through a subclassing pattern, it can be difficult for the
 * system to determine if this element can exist as a singleton or a collection.
 *
 * For instance, when the parent contains : <code>
 * &#64;Element("*")
 * List<Extension> getExtensions();
 * </code>
 *
 * A subclass of Extension can have a single or multiple instances stored in the extensions list. Adding this annotation
 * will qualify that only one instance of the annotated configuration can be found in that collection. Note that the
 * collection can contain other instances of other subclasses of Extension.
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Singleton {
}
