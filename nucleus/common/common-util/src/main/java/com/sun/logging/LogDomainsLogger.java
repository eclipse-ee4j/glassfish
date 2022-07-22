/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.logging;

import java.util.ResourceBundle;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.GlassFishLogger;


/**
 * Reason for {@link ResourceBundle} management here - the Logger resource bundle resolution
 * is sensitive to caller's classloader. We also never change it.
 */
class LogDomainsLogger extends GlassFishLogger {

    private final ResourceBundle resourceBundle;

    LogDomainsLogger(final String loggerName, final ResourceBundle resourceBundle) {
        super(loggerName);
        this.resourceBundle = resourceBundle;
    }


    @Override
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }


    @Override
    public String getResourceBundleName() {
        return resourceBundle == null ? null : resourceBundle.getBaseBundleName();
    }


    @Override
    public void log(LogRecord record) {
        record.setResourceBundle(resourceBundle);
        super.log(record);
    }


    @Override
    public String toString() {
        return getClass().getName() + '@' + Integer.toHexString(hashCode())
            + "[name=" + getName() + ", level=" + getLevel() + ", bundleName=" + getResourceBundleName() + "]";
    }


    @Override
    public void setResourceBundle(ResourceBundle bundle) {
        // noop
    }
}
