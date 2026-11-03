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

import com.sun.faces.mirror.ClassInfo;
import com.sun.faces.mirror.DeclaredComponentInfo;
import com.sun.faces.mirror.DeclaredRendererInfo;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Debug generator.
 */
public final class DebugGenerator extends Generator {

    /**
     * Template resource path.
     */
    private static final String TEMPLATE =
            "com/sun/faces/mirror/generator/Debug.template";

    /**
     * Template engine.
     */
    private final VelocityEngine velocityEngine;

    /**
     * Namespace prefix.
     */
    private String namespacePrefix;

    /**
     * Components.
     */
    private Set<DeclaredComponentInfo> compInfos;

    /**
     * Namespace.
     */
    private String namespace;

    /**
     * Renderers.
     */
    private Set<DeclaredRendererInfo> rendererInfos;

    /**
     * Package names.
     */
    private Set<String> packageNames;

    /**
     * Create a new instance.
     * @param velocity template engine
     */
    public DebugGenerator(final VelocityEngine velocity) {
        this.velocityEngine = velocity;
    }

    @Override
    public void generate() throws GeneratorException {
        try {
            VelocityContext velocityContext = new VelocityContext();
            String ns = this.getNamespace();
            String nsPrefix = this.getNamespacePrefix();
            velocityContext.put("packageNameSet", this.getPackageNames());
            velocityContext.put("componentInfoSet",
                    this.getDeclaredComponentInfos());
            velocityContext.put("rendererInfoSet",
                    this.getDeclaredRendererInfos());
            if (ns == null) {
                velocityContext.put("namespace", "");
            } else {
                velocityContext.put("namespace", ns);
            }
            if (nsPrefix == null) {
                velocityContext.put("namespacePrefix", "");
            } else {
                velocityContext.put("namespacePrefix", nsPrefix);
            }
            Template template = velocityEngine.getTemplate(TEMPLATE);
            PrintWriter printWriter = this.getPrintWriter();
            template.merge(velocityContext, printWriter);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneratorException(e);
        }
    }

    /**
     * Getter for property namespace.
     * @return namespace
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Setter for property namespace.
     * @param newNs new namespace
     */
    public void setNamespace(final String newNs) {
        this.namespace = newNs;
    }

    /**
     * Getter for property namespacePrefix.
     *
     * @return Value of property namespacePrefix.
     */
    public String getNamespacePrefix() {
        return this.namespacePrefix;
    }

    /**
     * Setter for property namespacePrefix.
     * @param newNsPrefix new namespace prefix
     */
    public void setNamespacePrefix(final String newNsPrefix) {
        this.namespacePrefix = newNsPrefix;
    }

    /**
     * Protected getter for property declaredComponentInfoSet.
     * @return set of component info
     */
    @SuppressWarnings("unchecked")
    public Set<DeclaredComponentInfo> getDeclaredComponentInfos() {
        TreeSet<DeclaredComponentInfo> sortedSet =
                new TreeSet<DeclaredComponentInfo>(new ClassInfoComparator());
        sortedSet.addAll(this.compInfos);
        return sortedSet;
    }

    /**
     * Setter for property declaredComponentInfoSet.
     * @param newCompInfos new component info
     */
    public void setDeclaredComponentInfo(
            final Set<DeclaredComponentInfo> newCompInfos) {

        this.compInfos = newCompInfos;
    }

    /**
     * Protected getter for property declaredRendererInfoSet.
     * @return set of renderer info
     */
    @SuppressWarnings("unchecked")
    protected Set<DeclaredRendererInfo> getDeclaredRendererInfos() {
        TreeSet<DeclaredRendererInfo> sortedSet =
                new TreeSet<DeclaredRendererInfo>(new ClassInfoComparator());
        sortedSet.addAll(this.rendererInfos);
        return sortedSet;
    }

    /**
     * Setter for property declaredRendererInfoSet.
     * @param newRendererInfos new set of renderer info
     */
    public void setDeclaredRendererInfos(
            final Set<DeclaredRendererInfo> newRendererInfos) {

        this.rendererInfos = newRendererInfos;
    }

    /**
     * Getter for property packageNameSet.
     * @return set of package name
     */
    public Set<String> getPackageNames() {
        TreeSet<String> sortedSet = new TreeSet<String>();
        sortedSet.addAll(this.packageNames);
        return sortedSet;
    }

    /**
     * Setter for property packageNameSet.
     * @param newPackageNames new set of package name
     */
    public void setPackageNames(final Set<String> newPackageNames) {
        this.packageNames = newPackageNames;
    }

    /**
     * Class info comparator.
     */
    private static final class ClassInfoComparator implements Comparator {

        @Override
        public int compare(final Object obj1, final Object obj2) {
            return ((ClassInfo) obj1).getQualifiedName()
                    .compareTo(((ClassInfo) obj2).getClassName());
        }
    }
}
