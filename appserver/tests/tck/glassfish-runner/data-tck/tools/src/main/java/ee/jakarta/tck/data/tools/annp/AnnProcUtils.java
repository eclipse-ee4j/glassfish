/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ee.jakarta.tck.data.tools.annp;

import ee.jakarta.tck.data.tools.qbyn.ParseUtils;
import ee.jakarta.tck.data.tools.qbyn.QueryByNameInfo;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AnnProcUtils {
    // The name of the template for the TCK override imports
    public static final String TCK_IMPORTS = "/tckImports";
    // The name of the template for the TCK overrides
    public static final String TCK_OVERRIDES = "/tckOverrides";

    /**
     * Get a list of non-lifecycle methods in a type element. This will also process superinterfaces
     * @param typeElement a repository interface
     * @return a list of non-lifecycle methods as candidate repository methods
     */
    public static List<ExecutableElement> methodsIn(TypeElement typeElement) {
        ArrayList<ExecutableElement> methods = new ArrayList<>();
        List<ExecutableElement> typeMethods = methodsIn(typeElement.getEnclosedElements());
        methods.addAll(typeMethods);
        List<? extends TypeMirror> superifaces = typeElement.getInterfaces();
        for (TypeMirror iface : superifaces) {
            if(iface instanceof DeclaredType) {
                DeclaredType dt = (DeclaredType) iface;
                System.out.printf("Processing superinterface %s<%s>\n", dt.asElement(), dt.getTypeArguments());
                methods.addAll(methodsIn((TypeElement) dt.asElement()));
            }
        }
        return methods;
    }

    /**
     * Get a list of non-lifecycle methods in a list of repository elements
     * @param elements - a list of repository elements
     * @return possibly empty list of non-lifecycle methods
     */
    public static List<ExecutableElement> methodsIn(Iterable<? extends Element> elements) {
        ArrayList<ExecutableElement> methods = new ArrayList<>();
        for (Element e : elements) {
            if(e.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) e;
                // Skip lifecycle methods
                if(!isLifeCycleMethod(method)) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    /**
     * Is a method annotated with a lifecycle or Query annotation
     * @param method a repository method
     * @return true if the method is a lifecycle method
     */
    public static boolean isLifeCycleMethod(ExecutableElement method) {
        boolean standardLifecycle = method.getAnnotation(Insert.class) != null
                || method.getAnnotation(Find.class) != null
                || method.getAnnotation(Update.class) != null
                || method.getAnnotation(Save.class) != null
                || method.getAnnotation(Delete.class) != null
                || method.getAnnotation(Query.class) != null;
        return standardLifecycle;
    }

    public static String getFullyQualifiedName(Element element) {
        if (element instanceof TypeElement) {
            return ((TypeElement) element).getQualifiedName().toString();
        }
        return null;
    }


    public static QueryByNameInfo isQBN(ExecutableElement m) {
        String methodName = m.getSimpleName().toString();
        try {
            return ParseUtils.parseQueryByName(methodName);
        }
        catch (Throwable e) {
            System.out.printf("Failed to parse %s: %s\n", methodName, e.getMessage());
        }
        return null;
    }

    /**
     * Write a repository interface to a source file using the {@linkplain RepositoryInfo}. This uses the
     * RepoTemplate.stg template file to generate the source code. It also looks for a
     *
     * @param repo - parsed repository info
     * @param processingEnv - the processing environment
     * @throws IOException - if the file cannot be written
     */
    public static void writeRepositoryInterface(RepositoryInfo repo, ProcessingEnvironment processingEnv) throws IOException {
        STGroup repoGroup = new STGroupFile("RepoTemplate.stg");
        ST genRepo = repoGroup.getInstanceOf("genRepo");
        try {
            URL stgURL = AnnProcUtils.class.getResource("/"+repo.getFqn()+".stg");
            STGroup tckGroup = new STGroupFile(stgURL);
            long count = tckGroup.getTemplateNames().stream().filter(t -> t.equals(TCK_IMPORTS) | t.equals(TCK_OVERRIDES)).count();
            if(count != 2) {
                System.out.printf("No TCK overrides for %s\n", repo.getFqn());
            } else {
                tckGroup.importTemplates(repoGroup);
                System.out.printf("Found TCK overrides(%s) for %s\n", tckGroup.getRootDirURL(), repo.getFqn());
                System.out.printf("tckGroup: %s\n", tckGroup.show());
                genRepo = tckGroup.getInstanceOf("genRepo");
            }
        } catch (IllegalArgumentException e) {
            System.out.printf("No TCK overrides for %s\n", repo.getFqn());
        }

        genRepo.add("repo", repo);

        String ifaceSrc = genRepo.render();
        String ifaceName = repo.getFqn() + "$";
        Filer filer = processingEnv.getFiler();
        JavaFileObject srcFile = filer.createSourceFile(ifaceName, repo.getRepositoryElement());
        try(Writer writer = srcFile.openWriter()) {
            writer.write(ifaceSrc);
            writer.flush();
        }
        System.out.printf("Wrote %s, to: %s\n", ifaceName, srcFile.toUri());
    }
}
