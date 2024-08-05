/*
 * Copyright (c) 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool;

import java.lang.reflect.Field;

public class InjectionUtil {

    /**
     * Use injection to fill in private fields in (final) classes. E.g. fields annotated with jakarta.inject.Inject.
     *
     * @param clazz the class to be altered
     * @param clazzInstance the instance of the class that needs to be altered
     * @param fieldName the name of the field in the class
     * @param fieldValue the new value for the field
     * @throws Exception if the injection of the value failed
     */
    public static void injectPrivateField(Class<?> clazz, Object clazzInstance, String fieldName, Object fieldValue) throws Exception {
        Field declaredField = clazz.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        declaredField.set(clazzInstance, fieldValue);
    }
}
