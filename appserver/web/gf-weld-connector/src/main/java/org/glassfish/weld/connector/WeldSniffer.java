/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.weld.connector;

import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.deployment.GenericSniffer;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINE;
import static org.glassfish.weld.connector.WeldUtils.EXPANDED_JAR_SUFFIX;
import static org.glassfish.weld.connector.WeldUtils.EXPANDED_RAR_SUFFIX;
import static org.glassfish.weld.connector.WeldUtils.JAR_SUFFIX;
import static org.glassfish.weld.connector.WeldUtils.META_INF_BEANS_XML;
import static org.glassfish.weld.connector.WeldUtils.SEPARATOR_CHAR;
import static org.glassfish.weld.connector.WeldUtils.WEB_INF;
import static org.glassfish.weld.connector.WeldUtils.WEB_INF_BEANS_XML;
import static org.glassfish.weld.connector.WeldUtils.WEB_INF_CLASSES_META_INF_BEANS_XML;
import static org.glassfish.weld.connector.WeldUtils.WEB_INF_LIB;
import static org.glassfish.weld.connector.WeldUtils.getBeanDiscoveryMode;
import static org.glassfish.weld.connector.WeldUtils.getBeansXmlInputStream;
import static org.glassfish.weld.connector.WeldUtils.getCDIEnablingAnnotations;
import static org.glassfish.weld.connector.WeldUtils.hasExtension;
import static org.glassfish.weld.connector.WeldUtils.isImplicitBeanArchive;
import static org.glassfish.weld.connector.WeldUtils.isImplicitBeanDiscoveryEnabled;
import static org.glassfish.weld.connector.WeldUtils.isValidBdaBasedOnExtensionAndBeansXml;

/**
 * Implementation of the Sniffer for Weld.
 */
@Service(name = "weld")
@Singleton
public class WeldSniffer extends GenericSniffer {

    private static final String[] containers = { "org.glassfish.weld.WeldContainer" };
    private static final Logger logger = Logger.getLogger(WeldSniffer.class.getName());

    public WeldSniffer() {
        // We do not haGenericSniffer(String containerName, String appStigma, String urlPattern
        super("weld", null /* appStigma */, null /* urlPattern */);
    }

    /**
    *
    * This API is used to help determine if the sniffer should recognize the current archive. If the sniffer does not
    * support the archive type associated with the current deployment, the sniffer should not recognize the archive.
    *
    * @param archiveType the archive type to check
    * @return whether the sniffer supports the archive type
    *
    */
   @Override
   public boolean supportsArchiveType(ArchiveType archiveType) {
       if (archiveType.toString().equals("war") || archiveType.toString().equals("ejb") || archiveType.toString().equals("rar")) {
           return true;
       }

       return false;
   }

    /**
     * Returns true if the archive contains beans.xml as defined by packaging rules of Weld
     */
    @Override
    public boolean handles(DeploymentContext context) {
        ArchiveType archiveType = habitat.getService(ArchiveType.class, context.getArchiveHandler().getArchiveType());
        if (archiveType != null && !supportsArchiveType(archiveType)) {
            return false;
        }

        ReadableArchive archive = context.getSource();

        boolean isWeldArchive = false;

        // Scan for beans.xml in expected locations. If at least one is found without bean-discovery-mode="none", this is
        // a Weld archive
        if (isEntryPresent(archive, WEB_INF)) {
            isWeldArchive =
                isArchiveCDIEnabled(context, archive, WEB_INF_BEANS_XML) ||
                isArchiveCDIEnabled(context, archive, WEB_INF_CLASSES_META_INF_BEANS_XML);

            if (!isWeldArchive) {
                // Check jars under WEB_INF/lib
                if (isEntryPresent(archive, WEB_INF_LIB)) {
                    isWeldArchive = scanLibDir(context, archive, WEB_INF_LIB);
                }
            }

            // Test for extension present.
            // The CDI 4.0 TCK introduced the requirement of war archive having an extension in
            // WEB-INF/classes/META-INF/services with no beans.xml being no BDA, but do need to
            // have the bean manager in JNDI.
            if (!isWeldArchive) {
                isWeldArchive = hasExtension(archive);
            }
        }

        // TODO This doesn't seem to match the ReadableArchive for a stand-alone ejb-jar.
        // It might only be true for an ejb-jar within an .ear. Revisit when officially
        // adding support for .ears
        String archiveName = archive.getName();
        if (!isWeldArchive && archiveName != null && archiveName.endsWith(EXPANDED_JAR_SUFFIX)) {
            isWeldArchive = isArchiveCDIEnabled(context, archive, META_INF_BEANS_XML);
        }

        // If stand-alone ejb-jar
        if (!isWeldArchive && isArchiveCDIEnabled(context, archive, META_INF_BEANS_XML)) {
            isWeldArchive = true;
        }

        if (!isWeldArchive && archiveName != null && archiveName.endsWith(EXPANDED_RAR_SUFFIX)) {
            isWeldArchive = isArchiveCDIEnabled(context, archive, META_INF_BEANS_XML);
            if (!isWeldArchive) {
                // Check jars in root dir of rar
                isWeldArchive = scanLibDir(context, archive, "");
            }
        }

        return isWeldArchive;
    }

    @Override
    public String[] getContainersNames() {
        return containers;
    }

    @Override
    public String[] getAnnotationNames(DeploymentContext context) {
        // First see if bean-discovery-mode is explicitly set to "none".
        InputStream beansXmlInputStream = getBeansXmlInputStream(context);
        if (beansXmlInputStream != null) {
            try {
                String beanDiscoveryMode = getBeanDiscoveryMode(beansXmlInputStream);
                if (beanDiscoveryMode.equals("none")) {
                    return null;
                }
            } finally {
                try {
                    beansXmlInputStream.close();
                } catch (IOException notignore) {
                    logger.log(FINE, "", notignore);
                }
            }
        }

        // Make sure it's not an extension
        if (!isValidBdaBasedOnExtensionAndBeansXml(context.getSource())) {
            return null;
        }

        return isImplicitBeanDiscoveryEnabled(context) ? getCDIEnablingAnnotations(context) : null;
    }


    // ### Private and protected methods

    private boolean scanLibDir(DeploymentContext context, ReadableArchive archive, String libLocation) {
        boolean entryPresent = false;

        if (libLocation != null) {
            Enumeration<String> entries = archive.entries(libLocation);
            while (entries.hasMoreElements() && !entryPresent) {
                String entryName = entries.nextElement();
                // If a jar in lib dir and not WEB-INF/lib/foo/bar.jar
                if (entryName.endsWith(JAR_SUFFIX) && entryName.indexOf(SEPARATOR_CHAR, libLocation.length() + 1) == -1) {
                    try (ReadableArchive jarInLib = archive.getSubArchive(entryName)) {
                        entryPresent = isArchiveCDIEnabled(context, jarInLib, META_INF_BEANS_XML);
                    } catch (IOException e) {
                        logger.log(FINE, "", e);
                    }
                }
            }
        }

        return entryPresent;
    }

    protected boolean isEntryPresent(ReadableArchive archive, String entry) {
        boolean entryPresent = false;
        try {
            entryPresent = archive.exists(entry);
        } catch (IOException e) {
            // do not ignore
            logger.log(FINE, "", e);
        }
        return entryPresent;
    }

    protected boolean isArchiveCDIEnabled(DeploymentContext context, ReadableArchive archive, String relativeBeansXmlPath) {
        String beanDiscoveryMode = null;
        InputStream beansXmlInputStream = null;
        try {
            beansXmlInputStream = archive.getEntry(relativeBeansXmlPath);
            if (beansXmlInputStream != null) {
                try {
                    beanDiscoveryMode = getBeanDiscoveryMode(beansXmlInputStream);
                } finally {
                    try {
                        beansXmlInputStream.close();
                    } catch (Exception notignore) {
                        logger.log(FINE, "", notignore);
                    }
                }
            }
        } catch (IOException ignore) {
        }

        if (beansXmlInputStream == null) {
            // No beans.xml
            try {
                return isImplicitBeanArchive(context, archive);
            } catch (IOException e) {
                return false;
            }
        }

        // There is a beans.xml.
        if (beanDiscoveryMode == null) {
            return true;
        }

        if (beanDiscoveryMode.equals("annotated") || beanDiscoveryMode.equals("all")) {
            return true;
        }

        if (beanDiscoveryMode.equals("none")) {
            // beanDiscoveryMode = none
            return false;
        }

        // Last case is beanDiscoveryMode = annotated
        try {
            return isImplicitBeanArchive(context, archive);
        } catch (IOException e) {
            return false;
        }
    }

}
