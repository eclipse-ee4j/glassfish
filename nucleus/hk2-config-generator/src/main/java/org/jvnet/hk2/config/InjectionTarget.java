/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.jvnet.hk2.annotations.Contract;

import jakarta.inject.Qualifier;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Target;

/**
 * Used on {@link ConfigInjector} to indicate
 * the target class of the injection.
 * @author Kohsuke Kawaguchi
 */
@Contract
@Retention(RUNTIME)
@Target(TYPE)
@Qualifier
public @interface InjectionTarget {
    Class<?> value();
}
