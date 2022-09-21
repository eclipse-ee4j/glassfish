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

package org.glassfish.apf.impl;

import java.io.File;
import java.io.IOException;

import org.glassfish.apf.ComponentInfo;
import org.glassfish.apf.Scanner;

/**
 * Super class for all JavaEE scanners
 *
 * @author Jerome Dochez
 */
public abstract class JavaEEScanner<T> implements Scanner {

    /**
     * Scan the archive file and gather a list of classes
     * that should be processed for anntoations
     *
     * @param archiveFile the archive file for scanning
     * @param classLoader the classloader used to scan the annotation
     * @throws IOException if the file cannot be read.
     */
    protected abstract void process(File archiveFile, T descriptor, ClassLoader classLoader) throws IOException;


    @Override
    public ComponentInfo getComponentInfo(Class<?> componentImpl) {
        return new ComponentDefinition(componentImpl);
    }
}
