/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.generator;

import java.lang.System.Logger;

import org.glassfish.hk2.api.ServiceLocator;

import static java.lang.System.Logger.Level.TRACE;

/**
 * @author Ludovic Champenois
 */
public class ASMResourcesGenerator extends ResourcesGeneratorBase {
    private static final Logger LOG = System.getLogger(ASMResourcesGenerator.class.getName());

    private final static String GENERATED_PATH = "org/glassfish/admin/rest/resources/generatedASM/";
    private final static String GENERATED_PACKAGE = GENERATED_PATH.replace("/", ".");

    public ASMResourcesGenerator(ServiceLocator habitat) {
        super(habitat);
    }

    @Override
    public ClassWriter getClassWriter(String className, String baseClassName, String resourcePath) {
        String fullClassName = GENERATED_PACKAGE + className;
        try {
            Class.forName(fullClassName);
            LOG.log(TRACE, () -> "Class already exists, no need to regenerate it: " + fullClassName);
            return null;
        } catch (ClassNotFoundException ex) {
            return new ASMClassWriter(habitat, GENERATED_PATH, className, baseClassName, resourcePath);
        }
    }

    @Override
    public String endGeneration() {
        return "Code Generation done at ";
    }
}
