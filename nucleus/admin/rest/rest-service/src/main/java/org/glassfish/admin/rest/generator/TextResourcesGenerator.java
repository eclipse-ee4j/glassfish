/*
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author Mitesh Meswani
 */
public class TextResourcesGenerator extends ResourcesGeneratorBase {

    /** The absolute path to dir where resources are generated */
    private final File generationDir;

    public TextResourcesGenerator(String outputDir, ServiceLocator habitat) {
        super(habitat);
        generationDir = new File(outputDir);
        if (!generationDir.mkdirs()) {
            throw new RuntimeException("Unable to create output directory: " + outputDir);
        }
    }

    @Override
    public ClassWriter getClassWriter(String className, String baseClassName, String resourcePath) {
        ClassWriter writer = null;
        try {
            writer = new TextClassWriter(habitat, generationDir, className, baseClassName, resourcePath);
        } catch (IOException e) {
            // Log the root cause. The generation is going to fail with NPE.
            RestLogging.restLogger.log(Level.SEVERE, e.getMessage());
            throw new GeneratorException(e);
        }
        return writer;
    }

    @Override
    public String endGeneration() {
        Properties properties = new Properties(1);
        properties.setProperty("generation_date", new Date().toString());
        File file = new File(generationDir, "codegeneration.properties");
        try (OutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate " + file, e);
        }
        return "Code Generation done at : " + generationDir;
    }

    @Override
    protected boolean alreadyGenerated(String className) {
        return false; // Always generate. It just overwrites the file.
    }
}
