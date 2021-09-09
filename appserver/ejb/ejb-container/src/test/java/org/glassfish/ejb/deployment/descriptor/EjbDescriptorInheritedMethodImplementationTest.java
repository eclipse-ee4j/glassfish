/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.WritableJndiNameEnvironment;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.object.IsCompatibleType.typeCompatibleWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EjbDescriptorInheritedMethodImplementationTest {

    /**
     * This method tests if methods inherited from WritableJndiNameEnvironment are
     * directly implemented in EjbDescriptor or not and if implemented, methods
     * are marked final or not.
     */
    @Test
    public void testEjbDescriptorInheritedMethodImplementation() {
        assertThat(EjbDescriptor.class, typeCompatibleWith(com.sun.enterprise.deployment.EjbDescriptor.class));
        assertThat(com.sun.enterprise.deployment.EjbDescriptor.class,
            typeCompatibleWith(WritableJndiNameEnvironment.class));

        List<Method> methodsDefinedByWritableJndiNameEnvInterface = Arrays
            .asList(WritableJndiNameEnvironment.class.getMethods());
        Map<Error, List<Method>> unimplementedMethods = new HashMap<>();
        for (Method writableJndiNameEnvMethod : methodsDefinedByWritableJndiNameEnvInterface) {
            try {
                Method ejbDescriptorMethod = EjbDescriptor.class.getDeclaredMethod(
                        writableJndiNameEnvMethod.getName(),
                        writableJndiNameEnvMethod.getParameterTypes());
                if (!Modifier.isFinal(ejbDescriptorMethod.getModifiers())) {
                    updateUnimplementedMethodsMap(Error.NON_FINAL_METHOD, ejbDescriptorMethod, unimplementedMethods);
                }
            } catch (NoSuchMethodException e) {
                updateUnimplementedMethodsMap(Error.UNIMPLEMENTED_METHOD, writableJndiNameEnvMethod, unimplementedMethods);
            }
        }

        assertEquals("", getErrorMessage(unimplementedMethods));
    }


    private String getErrorMessage(Map<Error, List<Method>> unimplementedMethods) {
        StringBuilder sb = new StringBuilder();
        for (Error error : unimplementedMethods.keySet()) {
            sb.append("\n" + error.getErrorMsg() + "\n");
            sb.append("\t");
            for (Method method : unimplementedMethods.get(error)) {
                sb.append(method);
                sb.append("\n\t");
            }
        }
        return sb.toString();
    }


    private void updateUnimplementedMethodsMap(Error error, Method method,
        Map<Error, List<Method>> unimplementedMethods) {
        if (unimplementedMethods.containsKey(error)) {
            List<Method> methods = unimplementedMethods.get(error);
            methods.add(method);
        } else {
            List<Method> methods = new ArrayList<>();
            methods.add(method);
            unimplementedMethods.put(error, methods);
        }
    }

    private enum Error {

        NON_FINAL_METHOD("Following com.sun.enterprise.deployment.WritableJndiNameEnvironment" +
                " methods are not marked final when implemented in " +
                "org.glassfish.ejb.deployment.descriptor.EjbDescriptor." +
                "None of the sub-classes of EjbDescriptor are expected to " +
                "override these methods as it might lead to change in intended behavior " +
                "and hence these methods must be marked final in EjbDescriptor."),

        UNIMPLEMENTED_METHOD("Following com.sun.enterprise.deployment.WritableJndiNameEnvironment " +
                "methods are not implemented directly in " +
                "org.glassfish.ejb.deployment.descriptor.EjbDescriptor. " +
                "Implementation of these methods is mandatory within " +
                "EjbDescriptor to ensure expected behavior when any of these" +
                " methods are invoked in EjbDescriptor's context.");

        String error;

        Error(String error) {
            this.error = error;
        }

        public String getErrorMsg() {
            return error;
        }
    }
}
