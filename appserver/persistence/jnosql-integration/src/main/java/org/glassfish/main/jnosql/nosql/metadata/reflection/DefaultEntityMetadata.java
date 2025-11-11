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


import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

class DefaultEntityMetadata implements EntityMetadata {


    private final String name;

    private final String simpleName;

    private final String className;

    private final List<String> fieldsName;

    private final Class<?> type;

    private final List<FieldMetadata> fields;

    private final InstanceSupplier instanceSupplier;

    private final Map<String, NativeMapping> javaFieldGroupedByColumn;

    private final Map<String, FieldMetadata> fieldsGroupedByName;

    private final FieldMetadata id;

    private final InheritanceMetadata inheritance;

    private final boolean hasInheritanceAnnotation;

    private final ConstructorMetadata constructor;

    DefaultEntityMetadata(String name, List<String> fieldsName, Class<?> type,
                          List<FieldMetadata> fields,
                          Map<String, NativeMapping> javaFieldGroupedByColumn,
                          Map<String, FieldMetadata> fieldsGroupedByName,
                          InstanceSupplier instanceSupplier,
                          InheritanceMetadata inheritance,
                          ConstructorMetadata constructor,
                          boolean hasInheritanceAnnotation) {
        this.name = name;
        this.simpleName = type.getSimpleName();
        this.className = type.getName();
        this.fieldsName = fieldsName;
        this.type = type;
        this.fields = fields;
        this.fieldsGroupedByName = fieldsGroupedByName;
        this.javaFieldGroupedByColumn = javaFieldGroupedByColumn;
        this.instanceSupplier = instanceSupplier;
        this.id = fields.stream().filter(FieldMetadata::isId).findFirst().orElse(null);
        this.inheritance = inheritance;
        this.constructor = constructor;
        this.hasInheritanceAnnotation = hasInheritanceAnnotation;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String simpleName() {
        return simpleName;
    }

    @Override
    public String className() {
        return this.className;
    }

    @Override
    public List<String> fieldsName() {
        return fieldsName;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Optional<InheritanceMetadata> inheritance() {
        return ofNullable(inheritance);
    }

    @Override
    public boolean hasEntityName() {
        return Objects.isNull(inheritance) || hasInheritanceAnnotation;
    }

    @Override
    public boolean isInheritance() {
        return hasInheritanceAnnotation;
    }

    @Override
    public List<FieldMetadata> fields() {
        return fields;
    }

    @Override
    public <T> T newInstance() {
        return (T) instanceSupplier.get();
    }

    @Override
    public ConstructorMetadata constructor() {
        return constructor;
    }

    @Override
    public String columnField(String javaField) {
        requireNonNull(javaField, "javaField is required");
        return ofNullable(javaFieldGroupedByColumn.get(javaField))
                .map(NativeMapping::nativeField).orElse(javaField);

    }

    @Override
    public Optional<FieldMetadata> fieldMapping(String javaField) {
        requireNonNull(javaField, "javaField is required");
        return ofNullable(javaFieldGroupedByColumn.get(javaField))
                .map(NativeMapping::fieldMetadata);
    }

    @Override
    public Map<String, FieldMetadata> fieldsGroupByName() {
        return fieldsGroupedByName;
    }

    @Override
    public Optional<FieldMetadata> id() {
        return ofNullable(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultEntityMetadata that)) {
            return false;
        }
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public String toString() {
        return "DefaultEntityMetadata{" +
                "name='" + name + '\'' +
                ", fieldsName=" + fieldsName +
                ", classInstance=" + type +
                ", fields=" + fields +
                ", instanceSupplier=" + instanceSupplier +
                ", javaFieldGroupedByColumn=" + javaFieldGroupedByColumn +
                ", fieldsGroupedByName=" + fieldsGroupedByName +
                ", id=" + id +
                ", inheritance=" + inheritance +
                ", hasInheritanceAnnotation=" + hasInheritanceAnnotation +
                '}';
    }

    /**
     * Creates a builder
     *
     * @return {@link EntityMetadataBuilder}
     */
    static EntityMetadataBuilder builder() {
        return new EntityMetadataBuilder();
    }


}
