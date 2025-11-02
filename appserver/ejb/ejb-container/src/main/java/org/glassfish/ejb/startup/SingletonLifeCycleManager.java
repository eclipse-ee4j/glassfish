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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Logger;

import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

/**
 * @author Mahesh Kannan
 *         Date: Aug 6, 2008
 */
public class SingletonLifeCycleManager {

    private static final Logger LOG = LogDomains.getLogger(SingletonLifeCycleManager.class, LogDomains.EJB_LOGGER);

    private final Map<String, Set<String>> singletonDependencies = new HashMap<>();

    /** List of eagerly initialized singletons, in the order they were initialized. */
    private final Set<AbstractSingletonContainer> initializedSingletons = Collections.newSetFromMap(new LinkedHashMap<>());

    private final Map<String, AbstractSingletonContainer> containers = new HashMap<>();

    private final Map<String, EjbApplication> applications = new HashMap<>();

    private final boolean initializeInOrder;

    SingletonLifeCycleManager(boolean initializeInOrder) {
        this.initializeInOrder = initializeInOrder;
    }

    void addSingletonContainer(EjbApplication application, AbstractSingletonContainer container) {
        Set<String> dependsOn = Collections.newSetFromMap(new LinkedHashMap<>());
        EjbSessionDescriptor ejbSession = (EjbSessionDescriptor) container.getEjbDescriptor();
        String normalizedName = normalizeSingletonName(ejbSession);

        container.setSingletonLifeCycleManager(this);

        Arrays.stream(ejbSession.getDependsOn())
            .map(name -> resolveSingleton(name, ejbSession)).forEachOrdered(dependsOn::add);

        LOG.log(FINE, () -> "Partial order of dependent(s). " + normalizedName +
            " => {" + String.join(", ", dependsOn) + "}");

        addDependencies(normalizedName, dependsOn);

        containers.put(normalizedName, container);
        applications.put(normalizedName, application);
    }

    private String normalizeSingletonName(EjbDescriptor ejb) {
        return ejb.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri() + "#" + ejb.getName();
    }

    private String resolveSingleton(String name, EjbDescriptor ejb) {
        String resolvedName = null;

        EjbBundleDescriptor ejbBundle = ejb.getEjbBundleDescriptor();
        Application application = ejbBundle.getApplication();

        boolean fullyQualified = name.contains("#");

        if (fullyQualified) {
            int index = name.indexOf("#");
            String ejbName = name.substring(index + 1);
            String relativeJarPath = name.substring(0, index);

            BundleDescriptor bundle = application.getRelativeBundle(ejbBundle, relativeJarPath);
            if (bundle != null) {
                resolvedName = bundle.getModuleDescriptor().getArchiveUri() + "#" + ejbName;
            }
        } else {
            if (ejbBundle.hasEjbByName(name)) {
                resolvedName = ejbBundle.getModuleDescriptor().getArchiveUri() + "#" + name;
            } else {
                if (name.matches("^[^/]+/[^/]+$")) {
                    int index = name.indexOf("/");
                    String ejbName = name.substring(index + 1);
                    String moduleName = name.substring(0, index);

                    for (EjbBundleDescriptor bundle : application.getBundleDescriptors(EjbBundleDescriptor.class)) {
                        if (Objects.equals(moduleName, bundle.getModuleDescriptor().getModuleName())) {
                            if (bundle.hasEjbByName(ejbName)) {
                                resolvedName = bundle.getModuleDescriptor().getArchiveUri() + "#" + ejbName;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (resolvedName == null) {
            throw new IllegalStateException("Invalid @DependsOn value = " + name + " for Singleton " + ejb.getName());
        }

        return resolvedName;
    }

    void doStartup(EjbApplication application) {
        for (EjbDescriptor ejb : application.getEjbBundleDescriptor().getEjbs()) {
            if (ejb instanceof EjbSessionDescriptor ejbSession) {
                if (ejbSession.isSingleton()) {
                    if (ejbSession.getInitOnStartup()) {
                        String normalizedName = normalizeSingletonName(ejbSession);
                        initializeSingleton(containers.get(normalizedName));
                    }
                }
            }
        }
    }

    void doShutdown() {
        // Shutdown singletons in the reverse order of their initialization
        new ArrayDeque<>(initializedSingletons).descendingIterator()
            .forEachRemaining(AbstractSingletonContainer::onShutdown);
    }

    public void initializeSingleton(AbstractSingletonContainer container) {
        if (initializedSingletons.contains(container)) {
            return;
        }

        String normalizedName = normalizeSingletonName(container.getEjbDescriptor());
        Collection<String> dependsOn = computeDependencies(normalizedName);
        for (String dependency : dependsOn) {
            AbstractSingletonContainer dependencyContainer = containers.get(dependency);
            if (initializedSingletons.contains(dependencyContainer)) {
                continue;
            }
            if (initializeInOrder) {
                EjbApplication application = applications.get(dependency);
                if (!application.isStarted()) {
                    LOG.log(WARNING, () -> {
                        StringJoiner orderMessage = new StringJoiner(" -> ");
                        dependsOn.stream().takeWhile(d -> !d.equals(dependency)).forEachOrdered(orderMessage::add);
                        return "Partial order of singleton beans involved in this: " + orderMessage.add(dependency);
                    });
                    String errorMessage = "application.xml specifies module initialization ordering but "
                        + normalizedName + "depends on " + dependency
                        + "which is in a module that hasn't been started yet";
                    throw new RuntimeException(errorMessage);
                }
            }

            LOG.log(FINE, "SingletonLifeCycleManager: initializeSingleton: {0}", dependency);

            dependencyContainer.instantiateSingletonInstance();
            initializedSingletons.add(dependencyContainer);
        }

        LOG.log(FINE, "SingletonLifeCycleManager: initializeSingleton: {0}", normalizedName);

        container.instantiateSingletonInstance();
        initializedSingletons.add(container);
    }

    private void addDependencies(String singleton, Set<String> dependsOn) {
        if (!dependsOn.isEmpty()) {
            singletonDependencies.putIfAbsent(singleton, dependsOn);
        }
    }

    private Collection<String> computeDependencies(String singleton) {
        if (!hasDependencies(singleton)) {
            return Set.of();
        }

        Set<String> dependsOn = Collections.newSetFromMap(new LinkedHashMap<>());
        Deque<String> recursiveQueue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        recursiveQueue.addLast(singleton);

        while (!recursiveQueue.isEmpty()) {
            String current = recursiveQueue.peekLast();
            if (visited.contains(current)) {
                dependsOn.add(current);
                recursiveQueue.removeLast();
                continue;
            }

            visited.add(current);

            for (String dependency : singletonDependencies.get(current)) {
                if (hasDependencies(dependency)) {
                    if (visited.contains(dependency)) {
                        if (recursiveQueue.contains(dependency)) {
                            String errorMessage = "Cyclic dependency: " + current + " => " + dependency +"? ";
                            String cyclicMessage = buildCyclicMessage(dependency, current);
                            throw new IllegalArgumentException(errorMessage + cyclicMessage);
                        }
                    } else {
                        recursiveQueue.remove(dependency);
                        recursiveQueue.addLast(dependency);
                    }
                } else {
                    dependsOn.add(dependency);
                }
            }
        }

        dependsOn.remove(singleton);

        return dependsOn;
    }

    private boolean hasDependencies(String singleton) {
        return singletonDependencies.containsKey(singleton);
    }

    private String buildCyclicMessage(String tail, String head) {
        List<String> cyclicMessage = new ArrayList<>();
        for (List<String> path : findDirectedPaths(tail, head)) {
            cyclicMessage.add(String.join(" => ", path));
        }
        return String.join("; ", cyclicMessage);
    }

    private List<List<String>> findDirectedPaths(String tail, String head) {
        List<List<String>> directedPaths = new ArrayList<>();
        Deque<String> recursiveQueue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        recursiveQueue.addLast(tail);

        while (!recursiveQueue.isEmpty()) {
            String current = recursiveQueue.peekLast();
            if (visited.contains(current)) {
                recursiveQueue.removeLast();
                visited.remove(current);
                continue;
            }

            visited.add(current);

            for (String dependency : singletonDependencies.get(current)) {
                if (hasDependencies(dependency)) {
                    if (dependency.equals(head)) {
                        List<String> path = new ArrayList<>();
                        recursiveQueue.stream().filter(visited::contains).forEachOrdered(path::add);
                        path.add(dependency);
                        directedPaths.add(path);
                    } else {
                        recursiveQueue.addLast(dependency);
                    }
                }
            }
        }

        return directedPaths;
    }
}
