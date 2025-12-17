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

import jakarta.nosql.Convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public final class ReflectionClassConverter implements ClassConverter {

    private static final Logger LOGGER = Logger.getLogger(ReflectionClassConverter.class.getName());

    private final Reflections reflections;
    private final ConstructorMetadataBuilder constructorMetadataBuilder;

    public ReflectionClassConverter() {
        this.reflections = new Reflections();
        this.constructorMetadataBuilder = new ConstructorMetadataBuilder(reflections);
    }


    @Override
    public EntityMetadata apply(Class<?> entity) {

        long start = System.currentTimeMillis();
        String entityName = reflections.getEntityName(entity);

        List<FieldMetadata> fields = reflections.getFields(entity)
                .stream().map(this::to).collect(toList());

        List<String> fieldsName = fields.stream().map(FieldMetadata::name).collect(toList());

        Map<String, NativeMapping> nativeFieldGroupByJavaField =
                getNativeFieldGroupByJavaField(fields, "", "");

        Map<String, FieldMetadata> fieldsGroupedByName = fields.stream()
                .collect(collectingAndThen(toMap(FieldMetadata::name,
                        Function.identity()), Collections::unmodifiableMap));


        Constructor<?> constructor = Reflections.getConstructor(entity);
        InstanceSupplier instanceSupplier = () -> Reflections.newInstance(constructor);
        InheritanceMetadata inheritance = reflections.getInheritance(entity).orElse(null);
        boolean hasInheritanceAnnotation = reflections.hasInheritanceAnnotation(entity);

        EntityMetadata mapping = DefaultEntityMetadata.builder().name(entityName)
                .type(entity)
                .fields(fields)
                .fieldsName(fieldsName)
                .instanceSupplier(instanceSupplier)
                .javaFieldGroupedByColumn(nativeFieldGroupByJavaField)
                .fieldsGroupedByName(fieldsGroupedByName)
                .inheritance(inheritance)
                .hasInheritanceAnnotation(hasInheritanceAnnotation)
                .constructor(constructorMetadataBuilder.build(entity))
                .build();

        long end = System.currentTimeMillis() - start;
        LOGGER.finest(String.format("Scanned the entity %s loaded with time of %d ms", entity.getName(), end));
        return mapping;
    }

    private Map<String, NativeMapping> getNativeFieldGroupByJavaField(List<FieldMetadata> fields,
                                                                      String javaField, String nativeField) {

        Map<String, NativeMapping> nativeFieldGroupByJavaField = new HashMap<>();

        for (FieldMetadata field : fields) {
            appendValue(nativeFieldGroupByJavaField, field, javaField, nativeField);
        }

        return nativeFieldGroupByJavaField;
    }

    private void appendValue(Map<String, NativeMapping> nativeFieldGroupByJavaField, FieldMetadata field,
                             String javaField, String nativeField) {


        switch (field.mappingType()) {
            case ENTITY -> appendFields(nativeFieldGroupByJavaField, field, javaField,
                    appendPreparePrefix(nativeField, field.name()));
            case EMBEDDED -> appendFields(nativeFieldGroupByJavaField, field, javaField, nativeField);
            case COLLECTION -> {
                if (((CollectionFieldMetadata) field).isEmbeddable()) {
                    Class<?> type = ((CollectionFieldMetadata) field).elementType();
                    String nativeFieldAppended = appendPreparePrefix(nativeField, field.name());
                    appendFields(nativeFieldGroupByJavaField, field, javaField, nativeFieldAppended, type);
                    return;
                }
                appendDefaultField(nativeFieldGroupByJavaField, field, javaField, nativeField);
            }
            default -> appendDefaultField(nativeFieldGroupByJavaField, field, javaField, nativeField);
        }

    }

    private void appendDefaultField(Map<String, NativeMapping> nativeFieldGroupByJavaField,
                                    FieldMetadata field, String javaField, String nativeField) {

        nativeFieldGroupByJavaField.put(javaField.concat(field.fieldName()),
                NativeMapping.of(nativeField.concat(field.name()), field));
    }

    private void appendFields(Map<String, NativeMapping> nativeFieldGroupByJavaField,
                              FieldMetadata field,
                              String javaField, String nativeField) {

        Class<?> type = field.type();
        appendFields(nativeFieldGroupByJavaField, field, javaField, nativeField, type);
    }

    private void appendFields(Map<String, NativeMapping> nativeFieldGroupByJavaField,
                              FieldMetadata field, String javaField, String nativeField,
                              Class<?> type) {

        Map<String, NativeMapping> entityMap = getNativeFieldGroupByJavaField(
                reflections.getFields(type)
                        .stream().map(this::to).collect(toList()),
                appendPreparePrefix(javaField, field.fieldName()), nativeField);

        String nativeElement = entityMap.values().stream().map(NativeMapping::nativeField)
                .collect(Collectors.joining(","));

        nativeFieldGroupByJavaField.put(appendPrefix(javaField, field.fieldName()), NativeMapping.of(nativeElement, field));
        nativeFieldGroupByJavaField.putAll(entityMap);
    }

    private String appendPreparePrefix(String prefix, String field) {
        return appendPrefix(prefix, field).concat(".");
    }

    private String appendPrefix(String prefix, String field) {
        if (prefix.isEmpty()) {
            return field;
        } else {
            return prefix.concat(field);
        }
    }


    private FieldMetadata to(Field field) {
        MappingType mappingType = MappingType.of(field.getType());
        reflections.makeAccessible(field);
        Convert convert = field.getAnnotation(Convert.class);
        boolean id = reflections.isIdField(field);
        String columnName = id ? reflections.getIdName(field) : reflections.getColumnName(field);
        String udt = reflections.getUDTName(field);
        FieldMappingBuilder builder = new FieldMappingBuilder().name(columnName)
                .field(field).type(mappingType).id(id).udt(udt)
                .reader(bean -> reflections.getValue(bean, field))
                .writer((bean, value) -> reflections.setValue(bean, field, value));

        if (nonNull(convert)) {
            builder.converter(convert.value());
        }
        switch (mappingType) {
            case COLLECTION -> {
                builder.typeSupplier(field::getGenericType);
                return builder.buildCollection();
            }
            case MAP -> {
                builder.typeSupplier(field::getGenericType);
                return builder.buildMap();
            }
            case EMBEDDED -> {
                return builder.entityName(reflections.getEntityName(field.getType())).buildEmbedded();
            }
            case ARRAY -> {
                return builder.elementType(field.getType().getComponentType()).buildArray();
            }
            default -> {
                return builder.buildDefault();
            }
        }
    }

}
