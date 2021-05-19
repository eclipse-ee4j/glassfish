/*
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

package org.glassfish.ha.store.apt.processor;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.util.DeclarationFilter;
import com.sun.mirror.type.TypeMirror;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.glassfish.ha.store.annotations.Attribute;
import org.glassfish.ha.store.annotations.Version;
import org.glassfish.ha.store.apt.generators.StorableGenerator;
import org.glassfish.ha.store.apt.generators.StoreEntryMetadataGenerator;

/**
 * @author Mahesh Kannan
 */
public class StoreEntryAnnotationProcessor
        implements AnnotationProcessor {

    private Set<AnnotationTypeDeclaration> decls;

    private AnnotationProcessorEnvironment env;

    private String qName;

    private Collection<ClassInfo> classInfos
            = new ArrayList<ClassInfo>();

    StoreEntryAnnotationProcessor(Set<AnnotationTypeDeclaration> decls,
                                  AnnotationProcessorEnvironment env) {
        this.decls = decls;
        this.env = env;
    }

    public void process() {
        int counter = 0;
        AnnotationTypeDeclaration storeEntryAnnDecls = decls.iterator().next();
        DeclarationFilter classFilter = DeclarationFilter.getFilter(ClassDeclaration.class);
        for (Declaration decl : classFilter.filter(env.getDeclarationsAnnotatedWith(storeEntryAnnDecls))) {
            if (decl != null) {
                ClassDeclaration classDecl = (ClassDeclaration) decl;
                ClassInfo classInfo = new ClassInfo(classDecl);
                classInfo.setJavaDoc(classDecl.getDocComment());
                classInfos.add(classInfo);
                qName = classDecl.getQualifiedName();


                DeclarationFilter getterFilter = new DeclarationFilter() {
                    public boolean matches(Declaration d) {
                        return d.getSimpleName().startsWith("set");
                    }
                };

                Collection<? extends MethodDeclaration> methods = classDecl.getMethods();
                TypeMirror paramType = null;
                for (MethodDeclaration m : getterFilter.filter(methods)) {
                    MethodInfo methodInfo = new MethodInfo();
                    String attributeName = null;
                    Attribute attrAnn = m.getAnnotation(Attribute.class);
                    if (attrAnn != null) {
                        attributeName = attrAnn.value();
                        methodInfo.type = MethodInfo.MethodType.SETTER;
                    } else {
                        Version versionAnn = m.getAnnotation(Version.class);
                        if (versionAnn != null) {
                            attributeName = versionAnn.name();
                            methodInfo.type = MethodInfo.MethodType.VERSION;
                        } else {
                            //Some getter method
                            continue;
                        }
                    }

                    String simpleName = m.getSimpleName();
                    if (! simpleName.startsWith("set")) {
                        //TODO Warning??
                        continue;
                    }

                    if (attributeName == null || attributeName.length() == 0) {
                        attributeName = simpleName;
                        attributeName = Character.toLowerCase(attributeName.charAt(3)) + attributeName.substring(4);
                    }
                    methodInfo.attrName = attributeName;

                    System.out.println("Found attribute: " + attributeName);
                    Collection<ParameterDeclaration> paramDecls = m.getParameters();
                    if ((paramDecls != null) && (paramDecls.size() == 1)) {
                        ParameterDeclaration paramDecl =  paramDecls.iterator().next();
                        paramType = paramDecl.getType();
                    }

                    methodInfo.getter = m;
                    methodInfo.paramType = paramType;
                    classInfo.addMethodInfo(methodInfo);
                }
            }
        }

        this.accept(new StorableGenerator());
        this.accept(new StoreEntryMetadataGenerator());
    }

    public void accept(ClassVisitor cv) {
        for (ClassInfo classInfo : classInfos) {
            ClassDeclaration classDecl = classInfo.getClassDeclaration();
            cv.visit(classDecl.getPackage().toString(), classInfo.getJavaDoc(), classDecl.getSimpleName());
            for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                switch (methodInfo.type) {
                    case SETTER:
                        cv.visitSetter(methodInfo.getter.getSimpleName(), methodInfo.attrName, null, methodInfo.paramType);
                        break;
                    case VERSION:
                        cv.visitVersionMethod(methodInfo.getter.getSimpleName(), methodInfo.attrName, null, methodInfo.paramType);
                        break;
                    /*
                    case HASHKEY:
                        cv.visitHashKeyMethod(methodInfo.getter.getSimpleName(), methodInfo.attrName, null, methodInfo.paramType);
                        break;
                    */
                }
            }
            cv.visitEnd();
        }
    }
}
