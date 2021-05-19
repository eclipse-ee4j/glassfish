/*
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

import com.sun.enterprise.deployment.util.TypeUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class holds information about an injection target like the class name
 * of the injected target, and field/method information used for injection.
 *
 * @author Jerome Dochez
 */
public class InjectionTarget implements Serializable {

    private String className=null;
    private String targetName=null;
    private String fieldName=null;
    private String methodName=null;
    private MetadataSource metadataSource = MetadataSource.XML;

    // runtime info, not persisted
    private transient Field field=null;
    private transient Method method=null;

    public boolean isFieldInjectable() {
        return fieldName!=null;
    }

    public boolean isMethodInjectable() {
        return methodName!=null;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

   /**
     * This is the form used by the .xml injection-group elements to
     * represent the target of injection.   It either represents the
     * javabeans property name of the injection method or the name
     * of the injected field.  This value is set on the descriptor
     * during .xml processing and converted into the appropriate
     * field/method name during validation.
     */

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
        this.targetName = fieldName;
    }

    /*
     * runtime cached information for faster lookup
     */
    public Field getField() {
        return field;
    }
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * Inject method name is the actual java method name of the setter method,
     * not the bean property name.  E.g., for @Resource void setFoo(Bar b)
     * it would be "setFoo", not the property name "foo".
     */
    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
        // Method name follows java beans setter syntax
        this.targetName = TypeUtil.setterMethodToPropertyName(methodName);
;
    }

    // runtime cached information
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }

    public MetadataSource getMetadataSource() {
        return metadataSource;
    }

    public void setMetadataSource(MetadataSource metadataSource) {
        this.metadataSource = metadataSource;
    }

    public boolean equals(Object o) {
        if (!(o instanceof InjectionTarget)) {
            return false;
        } else {
            // Note that from xml, one only have className and targetName.
            // From annotation processing, one may define methodName,
            // fieldName and a different metadataSource.
            // Since an applicient container also does annotation processing
            // itself, one would like to avoid duplication from xml and
            // annotation processing.
            // So, one will only check className and targetName here.

            InjectionTarget injTarget = (InjectionTarget)o;
            return equals(className, injTarget.className) &&
                   equals(targetName, injTarget.targetName);
        }
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + (className == null ? 0 : className.hashCode());
        result = 37*result + (targetName == null ? 0: targetName.hashCode());
        result = 37*result + (fieldName == null ? 0: fieldName.hashCode());
        result = 37*result + (methodName == null ? 0: methodName.hashCode());
        return result;
    }


    private boolean equals(String s1, String s2) {
        return (s1 != null && s1.equals(s2) || s1 == null && s2 == null);
    }
}
