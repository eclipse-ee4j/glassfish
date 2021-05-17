/*
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

import static java.util.Collections.emptyList;
import static java.util.logging.Level.FINE;
import static org.glassfish.cdi.CDILoggerInfo.GET_BEAN_DEPLOYMENT_ARCHIVES;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_ADD_TO_EXISTING;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_CHECKING;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_CHECKING_SUBBDA;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_CREATE_NEW_BDA;
import static org.glassfish.cdi.CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_RETURNING_NEWLY_CREATED_BDA;
import static org.glassfish.weld.WeldDeployer.WELD_BOOTSTRAP;
import static org.glassfish.weld.connector.WeldUtils.JAR_SUFFIX;
import static org.glassfish.weld.connector.WeldUtils.META_INF_BEANS_XML;
import static org.glassfish.weld.connector.WeldUtils.SEPARATOR_CHAR;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.cdi.CDILoggerInfo;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.deployment.common.InstalledLibrariesResolver;
import org.glassfish.javaee.core.deployment.ApplicationHolder;
import org.glassfish.weld.connector.WeldUtils;
import org.glassfish.weld.connector.WeldUtils.BDAType;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.CDI11Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.EjbDescriptor;

import jakarta.enterprise.inject.spi.Extension;

/*
 * Represents a deployment of a CDI (Weld) application.
 */
public class DeploymentImpl implements CDI11Deployment {

    // Keep track of our BDAs for this deployment
    private List<RootBeanDeploymentArchive> rarRootBdas;
    private List<RootBeanDeploymentArchive> ejbRootBdas;
    private List<RootBeanDeploymentArchive> warRootBdas;
    private List<RootBeanDeploymentArchive> libJarRootBdas;

    private List<BeanDeploymentArchive> beanDeploymentArchives;
    private DeploymentContext context;

    // A convenience Map to get BDA for a given BDA ID
    private Map<String, BeanDeploymentArchive> idToBeanDeploymentArchive = new HashMap<>();
    private SimpleServiceRegistry simpleServiceRegistry = null;

    private Logger logger = CDILoggerInfo.getLogger();

    // holds BDA's created for extensions
    private Map<ClassLoader, BeanDeploymentArchive> extensionBDAMap = new HashMap<>();

    private Iterable<Metadata<Extension>> extensions;

    private Collection<EjbDescriptor> deployedEjbs = new LinkedList<>();
    private ArchiveFactory archiveFactory;

    private boolean earContextAppLibBdasProcessed;

    /**
     * Produce <code>BeanDeploymentArchive</code>s for this <code>Deployment</code> from information from the provided
     * <code>ReadableArchive</code>.
     */
    public DeploymentImpl(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext context,
            ArchiveFactory archiveFactory) {
        if (logger.isLoggable(FINE)) {
            logger.log(FINE, CDILoggerInfo.CREATING_DEPLOYMENT_ARCHIVE, new Object[] { archive.getName() });
        }
        this.archiveFactory = archiveFactory;
        this.beanDeploymentArchives = new ArrayList<>();
        this.context = context;

        // Collect /lib Jar BDAs (if any) from the parent module.
        // If we've produced BDA(s) from any /lib jars, <code>return</code> as
        // additional BDA(s) will be produced for any subarchives (war/jar).
        libJarRootBdas = scanForLibJars(archive, ejbs, context);
        if (libJarRootBdas != null && libJarRootBdas.size() > 0) {
            return;
        }

        createModuleBda(archive, ejbs, context);
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

    /**
     * Produce <code>BeanDeploymentArchive</code>s for this <code>Deployment</code> from information from the provided
     * <code>ReadableArchive</code>. This method is called for subsequent modules after This <code>Deployment</code> has
     * been created.
     */
    public void scanArchive(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext context) {
        if (libJarRootBdas == null) {
            libJarRootBdas = scanForLibJars(archive, ejbs, context);
            if (libJarRootBdas != null && libJarRootBdas.size() > 0) {
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
            ListIterator<RootBeanDeploymentArchive> warIter = warRootBdas.listIterator();
            boolean modifiedArchive = false;
            while (warIter.hasNext()) {
                RootBeanDeploymentArchive warRootBda = warIter.next();
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

    @Override
    public List<BeanDeploymentArchive> getBeanDeploymentArchives() {
        if (logger.isLoggable(FINE)) {
            logger.log(FINE, GET_BEAN_DEPLOYMENT_ARCHIVES, new Object[] { beanDeploymentArchives });
        }
        if (!beanDeploymentArchives.isEmpty()) {
            return beanDeploymentArchives;
        }

        return emptyList();
    }

    @Override
    public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        if (logger.isLoggable(FINE)) {
            logger.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE, new Object[] { beanClass });
        }
        List<BeanDeploymentArchive> beanDeploymentArchives = getBeanDeploymentArchives();

        ListIterator<BeanDeploymentArchive> lIter = beanDeploymentArchives.listIterator();
        while (lIter.hasNext()) {
            BeanDeploymentArchive beanDeploymentArchive = lIter.next();
            if (logger.isLoggable(FINE)) {
                logger.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_CHECKING, new Object[] { beanClass, beanDeploymentArchive.getId() });
            }

            if (((BeanDeploymentArchiveImpl) beanDeploymentArchive).getModuleBeanClasses().contains(beanClass.getName())) {

                // Don't stuff this Bean Class into the BDA's beanClasses,
                // as Weld automatically add theses classes to the BDA's bean Classes
                if (logger.isLoggable(FINE)) {
                    logger.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_ADD_TO_EXISTING, new Object[] { beanClass.getName(), beanDeploymentArchive });
                }
                return beanDeploymentArchive;
            }

            // XXX: As of now, we handle one-level. Ideally, a bean deployment
            // descriptor is a composite and we should be able to search the tree
            // and get the right BDA for the beanClass
            if (beanDeploymentArchive.getBeanDeploymentArchives().size() > 0) {
                for (BeanDeploymentArchive subBeanDeploymentArchive : beanDeploymentArchive.getBeanDeploymentArchives()) {
                    Collection<String> moduleBeanClassNames = ((BeanDeploymentArchiveImpl) subBeanDeploymentArchive).getModuleBeanClasses();
                    if (logger.isLoggable(FINE)) {
                        logger.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_CHECKING_SUBBDA,
                                new Object[] { beanClass, subBeanDeploymentArchive.getId() });
                    }

                    if (moduleBeanClassNames.contains(beanClass.getName())) {

                        // Don't stuff this Bean Class into the BDA's beanClasses,
                        // as Weld automatically add theses classes to the BDA's bean Classes
                        if (logger.isLoggable(FINE)) {
                            logger.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_ADD_TO_EXISTING,
                                    new Object[] { beanClass.getName(), subBeanDeploymentArchive });
                        }

                        return subBeanDeploymentArchive;
                    }
                }
            }
        }

        BeanDeploymentArchive extensionBeanDeploymentArchive = extensionBDAMap.get(beanClass.getClassLoader());
        if (extensionBeanDeploymentArchive != null) {
            return extensionBeanDeploymentArchive;
        }

        // If the beanDeploymentArchive was not found for the Class, create one and add it
        if (logger.isLoggable(FINE)) {
            logger.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_CREATE_NEW_BDA, new Object[] { beanClass });
        }

        List<Class<?>> beanClasses = new ArrayList<>();
        List<URL> beanXMLUrls = new CopyOnWriteArrayList<>();
        Set<EjbDescriptor> ejbs = new HashSet<>();
        beanClasses.add(beanClass);

        BeanDeploymentArchive newBeanDeploymentArchive = new BeanDeploymentArchiveImpl(beanClass.getName(), beanClasses, beanXMLUrls, ejbs, context);
        BeansXml beansXml = newBeanDeploymentArchive.getBeansXml();
        if (beansXml == null || !beansXml.getBeanDiscoveryMode().equals(BeanDiscoveryMode.NONE)) {
            if (logger.isLoggable(FINE)) {
                logger.log(FINE, CDILoggerInfo.LOAD_BEAN_DEPLOYMENT_ARCHIVE_ADD_NEW_BDA_TO_ROOTS, new Object[] {});
            }

            lIter = beanDeploymentArchives.listIterator();
            while (lIter.hasNext()) {
                BeanDeploymentArchive bda = lIter.next();
                bda.getBeanDeploymentArchives().add(newBeanDeploymentArchive);
            }

            if (logger.isLoggable(FINE)) {
                logger.log(FINE, LOAD_BEAN_DEPLOYMENT_ARCHIVE_RETURNING_NEWLY_CREATED_BDA,
                        new Object[] { beanClass, newBeanDeploymentArchive });
            }

            beanDeploymentArchives.add(newBeanDeploymentArchive);
            idToBeanDeploymentArchive.put(newBeanDeploymentArchive.getId(), newBeanDeploymentArchive);
            extensionBDAMap.put(beanClass.getClassLoader(), newBeanDeploymentArchive);
            return newBeanDeploymentArchive;
        }

        return null;
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

        List<BeanDeploymentArchive> beanDeploymentArchives = getBeanDeploymentArchives();
        List<Metadata<Extension>> extnList = new ArrayList<>();
        for (BeanDeploymentArchive beanDeploymentArchive : beanDeploymentArchives) {
            if (!(beanDeploymentArchive instanceof RootBeanDeploymentArchive)) {
                ClassLoader moduleClassLoader = ((BeanDeploymentArchiveImpl) beanDeploymentArchive).getModuleClassLoaderForBDA();
                extensions = context.getTransientAppMetaData(WELD_BOOTSTRAP, WeldBootstrap.class)
                        .loadExtensions(moduleClassLoader);

                if (extensions != null) {
                    for (Metadata<Extension> beanDeploymentArchiveExtension : extensions) {
                        extnList.add(beanDeploymentArchiveExtension);
                    }
                }
            }
        }
        return extnList;
    }

    @Override
    public String toString() {
        StringBuffer valBuff = new StringBuffer();
        List<BeanDeploymentArchive> beanDeploymentArchives = getBeanDeploymentArchives();
        ListIterator<BeanDeploymentArchive> lIter = beanDeploymentArchives.listIterator();
        while (lIter.hasNext()) {
            BeanDeploymentArchive bda = lIter.next();
            valBuff.append(bda.toString());
        }
        return valBuff.toString();
    }

    public BeanDeploymentArchive getBeanDeploymentArchiveForArchive(String archiveId) {
        return idToBeanDeploymentArchive.get(archiveId);
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

    // This method creates and returns a List of BeanDeploymentArchives for each
    // Weld enabled jar under /lib of an existing Archive.
    private List<RootBeanDeploymentArchive> scanForLibJars(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext context) {
        List<ReadableArchive> libJars = null;
        ApplicationHolder holder = context.getModuleMetaData(ApplicationHolder.class);
        if (holder != null && holder.app != null) {
            String libDir = holder.app.getLibraryDirectory();
            if (libDir != null && !libDir.isEmpty()) {
                Enumeration<String> entries = archive.entries(libDir);
                while (entries.hasMoreElements()) {
                    String entryName = entries.nextElement();
                    // if a jar is directly in lib dir and not WEB-INF/lib/foo/bar.jar
                    if (entryName.endsWith(JAR_SUFFIX) && entryName.indexOf(SEPARATOR_CHAR, libDir.length() + 1) == -1) {
                        try {
                            ReadableArchive jarInLib = archive.getSubArchive(entryName);
                            if (jarInLib.exists(META_INF_BEANS_XML) || WeldUtils.isImplicitBeanArchive(context, jarInLib)) {
                                if (libJars == null) {
                                    libJars = new ArrayList<>();
                                }
                                libJars.add(jarInLib);
                            }
                        } catch (IOException e) {
                            logger.log(FINE, CDILoggerInfo.EXCEPTION_SCANNING_JARS, new Object[] { e });
                        }
                    }
                }
            }
        }

        if (libJars != null) {
            String libDir = holder.app.getLibraryDirectory();
            for (ReadableArchive libJarArchive : libJars) {
                createLibJarBda(libJarArchive, ejbs, libDir);
            }
        }

        return libJarRootBdas;
    }

    private void createLibJarBda(ReadableArchive libJarArchive, Collection<EjbDescriptor> ejbs, String libDir) {
        createLibJarBda(
            new RootBeanDeploymentArchive(
                libJarArchive, ejbs, context,
                libDir + SEPARATOR_CHAR + libJarArchive.getName()));
    }

    private void createLibJarBda(RootBeanDeploymentArchive rootLibBda) {
        BeanDeploymentArchive libModuleBda = rootLibBda.getModuleBda();
        BeansXml moduleBeansXml = libModuleBda.getBeansXml();
        if (moduleBeansXml == null || !moduleBeansXml.getBeanDiscoveryMode().equals(BeanDiscoveryMode.NONE)) {
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
            // each appLib in context.getAppLibs is a URI of the form "file:/glassfish/runtime/trunk/glassfish6/glassfish/domains/domain1/lib/applibs/mylib.jar"
            List<URI> appLibs = context.getAppLibs();

            Set<String> installedLibraries = InstalledLibrariesResolver.getInstalledLibraries(archive);
            if (appLibs != null && appLibs.size() > 0 && installedLibraries != null && installedLibraries.size() > 0) {
                for (URI oneAppLib : appLibs) {
                    for (String oneInstalledLibrary : installedLibraries) {
                        if (oneAppLib.getPath().endsWith(oneInstalledLibrary)) {
                            ReadableArchive libArchive = null;
                            try {
                                libArchive = archiveFactory.openArchive(oneAppLib);
                                if (libArchive.exists(WeldUtils.META_INF_BEANS_XML)) {
                                    String bdaId = archive.getName() + "_" + libArchive.getName();
                                    RootBeanDeploymentArchive rootBda = new RootBeanDeploymentArchive(libArchive,
                                            emptyList(), context, bdaId);
                                    libBdas.add(rootBda);
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

        for (RootBeanDeploymentArchive oneBda : libBdas) {
            createLibJarBda(oneBda);
        }
    }

    protected void addDeployedEjbs(Collection<EjbDescriptor> ejbs) {
        if (ejbs != null) {
            deployedEjbs.addAll(ejbs);
        }
    }

    public Collection<EjbDescriptor> getDeployedEjbs() {
        return deployedEjbs;
    }

    /**
     * Get a bda for the specified beanClass
     *
     * @param beanClass The beanClass to get the bda for.
     *
     * @return If the beanClass is in the archive represented by the bda then return that bda. Otherwise if the class loader
     * of the beanClass matches the module class loader of any of the root bdas then return that root bda. Otherwise return
     * null.
     */
    @Override
    public BeanDeploymentArchive getBeanDeploymentArchive(Class<?> beanClass) {
        if (beanClass == null) {
            return null;
        }

        for (BeanDeploymentArchive oneBda : beanDeploymentArchives) {
            BeanDeploymentArchiveImpl beanDeploymentArchiveImpl = (BeanDeploymentArchiveImpl) oneBda;
            if (beanDeploymentArchiveImpl.getBeanClassObjects().contains(beanClass)) {
                return oneBda;
            }
        }

        // find a root bda
        ClassLoader classLoader = beanClass.getClassLoader();

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

    private RootBeanDeploymentArchive findRootBda(ClassLoader classLoader, List<RootBeanDeploymentArchive> rootBdas) {
        if (rootBdas == null || classLoader == null) {
            return null;
        }

        for (RootBeanDeploymentArchive oneRootBda : rootBdas) {
            if (classLoader.equals(oneRootBda.getModuleClassLoaderForBDA())) {
                return oneRootBda;
            }
        }

        return null;
    }

    private void createModuleBda(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext context) {
        RootBeanDeploymentArchive rootBda = new RootBeanDeploymentArchive(archive, ejbs, context);

        BeanDeploymentArchive moduleBda = rootBda.getModuleBda();
        BeansXml moduleBeansXml = moduleBda.getBeansXml();
        if (moduleBeansXml == null || !moduleBeansXml.getBeanDiscoveryMode().equals(BeanDiscoveryMode.NONE)) {
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
}
