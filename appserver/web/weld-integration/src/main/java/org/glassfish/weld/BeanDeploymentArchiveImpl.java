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

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.SEVERE;
import static org.glassfish.cdi.CDILoggerInfo.ADD_BEAN_CLASS;
import static org.glassfish.cdi.CDILoggerInfo.ADD_BEAN_CLASS_ERROR;
import static org.glassfish.cdi.CDILoggerInfo.SETTING_CONTEXT_CLASS_LOADER;
import static org.glassfish.weld.WeldDeployer.WELD_BOOTSTRAP;
import static org.glassfish.weld.connector.WeldUtils.BEANS_XML_FILENAME;
import static org.glassfish.weld.connector.WeldUtils.CLASS_SUFFIX;
import static org.glassfish.weld.connector.WeldUtils.EXPANDED_RAR_SUFFIX;
import static org.glassfish.weld.connector.WeldUtils.JAR_SUFFIX;
import static org.glassfish.weld.connector.WeldUtils.META_INF_BEANS_XML;
import static org.glassfish.weld.connector.WeldUtils.RAR_SUFFIX;
import static org.glassfish.weld.connector.WeldUtils.SEPARATOR_CHAR;
import static org.glassfish.weld.connector.WeldUtils.WEB_INF_BEANS_XML;
import static org.glassfish.weld.connector.WeldUtils.WEB_INF_CLASSES;
import static org.glassfish.weld.connector.WeldUtils.WEB_INF_CLASSES_META_INF_BEANS_XML;
import static org.glassfish.weld.connector.WeldUtils.WEB_INF_LIB;
import static org.glassfish.weld.connector.WeldUtils.getCDIAnnotatedClassNames;
import static org.glassfish.weld.connector.WeldUtils.isImplicitBeanArchive;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.cdi.CDILoggerInfo;
import org.glassfish.weld.connector.WeldUtils;
import org.glassfish.weld.connector.WeldUtils.BDAType;
import org.glassfish.weld.ejb.EjbDescriptorImpl;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.ejb.spi.EjbDescriptor;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionTarget;

/*
 * The means by which Weld Beans are discovered on the classpath.
 */
public class BeanDeploymentArchiveImpl implements BeanDeploymentArchive {

    private Logger logger = Logger.getLogger(BeanDeploymentArchiveImpl.class.getName());

    private ReadableArchive archive;
    private String id;
    private List<String> moduleClassNames; // Names of classes in the module
    private List<String> beanClassNames; // Names of bean classes in the module
    private List<Class<?>> moduleClasses; // Classes in the module
    private List<Class<?>> beanClasses; // Classes identified as Beans through Weld SPI
    private List<URL> beansXmlURLs;
    private final Collection<EjbDescriptor<?>> ejbDescImpls;
    private List<BeanDeploymentArchive> beanDeploymentArchives;

    private SimpleServiceRegistry simpleServiceRegistry;

    private BDAType bdaType = BDAType.UNKNOWN;

    private DeploymentContext context;

    private final Map<AnnotatedType<?>, InjectionTarget<?>> itMap = new HashMap<>();

    // Workaround: WELD-781
    private ClassLoader moduleClassLoaderForBDA;

    private String friendlyId = "";

    private Collection<String> cdiAnnotatedClassNames;

    private boolean deploymentComplete;

    /**
     * Produce a <code>BeanDeploymentArchive</code> form information contained in the provided <code>ReadableArchive</code>.
     */
    public BeanDeploymentArchiveImpl(ReadableArchive archive, Collection<com.sun.enterprise.deployment.EjbDescriptor> ejbs, DeploymentContext ctx) {
        this(archive, ejbs, ctx, null);
    }

    public BeanDeploymentArchiveImpl(ReadableArchive archive, Collection<com.sun.enterprise.deployment.EjbDescriptor> ejbs, DeploymentContext ctx, String bdaID) {
        this.beanClasses = new ArrayList<>();
        this.beanClassNames = new ArrayList<>();
        this.moduleClasses = new ArrayList<>();
        this.moduleClassNames = new ArrayList<>();
        this.beansXmlURLs = new CopyOnWriteArrayList<>();
        this.archive = archive;
        if (bdaID == null) {
            this.id = archive.getName();
        } else {
            this.id = bdaID;
        }

        this.friendlyId = this.id;
        this.ejbDescImpls = new HashSet<>();
        this.beanDeploymentArchives = new ArrayList<>();
        this.context = ctx;

        populate(ejbs);
        populateEJBsForThisBDA(ejbs);
        try {
            this.archive.close();
        } catch (Exception e) {
        }
        this.archive = null;

        // This assigns moduleClassLoaderForBDA
        getClassLoader();
    }

    private void populateEJBsForThisBDA(Collection<com.sun.enterprise.deployment.EjbDescriptor> ejbs) {
        for (com.sun.enterprise.deployment.EjbDescriptor next : ejbs) {
            for (String className : moduleClassNames) {
                if (className.equals(next.getEjbClassName())) {
                    EjbDescriptorImpl wbEjbDesc = new EjbDescriptorImpl(next);
                    ejbDescImpls.add(wbEjbDesc);
                }
            }
        }
    }

    // These are for empty BDAs that do not model Bean classes in the current
    // deployment unit -- for example: BDAs for portable Extensions.
    public BeanDeploymentArchiveImpl(String id, List<Class<?>> wClasses, List<URL> beansXmlUrls, Collection<com.sun.enterprise.deployment.EjbDescriptor> ejbs, DeploymentContext ctx) {
        this.id = id;
        this.moduleClasses = wClasses;
        this.beanClasses = new ArrayList<>(wClasses);

        this.moduleClassNames = new ArrayList<>();
        this.beanClassNames = new ArrayList<>();
        for (Class<?> c : wClasses) {
            moduleClassNames.add(c.getName());
            beanClassNames.add(c.getName());
        }

        this.beansXmlURLs = beansXmlUrls;
        this.ejbDescImpls = new HashSet<>();
        this.beanDeploymentArchives = new ArrayList<>();
        this.context = ctx;

        populateEJBsForThisBDA(ejbs);

        // This assigns moduleClassLoaderForBDA
        getClassLoader();
    }

    @Override
    public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return beanDeploymentArchives;
    }

    @Override
    public Collection<String> getBeanClasses() {
        // This method is called during BeanDeployment.deployBeans, so this would
        // be the right time to place the module classloader for the BDA as the TCL
        if (logger.isLoggable(FINER)) {
            logger.log(FINER, SETTING_CONTEXT_CLASS_LOADER, new Object[] { this.id, this.moduleClassLoaderForBDA });
        }

        if (!isDeploymentComplete()) {
            // The TCL is unset at the end of deployment of CDI beans in WeldDeployer.event
            // XXX: This is a workaround for issue https://issues.jboss.org/browse/WELD-781.
            // Remove this as soon as the SPI comes in.
            Thread.currentThread().setContextClassLoader(this.moduleClassLoaderForBDA);
        }
        return beanClassNames;
    }

    public Collection<Class<?>> getBeanClassObjects() {
        return beanClasses;
    }

    public Collection<String> getModuleBeanClasses() {
        return beanClassNames;
    }

    public Collection<Class<?>> getModuleBeanClassObjects() {
        return moduleClasses;
    }

    public void addBeanClass(String beanClassName) {
        boolean added = false;
        for (String moduleClassName : moduleClassNames) {
            if (moduleClassName.equals(beanClassName)) {
                if (logger.isLoggable(FINE)) {
                    logger.log(FINE, ADD_BEAN_CLASS, new Object[] { moduleClassName, beanClassNames });
                }

                beanClassNames.add(moduleClassName);
                try {
                    beanClasses.add(getClassLoader().loadClass(moduleClassName));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                added = true;
            }
        }
        if (!added) {
            if (logger.isLoggable(FINE)) {
                logger.log(FINE, ADD_BEAN_CLASS_ERROR, new Object[] { beanClassName });
            }
        }
    }

    @Override
    public BeansXml getBeansXml() {
        WeldBootstrap weldBootstrap = context.getTransientAppMetaData(WELD_BOOTSTRAP, WeldBootstrap.class);
        if (beansXmlURLs.size() == 1) {
            return weldBootstrap.parse(beansXmlURLs.get(0));
        }

        // This method attempts to performs a merge, but loses some
        // information (e.g., version, bean-discovery-mode)
        return weldBootstrap.parse(beansXmlURLs);
    }

    /**
     * Gets a descriptor for each EJB
     *
     * @return the EJB descriptors
     */
    @Override
    public Collection<EjbDescriptor<?>> getEjbs() {
        return ejbDescImpls;
    }

    public EjbDescriptor<?> getEjbDescriptor(String ejbName) {
        EjbDescriptor<?> match = null;

        for (EjbDescriptor<?> ejbDescriptor : ejbDescImpls) {
            if (ejbDescriptor.getEjbName().equals(ejbName)) {
                match = ejbDescriptor;
                break;
            }
        }

        return match;
    }

    @Override
    public ServiceRegistry getServices() {
        if (simpleServiceRegistry == null) {
            simpleServiceRegistry = new SimpleServiceRegistry();
        }

        return simpleServiceRegistry;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getFriendlyId() {
        return this.friendlyId;
    }

    // A graphical representation of the BDA hierarchy to aid in debugging
    // and to provide a better representation of how Weld treats the deployed
    // archive.
    @Override
    public String toString() {
        String beanClassesString = getBeanClasses().size() > 0 ? getBeanClasses().toString() : "";
        String initVal = "|ID: " + getId() + ", bdaType= " + bdaType + ", accessibleBDAs #:" + getBeanDeploymentArchives().size() + ", "
                + formatAccessibleBDAs(this) + ", Bean Classes #: " + getBeanClasses().size() + "," + beanClassesString + ", ejbs="
                + getEjbs() + "\n";
        StringBuffer valBuff = new StringBuffer(initVal);

        Collection<BeanDeploymentArchive> bdas = getBeanDeploymentArchives();
        Iterator<BeanDeploymentArchive> iter = bdas.iterator();
        while (iter.hasNext()) {
            BeanDeploymentArchive bda = iter.next();
            BDAType embedBDAType = BDAType.UNKNOWN;
            if (bda instanceof BeanDeploymentArchiveImpl) {
                embedBDAType = ((BeanDeploymentArchiveImpl) bda).getBDAType();
            }
            String embedBDABeanClasses = bda.getBeanClasses().size() > 0 ? bda.getBeanClasses().toString() : "";
            String val = "|---->ID: " + bda.getId() + ", bdaType= " + embedBDAType.toString() + ", accessibleBDAs #:"
                    + bda.getBeanDeploymentArchives().size() + ", " + formatAccessibleBDAs(bda) + ", Bean Classes #: "
                    + bda.getBeanClasses().size() + "," + embedBDABeanClasses + ", ejbs=" + bda.getEjbs() + "\n";
            valBuff.append(val);
        }
        return valBuff.toString();
    }

    private String formatAccessibleBDAs(BeanDeploymentArchive bda) {
        StringBuilder sb = new StringBuilder("[");
        for (BeanDeploymentArchive accessibleBDA : bda.getBeanDeploymentArchives()) {
            if (accessibleBDA instanceof BeanDeploymentArchiveImpl) {
                sb.append(((BeanDeploymentArchiveImpl) accessibleBDA).getFriendlyId() + ",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public BDAType getBDAType() {
        return bdaType;
    }

    private void populate(Collection<com.sun.enterprise.deployment.EjbDescriptor> ejbs) {
        try {
            boolean webinfbda = false;
            boolean hasBeansXml = false;

            String beansXMLURL = null;
            if (archive.exists(WEB_INF_BEANS_XML)) {
                beansXMLURL = WEB_INF_BEANS_XML;
            }

            if (beansXMLURL == null && archive.exists(WEB_INF_CLASSES_META_INF_BEANS_XML)) {
                beansXMLURL = WEB_INF_CLASSES_META_INF_BEANS_XML;
            }

            if (beansXMLURL != null) {
                // Parse the descriptor to determine if CDI is disabled
                BeansXml beansXML = parseBeansXML(archive, beansXMLURL);
                BeanDiscoveryMode bdMode = beansXML.getBeanDiscoveryMode();
                if (!bdMode.equals(BeanDiscoveryMode.NONE)) {

                    webinfbda = true;

                    // If the mode is explicitly set to "annotated", then pretend there is no beans.xml
                    // to force the implicit behavior
                    hasBeansXml = !bdMode.equals(BeanDiscoveryMode.ANNOTATED);

                    if (logger.isLoggable(FINE)) {
                        logger.log(FINE, CDILoggerInfo.PROCESSING_BEANS_XML,
                                new Object[] { archive.getURI(), WEB_INF_BEANS_XML, WEB_INF_CLASSES_META_INF_BEANS_XML });
                    }
                } else {
                    addBeansXMLURL(archive, beansXMLURL);
                }
            } else if (archive.exists(WEB_INF_CLASSES)) { // If WEB-INF/classes exists, check for CDI beans there
                // Check WEB-INF/classes for CDI-enabling annotations
                URI webinfclasses = new File(context.getSourceDir().getAbsolutePath(), WEB_INF_CLASSES).toURI();
                if (WeldUtils.isImplicitBeanArchive(context, webinfclasses)) {
                    webinfbda = true;
                    if (logger.isLoggable(FINE)) {
                        logger.log(FINE, CDILoggerInfo.PROCESSING_CDI_ENABLED_ARCHIVE, new Object[] { archive.getURI() });
                    }
                }
            }

            if (webinfbda) {
                bdaType = BDAType.WAR;
                Enumeration<String> entries = archive.entries();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    if (legalClassName(entry)) {
                        if (entry.contains(WEB_INF_CLASSES)) {
                            //Workaround for incorrect WARs that bundle classes above WEB-INF/classes
                            //[See. GLASSFISH-16706]
                            entry = entry.substring(WEB_INF_CLASSES.length() + 1);
                        }
                        String className = filenameToClassname(entry);
                        try {
                            if (hasBeansXml || isCDIAnnotatedClass(className)) {
                                beanClassNames.add(className);
                                beanClasses.add(getClassLoader().loadClass(className));
                            }
                            moduleClassNames.add(className);
                        } catch (Throwable t) {
                            if (logger.isLoggable(Level.WARNING)) {
                                logger.log(Level.WARNING, CDILoggerInfo.ERROR_LOADING_BEAN_CLASS, new Object[] { className, t.toString() });
                            }
                        }
                    } else if (entry.endsWith(BEANS_XML_FILENAME)) {
                        addBeansXMLURL(archive, entry);
                    }
                }
                archive.close();
            }

            // If this archive has WEB-INF/lib entry..
            // Examine all jars;  If the examined jar has a META_INF/beans.xml:
            //  collect all classes in the jar archive
            //  beans.xml in the jar archive

            if (archive.exists(WEB_INF_LIB)) {
                if (logger.isLoggable(FINE)) {
                    logger.log(FINE, CDILoggerInfo.PROCESSING_WEB_INF_LIB, new Object[] { archive.getURI() });
                }
                bdaType = BDAType.WAR;
                Enumeration<String> entries = archive.entries(WEB_INF_LIB);
                List<ReadableArchive> weblibJarsThatAreBeanArchives = new ArrayList<>();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    //if directly under WEB-INF/lib
                    if (entry.endsWith(JAR_SUFFIX) && entry.indexOf(SEPARATOR_CHAR, WEB_INF_LIB.length() + 1) == -1) {
                        ReadableArchive weblibJarArchive = archive.getSubArchive(entry);
                        if (weblibJarArchive.exists(META_INF_BEANS_XML)) {
                            // Parse the descriptor to determine if CDI is disabled
                            BeansXml beansXML = parseBeansXML(weblibJarArchive, META_INF_BEANS_XML);
                            BeanDiscoveryMode bdMode = beansXML.getBeanDiscoveryMode();
                            if (!bdMode.equals(BeanDiscoveryMode.NONE)) {
                                if (logger.isLoggable(FINE)) {
                                    logger.log(FINE, CDILoggerInfo.WEB_INF_LIB_CONSIDERING_BEAN_ARCHIVE, new Object[] { entry });
                                }

                                if (!bdMode.equals(BeanDiscoveryMode.ANNOTATED) || isImplicitBeanArchive(context, weblibJarArchive)) {
                                    weblibJarsThatAreBeanArchives.add(weblibJarArchive);
                                }
                            }
                        } else // Check for classes annotated with qualified annotations
                        if (WeldUtils.isImplicitBeanArchive(context, weblibJarArchive)) {
                            if (logger.isLoggable(FINE)) {
                                logger.log(FINE, CDILoggerInfo.WEB_INF_LIB_CONSIDERING_BEAN_ARCHIVE, new Object[] { entry });
                            }
                            weblibJarsThatAreBeanArchives.add(weblibJarArchive);
                        } else {
                            if (logger.isLoggable(FINE)) {
                                logger.log(FINE, CDILoggerInfo.WEB_INF_LIB_SKIPPING_BEAN_ARCHIVE, new Object[] { archive.getName() });
                            }
                        }
                    }
                }

                //process all web-inf lib JARs and create BDAs for them
                List<BeanDeploymentArchiveImpl> webLibBDAs = new ArrayList<>();
                if (weblibJarsThatAreBeanArchives.size() > 0) {
                    ListIterator<ReadableArchive> libJarIterator = weblibJarsThatAreBeanArchives.listIterator();
                    while (libJarIterator.hasNext()) {
                        ReadableArchive libJarArchive = libJarIterator.next();
                        BeanDeploymentArchiveImpl wlbda = new BeanDeploymentArchiveImpl(libJarArchive, ejbs, context,
                                WEB_INF_LIB + SEPARATOR_CHAR + libJarArchive.getName() /* Use WEB-INF/lib/jarName as BDA Id*/);
                        this.beanDeploymentArchives.add(wlbda); //add to list of BDAs for this WAR
                        webLibBDAs.add(wlbda);
                    }
                }
                ensureWebLibJarVisibility(webLibBDAs);
            } else if (archive.getName().endsWith(RAR_SUFFIX) || archive.getName().endsWith(EXPANDED_RAR_SUFFIX)) {
                //Handle RARs. RARs are packaged differently from EJB-JARs or WARs.
                //see 20.2 of Connectors 1.6 specification
                //The resource adapter classes are in a jar file within the
                //RAR archive
                bdaType = BDAType.RAR;
                collectRarInfo(archive);
            } else if (archive.exists(META_INF_BEANS_XML)) {
                // Parse the descriptor to determine if CDI is disabled
                BeansXml beansXML = parseBeansXML(archive, META_INF_BEANS_XML);
                BeanDiscoveryMode bdMode = beansXML.getBeanDiscoveryMode();
                if (!bdMode.equals(BeanDiscoveryMode.NONE)) {

                    if (logger.isLoggable(FINE)) {
                        logger.log(FINE, CDILoggerInfo.PROCESSING_BDA_JAR, new Object[] { archive.getURI() });
                    }
                    bdaType = BDAType.JAR;
                    collectJarInfo(archive, true, !bdMode.equals(BeanDiscoveryMode.ANNOTATED));
                } else {
                    addBeansXMLURL(archive, META_INF_BEANS_XML);
                }
            } else if (WeldUtils.isImplicitBeanArchive(context, archive)) {
                if (logger.isLoggable(FINE)) {
                    logger.log(FINE, CDILoggerInfo.PROCESSING_BECAUSE_SCOPE_ANNOTATION, new Object[] { archive.getURI() });
                }
                bdaType = BDAType.JAR;
                collectJarInfo(archive, true, false);
            }

            // This is causing tck failures, specifically
            // MultiModuleProcessingTest.testProcessedModulesCount
            // creating a bda for an extionsion that does not include a beans.xml is handled later
            // when annotated types are created by that extension.  This is done in
            // DeploymentImpl.loadBeanDeploymentArchive(Class<?> beanClass)
            //            if (archive.exists(META_INF_SERVICES_EXTENSION)){
            //                if ( logger.isLoggable( FINE ) ) {
            //                    logger.log(FINE, "-JAR processing: " + archive.getURI()
            //                            + " as an extensions jar since it has META-INF/services extension");
            //                }
            //                bdaType = BDAType.UNKNOWN;
            //                collectJarInfo(archive, false);
            //            }

        } catch (IOException | ClassNotFoundException cne) {
            logger.log(SEVERE, cne.getLocalizedMessage(), cne);
        }
    }

    private void ensureWebLibJarVisibility(List<BeanDeploymentArchiveImpl> webLibBDAs) {
        //ensure all web-inf/lib JAR BDAs are visible to each other
        for (int i = 0; i < webLibBDAs.size(); i++) {
            BeanDeploymentArchiveImpl firstBDA = webLibBDAs.get(i);
            boolean modified = false;
            //loop through the list once more
            for (BeanDeploymentArchiveImpl otherBDA : webLibBDAs) {
                if (!firstBDA.getId().equals(otherBDA.getId())) {
                    if (logger.isLoggable(FINE)) {
                        logger.log(FINE, CDILoggerInfo.ENSURE_WEB_LIB_JAR_VISIBILITY_ASSOCIATION,
                                new Object[] { firstBDA.getFriendlyId(), otherBDA.getFriendlyId() });
                    }
                    firstBDA.getBeanDeploymentArchives().add(otherBDA);
                    modified = true;
                }
            }
            //update modified BDA
            if (modified) {
                int idx = this.beanDeploymentArchives.indexOf(firstBDA);
                if (logger.isLoggable(FINE)) {
                    logger.log(FINE, CDILoggerInfo.ENSURE_WEB_LIB_JAR_VISIBILITY_ASSOCIATION_UPDATING,
                            new Object[] { firstBDA.getFriendlyId() });
                }
                if (idx >= 0) {
                    this.beanDeploymentArchives.set(idx, firstBDA);
                }
            }
        }

        //Include WAR's BDA in list of accessible BDAs of WEB-INF/lib jar BDA.
        for (int i = 0; i < webLibBDAs.size(); i++) {
            BeanDeploymentArchiveImpl subBDA = webLibBDAs.get(i);
            subBDA.getBeanDeploymentArchives().add(this);
            if (logger.isLoggable(FINE)) {
                logger.log(FINE, CDILoggerInfo.ENSURE_WEB_LIB_JAR_VISIBILITY_ASSOCIATION_INCLUDING,
                        new Object[] { subBDA.getId(), this.getId() });
            }
            int idx = this.beanDeploymentArchives.indexOf(subBDA);
            if (idx >= 0) {
                this.beanDeploymentArchives.set(idx, subBDA);
            }
        }
    }

    private void collectJarInfo(ReadableArchive archive, boolean isBeanArchive, boolean hasBeansXml)
            throws IOException, ClassNotFoundException {
        if (logger.isLoggable(FINE)) {
            logger.log(FINE, CDILoggerInfo.COLLECTING_JAR_INFO, new Object[] { archive.getURI() });
        }
        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entry = entries.nextElement();
            handleEntry(archive, entry, isBeanArchive, hasBeansXml);
        }
    }

    private void handleEntry(ReadableArchive archive, String entry, boolean isBeanArchive, boolean hasBeansXml)
            throws ClassNotFoundException {
        if (legalClassName(entry)) {
            String className = filenameToClassname(entry);
            try {
                if (isBeanArchive) {
                    // If the jar is a bean archive, or the individual class should be managed,
                    // based on its annotation(s)
                    if (hasBeansXml || isCDIAnnotatedClass(className)) {
                        beanClasses.add(getClassLoader().loadClass(className));
                        beanClassNames.add(className);
                    }
                }
                // Add the class as a module class
                moduleClassNames.add(className);
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, CDILoggerInfo.ERROR_LOADING_BEAN_CLASS, new Object[] { className, t.toString() });
                }
            }
        } else if (entry.endsWith("/beans.xml")) {
            try {
                // use a throwaway classloader to load the application's beans.xml
                ClassLoader throwAwayClassLoader = new URLClassLoader(new URL[] { archive.getURI().toURL() }, null);
                URL beansXmlUrl = throwAwayClassLoader.getResource(entry);
                if (beansXmlUrl != null && !beansXmlURLs.contains(beansXmlUrl)) { // http://java.net/jira/browse/GLASSFISH-17157
                    beansXmlURLs.add(beansXmlUrl);
                }
            } catch (MalformedURLException e) {
                if (logger.isLoggable(Level.SEVERE)) {
                    logger.log(Level.SEVERE, CDILoggerInfo.SEVERE_ERROR_READING_ARCHIVE, new Object[] { e.getMessage() });
                }
            }
        }
    }

    private boolean legalClassName(String className) {
        return className.endsWith(CLASS_SUFFIX) && !className.startsWith(WEB_INF_LIB);
    }

    private void collectRarInfo(ReadableArchive archive) throws IOException, ClassNotFoundException {
        if (logger.isLoggable(FINE)) {
            logger.log(FINE, CDILoggerInfo.COLLECTING_RAR_INFO, new Object[] { archive.getURI() });
        }
        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entry = entries.nextElement();
            if (entry.endsWith(JAR_SUFFIX)) {
                ReadableArchive jarArchive = archive.getSubArchive(entry);
                collectJarInfo(jarArchive, true, true);
            } else {
                handleEntry(archive, entry, true, true);
            }
        }
    }

    private static String filenameToClassname(String filename) {
        String className = null;
        if (filename.indexOf(File.separatorChar) >= 0) {
            className = filename.replace(File.separatorChar, '.');
        } else {
            className = filename.replace(SEPARATOR_CHAR, '.');
        }
        className = className.substring(0, className.length() - 6);
        return className;
    }

    private ClassLoader getClassLoader() {
        ClassLoader cl;
        if (this.context.getClassLoader() != null) {
            cl = this.context.getClassLoader();
        } else if (Thread.currentThread().getContextClassLoader() != null) {
            if (logger.isLoggable(FINE)) {
                logger.log(FINE, "Using TCL");
            }
            cl = Thread.currentThread().getContextClassLoader();
        } else {
            if (logger.isLoggable(FINE)) {
                logger.log(FINE, CDILoggerInfo.TCL_NULL);
            }
            cl = BeanDeploymentArchiveImpl.class.getClassLoader();
        }

        //cache the moduleClassLoader for this BDA
        this.moduleClassLoaderForBDA = cl;
        return cl;
    }

    public InjectionTarget<?> getInjectionTarget(AnnotatedType<?> annotatedType) {
        return itMap.get(annotatedType);
    }

    void putInjectionTarget(AnnotatedType<?> annotatedType, InjectionTarget<?> it) {
        itMap.put(annotatedType, it);
    }

    public ClassLoader getModuleClassLoaderForBDA() {
        return moduleClassLoaderForBDA;
    }

    /**
     * Determines whether the specified class is annotated with any CDI bean-defining annotations.
     *
     * @param className The name of the class to check
     *
     * @return true, if the specified class has one or more bean-defining annotations; Otherwise, false.
     */
    private boolean isCDIAnnotatedClass(String className) {
        if (cdiAnnotatedClassNames == null) {
            cdiAnnotatedClassNames = getCDIAnnotatedClassNames(context);
        }

        return cdiAnnotatedClassNames.contains(className);
    }

    protected BeansXml parseBeansXML(ReadableArchive archive, String beansXMLPath) throws IOException {
        WeldBootstrap weldBootstrap = context.getTransientAppMetaData(WELD_BOOTSTRAP, WeldBootstrap.class);
        return weldBootstrap.parse(getBeansXMLFileURL(archive, beansXMLPath));
    }

    private void addBeansXMLURL(ReadableArchive archive, String beansXMLPath) throws IOException {
        URL beansXmlUrl = getBeansXMLFileURL(archive, beansXMLPath);
        if (!beansXmlURLs.contains(beansXmlUrl)) {
            beansXmlURLs.add(beansXmlUrl);
        }
    }

    private URL getBeansXMLFileURL(ReadableArchive archive, String beansXMLPath) throws IOException {
        URL url = null;

        File file = new File(archive.getURI().getPath());
        if (file.isDirectory()) {
            file = new File(file, beansXMLPath);
            url = file.toURI().toURL();
        } else {
            url = new URL("jar:" + file.toURI() + "!/" + beansXMLPath);
        }

        return url;
    }

    public boolean isDeploymentComplete() {
        return deploymentComplete;
    }

    public void setDeploymentComplete(boolean deploymentComplete) {
        this.deploymentComplete = deploymentComplete;
    }
}
