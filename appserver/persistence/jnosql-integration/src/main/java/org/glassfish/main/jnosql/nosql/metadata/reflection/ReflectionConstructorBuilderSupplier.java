/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License, version 2.0, which is available at
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
package org.glassfish.main.jnosql.nosql.metadata.reflection;

import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilderSupplier;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;

/**
 * A supplier of constructor using reflection.
 */
public class ReflectionConstructorBuilderSupplier implements ConstructorBuilderSupplier {

    @Override
    public ConstructorBuilder apply(ConstructorMetadata constructorMetadata) {
        return DefaultConstructorBuilder.of(constructorMetadata);
    }
}

