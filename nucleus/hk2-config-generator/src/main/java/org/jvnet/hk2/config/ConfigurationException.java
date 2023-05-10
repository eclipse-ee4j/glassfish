/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import javax.xml.stream.Location;

import org.glassfish.hk2.api.HK2RuntimeException;

/**
 * Indicates a problem in the configuration value.
 *
 * @author Kohsuke Kawaguchi
 */
public class ConfigurationException extends HK2RuntimeException {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -5739251253923553480L;

    private transient Location location = null;

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable origin) {
        super(message, origin);
    }

    public ConfigurationException(String format, Object... args) {
        super(String.format(format,args));
    }

    /**
     * Sets the location.
     *
     * This value is not set in the constructor so that we don't need to
     * carry around {@link Dom} to everywhere.
     */
    void setLocation(Location location) {
        assert this.location==null;
        this.location = location;
    }

    /**
     * Indicates the source position of the configuration file
     * where the problem happened.
     */
    public Location getLocation() {
        return location;
    }

    public String getMessage() {
        if(location==null)
            return super.getMessage();
        return String.format("%s at %s line %d",
            super.getMessage(), location.getSystemId(), location.getLineNumber());
    }
}
