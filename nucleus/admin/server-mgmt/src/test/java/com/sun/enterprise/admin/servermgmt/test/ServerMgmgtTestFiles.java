/*
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.test;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Purpose of this class is to simplify accessing files on classpath around test classes.
 *
 * @author David Matejcek
 */
public final class ServerMgmgtTestFiles {

    private final Class<?> baseClass;

    /**
     * @param baseClass - class used as an orientation point. Will be used to get classloader and to
     *            locate other files around the class.
     */
    public ServerMgmgtTestFiles(final Class<?> baseClass) {
        this.baseClass = baseClass;
    }


    public Class<?> getBaseClass() {
        return baseClass;
    }


    public Package getBasePackage() {
        return this.getBaseClass().getPackage();
    }


    public Path getBasePackageAbsolutePath() {
        return getPackageAbsolutePath(getBasePackage());
    }


    public File getBasePackageDirectory() {
        return getBasePackageAbsolutePath().toFile();
    }


    public InputStream openInputStream(final String filename) {
        return this.getBaseClass().getClassLoader()
            .getResourceAsStream(getPackageRelativePath(getBasePackage()).resolve(filename).toString());
    }


    public static File getClassFile(final Class<?> clazz) {
        return getPackageAbsolutePath(clazz.getPackage()).resolve(clazz.getSimpleName() + ".class").toFile();
    }


    private static Path getPackageAbsolutePath(final Package pkg) {
        try {
            String resourcePath = getPackageRelativePath(pkg).toString();
            return Paths.get(ServerMgmgtTestFiles.class.getClassLoader().getResource(resourcePath).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not convert the package " + pkg + " to URI", e);
        }
    }


    private static Path getPackageRelativePath(final Package pkg) {
        return Paths.get(toPath(pkg.getName()));
    }


    private static String toPath(final String classOrPackageName) {
        return classOrPackageName.replace('.', '/');
    }
}
