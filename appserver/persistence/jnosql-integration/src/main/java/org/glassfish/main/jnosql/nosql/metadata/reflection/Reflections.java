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
 *   Maximillian Arruda
 */
package org.glassfish.main.jnosql.nosql.metadata.reflection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.nosql.Column;

import jakarta.nosql.DiscriminatorValue;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import jakarta.nosql.DiscriminatorColumn;
import jakarta.nosql.Inheritance;
import jakarta.nosql.MappedSuperclass;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Utilitarian class to reflection
 */
@ApplicationScoped
public class Reflections {

    private static final Logger LOGGER = Logger.getLogger(Reflections.class.getName());

    private static final Predicate<String> IS_ID_ANNOTATION = Id.class.getName()::equals;
    private static final Predicate<String> IS_COLUMN_ANNOTATION = Column.class.getName()::equals;
    private static final Predicate<String> IS_NOSQL_ANNOTATION = IS_ID_ANNOTATION.or(IS_COLUMN_ANNOTATION);

    /**
     * Return The Object from the Column.
     *
     * @param object the object
     * @param field  the field to return object
     * @return - the field value in Object
     */
    Object getValue(Object object, Field field) {

        try {
            return field.get(object);
        } catch (Exception exception) {
            LOGGER.log(Level.FINEST, "There is an issue with returning value from this field.", exception);
        }
        return null;
    }


    /**
     * Set the field in the Object.
     *
     * @param object the object
     * @param field  the field to return object
     * @param value  the value to object
     * @return - if the operation was executed with success
     */
    boolean setValue(Object object, Field field, Object value) {
        try {

            field.set(object, value);

        } catch (Exception exception) {
            LOGGER.log(Level.FINEST, "There is an issue with setting value from this field.", exception);
            return false;
        }
        return true;
    }

    /**
     * Create new instance of this class.
     *
     * @param constructor the constructor
     * @param <T>         the instance type
     * @return the new instance that class
     */
    public static <T> T newInstance(Constructor<T> constructor) {
        try {
            return constructor.newInstance();
        } catch (Exception exception) {
            LOGGER.log(Level.FINEST, "There is an issue to creating an entity from this constructor", exception);
            return null;
        }
    }


    /**
     * Create new instance of this class.
     *
     * @param type the class's type
     * @param <T>  the instance type
     * @return the new instance that class
     */
    public static <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = getConstructor(type);
            return newInstance(constructor);
        } catch (Exception exception) {
            LOGGER.log(Level.FINEST, "There is an issue to creating an entity from this constructor", exception);
            return null;
        }
    }


    /**
     * Make the given field accessible, explicitly setting it accessible
     * if necessary. The setAccessible(true) method is only
     * called when actually necessary, to avoid unnecessary
     * conflicts with a JVM SecurityManager (if active).
     *
     * @param field field the field to make accessible
     */
    void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier
                .isPublic(field.getDeclaringClass().getModifiers()))
                && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * Make the given a constructor class accessible, explicitly setting it accessible
     * if necessary. The setAccessible(true) method is only
     * called when actually necessary, to avoid unnecessary
     * conflicts with a JVM SecurityManager (if active).
     *
     * @param type the class constructor accessible
     * @param <T>  the entity type
     * @return the constructor class
     * @throws ConstructorException when the constructor has public and default
     */
    public static <T> Constructor<T> getConstructor(Class<T> type) {

        final Predicate<Constructor<?>> defaultConstructorPredicate = c -> c.getParameterCount() == 0;
        final Predicate<Constructor<?>> customConstructorPredicate = c -> {
            for (Parameter parameter : c.getParameters()) {
                if (hasNoSQLAnnotation(parameter)) {
                    return true;
                }
            }
            return false;
        };

        List<Constructor<?>> constructors = Stream.
                of(type.getDeclaredConstructors())
                .filter(defaultConstructorPredicate.or(customConstructorPredicate))
                .toList();

        if (constructors.isEmpty()) {
            throw new ConstructorException(type);
        }

        Optional<Constructor<?>> publicConstructor = constructors
                .stream()
                .sorted(ConstructorComparable.INSTANCE)
                .filter(c -> Modifier.isPublic(c.getModifiers()))
                .findFirst();
        if (publicConstructor.isPresent()) {
            return (Constructor<T>) publicConstructor.get();
        }

        Constructor<?> constructor = constructors.get(0);
        constructor.setAccessible(true);
        return (Constructor<T>) constructor;
    }

    /**
     * Checks if the {@link Parameter} instance is annotated with
     * Jakarta NoSQL annotations (@{@link Id} or @{@link Column}).
     *
     * @param parameter the parameter
     * @return if the provided {@link Parameter} instance is annotated with
     * Jakarta NoSQL annotations (@{@link Id} or @{@link Column}).
     */
    static boolean hasNoSQLAnnotation(Parameter parameter) {
        return parameter != null && Arrays.stream(parameter.getAnnotations())
                .map(Annotation::annotationType)
                .map(Class::getName)
                .anyMatch(IS_NOSQL_ANNOTATION);
    }

    /**
     * Returns the name of the entity. So it tries to read the {@link Entity} otherwise
     * {@link Class#getSimpleName()}
     *
     * @param entity the class to read
     * @return the {@link Entity} when is not blank otherwise {@link Class#getSimpleName()}
     * @throws NullPointerException when entity is null
     */
    String getEntityName(Class<?> entity) {
        requireNonNull(entity, "class entity is required");

        if (isInheritance(entity)) {
            return readEntity(entity.getSuperclass());
        }
        return readEntity(entity);
    }

    /**
     * Returns the fields from the entity class
     *
     * @param type the entity class
     * @return the list of fields that is annotated with either {@link Column} or
     * {@link Id}
     * @throws NullPointerException when class entity is null
     */
    List<Field> getFields(Class<?> type) {
        requireNonNull(type, "class entity is required");

        List<Field> fields = new ArrayList<>();

        if (isMappedSuperclass(type)) {
            fields.addAll(getFields(type.getSuperclass()));
        }
        Predicate<Field> hasColumnAnnotation = f -> f.getAnnotation(Column.class) != null;
        Predicate<Field> hasIdAnnotation = f -> f.getAnnotation(Id.class) != null;

        Stream.of(type.getDeclaredFields())
                .filter(hasColumnAnnotation.or(hasIdAnnotation))
                .forEach(fields::add);
        return fields;
    }

    /**
     * Checks if the class is annotated with {@link MappedSuperclass} or
     * {@link Inheritance}
     *
     * @param type the entity class
     * @return if the class is annotated
     * @throws NullPointerException when type is null
     */
    boolean isMappedSuperclass(Class<?> type) {
        requireNonNull(type, "class entity is required");
        Class<?> superclass = type.getSuperclass();
        return superclass.getAnnotation(MappedSuperclass.class) != null
                || superclass.getAnnotation(Inheritance.class) != null;
    }

    /**
     * Checks if the field is annotated with {@link Column}
     *
     * @param field the field
     * @return if the field is annotated with {@link Column}
     * @throws NullPointerException when the field is null
     */
    boolean isIdField(Field field) {
        requireNonNull(field, "field is required");
        return field.getAnnotation(Id.class) != null;
    }

    /**
     * Gets the name from the field, so it reads the {@link Column#value()}
     * otherwise {@link Field#getName()}
     *
     * @param field the fields
     * @return the column name
     * @throws NullPointerException when the field is null
     */
    String getColumnName(Field field) {
        requireNonNull(field, "field is required");
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::value)
                .filter(StringUtils::isNotBlank)
                .orElse(field.getName());
    }

    /**
     * Gets the id name, so it reads the {@link Id#value()} otherwise {@link Field#getName()}
     *
     * @param field the field
     * @return the column name
     * @throws NullPointerException when the field is null
     */
    String getIdName(Field field) {
        requireNonNull(field, "field is required");
        return Optional.ofNullable(field.getAnnotation(Id.class))
                .map(Id::value)
                .filter(StringUtils::isNotBlank)
                .orElse(field.getName());
    }

    /**
     * Reads the type annotation and checks if the inheritance has an
     * {@link Inheritance} annotation.
     * If it has, it will return the {@link InheritanceMetadata} otherwise it will return
     * {@link Optional#empty()}
     *
     * @param type the type class
     * @return the {@link InheritanceMetadata} or {@link Optional#empty()}
     * @throws NullPointerException when type is null
     */
    Optional<InheritanceMetadata> getInheritance(Class<?> type) {
        requireNonNull(type, "entity is required");
        if (isInheritance(type)) {
            Class<?> parent = type.getSuperclass();
            String discriminatorColumn = getDiscriminatorColumn(parent);
            String discriminatorValue = getDiscriminatorValue(type);
            return Optional.of(new InheritanceMetadata(discriminatorValue, discriminatorColumn,
                    parent, type));
        } else if (type.getAnnotation(Inheritance.class) != null) {
            String discriminatorColumn = getDiscriminatorColumn(type);
            String discriminatorValue = getDiscriminatorValue(type);
            return Optional.of(new InheritanceMetadata(discriminatorValue, discriminatorColumn,
                    type, type));
        }
        return Optional.empty();
    }

    /**
     * Check if the entity has the {@link Inheritance} annotation
     *
     * @param entity the entity
     * @return true if it has the {@link Inheritance} annotation
     */
    boolean hasInheritanceAnnotation(Class<?> entity) {
        requireNonNull(entity, "entity is required");
        return entity.getAnnotation(Inheritance.class) != null;
    }

    /**
     * Retrieves the User-Defined Type (UDT) name associated with the given field.
     *
     * <p>
     * This method retrieves the UDT name specified in the {@link Column} annotation of the provided field.
     * If the field is not annotated with {@link Column}, or if the UDT name is blank or not specified,
     * this method returns {@code null}.
     * </p>
     *
     * @param field the field from which to retrieve the UDT name
     * @return the UDT name specified in the {@link Column} annotation of the field, or {@code null} if not specified
     * @throws NullPointerException if the field is null
     */
    public String getUDTName(Field field) {
        requireNonNull(field, "field is required");
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::udt)
                .filter(StringUtils::isNotBlank)
                .orElse(null);
    }

    /**
     * Attempts to locate the specific generic declaration of the desired type,
     * walking the interface and superclass hierarchy to locate it.
     *
     * @param type       the type to scan, such as a field's generic type
     * @param parentType the type to search for, such as {@code Map}
     * @return an {@link Optional} describing the found declaration, or an
     * empty one if it cannot be found
     * @since 1.1.5
     */
    public static Optional<ParameterizedType> findParameterizedType(Type type, Class<?> parentType) {
        if (type instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() instanceof Class rawClass) {
            if (parentType.isAssignableFrom(rawClass)) {
                return Optional.of(parameterizedType);
            }
        }
        if (type instanceof Class classType) {
            Type superType = classType.getGenericSuperclass();
            if (superType != null) {
                Optional<ParameterizedType> superResult = findParameterizedType(superType, parentType);
                if (superResult.isPresent()) {
                    return superResult;
                }
            }
            for (Type superInterface : classType.getGenericInterfaces()) {
                Optional<ParameterizedType> superResult = findParameterizedType(superInterface, parentType);
                if (superResult.isPresent()) {
                    return superResult;
                }
            }
        }

        return Optional.empty();
    }


    private String getDiscriminatorColumn(Class<?> parent) {
        return Optional
                .ofNullable(parent.getAnnotation(DiscriminatorColumn.class))
                .map(DiscriminatorColumn::value)
                .orElse(DiscriminatorColumn.DEFAULT_DISCRIMINATOR_COLUMN);
    }

    private String getDiscriminatorValue(Class<?> entity) {
        return Optional
                .ofNullable(entity.getAnnotation(DiscriminatorValue.class))
                .map(DiscriminatorValue::value)
                .orElse(entity.getSimpleName());
    }

    private String readEntity(Class<?> entity) {
        return Optional.ofNullable(entity.getAnnotation(Entity.class))
                .map(Entity::value)
                .filter(StringUtils::isNotBlank)
                .orElse(entity.getSimpleName());
    }

    private boolean isInheritance(Class<?> entity) {
        Class<?> superclass = entity.getSuperclass();
        return Optional.ofNullable(superclass).map(s -> s.getAnnotation(Inheritance.class)).isPresent();
    }


}
