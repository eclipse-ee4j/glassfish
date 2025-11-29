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

import jakarta.nosql.AttributeConverter;

import java.lang.reflect.Field;

import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.mapping.metadata.ArrayFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MapFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

class FieldMappingBuilder {

    private MappingType type;

    private Field field;

    private String name;

    private String entityName;

    private TypeSupplier<?> typeSupplier;

    private Class<? extends AttributeConverter<?, ?>> converter;

    private boolean id;

    private FieldReader reader;

    private FieldWriter writer;

    private String udt;

    private Class<?> elementType;


    public FieldMappingBuilder type(MappingType type) {
        this.type = type;
        return this;
    }

    public FieldMappingBuilder field(Field field) {
        this.field = field;
        return this;
    }

    public FieldMappingBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FieldMappingBuilder typeSupplier(TypeSupplier<?> typeSupplier) {
        this.typeSupplier = typeSupplier;
        return this;
    }

    public FieldMappingBuilder entityName(String entityName) {
        this.entityName = entityName;
        return this;
    }

    public FieldMappingBuilder converter(Class<? extends AttributeConverter<?, ?>> converter) {
        this.converter = converter;
        return this;
    }

    public FieldMappingBuilder id(boolean id) {
        this.id = id;
        return this;
    }

    public FieldMappingBuilder writer(FieldWriter writer) {
        this.writer = writer;
        return this;
    }

    public FieldMappingBuilder udt(String udt) {
        this.udt = udt;
        return this;
    }

    public FieldMappingBuilder reader(FieldReader reader) {
        this.reader = reader;
        return this;
    }

    public FieldMappingBuilder elementType(Class<?> elementType) {
        this.elementType = elementType;
        return this;
    }

    public DefaultFieldMetadata buildDefault() {
        return new DefaultFieldMetadata(type, field, name, converter, id, reader, writer, udt);
    }

    public CollectionFieldMetadata buildCollection() {
        return new DefaultCollectionFieldMetadata(type, field, name, typeSupplier, converter, reader, writer, udt);
    }

    public MapFieldMetadata buildMap() {
        return new DefaultMapFieldMetadata(type, field, name, typeSupplier, converter, reader, writer, udt);
    }

    public EmbeddedFieldMetadata buildEmbedded() {
        return new EmbeddedFieldMetadata(type, field, name, entityName, reader, writer, udt);
    }

    public ArrayFieldMetadata buildArray() {
        return new DefaultArrayFieldMetadata(type, field, name, elementType, converter, reader, writer, udt);
    }

}
