/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.startup;

import com.sun.ejb.containers.AbstractSingletonContainer;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.logging.LogDomains;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

/**
 * @author Mahesh Kannan
 *         Date: Aug 6, 2008
 */
public class SingletonLifeCycleManager {

    private Map<String, Set<String>> initialDependency =
            new HashMap<String, Set<String>>();

    private Map<String, Integer> name2Index = new HashMap<String, Integer>();

    private Map<Integer, String> index2Name = new HashMap<Integer, String>();

    private Set<String> leafNodes = new HashSet<String>();

    private int maxIndex = 0;

    private boolean adj[][];

    // List of eagerly initialized singletons, in the order they were
    // initialized.
    private List<AbstractSingletonContainer> initializedSingletons =
            new ArrayList<AbstractSingletonContainer>();

    private Map<String, AbstractSingletonContainer> name2Container =
            new HashMap<String, AbstractSingletonContainer>();

    private boolean initializeInOrder;

    private Map<String, EjbApplication> name2EjbApp = new HashMap<String, EjbApplication>();

    private static final Logger _logger =
            LogDomains.getLogger(SingletonLifeCycleManager.class, LogDomains.EJB_LOGGER);

    SingletonLifeCycleManager(boolean initializeInOrder) {
        this.initializeInOrder = initializeInOrder;
    }

    void addSingletonContainer(EjbApplication ejbApp, AbstractSingletonContainer c) {
        c.setSingletonLifeCycleManager(this);
        EjbSessionDescriptor sdesc = (EjbSessionDescriptor) c.getEjbDescriptor();
        String src = normalizeSingletonName(sdesc.getName(), sdesc);

        String[] depends = sdesc.getDependsOn();
        String[] newDepends = new String[depends.length];

        StringBuilder sb = new StringBuilder("Partial order of dependent(s). "  + src + " => {");
        for(int i=0; i < depends.length; i++) {
            newDepends[i] = resolveSingleton(depends[i], sdesc);
            sb.append(newDepends[i] + " ");
        }
        sb.append("}");
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, sb.toString());
        }
        this.addDependency(src, newDepends);

        name2Container.put(src, c);
        name2EjbApp.put(src, ejbApp);
    }

    private String normalizeSingletonName(String name, EjbSessionDescriptor sessionDesc) {
        return sessionDesc.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri() + "#" + name;
    }

    private String resolveSingleton(String name, EjbSessionDescriptor sessionDesc) {
        String normalizedName = null;

        EjbBundleDescriptor ejbBundle = sessionDesc.getEjbBundleDescriptor();
        Application app = ejbBundle.getApplication();

        boolean fullyQualified = name.contains("#");

        if (fullyQualified) {
            int index = name.indexOf("#");
            String ejbName = name.substring(index + 1);
            String relativeJarPath = name.substring(0, index);

            BundleDescriptor bundle = app.getRelativeBundle(ejbBundle, relativeJarPath);
            if (bundle != null) {
                normalizedName = bundle.getModuleDescriptor().getArchiveUri() + "#" + ejbName;
            }
        } else {
            if (ejbBundle.hasEjbByName(name)) {
                normalizedName = ejbBundle.getModuleDescriptor().getArchiveUri() + "#" + name;
            } else {
                if (name.matches("^[^/]+/[^/]+$")) {
                    int index = name.indexOf("/");
                    String ejbName = name.substring(index + 1);
                    String moduleName = name.substring(0, index);

                    for (EjbBundleDescriptor bundle : app.getBundleDescriptors(EjbBundleDescriptor.class)) {
                        if (Objects.equals(moduleName, bundle.getModuleDescriptor().getModuleName())) {
                            if (bundle.hasEjbByName(ejbName)) {
                                normalizedName = bundle.getModuleDescriptor().getArchiveUri() + "#" + ejbName;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (normalizedName == null) {
            throw new IllegalStateException(
                "Invalid @DependsOn value = " + name + " for Singleton " + sessionDesc.getName());
        }

        return normalizedName;
    }

    void doStartup(EjbApplication ejbApp) {
        Collection<EjbDescriptor> ejbs = ejbApp.getEjbBundleDescriptor().getEjbs();

        for (EjbDescriptor desc : ejbs) {
            if (desc instanceof EjbSessionDescriptor) {
                EjbSessionDescriptor sdesc = (EjbSessionDescriptor) desc;
                if ((sdesc.isSingleton())) {
                    if (sdesc.getInitOnStartup()) {
                        String normalizedSingletonName = normalizeSingletonName(sdesc.getName(), sdesc);
                        initializeSingleton(name2Container.get(normalizedSingletonName));
                    }
                }
            }
        }
    }

    void doShutdown() {

        // Shutdown singletons in the reverse order of their initialization
        Collections.reverse(initializedSingletons);

        for(AbstractSingletonContainer singletonContainer : initializedSingletons) {

            singletonContainer.onShutdown();

        }

        return;
    }

    public synchronized void initializeSingleton(AbstractSingletonContainer c) {
        initializeSingleton(c, new ArrayList<String>());
    }

    private void initializeSingleton(AbstractSingletonContainer c, List<String> initList) {
        if (! initializedSingletons.contains(c)) {
            String normalizedSingletonName = normalizeSingletonName(c.getEjbDescriptor().getName(),
                    (EjbSessionDescriptor) c.getEjbDescriptor());
            List<String> computedDeps = computeDependencies(normalizedSingletonName);
            int sz = computedDeps.size();
            AbstractSingletonContainer[] deps = new AbstractSingletonContainer[sz];
            initList.add(normalizedSingletonName);
            for (int i = 0; i < sz; i++) {
                String nextSingletonName = computedDeps.get(i);
                deps[i] = name2Container.get(nextSingletonName);
                if (initializeInOrder) {
                    EjbApplication ejbApp = name2EjbApp.get(nextSingletonName);
                    if (! ejbApp.isStarted()) {
                        String msg = "application.xml specifies module initialization ordering but "
                                + initList.get(0) + " depends on " + nextSingletonName
                                + " which is in a module that hasn't been started yet";
                        if (_logger.isLoggable(Level.WARNING)) {
                            StringBuilder sb = new StringBuilder(initList.get(0));
                            for (int k=1; k<initList.size(); k++) {
                                sb.append(" -> ").append(initList.get(k));
                            }
                            sb.append(" -> ").append(nextSingletonName);
                            _logger.log(Level.WARNING, "Partial order of singleton beans involved in this: "
                                + sb.toString());
                        }
                        throw new RuntimeException(msg);
                    }
                }

                initializeSingleton(deps[i], initList);
            }

            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "SingletonLifeCycleManager: initializingSingleton: " + normalizedSingletonName);
            }
            c.instantiateSingletonInstance();
            initializedSingletons.add(c);
        }
    }

    private void addDependency(String src, String[] depends) {
        if (depends != null && depends.length > 0) {
            for (String s : depends) {
                addDependency(src, s);
            }
        } else {
            addDependency(src, "");
        }
    }

    private void addDependency(String src, String depends) {
        src = src.trim();
        Set<String> deps = getExistingDependecyList(src);

        if (depends != null) {
            StringTokenizer tok = new StringTokenizer(depends, " ,");
            while (tok.hasMoreTokens()) {
                String dep = tok.nextToken();
                deps.add(dep);
                getExistingDependecyList(dep);
            }
        }
    }

    private List<String> computeDependencies(String root) {
        if (adj == null) {
            fillAdjacencyMatrix();
        }
        Stack<String> stk = new Stack<String>();
        stk.push(root);
        List<String> dependencies = new ArrayList<String>();
        do {
            String top = stk.peek();
            int topIndex = name2Index.get(top);
            boolean hasDep = false;

            for (int j = 0; j < maxIndex; j++) {
                if (adj[topIndex][j]) {
                    String name = index2Name.get(j);
                    if (stk.contains(name)) {
                        String str = "Cyclic dependency: " + top + " => " + name + "? ";
                        throw new IllegalArgumentException(
                                str + getCyclicString(adj));
                    } else {
                        if (!dependencies.contains(name)) {
                            if (leafNodes.contains(name)) {
                                dependencies.add(name);
                            } else {
                                hasDep = true;
                                stk.push(name);
                            }
                        }
                    }
                }
            }
            if (!hasDep) {
                stk.pop();
                if (!dependencies.contains(top)) {
                    dependencies.add(top);
                }
            }
        } while (!stk.empty());

        dependencies.remove(dependencies.size() - 1);

        return dependencies;
    }

    private Set<String> getExistingDependecyList(String src) {
        Set<String> existingDeps = initialDependency.get(src);
        if (existingDeps == null) {
            existingDeps = new HashSet<String>();
            initialDependency.put(src, existingDeps);
            name2Index.put(src, maxIndex);
            index2Name.put(maxIndex, src);
            maxIndex++;
        }

        return existingDeps;
    }

    private void fillAdjacencyMatrix() {
        adj = new boolean[maxIndex][maxIndex];
        for (int i = 0; i < maxIndex; i++) {
            String src = index2Name.get(i);
            for (int j = 0; j < maxIndex; j++) {
                adj[i][j] = false;
            }

            boolean isLeaf = true;
            Set<String> deps = initialDependency.get(src);
            for (String d : deps) {
                int k = name2Index.get(d);
                adj[i][k] = true;
                isLeaf = false;
            }

            if (isLeaf) {
                leafNodes.add(src);
            }
        }
    }

    private String getCyclicString(boolean[][] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxIndex; i++) {
            StringBuilder sb2 = new StringBuilder("");
            String delim = "";
            for (int j = 0; j < maxIndex; j++) {
                if (a[i][j]) {
                    sb2.append(delim).append(index2Name.get(j));
                    delim = ", ";
                }
            }
            String dep = sb2.toString();
            if (dep.length() > 0) {
                sb.append(" ").append(index2Name.get(i))
                        .append(" => ").append(sb2.toString()).append("; ");
            }

        }
        return sb.toString();
    }

/*

    public static void main(String[] args) {
        test1();
        test2();
        test3();
        test4();
    }

    private static void test1() {
        SingletonLifeCycleManager ts = new SingletonLifeCycleManager(false);
        ts.addDependency("A", "B, C");
        ts.addDependency("B", "C, D");
        ts.addDependency("D", "E");
        ts.addDependency("C", "D, E, G");
        ts.addDependency("E", "D");

        ts.getPartialOrdering();

        SingletonLifeCycleManager t = new SingletonLifeCycleManager(false);
        t.addDependency("C", ts.computeDependencies("C"));
        t.addDependency("D", ts.computeDependencies("D"));

        t.printAdjacencyMatrix();

        for (String s : t.computeDependencies("C")) {
            System.out.print(s + " ");
        }
        System.out.println();

        for (String s : t.getPartialOrdering()) {
            System.out.print(s + " ");
        }

    }

    private static void test2() {
        SingletonLifeCycleManager ts = new SingletonLifeCycleManager(false);
        ts.addDependency("A", "D, E");
        ts.addDependency("B", "F");
        ts.addDependency("C", "G, H");
        ts.addDependency("D", "I");
        ts.addDependency("E", "J");
        ts.addDependency("F", "J, K");
        ts.addDependency("H", "L");
        ts.addDependency("I", "M, N");
        ts.addDependency("J", "U");
        ts.addDependency("K", "U");
        ts.addDependency("L", "O");
        ts.addDependency("U", "N, O");
        ts.addDependency("N", "P, Q");
        ts.addDependency("O", "Q, R");
        ts.addDependency("Q", "S, T");

        ts.addDependency("E", "X, W");
        ts.addDependency("X", "Y");
        ts.addDependency("W", "Y");
        ts.addDependency("Y", "Z");
        ts.addDependency("Z", "O");
        //ts.addDependency("R", "J");

        String[] dep = ts.getPartialOrdering();
        for (String s : dep) {
            System.out.print(s + " ");
        }
        System.out.println();

        SingletonLifeCycleManager ts2 = new SingletonLifeCycleManager(false);
        ts2.addDependency("E", ts.computeDependencies("E"));
        ts2.addDependency("U", ts.computeDependencies("U"));
        ts2.addDependency("H", ts.computeDependencies("H"));
        for (String s : ts2.getPartialOrdering()) {
            System.out.print(s + " ");
        }
    }

    private static void test3() {
        SingletonLifeCycleManager ts = new SingletonLifeCycleManager(false);
        ts.addDependency("A", (String) null);
        ts.addDependency("B", (String) null);
        ts.addDependency("C", (String) null);

        String[] dep = ts.getPartialOrdering();
        for (String s : dep) {
            System.out.print(s + " ");
        }
        System.out.println();
        for (String s : ts.computeDependencies("B")) {
            System.out.print(s + " ");
        }
        System.out.println();
    }

    private static void test4() {
        SingletonLifeCycleManager ts = new SingletonLifeCycleManager(false);
        ts.addDependency("A", "D, B, C");
        ts.addDependency("B", "F, I");
        ts.addDependency("C", "F, H, G");
        ts.addDependency("D", "E");
        ts.addDependency("E", (String) null);
        ts.addDependency("F", "E");
        ts.addDependency("G", "E, I, K");
        ts.addDependency("H", "J");
        ts.addDependency("I", "J");
        ts.addDependency("K", (List) null);

        String[] dep = ts.getPartialOrdering();
        for (String s : dep) {
            System.out.print(s + " ");
        }
        System.out.println();
        for (String s : ts.computeDependencies("C")) {
            System.out.print(s + " ");
        }
        System.out.println();
    }

    private String[] getPartialOrdering() {
        if (adj == null) {
            fillAdjacencyMatrix();
        }
        boolean[][] tempAdj = new boolean[maxIndex][maxIndex];
        for (int i = 0; i < maxIndex; i++) {
            for (int j = 0; j < maxIndex; j++) {
                tempAdj[i][j] = adj[i][j];
            }
        }
        List<String> dependencies = new ArrayList<String>();
        do {
            String src = null;
            boolean foundAtLeastOneLeaf = false;
            for (int i = 0; i < maxIndex; i++) {
                src = index2Name.get(i);
                if (!dependencies.contains(src)) {
                    boolean hasDep = false;
                    for (int j = 0; j < maxIndex; j++) {
                        if (tempAdj[i][j]) {
                            hasDep = true;
                            break;
                        }
                    }

                    if ((!hasDep) && (!dependencies.contains(src))) {
                        dependencies.add(src);
                        for (int k = 0; k < maxIndex; k++) {
                            tempAdj[k][i] = false;
                        }
                        foundAtLeastOneLeaf = true;
                    }
                }
            }

            if ((!foundAtLeastOneLeaf) && (dependencies.size() < name2Index.size())) {
                throw new IllegalArgumentException("Circular dependency: "
                        + getCyclicString(tempAdj));
            }

        } while (dependencies.size() < name2Index.size());

        return dependencies.toArray(new String[dependencies.size()]);

    }

    private void addDependency(String src, List<String> depends) {
        if (depends != null) {
            for (String s : depends) {
                addDependency(src, s);
            }
        } else {
            addDependency(src, "");
        }
    }

    private void printAdjacencyMatrix() {
        if (adj == null) {
            fillAdjacencyMatrix();
        }
        System.out.print(" ");
        for (int i = 0; i < maxIndex; i++) {
            System.out.print(" " + index2Name.get(i));
        }
        System.out.println();
        for (int i = 0; i < maxIndex; i++) {
            System.out.print(index2Name.get(i) + " ");
            for (int j = 0; j < maxIndex; j++) {
                System.out.print(adj[i][j] ? "1 " : "0 ");
            }
            System.out.println();
        }
    }

*/
}
