/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.apt;

import org.glassfish.hk2.api.Metadata;
import org.jvnet.hk2.component.MultiMap;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Discoveres all {@link InhabitantMetadata} and puts them into the bag.
 *
 * @author Kohsuke Kawaguchi
 */
public class InhabitantMetadataProcessor extends TypeHierarchyVisitor<MultiMap<String,String>> {

    private final Map<DeclaredType, Model> models = new HashMap<DeclaredType, Model>();

    /**
     * For a particular {@link DeclaredType}, remember what properties are to be added as metadata.
     */
    private static final class Model {
        private final DeclaredType type;
        private final Map<ExecutableElement, String> metadataProperties = new HashMap<ExecutableElement, String>();

        public Model(DeclaredType type) {
            this.type = type;
            for (ExecutableElement e : ElementFilter.methodsIn(type.asElement().getEnclosedElements())) {
                Metadata im = e.getAnnotation(Metadata.class);
                if(im==null)    continue;

                String name = im.value();
                if (name.length() == 0) name = ((TypeElement) type.asElement()).getQualifiedName().toString() + '.' + e.getSimpleName();

                metadataProperties.put(e,name);
            }
        }

        /**
         * Based on the model, parse the annotation mirror and updates the metadata bag by adding
         * discovered values.
         */
        public void parse(AnnotationMirror a, MultiMap<String,String> metadataBag) {
            assert a.getAnnotationType().equals(type);

            for (Map.Entry<ExecutableElement, String> e : metadataProperties.entrySet()) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> vals = a.getElementValues();
                AnnotationValue value = vals.get(e.getKey());
                if (value!=null) {
                    metadataBag.add(e.getValue(), toString(value));
                } else {
                    Collection<ExecutableElement> methods = ElementFilter.methodsIn(a.getAnnotationType().asElement().getEnclosedElements());
                    for (ExecutableElement decl : methods) {
                        if (e.getKey().equals(decl)) {
                            value = decl.getDefaultValue();
                            metadataBag.add(e.getValue(), toString(value));
                            break;
                        }
                    }
                }
            }
        }

        private String toString(AnnotationValue value) {
            if (value.getValue() instanceof TypeMirror) {
                TypeMirror tm = (TypeMirror) value.getValue();
                if (tm.getKind().equals(TypeKind.DECLARED)) {
                    DeclaredType dt = (DeclaredType) tm;
                    return getClassName((TypeElement) dt.asElement());
                }
            }
            return value.toString();
        }

        /**
         * Returns the fully qualified class name.
         * The difference between this and {@link TypeElement#getQualifiedName()}
         * is that this method returns the same format as {@link Class#getName()}.
         *
         * Notably, separator for nested classes is '$', not '.'
         */
        private String getClassName(TypeElement d) {
            if (d.getEnclosingElement() != null)
                return getClassName((TypeElement) d.getEnclosingElement()) + '$' + d.getSimpleName();
            else
                return d.getQualifiedName().toString();
        }
    }

    public MultiMap<String, String> process(TypeElement d) {
        visited.clear();
        MultiMap<String,String> r = new MultiMap<String, String>();
        check(d,r);
        return r;
    }

    protected void check(TypeElement d, MultiMap<String, String> result) {
        checkAnnotations(d, result);
        super.check(d,result);
    }

    private void checkAnnotations(TypeElement d, MultiMap<String, String> result) {
        for (AnnotationMirror a : d.getAnnotationMirrors()) {
            getModel(a.getAnnotationType()).parse(a,result);
            // check meta-annotations
            for (AnnotationMirror b : a.getAnnotationType().asElement().getAnnotationMirrors()) {
                getModel(b.getAnnotationType()).parse(b,result);
            }
        }
    }

    /**
     * Checks if the given annotation mirror has the given meta-annotation on it.
     */
    /*private boolean hasMetaAnnotation(AnnotationMirror a, Class<? extends Annotation> type) {
        return a.getAnnotationType().asElement().getAnnotation(type) != null;
    }*/

    private Model getModel(DeclaredType type) {
        Model model = models.get(type);
        if(model==null)
            models.put(type,model=new Model(type));
        return model;
    }
}
