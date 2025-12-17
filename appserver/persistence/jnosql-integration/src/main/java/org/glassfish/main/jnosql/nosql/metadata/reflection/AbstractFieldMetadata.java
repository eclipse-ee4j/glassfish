/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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
import jakarta.nosql.NoSQLException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

/**
 * Base class to all {@link FieldMetadata}
 *
 * @see FieldMetadata
 */
abstract class AbstractFieldMetadata implements FieldMetadata {

    protected final MappingType mappingType;

    protected final Field field;

    protected final String name;

    protected final String fieldName;

    protected final Class<? extends AttributeConverter<?, ?>> converter;

    protected final FieldReader reader;

    protected final FieldWriter writer;

    protected final Class<?> type;

    protected final String udt;

    AbstractFieldMetadata(MappingType mappingType, Field field, String name,
                          Class<? extends AttributeConverter<?, ?>> converter,
                          FieldReader reader, FieldWriter writer, String udt) {
        this.mappingType = mappingType;
        this.field = field;
        this.name = name;
        this.fieldName = field.getName();
        this.converter = converter;
        this.reader = reader;
        this.writer = writer;
        this.type = field.getType();
        this.udt = udt;
    }

    @Override
    public MappingType mappingType() {
        return mappingType;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String fieldName() {
        return fieldName;
    }

    @Override
    public Optional<String> udt(){
        if(udt == null|| udt.isEmpty() || udt.isBlank()){
            return Optional.empty();
        }
        return Optional.of(udt);
    }

    @Override
    public Object read(Object bean) {
        Objects.requireNonNull(bean, "bean is required");
        return this.reader.read(bean);
    }

    @Override
    public void write(Object bean, Object value) {
        Objects.requireNonNull(bean, "bean is required");
        this.writer.write(bean, value);
    }

    @Override
    public Class<?> type() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X, Y, T extends AttributeConverter<X, Y>> Optional<Class<T>> converter() {
        return Optional.ofNullable((Class<T>) converter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X, Y, T extends AttributeConverter<X, Y>> Optional<T> newConverter() {
        return (Optional<T>) Optional.ofNullable(converter).map(Reflections::newInstance);
    }

    @Override
    public Object value(Value value) {
        return value.get(field.getType());
    }


    @Override
    public <T extends Annotation> Optional<String> value(Class<T> type){
        Objects.requireNonNull(type, "type is required");
        Optional<Method> method = Arrays.stream(type.getDeclaredMethods()).filter(m -> "value".equals(m.getName()))
                .findFirst();
        T annotation = this.field.getAnnotation(type);
        if (method.isEmpty() || annotation == null) {
            return Optional.empty();
        }
        return method.map(m -> {
            try {
                Object invoke = m.invoke(annotation);
                return invoke.toString();
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new NoSQLException("There is an issue invoking the method: " + m + " using the annotation: "
                + type, e);
            }
        });
    }
}
