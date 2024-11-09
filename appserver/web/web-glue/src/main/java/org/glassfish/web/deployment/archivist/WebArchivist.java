/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.web.deployment.archivist;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFor;
import com.sun.enterprise.deployment.archivist.ExtensionsArchivist;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.DOLUtils;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WarArchiveType;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.annotation.impl.WarScanner;
import org.glassfish.web.deployment.descriptor.OrderingDescriptor;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.deployment.descriptor.WebFragmentDescriptor;
import org.glassfish.web.deployment.io.WebDeploymentDescriptorFile;
import org.glassfish.web.deployment.util.WebBundleValidator;
import org.jvnet.hk2.annotations.Service;
import org.xml.sax.SAXException;


/**
 * This module is responsible for reading and write web applications
 * archive files (war).
 *
 * @author Jerome Dochez
 */
@Service
@PerLookup
@ArchivistFor(WarArchiveType.ARCHIVE_TYPE)
public class WebArchivist extends Archivist<WebBundleDescriptorImpl> {

    private static final Logger logger = LogFacade.getLogger();


    private static final String DEFAULT_WEB_XML = "default-web.xml";

    @Inject
    private ServerEnvironment env;

    private WebBundleDescriptorImpl defaultWebXmlBundleDescriptor;

    /**
     * @return the  module type handled by this archivist
     * as defined in the application DTD
     *
     */
    @Override
    public ArchiveType getModuleType() {
        return DOLUtils.warType();
    }

    /**
     * Archivist read XML deployment descriptors and keep the
     * parsed result in the DOL descriptor instances. Sets the descriptor
     * for a particular Archivst type
     */
    public void setDescriptor(Application descriptor) {
        Set<WebBundleDescriptorImpl> webBundles = descriptor.getBundleDescriptors(WebBundleDescriptorImpl.class);
        if (!webBundles.isEmpty()) {
            this.descriptor = webBundles.iterator().next();
            if (!this.descriptor.getModuleDescriptor().isStandalone()) {
                this.descriptor = null;
            }
        }
    }

    /**
     * @return the DeploymentDescriptorFile responsible for handling
     * standard deployment descriptor
     */
    @Override
    public DeploymentDescriptorFile<WebBundleDescriptorImpl> getStandardDDFile() {
        if (standardDD == null) {
            standardDD = new WebDeploymentDescriptorFile();
        }
        return standardDD;
    }

    /**
     * @return the list of the DeploymentDescriptorFile responsible for
     *         handling the configuration deployment descriptors
     */
    @Override
    public List<ConfigurationDeploymentDescriptorFile> getConfigurationDDFiles() {
        if (confDDFiles == null) {
            confDDFiles = DOLUtils.getConfigurationDeploymentDescriptorFiles(habitat, WarArchiveType.ARCHIVE_TYPE);
        }
        return confDDFiles;
    }

    /**
     * @return a default BundleDescriptor for this archivist
     */
    @Override
    public WebBundleDescriptorImpl getDefaultBundleDescriptor() {
        return new WebBundleDescriptorImpl();
    }

    /**
     * @return a validated WebBundleDescriptor corresponding to default-web.xml
     *         that can be used in webtier.
     */
    public synchronized WebBundleDescriptorImpl getDefaultWebXmlBundleDescriptor() {
        if (defaultWebXmlBundleDescriptor == null) {
            defaultWebXmlBundleDescriptor = getPlainDefaultWebXmlBundleDescriptor();
            WebBundleValidator validator = new WebBundleValidator();
            validator.accept(defaultWebXmlBundleDescriptor );
        }
        return defaultWebXmlBundleDescriptor ;
    }

    /**
     * After reading all the standard deployment descriptors, merge any
     * resource descriptors from EJB descriptors into the WebBundleDescriptor.
     *
     * @param descriptor the deployment descriptor for the module
     * @param archive the module archive
     * @param extensions map of extension archivists
     */
    @Override
    protected void postStandardDDsRead(WebBundleDescriptorImpl descriptor,
                ReadableArchive archive,
                Map<ExtensionsArchivist<?>, RootDeploymentDescriptor> extensions)
                throws IOException {
        for (RootDeploymentDescriptor rd : extensions.values()) {
            if (rd instanceof EjbBundleDescriptor) {
                EjbBundleDescriptor eb = (EjbBundleDescriptor) rd;
                descriptor.addJndiNameEnvironment(eb);
                for (EjbDescriptor ejb : eb.getEjbs()) {
                    ejb.notifyNewModule(descriptor);
                }
            }
        }
        super.postStandardDDsRead(descriptor, archive, extensions);
    }

    /**
     * @return a non-validated WebBundleDescriptor corresponding to default-web.xml
     */
    private WebBundleDescriptorImpl getPlainDefaultWebXmlBundleDescriptor() {
        WebBundleDescriptorImpl defaultWebBundleDesc = new WebBundleDescriptorImpl();
        try {
            // parse default-web.xml contents
            URL defaultWebXml = getDefaultWebXML();
            if (defaultWebXml == null) {
                return defaultWebBundleDesc;
            }
            try (InputStream fis = defaultWebXml.openStream()) {
                WebDeploymentDescriptorFile wddf = new WebDeploymentDescriptorFile();
                wddf.setXMLValidation(false);
                defaultWebBundleDesc.addWebBundleDescriptor(wddf.read(fis));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, LogFacade.ERROR_PARSING, e);
        }
        return defaultWebBundleDesc;
    }

    /**
     * Obtains the location of <tt>default-web.xml</tt>.
     * This allows subclasses to load the file from elsewhere.
     *
     * @return
     *      null if not found, in which case the default web.xml will not be read
     *      and <tt>web.xml</tt> in the applications need to have everything.
     */
    protected URL getDefaultWebXML() throws IOException {
        File file = new File(env.getConfigDirPath(), DEFAULT_WEB_XML);
        if (file.exists()) {
            return file.toURI().toURL();
        }
        return null;
    }


    /**
     * perform any post deployment descriptor reading action
     *
     * @param descriptor the deployment descriptor for the module
     * @param archive the module archive
     */
    @Override
    protected void postOpen(WebBundleDescriptorImpl descriptor, ReadableArchive archive)
        throws IOException
    {
        super.postOpen(descriptor, archive);
        postValidate(descriptor, archive);
    }

    /**
     * validates the DOL Objects associated with this archivist, usually
     * it requires that a class loader being set on this archivist or passed
     * as a parameter
     */
    @Override
    public void validate(ClassLoader aClassLoader) {
        ClassLoader cl = aClassLoader;
        if (cl == null) {
            cl = classLoader;
        }
        if (cl == null) {
            return;
        }
        descriptor.setClassLoader(cl);
        descriptor.visit(new WebBundleValidator());
    }


    /**
     * In the case of web archive, the super handles() method should be able
     * to make a unique identification.  If not, then the archive is definitely
     * not a war.
     */
    @Override
    protected boolean postHandles(ReadableArchive abstractArchive)
            throws IOException {
        return DeploymentUtils.isArchiveOfType(abstractArchive, getModuleType(), locator);
    }

    @Override
    protected String getArchiveExtension() {
        return WEB_EXTENSION;
    }

    /**
     * @return a list of libraries included in the archivist
     */
    @Override
    public Vector<String> getLibraries(Archive archive) {
        Enumeration<String> entries = archive.entries();
        if (entries == null) {
            return null;
        }

        Vector<String> libs = new Vector<>();
        while (entries.hasMoreElements()) {

            String entryName = entries.nextElement();
            if (!entryName.startsWith("WEB-INF/lib")) {
                // not in WEB-INF...
                continue;
            }
            if (entryName.endsWith(".jar")) {
                libs.add(entryName);
            }
        }
        return libs;
    }

    @Override
    protected void postAnnotationProcess(WebBundleDescriptorImpl descriptor,
            ReadableArchive archive) throws IOException {
        super.postAnnotationProcess(descriptor, archive);

        // read web-fragment.xml
        List<WebFragmentDescriptor> wfList = readStandardFragments(descriptor, archive);

        // process annotations in web-fragment
        // extension annotation processing will be done in top level
        if (isProcessAnnotation(descriptor)) {
            Map<ExtensionsArchivist<?>, RootDeploymentDescriptor> localExtensions = new HashMap<>();
            for (WebFragmentDescriptor wfDesc : wfList) {
                // if web.xml specifies metadata-complete=true,
                // all web fragment metadata-complete
                // should be overridden and be true also
                if (descriptor.isFullAttribute()) {
                    wfDesc.setFullAttribute(String.valueOf(descriptor.isFullAttribute()));
                }
                super.readAnnotations(archive, wfDesc, localExtensions);
            }

            // scan manifest classpath
            ModuleScanner<?> scanner = getScanner();
            if (scanner instanceof WarScanner) {
                ((WarScanner)scanner).setScanOtherLibraries(true);
                readAnnotations(archive, descriptor, localExtensions, scanner);
            }
        }

        WebFragmentDescriptor mergedWebFragment = null;
        for (WebFragmentDescriptor wf : wfList) {
            if (mergedWebFragment == null) {
                mergedWebFragment = wf;
            } else {
                mergedWebFragment.addWebBundleDescriptor(wf);
            }
        }

        if (mergedWebFragment != null) {
            descriptor.addWebBundleDescriptor(mergedWebFragment);
        }

        // apply default from default-web.xml to web.xml
        WebBundleDescriptorImpl defaultWebBundleDescriptor = getPlainDefaultWebXmlBundleDescriptor();
        descriptor.addDefaultWebBundleDescriptor(defaultWebBundleDescriptor);
    }

    /**
     * This method will return the list of web fragment in the desired order.
     */
    private List<WebFragmentDescriptor> readStandardFragments(WebBundleDescriptorImpl descriptor,
        ReadableArchive archive) throws IOException {
        List<WebFragmentDescriptor> wfList = new ArrayList<>();
        Vector<String> libs = getLibraries(archive);
        if (libs == null || libs.isEmpty()) {
            return wfList;
        }
        for (String lib : libs) {
            Archivist<?> wfArchivist = new WebFragmentArchivist(this, habitat);
            wfArchivist.setRuntimeXMLValidation(this.getRuntimeXMLValidation());
            wfArchivist.setRuntimeXMLValidationLevel(this.getRuntimeXMLValidationLevel());
            wfArchivist.setAnnotationProcessingRequested(false);

            final WebFragmentDescriptor wfDesc;
            try (ReadableArchive embeddedArchive = archive.getSubArchive(lib)) {
                if (embeddedArchive != null && wfArchivist.hasStandardDeploymentDescriptor(embeddedArchive)) {
                    try {
                        wfDesc = (WebFragmentDescriptor) wfArchivist.open(embeddedArchive);
                    } catch (SAXException ex) {
                        throw new IOException(ex);
                    }
                } else {
                    wfDesc = new WebFragmentDescriptor();
                }
            }
            wfDesc.setJarName(lib.substring(lib.lastIndexOf('/') + 1));
            wfList.add(wfDesc);
            descriptor.putJarNameWebFragmentNamePair(wfDesc.getJarName(), wfDesc.getName());
        }

        if (descriptor.getAbsoluteOrderingDescriptor() == null) {
            OrderingDescriptor.sort(wfList);
        } else {
            wfList = descriptor.getAbsoluteOrderingDescriptor().order(wfList);
        }

        for (WebFragmentDescriptor wf : wfList) {
            descriptor.addOrderedLib(wf.getJarName());
        }
        return wfList;
    }
}
