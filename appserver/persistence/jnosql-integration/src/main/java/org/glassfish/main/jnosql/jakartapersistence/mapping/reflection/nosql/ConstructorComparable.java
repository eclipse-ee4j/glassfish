/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.glassfish.main.jnosql.jakartapersistence.mapping.reflection.nosql;

import java.lang.reflect.Constructor;
import java.util.Comparator;

/**
 * This Comparator defines the priority of the entity's constructor that JNoSQL will use as a priority.
 * The emphasis will be on a default constructor, a non-arg-param constructor.
 *
 */
enum ConstructorComparable implements Comparator<Constructor<?>> {

    INSTANCE;

    @Override
    public int compare(Constructor<?> constructorA, Constructor<?> constructorB) {
        int parameterCount = constructorA.getParameterCount();
        int parameterCountB = constructorB.getParameterCount();
        if (parameterCount == 0 && parameterCountB == 0) {
            return 0;
        } else if (parameterCount == 0) {
            return -1;
        } else if (parameterCountB == 0) {
            return 1;
        }
        return 0;
    }
}
