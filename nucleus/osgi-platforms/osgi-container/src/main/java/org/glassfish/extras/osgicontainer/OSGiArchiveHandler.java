/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.extras.osgicontainer;

import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.CompositeHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.deployment.archive.WritableArchiveEntry;
import org.glassfish.internal.deployment.GenericHandler;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * Archive Handler for OSGi modules.
 * This understands a special deployment property called UriScheme.
 * The value of this property must be a url scheme for which there is a URL handler currently registered in the JVM.
 * Any other deployment properties are treated as query parameters.
 * The rules are pretty much same as what's the case for webbundle url handler
 * as defined in OSGi Web Application spec except that the solution here is not limited to webbundle scheme.
 * Since the deployment properties are used as query parameters, they must be encoded such that they
 * conform to URL RFC 1738.
 *
 * @author Jerome Dochez
 * @author TangYong(tangyong@cn.fujitsu.com)
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = OSGiArchiveDetector.OSGI_ARCHIVE_TYPE)
@Singleton
public class OSGiArchiveHandler extends GenericHandler implements CompositeHandler {

    @LoggerInfo(subsystem = "OSGI", description="OSGI container logger", publish=true)
    private static final String LOGGER_NAME = "jakarta.enterprise.osgi.container";

    @LogMessagesResourceBundle()
    public static final String RB_NAME = "org.glassfish.extras.osgicontainer.LogMessages";

    private static Logger logger = Logger.getLogger(LOGGER_NAME, RB_NAME);

    @LogMessageInfo(message = "Decorated url = {0}", level="INFO")
    public static final String DECORATED_URL = "NCLS-OSGI-00001";


    @Inject
    private OSGiArchiveDetector detector;
    private final String URI_SCHEME_PROP_NAME = "UriScheme";
    private final char QUERY_PARAM_SEP = '&';
    private final String QUERY_DELIM = "?";
    private final String SCHEME_SEP = ":";


    @Override
    public String getArchiveType() {
        return OSGiArchiveDetector.OSGI_ARCHIVE_TYPE;
    }

    @Override
    public boolean accept(ReadableArchive source, String entryName) {
        // we hide everything so far.
        return false;
    }

    @Override
    public void initCompositeMetaData(DeploymentContext context) {
        // nothing to initialize
    }

    @Override
    public boolean handles(ReadableArchive archive) throws IOException {
        return detector.handles(archive);
    }

    @Override
    public ClassLoader getClassLoader(ClassLoader parent, DeploymentContext context) {
        return parent;
    }

    @Override
    public String getDefaultApplicationName(ReadableArchive archive,
                                            DeploymentContext context) {
        return getDefaultApplicationNameFromArchiveName(archive);
    }

    /**
     * Overriding the expand method of base class(GenericHandler) in order to
     * support allowing wrapping of non-OSGi bundles when --type=osgi option is
     * used in deploy command or GUI. Pl. see [GLASSFISH-16651]
     *
     * @param source  of the expanding
     * @param target  of the expanding
     * @param context deployment context
     * @throws IOException when the archive is corrupted
     */
    @Override
    public void expand(ReadableArchive source, WritableArchive target, DeploymentContext context) throws IOException {
        Properties props = context.getCommandParameters(DeployCommandParameters.class).properties;
        if ((props != null) && (props.containsKey(URI_SCHEME_PROP_NAME))) {
            // if UriScheme is specified, we need to construct a new URL based on user's input
            // and souce parameter and call openConnection() and getInputStream() on it.
            URL url = prepareUrl(context, props);
            logger.log(Level.INFO, DECORATED_URL, url);
            try (JarInputStream jis = new JarInputStream(url.openStream())) {
                expandJar(jis, target);
            }
        } else {
            super.expand(source, target, context);
        }
    }


    /**
     * This method creates a new URL based on user's input. The new URL is expected to be backed by a URL stream handler
     * that decorates the input stream. The general syntax to create the decoarated URI is:
     * newScheme:embeddedUri?query
     * e.g., when user's input is:
     * deploy --type osgi --properties
     *     UriScheme=webbundle:Bundle-SymbolicName=foo:Import-Package=jakarta.servlet:Web-ContextPath=/foo /tmp/foo.war
     * we create a new URI like this:
     * webbundle:file:/tmp/foo.war?Bundle-SymbolicName=foo&Import-Package=jakarta.servlet&Web-ContextPath=/foo
     *
     * Please note two things here:
     * a) We add the URI Scheme provided by user as a prefix.
     * b) We always add a ? at the end of embeddedUrl even when user has not provided any query params.
     * This strategy works really well as is proven by OSGi Web Applications spec.
     *
     * We expect the input to be already encoded.
     *
     * @param context DeploymentContext
     * @param props   properties passed in --properties argument of deploy command
     * @return a new URL which can be used to read the decorated content
     * @throws MalformedURLException
     */
    private URL prepareUrl(DeploymentContext context, Properties props)
            throws MalformedURLException {
        logger.logp(Level.FINE, "OSGiArchiveHandler", "prepareUrl", "Deployment properties = {0}", new Object[]{props});
        final String uriScheme = props.getProperty(URI_SCHEME_PROP_NAME);
        final URI embeddedUri = context.getOriginalSource().getURI();
        StringBuilder query = new StringBuilder();
        Enumeration<?> p = props.propertyNames();
        while (p.hasMoreElements()) {
            String key = (String) p.nextElement();
            if (URI_SCHEME_PROP_NAME.equalsIgnoreCase(key)) {
                continue; // separately taken care of
            }
            query.append(key);
            query.append("=");
            query.append(props.getProperty(key));
            query.append(QUERY_PARAM_SEP);
        }
        final int lastIdx = query.length() - 1;
        if (query.charAt(lastIdx) == QUERY_PARAM_SEP) {
            // Remove the trailing &
            query.deleteCharAt(lastIdx);
        }
        // We always add ? at the end of embeddedUri to indicate that's the end of embeddedUri
        String decoratedUriStr = uriScheme + SCHEME_SEP + embeddedUri + QUERY_DELIM + query;
        logger.logp(Level.FINE, "OSGiArchiveHandler", "prepareUrl", "Constructing a new URL from string [{0}]",
                new Object[]{decoratedUriStr});
        try {
            return new URI(decoratedUriStr).toURL(); // Calling new URI().toURL() performs appropriate decoding/encoding
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Populates a writable archive by reading the input JarInputStream.
     *
     * @param jis
     * @param target
     * @throws IOException
     */
    private void expandJar(JarInputStream jis, WritableArchive target) throws IOException {
        JarEntry je;
        while ((je = jis.getNextJarEntry()) != null) {
            try {
                if (je.isDirectory()) {
                    logger.logp(Level.FINER, "OSGiArchiveHandler", "expandJar",
                        "Skipping jar entry = {0} since this is of directiry type", new Object[] {je});
                    continue;
                }
                final String entryName = je.getName();
                final long entrySize = je.getSize();
                logger.logp(Level.FINER, "OSGiArchiveHandler", "expandJar", "Writing jar entry name = {0}, size = {1}",
                    new Object[] {entryName, entrySize});
                try (WritableArchiveEntry os = target.putNextEntry(entryName)) {
                    FileUtils.copy(jis, os);
                }
            } finally {
                jis.closeEntry();
            }
        }

        // Add MANIFEST File To Target and Write the MANIFEST File To Target
        Manifest m = jis.getManifest();
        if (m != null) {
            logger.logp(Level.FINER, "OSGiArchiveHandler", "expandJar", "Writing manifest entry");
            try (OutputStream os = target.putNextEntry(JarFile.MANIFEST_NAME)) {
                m.write(os);
            }
        }
    }

    /**
     * Returns whether this archive requires annotation scanning.
     *
     * @param archive file
     * @return whether this archive requires annotation scanning
     */
    @Override
    public boolean requiresAnnotationScanning(ReadableArchive archive) {
        return false;
    }
}
