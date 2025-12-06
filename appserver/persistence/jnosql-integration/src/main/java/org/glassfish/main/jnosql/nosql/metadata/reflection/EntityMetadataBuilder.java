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


import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;

import static java.util.Collections.emptyMap;

class EntityMetadataBuilder {

    private String name;

    private List<String> fieldsName = Collections.emptyList();

    private Class<?> type;

    private List<FieldMetadata> fields = Collections.emptyList();

    private Map<String, NativeMapping> javaFieldGroupedByColumn = emptyMap();

    private Map<String, FieldMetadata> fieldsGroupedByName = emptyMap();

    private InstanceSupplier instanceSupplier;

    private InheritanceMetadata inheritance;

    private boolean hasInheritanceAnnotation;

    private ConstructorMetadata constructor;


    public EntityMetadataBuilder name(String name) {
        this.name = name;
        return this;
    }

    public EntityMetadataBuilder fieldsName(List<String> fieldsName) {
        this.fieldsName = fieldsName;
        return this;
    }

    public EntityMetadataBuilder type(Class<?> type) {
        this.type = type;
        return this;
    }

    public EntityMetadataBuilder fields(List<FieldMetadata> fields) {
        this.fields = fields;
        return this;
    }

    public EntityMetadataBuilder javaFieldGroupedByColumn(Map<String, NativeMapping> javaFieldGroupedByColumn) {
        this.javaFieldGroupedByColumn = javaFieldGroupedByColumn;
        return this;
    }

    public EntityMetadataBuilder fieldsGroupedByName(Map<String, FieldMetadata> fieldsGroupedByName) {
        this.fieldsGroupedByName = fieldsGroupedByName;
        return this;
    }

    public EntityMetadataBuilder instanceSupplier(InstanceSupplier instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
        return this;
    }

    public EntityMetadataBuilder inheritance(InheritanceMetadata inheritance) {
        this.inheritance = inheritance;
        return this;
    }

    public EntityMetadataBuilder hasInheritanceAnnotation(boolean hasInheritanceAnnotation) {
        this.hasInheritanceAnnotation = hasInheritanceAnnotation;
        return this;
    }

    public EntityMetadataBuilder constructor(ConstructorMetadata constructor) {
        this.constructor = constructor;
        return this;
    }


    public EntityMetadata build() {
        return new DefaultEntityMetadata(name, fieldsName, type, fields,
                javaFieldGroupedByColumn, fieldsGroupedByName, instanceSupplier, inheritance,
                constructor, hasInheritanceAnnotation);
    }
}