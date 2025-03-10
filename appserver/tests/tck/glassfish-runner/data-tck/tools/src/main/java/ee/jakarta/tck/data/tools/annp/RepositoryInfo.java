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
import ee.jakarta.tck.data.tools.qbyn.QueryByNameInfo.OrderBy;
import jakarta.data.repository.Repository;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;


public class RepositoryInfo {
    public static class MethodInfo {
        String name;
        String returnType;
        String query;
        List<OrderBy> orderBy;
        List<String> parameters = new ArrayList<>();
        List<String> exceptions = new ArrayList<>();

        public MethodInfo(String name, String returnType, String query, List<OrderBy> orderBy) {
            this.name = name;
            this.returnType = returnType;
            this.query = query;
            this.orderBy = orderBy;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
        public List<String> getParameters() {
            return parameters;
        }
        public void addParameter(String p) {
            parameters.add(p);
        }
        public List<OrderBy> getOrderBy() {
            return orderBy;
        }
    }
    private Element repositoryElement;
    private String fqn;
    private String pkg;
    private String name;
    private String dataStore = "";
    private ArrayList<MethodInfo> methods = new ArrayList<>();
    public ArrayList<ExecutableElement> qbnMethods = new ArrayList<>();

    public RepositoryInfo() {
    }
    public RepositoryInfo(Element repositoryElement) {
        this.repositoryElement = repositoryElement;
        Repository ann = repositoryElement.getAnnotation(Repository.class);
        setFqn(AnnProcUtils.getFullyQualifiedName(repositoryElement));
        setName(repositoryElement.getSimpleName().toString());
        setDataStore(ann.dataStore());
    }

    public Element getRepositoryElement() {
        return repositoryElement;
    }
    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
        int index = fqn.lastIndexOf(".");
        if(index > 0) {
            setPkg(fqn.substring(0, index));
        }
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataStore() {
        return dataStore;
    }

    public void setDataStore(String dataStore) {
        this.dataStore = dataStore;
    }


    /**
     * Add a Query By Name method to the repository
     * @param m - the method
     * @param info - parsed QBN info
     * @param types - annotation processing types utility
     */
    public void addQBNMethod(ExecutableElement m, QueryByNameInfo info, Types types) {
        qbnMethods.add(m);
        // Deal with generics
        DeclaredType returnType = null;
        if(m.getReturnType() instanceof DeclaredType) {
            returnType = (DeclaredType) m.getReturnType();
        }
        String returnTypeStr = returnType == null ? m.getReturnType().toString() : toString(returnType);
        System.out.printf("addQBNMethod: %s, returnType: %s, returnTypeStr: %s\n",
                m.getSimpleName().toString(), returnType, returnTypeStr);
        ParseUtils.ToQueryOptions options = ParseUtils.ToQueryOptions.NONE;
        String methodName = m.getSimpleName().toString();
        // Select the appropriate cast option if this is a countBy method
        if(methodName.startsWith("count")) {
            options = switch (returnTypeStr) {
                case "long" -> ParseUtils.ToQueryOptions.CAST_LONG_TO_INTEGER;
                case "int" -> ParseUtils.ToQueryOptions.CAST_COUNT_TO_INTEGER;
                default -> ParseUtils.ToQueryOptions.NONE;
            };
        }
        // Build the query string
        String query = ParseUtils.toQuery(info, options);

        MethodInfo mi = new MethodInfo(methodName, m.getReturnType().toString(), query, info.getOrderBy());
        for (VariableElement p : m.getParameters()) {
            mi.addParameter(p.asType().toString() + " " + p.getSimpleName());
        }
        addMethod(mi);
    }
    public String toString(DeclaredType tm) {
        StringBuilder buf = new StringBuilder();
        TypeElement returnTypeElement = (TypeElement) tm.asElement();
        buf.append(returnTypeElement.getQualifiedName());
        if (!tm.getTypeArguments().isEmpty()) {
            buf.append('<');
            buf.append(tm.getTypeArguments().toString());
            buf.append(">");
        }
        return buf.toString();
    }
    public List<ExecutableElement> getQBNMethods() {
        return qbnMethods;
    }
    public boolean hasQBNMethods() {
        return !qbnMethods.isEmpty();
    }

    public ArrayList<MethodInfo> getMethods() {
        return methods;
    }

    public void addMethod(MethodInfo m) {
        methods.add(m);
    }
}
