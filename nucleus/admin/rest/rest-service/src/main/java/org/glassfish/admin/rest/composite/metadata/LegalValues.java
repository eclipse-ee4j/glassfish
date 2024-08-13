/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.composite.metadata;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that a String model property can only contain a specific list of strings.
 *
 * Typically a model property will use a java enum instead. However, there are some cases where a String property is
 * defined in a base model (e.g. a Job's status property) and the various derived models specify what values they allow
 * for the that inherited property (e.g. a ServerJob's status property). In these cases, the model property needs to use
 * a String instead of a java enum (since the base model doesn't know what the possible values are), and this annotation
 * is added in the derived models to the inherited property to restrict what values it can hold.
 *
 * @author tmoreau
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LegalValues {
    String[] values();
}
