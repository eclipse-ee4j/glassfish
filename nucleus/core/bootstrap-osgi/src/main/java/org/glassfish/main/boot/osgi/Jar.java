/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.boot.osgi;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * This class is used to cache vital information of a bundle or a jar file
 * that is used during later processing. It also overrides hashCode and
 * equals methods so that it can be used in various Set operations.
 * It uses file's path as the primary key.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class Jar {

    private final URI uri;
    private final long lastModified;
    private final long bundleId;

    Jar(File file) {
        // Convert to a URI because the location of a bundle
        // is typically a URI. At least, that's the case for
        // autostart bundles.
        // No need to normalize, because file.toURI() removes unnecessary slashes
        // /tmp/foo and /tmp//foo differently.
        uri = file.toURI();
        lastModified = file.lastModified();
        bundleId = -1L;
    }


    Jar(Bundle b) throws URISyntaxException {
        // Convert to a URI because the location of a bundle
        // is typically a URI. At least, that's the case for
        // autostart bundles.
        // Normalisation is needed to ensure that we don't treat (e.g.)
        // /tmp/foo and /tmp//foo differently.
        String location = b.getLocation();
        if (location != null && !location.equals(Constants.SYSTEM_BUNDLE_LOCATION)) {
            uri = new URI(b.getLocation()).normalize();
        } else {
            uri = null;
        }

        lastModified = b.getLastModified();
        bundleId = b.getBundleId();
    }


    Jar(URI uri) {
        this.uri = uri.normalize();
        long localLastModified = -1L;
        bundleId = -1L;

        try {
            File f = new File(uri);
            localLastModified = f.lastModified();
        } catch (Exception e) {
            // can't help
        }

        lastModified = localLastModified;
    }


    public URI getURI() {
        return uri;
    }


    public String getPath() {
        return uri == null ? null : uri.getPath();
    }


    public long getLastModified() {
        return lastModified;
    }


    public long getBundleId() {
        return bundleId;
    }


    public boolean isNewer(Jar other) {
        return (getLastModified() > other.getLastModified());
    }


    // Override hashCode and equals as this object is used in Set
    @Override
    public int hashCode() {
        return uri == null ? 0 : uri.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Jar)) {
            return false;
        }

        Jar other = (Jar) obj;

        if (uri == null) {
            if (other.uri == null) {
                return true;
            }
            return false;
        }
        if (other.uri == null) {
            return false;
        }

        // For optimization reason, we use toString.
        // It works, as we anyway use normalize()
        return uri.toString().equals(other.uri.toString());
    }
}
