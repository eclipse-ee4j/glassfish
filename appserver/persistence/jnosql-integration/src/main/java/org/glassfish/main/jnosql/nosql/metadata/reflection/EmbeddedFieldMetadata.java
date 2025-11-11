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
package org.glassfish.main.jnosql.nosql.metadata.reflection;

import org.eclipse.jnosql.mapping.metadata.MappingType;

import java.lang.reflect.Field;
import java.util.Objects;

final class EmbeddedFieldMetadata extends AbstractFieldMetadata {

    private final String entityName;

    public EmbeddedFieldMetadata(MappingType type, Field field, String name, String entityName,
                                 FieldReader reader, FieldWriter writer, String udt) {
        super(type, field, name, null, reader, writer, udt);
        this.entityName = entityName;
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmbeddedFieldMetadata that = (EmbeddedFieldMetadata) o;
        return mappingType == that.mappingType &&
                Objects.equals(field, that.field) &&
                Objects.equals(entityName, that.entityName) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingType, field, name, entityName);
    }

    @Override
    public String toString() {
        return  "EmbeddedFieldMapping{" + "entityName='" + entityName + '\'' +
                ", type=" + mappingType +
                ", field=" + field +
                ", name='" + name + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", converter=" + converter +
                '}';
    }
}
