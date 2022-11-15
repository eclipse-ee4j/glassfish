/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.runtime.application.wls.ApplicationParam;
import com.sun.enterprise.deployment.types.EnvironmentPropertyValueTypes;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.web.ContextParameter;
import com.sun.enterprise.deployment.web.InitializationParameter;
import com.sun.enterprise.deployment.web.WebDescriptor;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.internal.api.RelativePathResolver;

/**
 * The EnvironmentProperty class hold the data about a single environment entry for J2EE components.
 *
 * @author Danny Coward
 */
public class EnvironmentProperty extends Descriptor implements InitializationParameter, ContextParameter,
    ApplicationParam, WebDescriptor, InjectionCapable {

    private static final long serialVersionUID = 1L;
    static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EnvironmentProperty.class);

    private String value;
    private String type;
    private Object valueObject;
    private boolean setValueCalled;

    // list of injection targes
    private Set<InjectionTarget> injectionTargets;

    protected SimpleJndiName mappedName;
    protected SimpleJndiName lookupName;

    /**
     * Construct an environment property if type String and empty string value and no description.
     */
    public EnvironmentProperty() {
    }


    /**
     * Construct an environment property of given name value and description.
     */

    public EnvironmentProperty(String name, String value, String description) {
        this(name, value, description, null);
    }


    /**
     * Construct an environment property of given name value and description and type.
     * Throws an IllegalArgumentException if bounds checking is true and the value cannot be
     * reconciled with the given type.
     */
    public EnvironmentProperty(String name, String value, String description, String type) {
        super(name, description);
        this.value = value;
        checkType(type);
        this.type = convertPrimitiveTypes(type);
    }


    // in this class it is not deprecated.
    @Override
    public String getName() {
        return super.getName();
    }


    /**
     * Returns the String value of this environment property
     */
    @Override
    public String getValue() {
        if (this.value == null) {
            this.value = "";
        }
        return value;
    }


    /**
     * Resolves value written as ${} string using system properties.
     *
     * @return a resolved value of this environment property.
     */
    public String getResolvedValue() {
        return RelativePathResolver.resolvePath(getValue());
    }


    /**
     * Resolves value written as ${} string using system properties, then parses it using
     * {@link #getValueType()}.
     *
     * @return the typed value object of this environment property.
     * @throws IllegalArgumentException if bounds checking is true and the value cannot be
     *             reconciled with the given type.
     */
    public final <T> T getResolvedValueObject(final Class<T> expectedType) {
        if (this.valueObject == null) {
            this.valueObject = "";
        }
        return getObjectFromString(this.getResolvedValue(), expectedType);
    }


    /**
     * checks the given class type. throws an IllegalArgumentException if bounds checking
     * if the type is not allowed.
     */
    private void checkType(String type) {
        if (type == null) {
            return;
        }
        Class<?> typeClass = null;
        // is it loadable ?
        try {
            typeClass = Class.forName(type, true, Thread.currentThread().getContextClassLoader());
        } catch (Throwable t) {
            if (Descriptor.isBoundsChecking()) {
                throw new IllegalArgumentException(
                    localStrings.getLocalString("enterprise.deployment.exceptiontypenotallowedpropertytype",
                        "{0} is not an allowed property value type", new Object[] {type}));
            }
            return;
        }
        boolean allowedType = false;
        for (Class<?> clazz : EnvironmentPropertyValueTypes.ALLOWED_TYPES) {
            if (clazz.equals(typeClass)) {
                allowedType = true;
                break;
            }
        }
        if (typeClass != null && typeClass.isEnum()) {
            allowedType = true;
        }

        if (Descriptor.isBoundsChecking() && !allowedType) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("enterprise.deployment.exceptiontypenotallowedprprtytype",
                    "{0} is not an allowed property value type", new Object[] {type}));
        }
    }


    /**
     * @param expectedType if null, uses {@link #getValueType()}
     * @return the typed value object of this environment property.
     * Throws an IllegalArgumentException if bounds checking is true and the value cannot be
     * reconciled with the given type.
     */
    public <T> T getValueObject(Class<T> expectedType) {
        return getObjectFromString(this.getValue(), expectedType);
    }


    /**
     * @return value type of this environment property.
     */
    private Class<?> getValueType() {
        if (this.type == null) {
            return String.class;
        }
        try {
            return Class.forName(this.type, true, Thread.currentThread().getContextClassLoader());
        } catch (Throwable t) {
            throw new IllegalStateException("The type is not reachable for the current classloader: " + type, t);
        }
    }


    /**
     * Sets the type of this environment property.
     *
     * @throws IllegalArgumentException if this is not an allowed type and bounds checking.
     */
    public void setType(String type) {
        checkType(type);
        this.type = convertPrimitiveTypes(type);
    }

    private String convertPrimitiveTypes(String type) {
        if (type == null) {
            return type;
        }
        if (type.equals("int")) {
            return "java.lang.Integer";
        } else if (type.equals("boolean")) {
            return "java.lang.Boolean";
        } else if (type.equals("double")) {
            return "java.lang.Double";
        } else if (type.equals("float")) {
            return "java.lang.Float";
        } else if (type.equals("long")) {
            return "java.lang.Long";
        } else if (type.equals("short")) {
            return "java.lang.Short";
        } else if (type.equals("byte")) {
            return "java.lang.Byte";
        } else if (type.equals("char")) {
            return "java.lang.Character";
        }
        return type;
    }


    /**
     * @return value type of this environment property as a classname.
     */
    public String getType() {
        if (type == null) {
            return String.class.getName();
        }
        return type;
    }


    public void setMappedName(SimpleJndiName mName) {
        mappedName = mName;
    }


    public SimpleJndiName getMappedName() {
        return mappedName == null ? new SimpleJndiName("") : mappedName;
    }


    public void setLookupName(SimpleJndiName lName) {
        lookupName = lName;
    }


    public SimpleJndiName getLookupName() {
        // FIXME: kill empty strings
        return lookupName == null ? new SimpleJndiName("") : lookupName;
    }


    public boolean hasLookupName() {
        return lookupName != null && !lookupName.isEmpty();
    }


    /**
     * Sets the value of the environment property to the given string.
     */
    @Override
    public void setValue(String value) {
        this.value = value;
        // String may be empty, but at the moment of this call the type is probably not set yet.
        // That's why we monitor the setter call. Null means "not set", that's alright.
        this.setValueCalled = value != null;
    }


    /**
     * @return true if the {@link #setValue(String)} was called with non-null value.
     */
    public boolean isSetValueCalled() {
        return setValueCalled;
    }


    /**
     * @return true if value, lookupName or mappedName was set.
     */
    public boolean hasContent() {
        return setValueCalled || !getLookupName().isEmpty() || !getMappedName().isEmpty();
    }


    /**
     * Returns true if the argument is an environment property of the same name, false else.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof EnvironmentProperty && this.getName().equals(((EnvironmentProperty) other).getName())) {
            return true;
        }
        return false;
    }


    /**
     * The hashCode of an environment property is the same as that of the name String.
     */
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }


    /**
     * Returns a String representation of this environment property.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append(getClass().getSimpleName()).append("[name=").append(getName());
        toStringBuffer.append(", type=").append(getType()).append(", value=").append(getValue());
        toStringBuffer.append(", lookupName=").append(getLookupName());
        toStringBuffer.append(", mappedName=").append(getMappedName());
        printInjectableResourceInfo(toStringBuffer);
        toStringBuffer.append(", description=").append(getDescription()).append(']');
    }

    private <T> T getObjectFromString(String string, Class<T> type) {
        if (type == null) {
            type = (Class<T>) getValueType();
        }
        T obj = getParsedPrimitiveValue(string, type);
        if (obj != null) {
            return obj;
        }
        if (string == null || (string.isEmpty() && !String.class.equals(type))) {
            return null;
        }
        try {
            if (String.class.equals(type)) {
                return (T) string;
            } else if (Boolean.class.equals(type)) {
                return (T) Boolean.valueOf(string);
            } else if (Integer.class.equals(type)) {
                return (T) Integer.valueOf(string);
            } else if (Double.class.equals(type)) {
                return (T) Double.valueOf(string);
            } else if (Float.class.equals(type)) {
                return (T) Float.valueOf(string);
            } else if (Short.class.equals(type)) {
                return (T) Short.valueOf(string);
            } else if (Byte.class.equals(type)) {
                return (T) Byte.valueOf(string);
            } else if (Long.class.equals(type)) {
                return (T) Long.valueOf(string);
            } else if (Character.class.equals(type)) {
                if (string.length() == 1) {
                    return (T) Character.valueOf(string.charAt(0));
                }
                throw new IllegalArgumentException("String cannot be converted to Character: " + string);
            } else if (SimpleJndiName.class.equals(type)) {
                return (T) SimpleJndiName.of(string);
            } else if (Class.class.equals(type)) {
                return (T) Class.forName(string, true, Thread.currentThread().getContextClassLoader());
            } else if (type != null && type.isEnum()) {
                return (T) Enum.valueOf((Class) type, string);
            }
        } catch (Throwable t) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("enterprise.deployment.exceptioncouldnotcreateinstancetype",
                    "Could not create instance of {0} from {1}\n reason: {2}" + t, new Object[] {type, string, t}));
        }
        throw new IllegalArgumentException(
            localStrings.getLocalString("enterprise.deployment.exceptionillegaltypeenvproperty",
                "Illegal type for environment properties: {0}", new Object[] {type}));
    }


    /**
     * @return parsed primitive or String
     */
    private <T> T getParsedPrimitiveValue(String string, Class<T> type) throws IllegalArgumentException {
        if (type == null) {
            throw new NullPointerException("type must not be null! String value was " + string);
        }
        if (type.equals(int.class)) {
            return (T) Integer.valueOf(string);
        } else if (type.equals(long.class)) {
            return (T) Long.valueOf(string);
        } else if (type.equals(short.class)) {
            return (T) Short.valueOf(string);
        } else if (type.equals(boolean.class)) {
            return (T) Boolean.valueOf(string);
        } else if (type.equals(float.class)) {
            return (T) Float.valueOf(string);
        } else if (type.equals(double.class)) {
            return (T) Double.valueOf(string);
        } else if (type.equals(byte.class)) {
            return (T) Byte.valueOf(string);
        } else if (type.equals(char.class)) {
            if (string.length() == 1) {
                return (T) Character.valueOf(string.charAt(0));
            }
            throw new IllegalArgumentException(type + ": " + string);
        }
        return null;
    }


    public boolean isConflict(EnvironmentProperty other) {
        return getName().equals(other.getName())
            && (!(DOLUtils.equals(getType(), other.getType()) && getValue().equals(other.getValue()))
                || isConflictResourceGroup(other));
    }

    protected boolean isConflictResourceGroup(EnvironmentProperty other) {
        return !getLookupName().equals(other.getLookupName()) || !getMappedName().equals(other.getMappedName());
    }

    @Override
    public void addInjectionTarget(InjectionTarget target) {
        if (injectionTargets==null) {
            injectionTargets = new HashSet<>();
        }
        for (InjectionTarget injTarget : injectionTargets) {
            if (injTarget.equals(target)) {
                return;
            }
        }
        injectionTargets.add(target);
    }

    @Override
    public Set<InjectionTarget> getInjectionTargets() {
        return injectionTargets == null ? new HashSet<>() : injectionTargets;
    }

    @Override
    public boolean isInjectable() {
        return injectionTargets != null && !injectionTargets.isEmpty();
    }

    public boolean hasInjectionTargetFromXml() {
        if (injectionTargets != null) {
            for (InjectionTarget injTarget: injectionTargets) {
                if (MetadataSource.XML == injTarget.getMetadataSource()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public final SimpleJndiName getComponentEnvName() {
        return new SimpleJndiName(getName());
    }

    @Override
    public String getInjectResourceType() {
        return type;
    }

    @Override
    public void setInjectResourceType(String resourceType) {
        type = convertPrimitiveTypes(resourceType);
    }


    private StringBuffer printInjectableResourceInfo(StringBuffer toStringBuffer) {
        toStringBuffer.append(", injectableTargets={");
        if (isInjectable()) {
            boolean first = true;
            for (InjectionTarget target : getInjectionTargets()) {
                if (first) {
                    first = false;
                } else {
                    toStringBuffer.append(", ");
                }
                toStringBuffer.append(target.getClassName()).append('.');
                if (target.isFieldInjectable()) {
                    toStringBuffer.append(target.getFieldName());
                } else {
                    toStringBuffer.append(target.getMethodName()).append("(...)");
                }
            }
        }
        return toStringBuffer.append('}');
    }
}
