/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.deployment.DeploymentContext;

/**
 * Records information about artifacts (files) that a deployer might need to
 * track.  For example, a deployer might generate artifacts as it runs, and these
 * might need to be cleaned up at some point.  Also, a deployer might need to
 * flag certain files for download to the client as part of "deploy --retrieve" or
 * "get-client-stubs."
 * <p>
 * Artifacts can be recorded into a DeploymentContext or into a Properties object.
 * Storing into a Properties object would normally be to store the Artifacts into
 * the application properties of an Application object so they can be persisted
 * into domain.xml.  The property names are
 * (keyPrefix)Artifact.(partURI) and the value is the corresponding fullURI.
 * <p>
 * Artifacts can also be optionally marked as temporary.  The intent is that
 * such artifacts will be deleted after they are placed into the response
 * payload for download.
 *
 * @author Tim Quinn
 */
public class Artifacts {

    public static final Logger deplLogger = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    /** the actual artifacts tracked - the part URI and the full URI */
    private final Set<FullAndPartURIs> artifacts = new HashSet<FullAndPartURIs>();

    /**
     * used as part of the key in getting/setting transient DC metadata and
     * in defining properties for each of the URI pairs
     */
    private final String keyPrefix;

    /**
     * Returns the Artifacts object from the deployment context with the
     * sepcified key prefix, creating a new one and storing it in the DC if
     * no matching Artifacts object already exists.
     * @param dc the deployment context
     * @param keyPrefix key prefix by which to look up or store the artifacts
     * @return
     */
    public static Artifacts get(
            final DeploymentContext dc,
            final String keyPrefix) {
        final String key = transientAppMetadataKey(keyPrefix);
        synchronized (dc) {
            Artifacts result = dc.getTransientAppMetaData(
                    transientAppMetadataKey(keyPrefix), Artifacts.class);

            if (result == null) {
                result = new Artifacts(keyPrefix);
                dc.addTransientAppMetaData(key, result);
            }
            return result;
        }
    }

    /**
     * Records the Artifacts object into the specified deployment context.
     * @param dc the DeploymentContent in which to persist the Artifacts object
     */
    public void record(final DeploymentContext dc) {
        synchronized (dc) {
            /*
             * Note that "addTransientAppMetaData" actually "puts" into a map,
             * so it's more like a "set" operation.
             */
            dc.addTransientAppMetaData(transientAppMetadataKey(), this);
        }
    }

    /**
     * Gets the artifacts matching the key prefix from the application properties
     * of the specified application.
     * @param app the application of interest
     * @param keyPrefix type of artifacts of interest (e.g., downloadable, generated)
     * @return
     */
    public static Artifacts get(
            final Properties props,
            final String keyPrefix) {
        final Artifacts result = new Artifacts(keyPrefix);

        for (String propName : props.stringPropertyNames()) {
            final String propNamePrefix = propNamePrefix(keyPrefix);
            if (propName.startsWith(propNamePrefix)) {
                /*
                 * The part URI is in the property name, after the keyPrefix and
                 * the separating dot.
                 */
                final URI fullURI = URI.create(props.getProperty(propName));
                result.addArtifact(fullURI, propName.substring(propNamePrefix.length()));
            }
        }
        return result;
    }

    private Artifacts(final String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    private static String propNamePrefix(final String keyPrefix) {
        return keyPrefix + "Artifact.";
    }

    private String propNamePrefix() {
        return propNamePrefix(keyPrefix);
    }

    /**
     * Adds an artifact.
     * @param full the full URI to the file to be tracked
     * @param part the (typically) relative URI to be associated with the part
     * @param isTemporary whether the artifact can be deleted once it is added to an output stream (typically for download)
     * (a frequent use of Artifacts is for working with Payloads which are
     * composed of parts - hence the "part" term)
     */
    public synchronized void addArtifact(URI full, URI part, boolean isTemporary) {
        FullAndPartURIs fullAndPart = new FullAndPartURIs(full, part, isTemporary);
        artifacts.add(fullAndPart);
        if (deplLogger.isLoggable(Level.FINE)) {
            deplLogger.log(Level.FINE, "Added {1} artifact: {0}",
                           new Object[] {fullAndPart, keyPrefix});
        }
    }

    /**
     * Adds an artifact.
     * @param full the full URI to the file to be tracked
     * @param part the (typically) relative URI, expressed as a String, to be
     * associated with the part
     */
    public synchronized void addArtifact(URI full, String part) {
        addArtifact(full, URI.create(part));
    }

    public synchronized void addArtifact(final URI full, final String part, final boolean isTemporary) {
        addArtifact(full, URI.create(part), isTemporary);
    }

    /**
     * Adds an artifact.
     * @param full
     * @param part
     */
    public synchronized void addArtifact(final URI full, final URI part) {
        addArtifact(full, part, false);
    }

    /**
     * Adds multiple artifacts at once.
     * @param urisCollection the URI pairs to add
     */
    public synchronized void addArtifacts(Collection<FullAndPartURIs> urisCollection) {
        artifacts.addAll(urisCollection);
        if (deplLogger.isLoggable(Level.FINE)) {
            deplLogger.log(Level.FINE, "Added downloadable artifacts: {0}", urisCollection);
        }
    }

    private static String transientAppMetadataKey(final String prefix) {
        return prefix + "Artifacts";
    }

    private String transientAppMetadataKey() {
        return transientAppMetadataKey(keyPrefix);
    }

    private String propName(final URI partURI) {
        return propNamePrefix() + partURI.toASCIIString();
    }

    private String propValue(final URI fullURI) {
        return fullURI.toASCIIString();
    }

    /**
     * Returns the URI pairs tracked by this Artifacts object.
     * @return
     */
    public synchronized Set<FullAndPartURIs> getArtifacts() {
        return artifacts;
    }

    /**
     * Records the artifacts in the provided Properties object.
     *
     * @param props
     * @throws URISyntaxException
     */
    public synchronized void record(
            final Properties props) throws URISyntaxException {
        for (Artifacts.FullAndPartURIs artifactInfo : artifacts) {
            props.setProperty(
                    propName(artifactInfo.getPart()),
                    propValue(artifactInfo.getFull()));
        }
    }

    /**
     * Clears the URI pairs recorded in this Artifacts object.
     */
    public synchronized void clearArtifacts() {
        artifacts.clear();
    }

    /**
     * Represents a file to be tracked (the full URI) and a relative URI to be
     * associated with that file if is to be downloaded (e.g., as a part in
     * a Payload).
     */
    public static class FullAndPartURIs {
        private final URI full;
        private final URI part;
        private final boolean isTemporary;

        public FullAndPartURIs(URI full, URI part) {
            this(full, part, false);
        }

        public FullAndPartURIs(URI full, String part) {
            this(full, part, false);
        }

        public FullAndPartURIs(URI full, String part, boolean isTemporary) {
            this(full, URI.create(part), isTemporary);
        }

        public FullAndPartURIs(URI full, URI part, boolean isTemporary) {
            this.full = full;
            this.part = part;
            this.isTemporary = isTemporary;
        }

        public URI getFull() {
            return full;
        }

        public URI getPart() {
            return part;
        }

        public boolean isTemporary() {
            return isTemporary;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FullAndPartURIs other = (FullAndPartURIs) obj;
            if (this.full != other.full && (this.full == null || !this.full.equals(other.full))) {
                return false;
            }
            if (this.part != other.part && (this.part == null || !this.part.equals(other.part))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + (this.full != null ? this.full.hashCode() : 0);
            hash = 29 * hash + (this.part != null ? this.part.hashCode() : 0);
            hash = 29 * hash + (isTemporary ? 0 : 1);
            return hash;
        }

        @Override
        public String toString() {
            return "full URI=" + full + "; part URI=" + part + "; isTemporary=" + isTemporary;
        }
    }
}
