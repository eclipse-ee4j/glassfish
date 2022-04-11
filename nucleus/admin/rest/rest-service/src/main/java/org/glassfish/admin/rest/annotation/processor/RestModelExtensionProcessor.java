/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.annotation.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

import org.glassfish.admin.rest.composite.RestModelExtension;

import static javax.tools.StandardLocation.CLASS_OUTPUT;


@SupportedAnnotationTypes("org.glassfish.admin.rest.composite.RestModelExtension")
public class RestModelExtensionProcessor extends AbstractProcessor {
    @Override
    public boolean process(final Set<? extends TypeElement> elements, final RoundEnvironment env) {
        try {
            final Map<String, List<String>> classes = new HashMap<>();
            for (final TypeElement typeElement : elements) {
                for (final Element element : env.getElementsAnnotatedWith(typeElement)) {
                    final RestModelExtension annotation = element.getAnnotation(RestModelExtension.class);
                    final String parent = annotation.parent();
                    List<String> list = classes.get(parent);
                    if (list == null) {
                        list = new ArrayList<>();
                        classes.put(parent, list);
                    }
                    list.add(element.toString());
                }
            }
            if (classes.isEmpty()) {
                return true;
            }
            final Filer filer = processingEnv.getFiler();
            final FileObject file = filer.createResource(CLASS_OUTPUT, "", "META-INF/restmodelextensions");
            try (BufferedWriter bw = new BufferedWriter(file.openWriter())) {
                // parent model:model extension
                for (final Map.Entry<String, List<String>> entry : classes.entrySet()) {
                    final String key = entry.getKey();
                    for (final String ext : entry.getValue()) {
                        bw.write(key);
                        bw.write(":");
                        bw.write(ext);
                        bw.write('\n');
                    }
                }
            }
        } catch (final IOException ex) {
            processingEnv.getMessager().printMessage(Kind.ERROR, ex.getLocalizedMessage());
        }

        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
