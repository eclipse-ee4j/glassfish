/*
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
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.glassfish.admin.rest.composite.RestModelExtension;

/**
 * Hello world!
 *
 */
@SupportedAnnotationTypes("org.glassfish.admin.rest.composite.RestModelExtension")
public class RestModelExtensionProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
        Messager messager = processingEnv.getMessager();
        BufferedWriter bw = null;
        try {
            Map<String, List<String>> classes = new HashMap<String, List<String>>();

            for (TypeElement te : elements) {
                for (Element e : env.getElementsAnnotatedWith(te)) {
                    final RestModelExtension annotation = e.getAnnotation(RestModelExtension.class);
                    final String parent = annotation.parent();
                    List<String> list = classes.get(parent);
                    if (list == null) {
                        list = new ArrayList<String>();
                        classes.put(parent, list);
                    }
                    list.add(e.toString());
                }
            }

            if (!classes.isEmpty()) {
                final Filer filer = processingEnv.getFiler();
                FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/restmodelextensions");
                bw = new BufferedWriter(fo.openWriter());
                // parent model:model extension
                for (Map.Entry<String, List<String>> entry : classes.entrySet()) {
                    final String key = entry.getKey();
                    for (String ext : entry.getValue()) {
                        bw.write(key + ":" + ext + "\n");
                    }
                }
                bw.close();
            }
        } catch (IOException ex) {
            messager.printMessage(Kind.ERROR, ex.getLocalizedMessage());
            if (bw != null) {
                try {
                    bw.close();
                } catch (Exception e) {

                }
            }
        }

        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
