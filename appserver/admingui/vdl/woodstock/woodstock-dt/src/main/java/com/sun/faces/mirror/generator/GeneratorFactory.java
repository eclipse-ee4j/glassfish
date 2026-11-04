/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.mirror.generator;

import org.apache.velocity.app.VelocityEngine;

/**
 * Generator factory.
 */
public final class GeneratorFactory {

    /**
     * Template engine.
     */
    private final VelocityEngine velocityEngine;

    /**
     * Create a new instance.
     */
    @SuppressWarnings("checkstyle:linelength")
    public GeneratorFactory() {
        this.velocityEngine = new VelocityEngine();
        this.velocityEngine.addProperty("resource.loader", "classpath");
        this.velocityEngine.addProperty("classpath.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        try {
            this.velocityEngine.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the faces config file generator.
     * @return FacesConfigFileGenerator
     */
    public FacesConfigFileGenerator getFacesConfigFileGenerator() {
        return new FacesConfigFileGeneratorImpl(this.velocityEngine);
    }

    /**
     * Get the bean info source generator.
     * @return BeanInfoSourceGenerator
     */
    public BeanInfoSourceGenerator getBeanInfoSourceGenerator() {
        return new BeanInfoSourceGeneratorImpl(this.velocityEngine);
    }

    /**
     * Get the tag lib file generator.
     * @return TagLibFileGenerator
     */
    public TagLibFileGenerator getTagLibFileGenerator() {
        return new TagLibFileGeneratorImpl(this.velocityEngine);
    }

    /**
     * Get the debug generator.
     * @return DebugGenerator
     */
    public DebugGenerator getDebugGenerator() {
        return new DebugGenerator(this.velocityEngine);
    }
}
