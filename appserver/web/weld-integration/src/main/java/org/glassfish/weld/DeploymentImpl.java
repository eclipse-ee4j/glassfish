/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.EjbDescriptor;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.SkipIfPortableExtensionPresent;
import jakarta.enterprise.inject.spi.Extension;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.cdi.CDILoggerInfo;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.javaee.core.deployment.ApplicationHolder;
import org.glassfish.weld.connector.WeldUtils.BDAType;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.CDI11Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.helpers.MetadataImpl;
import org.jboss.weld.lite.extension.translator.LiteExtensionTranslator;

import static com.sun.enterprise.util.Utility.isAnyEmpty;
import static com.sun.enterprise.util.Utility.isAnyNull;
import static com.sun.enterprise.util.Utility.isEmpty;
import static java.util.Collections.emptyList;
import static java.util.logging.Level.FINE;
import static java.util.stream.Collectors.toList;
import static org.glassfish.cdi.CDILoggerInfo.CREATING_DEPLOYMENT_ARCHIVE;
import static org.glassfish.cdi.CDILoggerInfo.EXCEPTION_SCANNING_JARS;
import static org.glassfish.cdi.CDILoggerInfo.GET_BEAN_DEPLOYMENT_ARCHIVES;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_ADD_NEW_BDA_TO_ROOTS;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_ADD_TO_EXISTING;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_CHECKING;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_CHECKING_SUBBDA;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_CREATE_NEW_BDA;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_RETURNING_NEWLY_CREATED_BDA;
import static org.glassfish.deployment.common.InstalledLibrariesResolver.getInstalledLibraries;
import static org.glassfish.weld.WeldDeployer.WELD_BOOTSTRAP;
import static org.glassfish.weld.connector.WeldUtils.JAR_SUFFIX;
import static org.glassfish.weld.connector.WeldUtils.META_INF_BEANS_XML;
import static org.glassfish.weld.connector.WeldUtils.SEPARATOR_CHAR;
import static org.glassfish.weld.connector.WeldUtils.isImplicitBeanArchive;
import static org.jboss.weld.bootstrap.spi.BeanDiscoveryMode.NONE;

/**
 * Represents a deployment of a CDI (Weld) application.
 */
public class DeploymentImpl implements CDI11Deployment {

    private static final Logger LOG = CDILoggerInfo.getLogger();
    private static final String IS_EMBEDDED_EJB_CONTAINER = "org.glassfish.ejb.embedded.active";

    // Keep track of our BDAs for this deployment
    private List<RootBeanDeploymentArchive> rarRootBdas;
    private List<RootBeanDeploymentArchive> ejbRootBdas;
    private List<RootBeanDeploymentArchive> warRootBdas;
    private List<RootBeanDeploymentArchive> libJarRootBdas;

    private List<BeanDeploymentArchive> beanDeploymentArchives;
    private DeploymentContext context;

    // A convenience Map to get a BeanDeploymentArchive for a given BeanDeploymentArchive ID
    private final Map<String, BeanDeploymentArchive> idToBeanDeploymentArchive = new HashMap<>();
    private SimpleServiceRegistry simpleServiceRegistry;


    // Holds BeanDeploymentArchives created for extensions
    private final Map<ClassLoader, BeanDeploymentArchive> extensionBDAMap = new HashMap<>();

    private Iterable<Metadata<Extension>> extensions;

    private final Collection<EjbDescriptor> deployedEjbs = new LinkedList<>();
    private ArchiveFactory archiveFactory;

    private boolean earContextAppLibBdasProcessed;

    /**
     * Produce <code>BeanDeploymentArchive</code>s for this <code>Deployment</code> from information from the provided
     * <code>ReadableArchive</code>.
     */
    public DeploymentImpl(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext context, ArchiveFactory archiveFactory) {
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, CREATING_DEPLOYMENT_ARCHIVE, new Object[] { archive.getName() });
        }

        this.archiveFactory = archiveFactory;
        this.beanDeploymentArchives = new ArrayList<>();
        this.context = context;

        // Collect /lib Jar BDAs (if any) from the parent module.
        // If we've produced BDA(s) from any /lib jars, <code>return</code> as
        // additional BDA(s) will be produced for any subarchives (war/jar).
        libJarRootBdas = scanForLibJars(archive, ejbs, context);
        if (!isEmpty(libJarRootBdas)) {
            return;
        }

        createModuleBda(archive, ejbs, context);
    }

    @Override
    public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE, new Object[] { beanClass });
        }

        // Check if we have already created a bean archive for this bean class, and if so return it.

        for (BeanDeploymentArchive beanDeploymentArchive : beanDeploymentArchives) {
            if (LOG.isLoggable(FINE)) {
                LOG.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_CHECKING, new Object[] { beanClass, beanDeploymentArchive.getId() });
            }

            if (((BeanDeploymentArchiveImpl) beanDeploymentArchive).getModuleBeanClasses().contains(beanClass.getName())) {

                // Don't stuff this Bean Class into the BeanDeploymentArchive's beanClasses,
                // as Weld automatically add theses classes to the BeanDeploymentArchive's bean Classes
                if (LOG.isLoggable(FINE)) {
                    LOG.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_ADD_TO_EXISTING, new Object[] { beanClass.getName(), beanDeploymentArchive });
                }

                return beanDeploymentArchive;
            }

            // XXX: As of now, we handle one-level. Ideally, a bean deployment
            // descriptor is a composite and we should be able to search the tree
            // and get the right BDA for the beanClass
            if (!beanDeploymentArchive.getBeanDeploymentArchives().isEmpty()) {
                for (BeanDeploymentArchive subBeanDeploymentArchive : beanDeploymentArchive.getBeanDeploymentArchives()) {
                    Collection<String> moduleBeanClassNames = ((BeanDeploymentArchiveImpl) subBeanDeploymentArchive).getModuleBeanClasses();
                    if (LOG.isLoggable(FINE)) {
                        LOG.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_CHECKING_SUBBDA,
                                new Object[] { beanClass, subBeanDeploymentArchive.getId() });
                    }

                    if (moduleBeanClassNames.contains(beanClass.getName())) {

                        // Don't stuff this Bean Class into the BeanDeploymentArchive's beanClasses,
                        // as Weld automatically add theses classes to the BeanDeploymentArchive's bean Classes
                        if (LOG.isLoggable(FINE)) {
                            LOG.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_ADD_TO_EXISTING,
                                    new Object[] { beanClass.getName(), subBeanDeploymentArchive });
                        }

                        return subBeanDeploymentArchive;
                    }
                }
            }
        }

        ClassLoader classLoaderKey = null;
        if (System.getProperty(IS_EMBEDDED_EJB_CONTAINER) != null) {
            // In the embedded EJB container, all extension classes go to the same BDA.
            // This is needed since we don't have separate class loaders per archive there.
            classLoaderKey = this.getClass().getClassLoader();
        } else {
            classLoaderKey = beanClass.getClassLoader();
        }

        BeanDeploymentArchive extensionBeanDeploymentArchive = extensionBDAMap.get(classLoaderKey);
        if (extensionBeanDeploymentArchive != null) {
            return extensionBeanDeploymentArchive;
        }

        // If the beanDeploymentArchive was not found for the Class, create one and add it

        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_CREATE_NEW_BDA, new Object[] { beanClass });
        }

        BeanDeploymentArchive newBeanDeploymentArchive =
            new BeanDeploymentArchiveImpl(
                beanClass.getName(),
                new ArrayList<>(List.of(beanClass)),
                new CopyOnWriteArrayList<>(),
                new HashSet<>(),
                context);

        // If bean.xml explicitly says to ignore this archive, return
        // without adding the bean archive

        BeansXml beansXml = newBeanDeploymentArchive.getBeansXml();
        if (beansXml != null && beansXml.getBeanDiscoveryMode().equals(NONE)) {
            return null;
        }

        // Add the new BeanDeploymentArchive to all root BeanDeploymentArchives of this deployment.

        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_ADD_NEW_BDA_TO_ROOTS, new Object[] {});
        }

        // Add the new archive to all existing archives
        for (BeanDeploymentArchive beanDeploymentArchive : beanDeploymentArchives) {
            beanDeploymentArchive.getBeanDeploymentArchives().add(newBeanDeploymentArchive);
        }

        // Add the existing archives archives to the new archive
        newBeanDeploymentArchive.getBeanDeploymentArchives().addAll(beanDeploymentArchives);

        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_RETURNING_NEWLY_CREATED_BDA,
                    new Object[] { beanClass, newBeanDeploymentArchive });
        }

        beanDeploymentArchives.add(newBeanDeploymentArchive);
        idToBeanDeploymentArchive.put(newBeanDeploymentArchive.getId(), newBeanDeploymentArchive);
        extensionBDAMap.put(beanClass.getClassLoader(), newBeanDeploymentArchive);

        return newBeanDeploymentArchive;
    }

    @Override
    public ServiceRegistry getServices() {
        if (simpleServiceRegistry == null) {
            simpleServiceRegistry = new SimpleServiceRegistry();
        }

        return simpleServiceRegistry;
    }

    @Override
    public Iterable<Metadata<Extension>> getExtensions() {
        if (extensions != null) {
            return extensions;
        }

        List<Metadata<Extension>> extensionsList = new ArrayList<>();
        // Register org.jboss.weld.lite.extension.translator.LiteExtensionTranslator in order to be able to execute build compatible extensions
        // Note that we only register this if we discovered at least one implementation of BuildCompatibleExtension

        List<Class<? extends BuildCompatibleExtension>> buildExtensions = getBuildCompatibleExtensions();

        if (!buildExtensions.isEmpty()) {
            try {
                extensionsList.add(new MetadataImpl<>(new LiteExtensionTranslator(buildExtensions, Thread.currentThread().getContextClassLoader())));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (BeanDeploymentArchive beanDeploymentArchive : getBeanDeploymentArchives()) {
            if (!(beanDeploymentArchive instanceof RootBeanDeploymentArchive)) {
                ClassLoader classLoader = new FilteringClassLoader(((BeanDeploymentArchiveImpl) beanDeploymentArchive)
                    .getModuleClassLoaderForBDA());
                Iterable<Metadata<Extension>> classPathExtensions = context.getTransientAppMetaData(WELD_BOOTSTRAP, WeldBootstrap.class)
                    .loadExtensions(classLoader);

                if (classPathExtensions != null) {
                    for (Metadata<Extension> beanDeploymentArchiveExtension : classPathExtensions) {
                        extensionsList.add(beanDeploymentArchiveExtension);
                    }
                }
            }
        }

        return extensions = extensionsList;
    }

    @Override
    public List<BeanDeploymentArchive> getBeanDeploymentArchives() {
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, GET_BEAN_DEPLOYMENT_ARCHIVES, new Object[] { beanDeploymentArchives });
        }

        if (!beanDeploymentArchives.isEmpty()) {
            return beanDeploymentArchives;
        }

        return emptyList();
    }

    /**
     * Get a BeanDeploymentArchive for the specified beanClass
     *
     * @param beanClass The beanClass to get the BeanDeploymentArchive for.
     *
     * @return If the beanClass is in the archive represented by the BeanDeploymentArchive then return that BeanDeploymentArchive.
     * If the class is represented by more than one, than perform matching by classloader of the bean class
     * to return the appropriate one. Otherwise if the class loader of the beanClass matches the module class
     * loader of any of the root BeanDeploymentArchives then return that root BeanDeploymentArchive.
     * Otherwise return null.
     */
    @Override
    public BeanDeploymentArchive getBeanDeploymentArchive(Class<?> beanClass) {
        if (beanClass == null) {
            return null;
        }

        ClassLoader classLoader = beanClass.getClassLoader();

        for (BeanDeploymentArchive beanDeploymentArchive : beanDeploymentArchives) {
            BeanDeploymentArchiveImpl beanDeploymentArchiveImpl = (BeanDeploymentArchiveImpl) beanDeploymentArchive;
            if (beanDeploymentArchiveImpl.getKnownClasses().contains(beanClass.getName()) &&
                beanDeploymentArchiveImpl.getModuleClassLoaderForBDA().equals(classLoader)) {
                return beanDeploymentArchive;
            }
        }

        // Find a root BeanDeploymentArchive
        RootBeanDeploymentArchive rootBda = findRootBda(classLoader, ejbRootBdas);
        if (rootBda == null) {
            rootBda = findRootBda(classLoader, warRootBdas);
            if (rootBda == null) {
                rootBda = findRootBda(classLoader, libJarRootBdas);
                if (rootBda == null) {
                    rootBda = findRootBda(classLoader, rarRootBdas);
                }
            }
        }

        return rootBda;
    }

    @Override
    public String toString() {
        StringBuilder valBuff = new StringBuilder();
        for (BeanDeploymentArchive bda : getBeanDeploymentArchives()) {
            valBuff.append(bda.toString());
        }

        return valBuff.toString();
    }


    // #### Public methods

    /**
     * Produce <code>BeanDeploymentArchive</code>s for this <code>Deployment</code> from information from the provided
     * <code>ReadableArchive</code>. This method is called for subsequent modules after This <code>Deployment</code> has
     * been created.
     */
    public void scanArchive(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext context) {
        if (libJarRootBdas == null) {
            libJarRootBdas = scanForLibJars(archive, ejbs, context);
            if (!isEmpty(libJarRootBdas)) {
                return;
            }
        }

        this.context = context;
        createModuleBda(archive, ejbs, context);
    }

    /**
     * Build the accessibility relationship between <code>BeanDeploymentArchive</code>s for this <code>Deployment</code>.
     * This method must be called after all <code>Weld</code> <code>BeanDeploymentArchive</code>s have been produced for the
     * <code>Deployment</code>.
     */
    public void buildDeploymentGraph() {
        // Make jars accessible to each other - Example:
        //    /ejb1.jar <----> /ejb2.jar
        // If there are any application (/lib) jars, make them accessible

        if (ejbRootBdas != null) {
            for (RootBeanDeploymentArchive ejbRootBda : ejbRootBdas) {
                BeanDeploymentArchive ejbModuleBda = ejbRootBda.getModuleBda();

                boolean modifiedArchive = false;
                for (RootBeanDeploymentArchive otherEjbRootBda : ejbRootBdas) {
                    BeanDeploymentArchive otherEjbModuleBda = otherEjbRootBda.getModuleBda();
                    if (otherEjbModuleBda.getId().equals(ejbModuleBda.getId())) {
                        continue;
                    }
                    ejbRootBda.getBeanDeploymentArchives().add(otherEjbRootBda);
                    ejbRootBda.getBeanDeploymentArchives().add(otherEjbModuleBda);
                    ejbModuleBda.getBeanDeploymentArchives().add(otherEjbModuleBda);
                    modifiedArchive = true;
                }

                // Make /lib jars accessible to the ejbs.
                if (libJarRootBdas != null) {
                    for (RootBeanDeploymentArchive libJarRootBda : libJarRootBdas) {
                        BeanDeploymentArchive libJarModuleBda = libJarRootBda.getModuleBda();
                        ejbRootBda.getBeanDeploymentArchives().add(libJarRootBda);
                        ejbRootBda.getBeanDeploymentArchives().add(libJarModuleBda);
                        ejbModuleBda.getBeanDeploymentArchives().add(libJarRootBda);
                        ejbModuleBda.getBeanDeploymentArchives().add(libJarModuleBda);
                        modifiedArchive = true;
                    }
                }

                // Make rars accessible to ejbs
                if (rarRootBdas != null) {
                    for (RootBeanDeploymentArchive rarRootBda : rarRootBdas) {
                        BeanDeploymentArchive rarModuleBda = rarRootBda.getModuleBda();
                        ejbRootBda.getBeanDeploymentArchives().add(rarRootBda);
                        ejbRootBda.getBeanDeploymentArchives().add(rarModuleBda);
                        ejbModuleBda.getBeanDeploymentArchives().add(rarRootBda);
                        ejbModuleBda.getBeanDeploymentArchives().add(rarModuleBda);
                        modifiedArchive = true;
                    }
                }

                if (modifiedArchive) {
                    int idx = getBeanDeploymentArchives().indexOf(ejbModuleBda);
                    if (idx >= 0) {
                        getBeanDeploymentArchives().remove(idx);
                        getBeanDeploymentArchives().add(ejbModuleBda);
                    }
                }
            }
        }

        // Make jars (external to WAR modules) accessible to WAR BDAs - Example:
        //    /web.war ----> /ejb.jar
        // If there are any application (/lib) jars, make them accessible

        if (warRootBdas != null) {
            boolean modifiedArchive = false;
            for (RootBeanDeploymentArchive warRootBda : warRootBdas) {

                BeanDeploymentArchive warModuleBda = warRootBda.getModuleBda();
                if (ejbRootBdas != null) {
                    for (RootBeanDeploymentArchive ejbRootBda : ejbRootBdas) {
                        BeanDeploymentArchive ejbModuleBda = ejbRootBda.getModuleBda();
                        warRootBda.getBeanDeploymentArchives().add(ejbRootBda);
                        warRootBda.getBeanDeploymentArchives().add(ejbModuleBda);
                        warModuleBda.getBeanDeploymentArchives().add(ejbRootBda);
                        warModuleBda.getBeanDeploymentArchives().add(ejbModuleBda);

                        for (BeanDeploymentArchive oneBda : warModuleBda.getBeanDeploymentArchives()) {
                            oneBda.getBeanDeploymentArchives().add(ejbRootBda);
                            oneBda.getBeanDeploymentArchives().add(ejbModuleBda);
                        }

                        modifiedArchive = true;
                    }
                }

                // Make /lib jars accessible to the war and it's sub bdas
                if (libJarRootBdas != null) {
                    for (RootBeanDeploymentArchive libJarRootBda : libJarRootBdas) {
                        BeanDeploymentArchive libJarModuleBda = libJarRootBda.getModuleBda();
                        warRootBda.getBeanDeploymentArchives().add(libJarRootBda);
                        warRootBda.getBeanDeploymentArchives().add(libJarModuleBda);
                        warModuleBda.getBeanDeploymentArchives().add(libJarRootBda);
                        warModuleBda.getBeanDeploymentArchives().add(libJarModuleBda);

                        for (BeanDeploymentArchive oneBda : warModuleBda.getBeanDeploymentArchives()) {
                            oneBda.getBeanDeploymentArchives().add(libJarRootBda);
                            oneBda.getBeanDeploymentArchives().add(libJarModuleBda);
                        }

                        modifiedArchive = true;
                    }
                }

                // Make rars accessible to wars and it's sub bdas
                if (rarRootBdas != null) {
                    for (RootBeanDeploymentArchive rarRootBda : rarRootBdas) {
                        BeanDeploymentArchive rarModuleBda = rarRootBda.getModuleBda();
                        warRootBda.getBeanDeploymentArchives().add(rarRootBda);
                        warRootBda.getBeanDeploymentArchives().add(rarModuleBda);
                        warModuleBda.getBeanDeploymentArchives().add(rarRootBda);
                        warModuleBda.getBeanDeploymentArchives().add(rarModuleBda);

                        for (BeanDeploymentArchive oneBda : warModuleBda.getBeanDeploymentArchives()) {
                            oneBda.getBeanDeploymentArchives().add(rarRootBda);
                            oneBda.getBeanDeploymentArchives().add(rarModuleBda);
                        }

                        modifiedArchive = true;
                    }
                }

                if (modifiedArchive) {
                    int idx = getBeanDeploymentArchives().indexOf(warModuleBda);
                    if (idx >= 0) {
                        getBeanDeploymentArchives().remove(idx);
                        getBeanDeploymentArchives().add(warModuleBda);
                    }
                    modifiedArchive = false;
                }
            }
        }

        addDependentBdas();
    }

    public void cleanup() {
        if (ejbRootBdas != null) {
            ejbRootBdas.clear();
        }

        if (warRootBdas != null) {
            warRootBdas.clear();
        }

        if (libJarRootBdas != null) {
            libJarRootBdas.clear();
        }

        if (rarRootBdas != null) {
            rarRootBdas.clear();
        }

        if (idToBeanDeploymentArchive != null) {
            idToBeanDeploymentArchive.clear();
        }
    }

    public BeanDeploymentArchive getBeanDeploymentArchiveForArchive(String archiveId) {
        return idToBeanDeploymentArchive.get(archiveId);
    }

    public Iterator<RootBeanDeploymentArchive> getLibJarRootBdas() {
        if (libJarRootBdas == null) {
            return null;
        }

        return libJarRootBdas.iterator();
    }

    public Iterator<RootBeanDeploymentArchive> getRarRootBdas() {
        if (rarRootBdas == null) {
            return null;
        }

        return rarRootBdas.iterator();
    }

    public Collection<EjbDescriptor> getDeployedEjbs() {
        return deployedEjbs;
    }

    protected void addDeployedEjbs(Collection<EjbDescriptor> ejbs) {
        if (ejbs != null) {
            deployedEjbs.addAll(ejbs);
        }
    }



    // #### Private methods

    private List<Class<? extends BuildCompatibleExtension>> getBuildCompatibleExtensions() {
        return
            ServiceLoader.load(BuildCompatibleExtension.class, Thread.currentThread().getContextClassLoader())
                         .stream()
                         .map(Provider::get)
                         .map(e -> e.getClass())
                         .filter(e -> !e.isAnnotationPresent(SkipIfPortableExtensionPresent.class))
                         .collect(toList());
    }

    private void addBeanDeploymentArchives(RootBeanDeploymentArchive bda) {
        BDAType moduleBDAType = bda.getModuleBDAType();
        if (moduleBDAType.equals(BDAType.WAR)) {
            if (warRootBdas == null) {
                warRootBdas = new ArrayList<>();
            }
            warRootBdas.add(bda);
        } else if (moduleBDAType.equals(BDAType.JAR)) {
            if (ejbRootBdas == null) {
                ejbRootBdas = new ArrayList<>();
            }
            ejbRootBdas.add(bda);
        } else if (moduleBDAType.equals(BDAType.RAR)) {
            if (rarRootBdas == null) {
                rarRootBdas = new ArrayList<>();
            }
            rarRootBdas.add(bda);
        }
    }

    private void addDependentBdas() {
        Set<BeanDeploymentArchive> additionalBdas = new HashSet<>();
        for (BeanDeploymentArchive oneBda : beanDeploymentArchives) {
            BeanDeploymentArchiveImpl beanDeploymentArchiveImpl = (BeanDeploymentArchiveImpl) oneBda;
            Collection<BeanDeploymentArchive> subBdas = beanDeploymentArchiveImpl.getBeanDeploymentArchives();
            for (BeanDeploymentArchive subBda : subBdas) {
                if (subBda.getBeanClasses().size() > 0) {
                    // only add it if it's cdi-enabled (contains at least one bean that is managed by cdi)
                    additionalBdas.add(subBda);
                }
            }
        }

        for (BeanDeploymentArchive oneBda : additionalBdas) {
            if (!beanDeploymentArchives.contains(oneBda)) {
                beanDeploymentArchives.add(oneBda);
            }
        }
    }


    // This method creates and returns a List of BeanDeploymentArchives for each
    // Weld enabled jar under /lib of an existing Archive.
    private List<RootBeanDeploymentArchive> scanForLibJars(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext context) {
        final List<ReadableArchive> libJars = new ArrayList<>();
        final ApplicationHolder holder = context.getModuleMetaData(ApplicationHolder.class);
        if (holder != null && holder.app != null) {
            String libDir = holder.app.getLibraryDirectory();
            if (!isEmpty(libDir)) {
                Enumeration<String> entries = archive.entries(libDir);
                while (entries.hasMoreElements()) {
                    String entryName = entries.nextElement();

                    // If a jar is directly in lib dir and not WEB-INF/lib/foo/bar.jar
                    if (entryName.endsWith(JAR_SUFFIX) && entryName.indexOf(SEPARATOR_CHAR, libDir.length() + 1) == -1) {
                        try {
                            final ReadableArchive jarInLib = archive.getSubArchive(entryName);
                            if (jarInLib.exists(META_INF_BEANS_XML) || isImplicitBeanArchive(context, jarInLib)) {
                                libJars.add(jarInLib);
                            } else {
                                jarInLib.close();
                            }
                        } catch (IOException e) {
                            LOG.log(FINE, EXCEPTION_SCANNING_JARS, new Object[] { e });
                        }
                    }
                }
            }
        }

        if (holder == null || libJars.isEmpty()) {
            return libJarRootBdas;
        }
        String libDir = holder.app.getLibraryDirectory();
        for (ReadableArchive libJarArchive : libJars) {
            createLibJarBda(libJarArchive, ejbs, libDir);
        }
        return libJarRootBdas;
    }

    /**
     * @param libJarArchive - this method uses and closes the archive
     */
    private void createLibJarBda(ReadableArchive libJarArchive, Collection<EjbDescriptor> ejbs, String libDir) {
        createLibJarBda(
            new RootBeanDeploymentArchive(
                libJarArchive, ejbs, context,
                libDir + SEPARATOR_CHAR + libJarArchive.getName()));
    }

    private void createLibJarBda(RootBeanDeploymentArchive rootLibBda) {
        BeanDeploymentArchive libModuleBda = rootLibBda.getModuleBda();
        BeansXml moduleBeansXml = libModuleBda.getBeansXml();
        if (moduleBeansXml == null || !moduleBeansXml.getBeanDiscoveryMode().equals(NONE)) {
            addBdaToDeploymentBdas(rootLibBda);
            addBdaToDeploymentBdas(libModuleBda);
            if (libJarRootBdas == null) {
                libJarRootBdas = new ArrayList<>();
            }

            for (RootBeanDeploymentArchive existingLibJarRootBda : libJarRootBdas) {
                rootLibBda.getBeanDeploymentArchives().add(existingLibJarRootBda);
                rootLibBda.getBeanDeploymentArchives().add(existingLibJarRootBda.getModuleBda());
                rootLibBda.getModuleBda().getBeanDeploymentArchives().add(existingLibJarRootBda);
                rootLibBda.getModuleBda().getBeanDeploymentArchives().add(existingLibJarRootBda.getModuleBda());

                existingLibJarRootBda.getBeanDeploymentArchives().add(rootLibBda);
                existingLibJarRootBda.getBeanDeploymentArchives().add(rootLibBda.getModuleBda());
                existingLibJarRootBda.getModuleBda().getBeanDeploymentArchives().add(rootLibBda);
                existingLibJarRootBda.getModuleBda().getBeanDeploymentArchives().add(rootLibBda.getModuleBda());
            }

            libJarRootBdas.add(rootLibBda);
        }
    }

    private RootBeanDeploymentArchive findRootBda(ClassLoader classLoader, List<RootBeanDeploymentArchive> rootBdas) {
        if (isAnyNull(rootBdas, classLoader)) {
            return null;
        }

        for (RootBeanDeploymentArchive rootBeanDeploymentArchive : rootBdas) {
            if (classLoader.equals(rootBeanDeploymentArchive.getModuleClassLoaderForBDA())) {
                return rootBeanDeploymentArchive;
            }
        }

        return null;
    }

    private void createModuleBda(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext context) {
        RootBeanDeploymentArchive rootBda = new RootBeanDeploymentArchive(archive, ejbs, context);

        BeanDeploymentArchive moduleBda = rootBda.getModuleBda();
        BeansXml moduleBeansXml = moduleBda.getBeansXml();
        if (moduleBeansXml == null || !moduleBeansXml.getBeanDiscoveryMode().equals(NONE)) {
            addBdaToDeploymentBdas(rootBda);
            addBdaToDeploymentBdas(moduleBda);
            addBeanDeploymentArchives(rootBda);
        }

        // First check if the parent is an ear and if so see if there are app libs defined there.
        if (!earContextAppLibBdasProcessed && context instanceof DeploymentContextImpl) {
            DeploymentContextImpl deploymentContext = (DeploymentContextImpl) context;
            DeploymentContext parentContext = deploymentContext.getParentContext();
            if (parentContext != null) {
                processBdasForAppLibs(parentContext.getSource(), parentContext);
                parentContext.getSource();
                earContextAppLibBdasProcessed = true;
            }
        }

        // then check the module
        processBdasForAppLibs(archive, context);
    }

    private void addBdaToDeploymentBdas(BeanDeploymentArchive bda) {
        if (!beanDeploymentArchives.contains(bda)) {
            beanDeploymentArchives.add(bda);
            idToBeanDeploymentArchive.put(bda.getId(), bda);
        }
    }

    // These are application libraries that reside outside of the ear.  They are usually specified by entries
    // in the manifest.
    //
    // To test this put a jar in domains/domain1/lib/applibs and in its manifest make sure it has something like:
    //                           Extension-Name: com.acme.extlib
    // In a war's manifest put in something like:
    //                           Extension-List: MyExtLib
    //                           MyExtLib-Extension-Name: com.acme.extlib
    private void processBdasForAppLibs(ReadableArchive archive, DeploymentContext context) {
        List<RootBeanDeploymentArchive> libBdas = new ArrayList<>();

        try {
            // Each appLib in context.getAppLibs is a URI of the form
            // "file:/glassfish/runtime/trunk/glassfish8/glassfish/domains/domain1/lib/applibs/mylib.jar"
            // parentArchiveAppLibs are the app libs in the manifest of the root archive and any embedded
            // archives.
            List<URI> rootArchiveAppLibs = context.getAppLibs();

            // Each appLib in getInstalledLibraries(archive) is a String of the form
            // "mylib.jar"
            // currentArchiveAppLibNames are the app libs from the manifest of only the current archive.
            // This may therefor be a subset of the rootArchiveAppLibs when the root archive has multiple
            // embedded libs with their own app lib references in their manifest.
            Set<String> currentArchiveAppLibNames = getInstalledLibraries(archive);

            if (!isAnyEmpty(rootArchiveAppLibs, currentArchiveAppLibNames)) {
                for (URI rootArchiveAppLib : rootArchiveAppLibs) {
                    for (String currentArchiveAppLibName : currentArchiveAppLibNames) {
                        if (rootArchiveAppLib.getPath().endsWith(currentArchiveAppLibName)) {
                            ReadableArchive libArchive = null;
                            try {
                                libArchive = archiveFactory.openArchive(rootArchiveAppLib);
                                if (libArchive.exists(META_INF_BEANS_XML)) {
                                    libBdas.add(new RootBeanDeploymentArchive(
                                        libArchive,
                                        emptyList(),
                                        context,
                                        archive.getName() + "_" + libArchive.getName()));
                                }
                            } finally {
                                if (libArchive != null) {
                                    try {
                                        libArchive.close();
                                    } catch (Exception ignore) {
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (URISyntaxException | IOException e) {
            //todo: log error
        }

        for (RootBeanDeploymentArchive libBeanDeploymentArchive : libBdas) {
            createLibJarBda(libBeanDeploymentArchive);
        }
    }
}
