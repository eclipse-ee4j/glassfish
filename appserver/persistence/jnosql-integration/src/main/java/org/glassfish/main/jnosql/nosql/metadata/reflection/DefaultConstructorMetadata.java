/*
 *  Copyright (c) 2023,2025 Contributors to the Eclipse Foundation
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
package org.glassfish.main.jnosql.nosql.metadata.reflection;

import java.lang.reflect.Constructor;
import java.util.List;

import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
record DefaultConstructorMetadata(Constructor<?> constructor,
                                  List<ParameterMetaData> parameters) implements ConstructorMetadata {

    @Override
    public boolean isDefault() {
        return parameters.isEmpty();
    }

}
