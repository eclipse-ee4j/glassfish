/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.glassfish.main.jnosql.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The beans that bean discovery finds in a library that is part of an application.
 */
public final class LibraryBeans {

    private LibraryBeans() {
        // utility class
    }

    /**
     * @param library a class from the library
     * @return the {@link ApplicationScoped} classes in the jar of the given class
     */
    public static Set<Class<?>> applicationScopedClassesIn(Class<?> library) throws Exception {
        Set<Class<?>> classes = new HashSet<>();
        File jar = new File(library.getProtectionDomain().getCodeSource().getLocation().toURI());
        try (JarFile jarFile = new JarFile(jar)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.endsWith(".class") && !name.contains("-")) {
                    Class<?> type = Class.forName(name.replace('/', '.').substring(0, name.length() - 6), false,
                            library.getClassLoader());
                    if (type.isAnnotationPresent(ApplicationScoped.class)) {
                        classes.add(type);
                    }
                }
            }
        }
        return classes;
    }
}
