/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import org.glassfish.hk2.api.MultiException;

public interface InjectionResolverQuery {

  /**
   * Returns the value to inject in the field or method of component annotated with
   * the annotated annotation.
   *
   * @param component injection target instance
   * @param onBehalfOf inhabitant doing the injection for
   * @param annotated is the annotated java element {@link java.lang.reflect.Method}
   * or {@link java.lang.reflect.Field}
   * @param genericType the generic type of the expected return
   * @param type type of the expected return
   * @return the resource to be injected
   * @throws MultiException if the resource cannot be located.
   */
  <V> V getValue(Object component,
      AnnotatedElement annotated,
      Type genericType,
      Class<V> type) throws MultiException;

}
