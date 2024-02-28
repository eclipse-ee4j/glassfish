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

package org.glassfish.deployment.common;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * Records artifacts generated during deployment that need
 * to be included inside the generated app client JAR so they are accessible
 * on the runtime classpath.
 * <p>
 * An example: jaxrpc classes from web services deployment
 * <p>
 * Important node:  Artifacts added to this manager are NOT downloaded to
 * the client as separate files.  In contrast, they are added to the
 * generated client JAR file.  That generated JAR, along with other files needed
 * by the client, are downloaded.
 * <p>
 * A Deployer that needs to request for files to be downloaded to the client
 * as part of the payload in the http command response should instead use
 * DownloadableArtifactsManager.
 * <p>
 * The various {@code add} methods permit adding content in a variety of ways.
 * Ultimately we need to know two things: where is the physical file on the server
 * the content of which needs to be included in the client JAR, and what is the
 * relative path within the client JAR where the content should reside.  Look
 * carefully at the doc for each {@code add} method when choosing which to use.
 * <p>
 * An instance of this class can be stored in the deployment
 * context's transient app metadata so the various deployers can add to the
 * same collection and so the app client deployer can find it and
 * act on its contents.
 * <p>
 * Because other modules should add their artifacts before the the artifacts
 * have been consumed and placed into the client JAR file, the <code>add</code> methods do not permit
 * further additions once the {@link #artifacts} method has been invoked.
 *
 * FIXME This class is unable to control open resources and can cause IO leaks.
 *
 * @author tjuinn
 */
public class ClientArtifactsManager implements Closeable {

    private static final Logger LOG = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    @LogMessageInfo(message = "Artifact {0} identified for inclusion in app clients after one or more app clients were generated.", level="SEVERE",
            cause = "The application might specify that modules are to be processed in the order they appear in the application and an app client module appears before a module that creates an artifact to be included in app clients.",
            action = "Make sure, if the application specifies initialize-in-order as true, that the app clients appear after other modules which generated artifacts that should be accessible to app clients.")
    private static final String CLIENT_ARTIFACT_OUT_OF_ORDER = "NCLS-DEPLOYMENT-00025";

    @LogMessageInfo(message = "Artifact with relative path {0} expected at {1} but does not exist or cannot be read", level="SEVERE",
            cause = "The server is attempting to register an artifact to be included in the generated client JAR but the artifact does not exist or cannot be read",
            action = "This is an internal server error.  Please file a bug report.")
    private static final String CLIENT_ARTIFACT_MISSING = "NCLS-DEPLOYMENT-00026";

    @LogMessageInfo(message = "Artifact with relative path {0} from {1} collides with an existing artifact from file {2}", level="SEVERE",
            cause = "The server has created more than one artifact with the same relative path to be included in the generated client JAR file",
            action = "This is an internal server error.  Please file a bug report.")
    private static final String CLIENT_ARTIFACT_COLLISION = "NCLS-DEPLOYMENT-00027";

    private boolean isArtifactSetConsumed = false;

    private static final String CLIENT_ARTIFACTS_KEY = "ClientArtifacts";

    private final Map<URI, Artifacts.FullAndPartURIs> artifacts = new HashMap<>();

    /**
     * To verify sources that are JAR entries we need to make sure the
     * requested entry exists in the specified JAR file.  To optimize the
     * opening of JAR files we record the JARs previous checked
     * here.
     */
    private final Map<URI,JarFile> jarFiles = new HashMap<>();

    /**
     * Retrieves the client artifacts store from the provided deployment
     * context, creating one and storing it back into the DC if none is
     * there yet.
     *
     * @param dc the deployment context to hold the ClientArtifactsManager object
     * @return the ClientArtifactsManager object from the deployment context (created
     * and stored in the DC if needed)
     */
    public static ClientArtifactsManager get(final DeploymentContext dc) {
        synchronized (dc) {
            ClientArtifactsManager manager = dc.getTransientAppMetaData(CLIENT_ARTIFACTS_KEY, ClientArtifactsManager.class);
            if (manager == null) {
                manager = new ClientArtifactsManager();
                dc.addTransientAppMetaData(CLIENT_ARTIFACTS_KEY, manager);
            }
            return manager;
        }
    }

    /**
     * Adds a new artifact to the collection of artifacts to be included in the
     * client JAR file so they can be delivered to the client during a
     * download.
     *
     * @param baseURI absolute URI of the base directory within which the
     * artifact lies
     * @param artifactURI absolute or relative URI where the artifact file resides
     * @throws IllegalStateException if invokes after the accumulated artifacts have been consumed
     */
    public void add(final URI baseURI, final URI artifactURI) {
        final URIPair uris = new URIPair(baseURI, artifactURI);
        final Artifacts.FullAndPartURIs newArtifact =
                    new Artifacts.FullAndPartURIs(
                    uris.absoluteURI, uris.relativeURI);
        add(newArtifact);
    }

    /**
     * Adds a new artifact to the collection of artifacts to be added to the
     * client facade JAR file so they can be delivered to the client during a
     * download.
     * <p>
     * The relative path within the client JAR will be computed using the position
     * of the artifact file relative to the base file.
     *
     * @param baseFile File for the base directory within which the artifact lies
     * @param artifactFile File for the artifact itself
     * @throws IllegalStateException if invoked after the accumulated artifacts have been consumed
     */
    public void add(final File baseFile, final File artifactFile) {
        add(baseFile.toURI(), artifactFile.toURI());
    }

    /**
     * Adds a new artifact to the collection of artifacts to be added to the
     * client JAR file so they can be delivered to the client during a
     * download.
     * <p>
     * This method helps when the contents of a temporary file are to be included
     * in the client JAR, in which case the temp file might not reside in a
     * useful place relative to a base directory.  The caller can just specify
     * the relative path directly.
     *
     * @param artifactFile file to be included in the client JAR
     * @param relativePath relative path within the JAR where the file's contents should appear
     * @param isTemporary whether the artifact file is a temporary file or not
     */
    public void add(final File artifactFile, final String relativePath, final boolean isTemporary) {
        final Artifacts.FullAndPartURIs artifact = new Artifacts.FullAndPartURIs(artifactFile.toURI(), relativePath, isTemporary);
        add(artifact);
    }

    public boolean isEmpty() {
        return artifacts.isEmpty();
    }

    /**
     * Adds a new artifact to the collection of artifacts to be added to the
     * client JAR file so they can be delivered to the client during a download.
     * <p>
     * Note that the "full" part of the FullAndPartURIs object can be of the
     * form "jar:path-to-jar!entry-within-jar"
     *
     * @param artifact
     */
    public void add(Artifacts.FullAndPartURIs artifact) {
        LOG.log(Level.FINEST, "add(artifact={0})", artifact);
        if (isArtifactSetConsumed) {
            throw new IllegalStateException(
                formattedString(CLIENT_ARTIFACT_OUT_OF_ORDER, artifact.getFull().toASCIIString()));
        }
        Artifacts.FullAndPartURIs existingArtifact = artifacts.get(artifact.getPart());
        if (existingArtifact != null) {
            throw new IllegalArgumentException(
                    formattedString(CLIENT_ARTIFACT_COLLISION,
                        artifact.getPart().toASCIIString(),
                        artifact.getFull().toASCIIString(),
                        existingArtifact.getFull().toASCIIString())
                    );
        }
        // Verify at add-time that we can read the specified source.
        final String scheme = artifact.getFull().getScheme();
        if (scheme.equals("file")) {
            verifyFileArtifact(artifact);
        } else if (scheme.equals("jar")) {
            verifyJarEntryArtifact(artifact);
        } else {
            throw new IllegalArgumentException(artifact.getFull().toASCIIString() + " != [file,jar]");
        }
        artifacts.put(artifact.getPart(), artifact);
    }

    private void verifyFileArtifact(final Artifacts.FullAndPartURIs artifact) {
        final URI fullURI = artifact.getFull();
        final File f = new File(fullURI);
        if (!f.exists() || !f.canRead()) {
            throw new IllegalArgumentException(
                formattedString(CLIENT_ARTIFACT_MISSING, artifact.getPart().toASCIIString(), fullURI.toASCIIString()));
        }
    }

    private void verifyJarEntryArtifact(final Artifacts.FullAndPartURIs artifact) {
        final URI fullURI = artifact.getFull();
        final String ssp = fullURI.getSchemeSpecificPart();
        /*
         * See if we already have opened this JAR file.
         */
        JarFile jf = jarFiles.get(fullURI);
        if (jf == null) {
            try {
                final String jarFilePath = ssp.substring(0, ssp.indexOf("!/"));
                final URI jarURI = new URI(jarFilePath);
                final File jar = new File(jarURI);
                jf = new JarFile(jar);
                jarFiles.put(fullURI, jf);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        /*
         * Look for the specified entry.
         */
        final String entryName = ssp.substring(ssp.indexOf("!/") + 2);
        final JarEntry jarEntry = jf.getJarEntry(entryName);
        if (jarEntry == null) {
            throw new IllegalArgumentException(formattedString("enterprise.deployment.backend.appClientArtifactMissing",
                        artifact.getPart().toASCIIString(),
                        fullURI.toASCIIString())
                    );
        }
    }

    /**
     * Adds all artifacts in Collection to those to be added to the client
     * facade JAR.
     *
     * @param baseFile File for the base directory within which each artifact lies
     * @param artifactFiles Collection of File objects for the artifacts to be included
     * @throws IllegalStateException if invoked after the accumulated artifacts have been consumed
     */
    public void addAll(final File baseFile, final Collection<File> artifactFiles) {
        for (File f : artifactFiles) {
            add(baseFile, f);
        }
    }

    public boolean contains(final URI baseURI, final URI artifactURI) {
        return artifacts.containsKey(artifactURI);
    }

    public boolean contains(final File baseFile, final File artifactFile) {
        return contains(baseFile.toURI(), artifactFile.toURI());
    }

    /**
     * Returns the set (in unmodifiable form) of FullAndPartURIs for the
     * accumulated artifacts.
     * <p>
     * Note: Intended for use only by the app client deployer.
     *
     * @return all client artifacts reported by various deployers
     */
    public Collection<Artifacts.FullAndPartURIs> artifacts() {
        isArtifactSetConsumed = true;
        closeOpenedJARs();
        LOG.log(Level.FINE, "ClientArtifactsManager returned artifacts");
        return Collections.unmodifiableCollection(artifacts.values());
    }

    @Override
    public void close() {
        closeOpenedJARs();
    }

    private String formattedString(final String key, final Object... args) {
        final String format = LOG.getResourceBundle().getString(key);
        return MessageFormat.format(format, args);
    }

    private void closeOpenedJARs() {
        for (JarFile jarFile : jarFiles.values()) {
            try {
                jarFile.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        jarFiles.clear();
    }

    /**
     * Represents a pair of URIs for an artifact, one being the URI where
     * the file already exists and one for the relative URI where the content
     * should appear in the generated client JAR file.
     */
    private static class URIPair {
        private final URI relativeURI;
        private final URI absoluteURI;

        /**
         * Creates a new URIPair, computing the relative URI for the pair using
         * the artifact URI; if it's relative, just copy it and if it's absolute
         * then relativize it to the base URI to compute the relative URI.
         * @param baseURI
         * @param artifactURI
         */
        private URIPair(final URI baseURI, final URI artifactURI) {
            if (artifactURI.isAbsolute()) {
                absoluteURI = artifactURI;
                relativeURI = baseURI.relativize(absoluteURI);
            } else {
                relativeURI = artifactURI;
                absoluteURI = baseURI.resolve(relativeURI);
            }
        }
    }
}
