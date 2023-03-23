/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
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

package com.sun.jdo.spi.persistence.support.ejb.model;

import com.sun.jdo.api.persistence.mapping.ejb.AbstractNameMapper;
import com.sun.jdo.api.persistence.model.RuntimeModel;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.api.persistence.model.jdo.RelationshipElement;
import com.sun.jdo.spi.persistence.support.ejb.model.util.NameMapper;
import com.sun.jdo.spi.persistence.utility.JavaTypeHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;
import org.glassfish.ejb.deployment.descriptor.PersistenceDescriptor;

/**
 * This is a subclass of RuntimeModel which uses the deployment descriptor
 * to augment the java metadata for a non-existent persistence-capable
 * java/class file. It is primarily used at ejbc time, though it could be
 * used at any time as long as sufficient mapping and deployment descriptor
 * information is available. See the javadoc for individual methods below
 * for differences and translations between persistence-capable and ejb
 * names and behavior. There are different answers to methods depending
 * on whether they are called on the persistence-capable or the ejb. These
 * are primarily for the handling of serializable, non-primitive, non-wrapper
 * type fields: isByteArray, getFieldType (returning byte array),
 * isPersistentTypeAllowed and isPersistentAllowed (returning true) which
 * return answers about the byte array when called on the persistence-capable
 * and return answers about the serializable type when called on the ejb.
 *
 * @author Rochelle Raccah 2001
 */
public class DeploymentDescriptorModel extends RuntimeModel {

    private final ClassLoader classLoader;
    private final NameMapper nameMapper;

    /**
     * Signature with CVS keyword substitution for identifying the generated code
     */
    public static final String SIGNATURE = "$RCSfile: DeploymentDescriptorModel.java,v $ $Revision: 1.2 $"; //NOI18N

    /** Creates a new instance of DeploymentDescriptorModel
     * @param nameMapper the name mapper to be used as a helper for
     * translation between persistence class and ejb names.
     * @param classLoader the class loader object which is used for the
     * application.
     */
    public DeploymentDescriptorModel(NameMapper nameMapper, ClassLoader classLoader) {
        super();
        this.classLoader = classLoader;
        this.nameMapper = nameMapper;
    }


    /**
     * {@inheritDoc}
     *
     * @param classLoader ignored parameter, using CL provided in constructor instead
     */
    @Override
    protected BufferedInputStream getInputStreamForResource(String className, ClassLoader classLoader,
        String resourceName) {
        return super.getInputStreamForResource(className, this.classLoader, resourceName);
    }


    /** Returns the name of the second to top (top excluding java.lang.Object)
     * superclass for the given class name.  This method overrides the one in
     * RuntimeModel in order to return the supplied className if it
     * represents a persistence-capable class.  This is because the
     * persistence-capable classes don't mirror the inheritance
     * hierarchy of the ejbs.
     * @param className the fully qualified name of the class to be checked
     * @return the top non-Object superclass for className,
     * <code>className</code> if an error occurs or none exists
     */
    @Override
    protected String findPenultimateSuperclass(String className) {
        return isPCClassName(className) ? className : super.findPenultimateSuperclass(className);
    }

    /** Returns the name of the superclass for the given class name.  This
     * method overrides the one in RuntimeModel in order to return
     * java.lang.Object if the supplied className represents a
     * persistence-capable class.  This is because the persistence-capable
     * classes don't mirror the inheritance hierarchy of the ejbs.
     * @param className the fully qualified name of the class to be checked
     * @return the superclass for className, <code>null</code> if an error
     * occurs or none exists
     */
    @Override
    protected String getSuperclass(String className) {
        return isPCClassName(className) ? "java.lang.Object" : super.getSuperclass(className);
    }

    /** Creates a file with the given base file name and extension
     * parallel to the supplied class (if it does not yet exist).  This
     * method overrides the one in RuntimeModel and throws
     * UnsupportedOperationException in the case where the file
     * doesn't yet exist.  If the file exists (even if it has
     * just been "touched"), this method will return that output stream,
     * but it is not capable of using the supplied class as a sibling location.
     * @param className the fully qualified name of the class
     * @param baseFileName the name of the base file
     * @param extension the file extension
     * @return the output stream for the specified resource, <code>null</code>
     * if an error occurs or none exists
     * @exception IOException if there is some error creating the file
     */
    @Override
    protected BufferedOutputStream createFile(String className, String baseFileName, String extension)
        throws IOException {
        BufferedOutputStream outputStream = super.createFile(className, baseFileName, extension);

        if (outputStream != null) {
            return outputStream;
        }

        throw new UnsupportedOperationException();
    }


    /** Deletes the file with the given file name which is parallel
     * to the supplied class.  This method overrides the one in RuntimeModel
     * and throws UnsupportedOperationException.
     * @param className the fully qualified name of the class
     * @param fileName the name of the file
     * @exception IOException if there is some error deleting the file
     */
    @Override
    protected void deleteFile(String className, String fileName) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** Returns the class element with the specified className.  If the
     * specified className represents a persistence-capable class, the
     * abstract bean class for the corresponding ejb is always returned
     * (even if there is a Class object available for the
     * persistence-capable).  If there is an ejb name and an abstract bean
     * class with the same name, the abstract bean class which is associated
     * with the ejb will be returned, not the abstract bean class which
     * corresponds to the supplied name (directly).  If the specified
     * className represents a persistence-capable key class name, the
     * corresponding bean's key class is returned.
     * @param className the fully qualified name of the class to be checked
     * @param classLoader the class loader used to find mapping information
     * @return the class element for the specified className
     */
    @Override
    public Object getClass(final String className, final ClassLoader classLoader) {
        String testClass = className;

        // translate the class name to corresponding ejb name's abstract
        // bean or key class if necessary
        if (className != null) {
            String ejbName = (isPCClassName(className) ? getEjbName(className) : className);
            if (nameMapper.isEjbName(ejbName)) {
                testClass = nameMapper.getAbstractBeanClassForEjbName(ejbName);
            } else {
                String keyClass = nameMapper.getKeyClassForPersistenceKeyClass(className);
                if (keyClass != null) {
                    // if it's a pk field of type primitive, byte[],
                    // or other array, return the primitive class or a dummy class
                    if (AbstractNameMapper.PRIMARY_KEY_FIELD == getPersistenceKeyClassType(className)) {
                        if (isPrimitive(keyClass)) {
                            return JavaTypeHelper.getPrimitiveClass(keyClass);
                        }
                        if (isByteArray(keyClass) || keyClass.endsWith("[]")) {
                            return byte[].class;
                        }
                    }

                    testClass = keyClass;
                }
            }
        }
        return super.getClass(testClass, this.classLoader);
    }

    /** Returns a wrapped constructor element for the specified argument types
     * in the class with the specified name.  If the specified class name is
     * a persistence-capable key class name which corresponds to a bean
     * with an unknown primary key class a dummy constructor will also be
     * returned.  Types are specified as type names for primitive type
     * such as int, float or as fully qualified class names.
     * @param className the name of the class which contains the constructor
     * to be checked
     * @param argTypeNames the fully qualified names of the argument types
     * @return the constructor element
     * @see #getClass
     */
    @Override
    public Object getConstructor(final String className, String[] argTypeNames) {
        if ((AbstractNameMapper.PRIMARY_KEY_FIELD == getPersistenceKeyClassType(className))
            && Arrays.equals(argTypeNames, NO_ARGS)) {
            return new MemberWrapper(className, null, Modifier.PUBLIC, (Class) getClass(className));
        }
        Object returnObject = super.getConstructor(className, argTypeNames);
        if (returnObject instanceof Constructor) {
            return new MemberWrapper((Constructor<?>) returnObject);
        }
        return returnObject;
    }

    /** Returns a wrapped method element for the specified method name and
     * argument types in the class with the specified name.  If the
     * specified className represents a persistence-capable class and
     * the requested methodName is readObject or writeObject, a dummy
     * method will be returned.  Similarly, if the specified class name is
     * a persistence-capable key class name which corresponds to a bean
     * with an unknown primary key class or a primary key field (in both
     * cases there is no user defined primary key class) and the requested
     * method is equals or hashCode, a dummy method will also be returned.
     * Types are specified as type names for primitive type such as int,
     * float or as fully qualified class names.  Note, the method does not
     * return inherited methods.
     * @param className the name of the class which contains the method
     * to be checked
     * @param methodName the name of the method to be checked
     * @param argTypeNames the fully qualified names of the argument types
     * @return the method element
     * @see #getClass
     */
    @Override
    public Object getMethod(final String className, final String methodName, String[] argTypeNames) {
        int keyClassType = getPersistenceKeyClassType(className);
        Object returnObject = null;

        if (isPCClassName(className)) {
            if ((methodName.equals("readObject") && Arrays.equals(argTypeNames, getReadObjectArgs()))
                || (methodName.equals("writeObject") && Arrays.equals(argTypeNames, getWriteObjectArgs()))) {
                returnObject = new MemberWrapper(methodName, Void.TYPE, Modifier.PRIVATE, (Class<?>) getClass(className));
            }
        }
        if ((AbstractNameMapper.UNKNOWN_KEY_CLASS == keyClassType)
            || (AbstractNameMapper.PRIMARY_KEY_FIELD == keyClassType)) {
            if (methodName.equals("equals") && Arrays.equals(argTypeNames, getEqualsArgs())) {
                returnObject = new MemberWrapper(methodName, Boolean.TYPE, Modifier.PUBLIC,
                    (Class<?>) getClass(className));
            } else if (methodName.equals("hashCode") && Arrays.equals(argTypeNames, NO_ARGS)) {
                returnObject = new MemberWrapper(methodName, Integer.TYPE, Modifier.PUBLIC,
                    (Class<?>) getClass(className));
            }
        }

        if (returnObject == null) {
            returnObject = super.getMethod(className, methodName, argTypeNames);
            if (returnObject instanceof Method) {
                // wrap it
                returnObject = new MemberWrapper((Method) returnObject);
            }
        }

        return returnObject;
    }

    /** Returns the inherited method element for the specified method
     * name and argument types in the class with the specified name.
     * Types are specified as type names for primitive type such as
     * int, float or as fully qualified class names.  Note that the class
     * with the specified className is not checked for this method, only
     * superclasses are checked.  This method overrides the one in
     * Model in order to do special handling for a persistence-capable key
     * class name which corresponds to a bean with a primary key
     * field.  In that case, we don't want to climb the inheritance
     * of the primary key field type.
     * @param className the name of the class which contains the method
     * to be checked
     * @param methodName the name of the method to be checked
     * @param argTypeNames the fully qualified names of the argument types
     * @return the method element
     * @see #getClass
     */
    @Override
    public Object getInheritedMethod(String className, String methodName, String[] argTypeNames) {
        // If the class name corresponds to a pk field (which means that
        // there is no user defined primary key class, we don't want to
        // climb the inheritance hierarchy, we only process this class.
        return ((AbstractNameMapper.PRIMARY_KEY_FIELD ==
            getPersistenceKeyClassType(className)) ?
            getMethod(className, methodName, argTypeNames) :
            super.getInheritedMethod(className, methodName, argTypeNames));
    }

    /** Returns a list of names of all the declared field elements in the
     * class with the specified name.  If the specified className represents
     * a persistence-capable class, the list of field names from the
     * corresponding ejb is returned (even if there is a Class object
     * available for the persistence-capable).
     * @param className the fully qualified name of the class to be checked
     * @return the names of the field elements for the specified class
     */
    @Override
    public List getFields(final String className) {
        final EjbCMPEntityDescriptor descriptor = getCMPDescriptor(className);
        String testClass = className;
        if (descriptor != null) {
            // need to get names of ejb fields
            List<String> returnList = new ArrayList<>();
            for (FieldDescriptor element : descriptor.getFieldDescriptors()) {
                returnList.add(element.getName());
            }
            return returnList;
        }
        String ejbName = nameMapper.getEjbNameForPersistenceKeyClass(className);
        switch (getPersistenceKeyClassType(className)) {
            // find the field names we need in the corresponding
            // ejb key class
            case AbstractNameMapper.USER_DEFINED_KEY_CLASS:
                testClass = nameMapper.getKeyClassForEjbName(ejbName);
                break;
            // find the field name we need in the abstract bean
            case AbstractNameMapper.PRIMARY_KEY_FIELD:
                return Arrays.asList(new String[] {getCMPDescriptor(ejbName).getPrimaryKeyFieldDesc().getName()});
            // find the field name we need in the persistence capable
            case AbstractNameMapper.UNKNOWN_KEY_CLASS:
                String pcClassName = nameMapper.getPersistenceClassForEjbName(ejbName);
                PersistenceFieldElement[] fields = getPersistenceClass(pcClassName).getFields();
                int i, count = ((fields != null) ? fields.length : 0);
                for (i = 0; i < count; i++) {
                    PersistenceFieldElement pfe = fields[i];
                    if (pfe.isKey()) {
                        return Arrays.asList(new String[] {pfe.getName()});
                    }
                }
                break;
        }

        return super.getFields(testClass);
    }

    /** Returns a list of names of all the field elements in the
     * class with the specified name.  This list includes the inherited
     * fields.  If the specified className represents a
     * persistence-capable class, the list of field names from the
     * corresponding ejb is returned (even if there is a Class object
     * available for the persistence-capable).  This method overrides
     * the one in Model in order to do special handling for a
     * persistence-capable key class name which corresponds to a bean
     * with a primary key field.  In that case, we don't want to
     * climb the inheritance hierarchy of the primary key field type.
     * @param className the fully qualified name of the class to be checked
     * @return the names of the field elements for the specified class
     */
    @Override
    public List getAllFields(String className) {
        // If the class name corresponds to a pk field (which means that
        // there is no user defined primary key class, we don't want to
        // climb the inheritance hierarchy, we only process this class.
        return ((AbstractNameMapper.PRIMARY_KEY_FIELD ==
            getPersistenceKeyClassType(className)) ? getFields(className) :
            super.getAllFields(className));
    }

    /** Returns a wrapped field element for the specified fieldName in the
     * class with the specified className.  If the specified className
     * represents a persistence-capable class, a field representing the
     * field in the abstract bean class for the corresponding ejb is always
     * returned (even if there is a Field object available for the
     * persistence-capable).  If there is an ejb name and an abstract bean
     * class with the same name, the abstract bean class which is associated
     * with the ejb will be used, not the abstract bean class which
     * corresponds to the supplied name (directly).
     * @param className the fully qualified name of the class which contains
     * the field to be checked
     * @param fieldName the name of the field to be checked
     * @return the wrapped field element for the specified fieldName
     */
    @Override
    public Object getField(final String className, String fieldName) {
        if (className == null) {
            return null;
        }
        String testClass = className;
        Object returnObject = null;
        boolean isPCClass = isPCClassName(className);
        boolean isPKClassName = false;
        String searchClassName = className;
        String searchFieldName = fieldName;

        // translate the class name & field names to corresponding
        // ejb name's abstract bean equivalents if necessary
        if (isPCClass) {
            searchFieldName = nameMapper.getEjbFieldForPersistenceField(className, fieldName);
            searchClassName = getEjbName(className);
        } else {
            // check if it's a pk class without a user defined key class
            String ejbName = nameMapper.getEjbNameForPersistenceKeyClass(className);
            switch (getPersistenceKeyClassType(className)) {
                // find the field we need in the corresponding
                // abstract bean (translated below from ejbName)
                case AbstractNameMapper.PRIMARY_KEY_FIELD:
                    testClass = ejbName;
                    searchClassName = ejbName;
                    isPKClassName = true;
                    break;
                // find the field we need by called updateFieldWrapper
                // below which handles the generated field for the
                // unknown key class - need to use the
                // persistence-capable class name and flag to call that
                // code, so we configure it here
                case AbstractNameMapper.UNKNOWN_KEY_CLASS:
                    testClass = nameMapper.
                        getPersistenceClassForEjbName(ejbName);
                    isPCClass = true;
                    isPKClassName = true;
                    break;
            }
        }

        if (nameMapper.isEjbName(searchClassName)) {
            searchClassName = nameMapper.getAbstractBeanClassForEjbName(searchClassName);
        }

        returnObject = super.getField(searchClassName, searchFieldName);

        if (returnObject == null) {
            // try getting it from the descriptor
            returnObject = getFieldWrapper(testClass, searchFieldName);
        } else if (returnObject instanceof Field) {
            // wrap it
            returnObject = new MemberWrapper((Field) returnObject);
        }

        if (isPCClass) {
            returnObject = updateFieldWrapper((MemberWrapper) returnObject, testClass, fieldName);
        }
        // when asking for these fields as part of the
        // persistence-capable's key class, we need to represent the
        // public modifier which will be generated in the inner class
        if (isPKClassName && returnObject instanceof MemberWrapper) {
            ((MemberWrapper) returnObject).modifiers = Modifier.PUBLIC;
        }

        return returnObject;
    }

    /** Returns the field type for the specified fieldName in the class
     * with the specified className.  This method is overrides the one in
     * Model in order to do special handling for non-collection relationship
     * fields.  If it's a generated relationship that case, the returned
     * MemberWrapper from getField contains a type of the abstract bean and
     * it's impossible to convert that into the persistence capable class name, so here
     * that case is detected, and if found, the ejb name is extracted and
     * used to find the corresponding persistence capable class.  For a
     * relationship which is of type of the local interface, we do the
     * conversion from local interface to persistence-capable class.  In the
     * case of a collection relationship (generated or not), the superclass'
     * implementation which provides the java type is sufficient.
     * @param className the fully qualified name of the class which contains
     * the field to be checked
     * @param fieldName the name of the field to be checked
     * @return the field type for the specified fieldName
     */
    @Override
    public String getFieldType(String className, String fieldName) {
        String returnType = super.getFieldType(className, fieldName);
        if (!isCollection(returnType) && isPCClassName(className)) {
            String ejbName = nameMapper.getEjbNameForPersistenceClass(className);
            String ejbField = nameMapper.getEjbFieldForPersistenceField(className, fieldName);
            if (nameMapper.isGeneratedEjbRelationship(ejbName, ejbField)) {
                String[] inverse = nameMapper.getEjbFieldForGeneratedField(ejbName, ejbField);
                returnType = nameMapper.getPersistenceClassForEjbName(inverse[0]);
            }
            if (nameMapper.isLocalInterface(returnType)) {
                returnType = nameMapper.getPersistenceClassForLocalInterface(className, fieldName, returnType);
            }
        }
        return returnType;
    }

    /** Returns the string representation of declaring class of
     * the specified member element.  Note, the member element is
     * either a class element as returned by getClass, a field element
     * as returned by getField, a constructor element as returned by
     * getConstructor, or a method element as returned by getMethod
     * executed on the same model instance.  This implementation expects
     * the member element to be a reflection instance or a wrapped member
     * instance.
     * @param memberElement the member element to be checked
     * @return the string representation of the declaring class of
     * the specified memberElement
     * @see #getClass
     * @see #getField
     * @see #getConstructor
     * @see #getMethod
     */
    @Override
    public String getDeclaringClass(Object memberElement) {
        if ((memberElement != null) && (memberElement instanceof MemberWrapper)) {
            Class<?> classElement = ((MemberWrapper) memberElement).getDeclaringClass();
            return ((classElement != null) ? classElement.getName() : null);
        }
        return super.getDeclaringClass(memberElement);
    }

    /** Returns the modifier mask for the specified member element.
     * Note, the member element is either a class element as returned by
     * getClass, a member wrapper element as returned by getField or
     * getMethod, a constructor element as returned by getConstructor
     * executed on the same model instance.
     * This implementation expects the member element being a reflection
     * instance or a wrapped member instance.
     * @param memberElement the member element to be checked
     * @return the modifier mask for the specified memberElement
     * @see java.lang.reflect.Modifier
     * @see #getClass
     * @see #getField
     * @see #getConstructor
     * @see #getMethod
     */
    @Override
    public int getModifiers(Object memberElement) {
        if ((memberElement != null) && (memberElement instanceof MemberWrapper)) {
            return ((MemberWrapper) memberElement).getModifiers();
        }
        return super.getModifiers(memberElement);
    }

    /** Returns the modifier mask for the specified className.  This method
     * overrides the one in Model to strip out the abstract modifier when
     * the persistence capable class is represented by the abstract bean.
     * It also adds the static modifier when the specified class represents
     * the persistence capable key class which will be generated.
     * @param className the fully qualified name of the class to be checked
     * @return the modifier mask for the specified class
     * @see java.lang.reflect.Modifier
     */
    @Override
    public int getModifiersForClass(String className) {
        int modifiers = super.getModifiersForClass(className);
        if (isPCClassName(className)) {
            modifiers &= ~Modifier.ABSTRACT;
        } else if (nameMapper.getKeyClassForPersistenceKeyClass(className) != null) {
            modifiers |= Modifier.STATIC;
        }

        return modifiers;
    }

    /** Determines if the specified className and fieldName pair represent a
     * field which has a type which is valid for key fields.  Valid key
     * field types include non-collection SCOs (wrappers, Date, Time, etc.)
     * and primitives for user defined key classes.  This method overrides
     * the one in Model in order to do special handling for a
     * persistence-capable key class name which corresponds to a bean
     * with a primary key field.  In that case, we want to restrict the
     * list of allowable types not to include primitives.
     * @param className the fully qualified name of the class which contains
     * the field to be checked
     * @param fieldName the name of the field to be checked
     * @return <code>true</code> if this field name represents a field
     * with a valid type for a key field; <code>false</code> otherwise.
     */
    @Override
    public boolean isValidKeyType(String className, String fieldName) {
        return (((AbstractNameMapper.PRIMARY_KEY_FIELD ==
            getPersistenceKeyClassType(className)) &&
            isPrimitive(className, fieldName)) ? false :
            super.isValidKeyType(className, fieldName));
    }

    /** Returns the Class type of the specified element.
     * If element denotes a field, it returns the type of the field.
     * If element denotes a method, it returns the return type of the method.
     * Note, element is either a field element as returned by getField, or a
     * method element as returned by getMethod executed on the same model
     * instance.
     * @param element the element to be checked
     * @return the Class type of the element
     * @see #getField
     * @see RuntimeModel#getMethod
     */
    @Override
    protected Class getTypeObject(Object element) {
        Class<?> type = super.getTypeObject(element);
        if ((element != null) && (element instanceof MemberWrapper)) {
            type = ((MemberWrapper) element).getType();
        }
        return type;
    }

    /**
     * This method returns the class loader used to find mapping information
     * for the specified className.  This implementation overrides the one in
     * RuntimeModel so that it always returns the ClassLoader provided at
     * construction time.
     * @param className the fully qualified name of the class to be checked
     * @param classLoader the class loader used to find mapping information
     * @return the class loader used to find mapping information for the
     * specified className
     */
    @Override
    protected ClassLoader findClassLoader(String className, ClassLoader classLoader) {
        return this.classLoader;
    }

    // return true if a conversion to a ejb class is needed for
    // java.lang.reflect metadata
    private boolean isPCClassName(String className) {
        return getEjbName(className) != null;
    }


    private String getEjbName(String className) {
        return nameMapper.getEjbNameForPersistenceClass(className);
    }


    private EjbCMPEntityDescriptor getCMPDescriptor(String className) {
        String descriptorName = (isPCClassName(className) ? getEjbName(className) : className);
        return nameMapper.getDescriptorForEjbName(descriptorName);
    }


    private int getPersistenceKeyClassType(String className) {
        int returnValue = -1;
        if (getCMPDescriptor(className) == null) {
            String ejbName = nameMapper.getEjbNameForPersistenceKeyClass(className);
            if (ejbName != null) {
                returnValue = nameMapper.getKeyClassTypeForEjbName(ejbName);
            }
        }
        return returnValue;
    }


    private MemberWrapper getFieldWrapper(String className, String fieldName) {
        EjbCMPEntityDescriptor descriptor = getCMPDescriptor(className);

        if (descriptor == null) {
            return null;
        }
        PersistenceDescriptor persistenceDescriptor = descriptor.getPersistenceDescriptor();
        if (persistenceDescriptor == null) {
            return null;
        }
        Class<?> fieldType = null;
        try {
            fieldType = persistenceDescriptor.getTypeFor(fieldName);
        } catch (RuntimeException e) {
            // fieldType will be null - there is no such field
        }
        return fieldType == null ? null
            : new MemberWrapper(fieldName, fieldType, Modifier.PRIVATE, (Class<?>) getClass(className));
    }


    private MemberWrapper updateFieldWrapper(MemberWrapper returnObject, String className, String fieldName) {
        if (returnObject == null) {
            // can't call isPersistent or isKey because that calls
            // hasField which calls getField and that would end up
            // in an endless loop
            PersistenceFieldElement field = getPersistenceFieldInternal(className, fieldName);
            if (field == null) {
                return null;
            }
            String ejbName = getEjbName(className);
            String ejbFieldName = nameMapper.getEjbFieldForPersistenceField(className, fieldName);

            // Check if this is the auto-added field for unknown pk
            // support. If so, return a private field of type Long.
            if (field.isKey() && (ejbName != null)
                && (nameMapper.getKeyClassTypeForEjbName(ejbName) == AbstractNameMapper.UNKNOWN_KEY_CLASS)) {
                returnObject = new MemberWrapper(ejbFieldName, Long.class, Modifier.PRIVATE,
                    (Class<?>) getClass(className));
            } else if ((field instanceof RelationshipElement)
                && nameMapper.isGeneratedEjbRelationship(ejbName, ejbFieldName)) {
                // Check if this is the auto-added field for 2 way managed rels
                // support. If so, return a private field of type according to
                // cardinality of the relationship.
                RelationshipElement rel = (RelationshipElement) field;
                Class<?> classType = null;

                // figure out the type
                if (rel.getUpperBound() > 1) {
                    classType = java.util.HashSet.class;
                } else {
                    String[] inverse = nameMapper.getEjbFieldForGeneratedField(ejbName, ejbFieldName);
                    classType = (Class<?>) getClass(inverse[0]);
                }

                if (classType != null) {
                    returnObject = new MemberWrapper(ejbFieldName, classType, Modifier.PRIVATE,
                        (Class<?>) getClass(className));
                }
            } else if (ejbFieldName.startsWith(AbstractNameMapper.GENERATED_VERSION_FIELD_PREFIX)
                && nameMapper.isGeneratedField(ejbName, ejbFieldName)) {
                // Check if this is the auto-added version field.
                // If so, return a private field of type long.
                returnObject = new MemberWrapper(ejbFieldName, Long.TYPE, Modifier.PRIVATE,
                    (Class<?>) getClass(className));
            } else {
                return null;
            }
        }

        // if the field in the corresponding ejb is a serializable,
        // non-primitive, non-wrapper type, convert it to byte[] here
        if (!isPersistentTypeAllowed(getType(returnObject), classLoader) && isSerializable(returnObject)) {
            returnObject.setType(byte[].class);
        }

        return returnObject;
    }

    private class MemberWrapper {

        private final String name;
        private Class<?> type;
        private int modifiers;
        private final Class<?> declaringClass;

        private MemberWrapper(Member member) {
            this(member.getName(),
                member instanceof Field ? ((Field) member).getType()
                    : member instanceof Method ? ((Method) member).getReturnType()
                        : null,
                member.getModifiers(), member.getDeclaringClass());
        }


        private MemberWrapper(String name, Class<?> type, int modifiers, Class<?> declaringClass) {
            this.name = name;
            this.type = type;
            this.modifiers = modifiers;
            this.declaringClass = declaringClass;
        }


        private Class<?> getType() {
            return type;
        }


        private void setType(Class<?> type) {
            this.type = type;
        }


        private String getName() {
            return name;
        }


        private int getModifiers() {
            return modifiers;
        }


        private Class<?> getDeclaringClass() {
            return declaringClass;
        }


        /**
         * Returns a string representation of this object.
         *
         * @return a string reprentation of the member wrapper object.
         */
        @Override
        public String toString() {
            return getName();
        }
    }
}
