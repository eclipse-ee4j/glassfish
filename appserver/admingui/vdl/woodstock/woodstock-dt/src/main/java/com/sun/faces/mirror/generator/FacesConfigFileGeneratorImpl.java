/*
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

import com.sun.faces.mirror.DeclaredComponentInfo;
import com.sun.faces.mirror.DeclaredRendererInfo;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * An implementation of FacesConfigFileGenerator that creates the config file by
 * merging component and renderer info with a template file.
 */
final class FacesConfigFileGeneratorImpl extends FacesConfigFileGenerator {

    /**
     * Template resource path.
     */
    private static final String TEMPLATE =
            "com/sun/faces/mirror/generator/FacesConfig.template";

    /**
     * Template engine.
     */
    private final VelocityEngine velocityEngine;

    /**
     * Create a new instance.
     * @param velocity template engine
     */
    FacesConfigFileGeneratorImpl(final VelocityEngine velocity) {
        this.velocityEngine = velocity;
    }

    @Override
    public void generate() throws GeneratorException {
        Set<DeclaredComponentInfo> componentInfoSet =
                this.getDeclaredComponentInfos();
        Set<DeclaredRendererInfo> rendererInfoSet =
                this.getDeclaredRendererInfos();
        Set<String> propertyResolverNameSet =
                this.getDeclaredPropertyResolverNames();
        Set<String> variableResolverNameSet =
                this.getDeclaredVariableResolverNameSet();
        Set<String> javaeeResolverNameSet =
                this.getDeclaredJavaEEResolverNames();
        PrintWriter printWriter = this.getPrintWriter();
        if (componentInfoSet.isEmpty() && rendererInfoSet.isEmpty()) {
            return;
        }
        try {
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("date", DateFormat
                    .getDateInstance(DateFormat.MEDIUM).format(new Date()));
            velocityContext.put("componentInfoSet", componentInfoSet);
            velocityContext.put("rendererInfoSet", rendererInfoSet);
            velocityContext.put("propertyResolverNameSet",
                    propertyResolverNameSet);
            velocityContext.put("variableResolverNameSet",
                    variableResolverNameSet);
            velocityContext.put("javaeeResolverNameSet",
                    javaeeResolverNameSet);
            Template template = velocityEngine.getTemplate(TEMPLATE);
            template.merge(velocityContext, printWriter);
            printWriter.flush();
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneratorException(e);
        }
    }
}
