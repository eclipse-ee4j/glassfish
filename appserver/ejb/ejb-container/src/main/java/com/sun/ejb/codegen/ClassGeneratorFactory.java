/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.ejb.codegen;

import org.glassfish.pfl.dynamic.codegen.spi.Wrapper;

/**
 * Convenience interface that defines a factory for ClassGenerator instances.
 * It puts the class name of the generated class in a single place.
 */
public interface ClassGeneratorFactory {

    /**
     * @return name of the generated class or interface
     */
    String getGeneratedClassName();

    /**
     * @return loadable class of the same package as {@link #getGeneratedClassName()}
     */
    Class<?> getAnchorClass();

    /**
     * Calls {@link Wrapper} methods to configure the class definition.
     */
    void evaluate();
}
