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

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * An implementation of TagLibFileGenerator that creates the tag lib file by
 * merging component info with a template file.
 */
final class TagLibFileGeneratorImpl extends TagLibFileGenerator {

    /**
     * Template resource path.
     */
    private static final String TEMPLATE =
            "com/sun/faces/mirror/generator/TagLib.template";

    /**
     * Template engine.
     */
    private final VelocityEngine velocityEngine;

    /**
     * Create a new instance.
     * @param velocity template engine
     */
    TagLibFileGeneratorImpl(final VelocityEngine velocity) {
        this.velocityEngine = velocity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void generate() throws GeneratorException {
        try {
            Set<DeclaredComponentInfo> componentInfoSet =
                    new TreeSet(new Comparator() {
                @Override
                public int compare(final Object obj1, final Object obj2) {
                    String tag1 = ((DeclaredComponentInfo) obj1).getTagName();
                    String tag2 = ((DeclaredComponentInfo) obj2).getTagName();
                    if (tag1 == null) {
                        if (tag2 == null) {
                            return 0;
                        }
                        return -1;
                    } else {
                        return tag1.compareTo(tag2);
                    }
                }
            });
            componentInfoSet.addAll(this.getDeclaredComponentInfoSet());
            String namespace = this.getNamespace();
            String namespacePrefix = this.getNamespacePrefix();
            PrintWriter printWriter = this.getPrintWriter();
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("date", DateFormat
                    .getDateInstance(DateFormat.MEDIUM).format(new Date()));
            velocityContext.put("componentInfoSet", componentInfoSet);
            if (namespace == null) {
                velocityContext.put("namespace", "");
            } else {
                velocityContext.put("namespace", namespace);
            }
            if (namespacePrefix == null) {
                velocityContext.put("namespacePrefix", "");
            } else {
                velocityContext.put("namespacePrefix", namespacePrefix);
            }
            Template template = this.velocityEngine.getTemplate(TEMPLATE);
            template.merge(velocityContext, printWriter);
            printWriter.flush();
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneratorException(e);
        }
    }
}
