/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul;

import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.trace;

/**
 * Special {@link GlassFishLogger} for system loggers which have non-standard behavior.
 *
 * @author David Matejcek
 */
class GlassFishLoggerWrapper extends GlassFishLogger {

    private final Logger logger;

    GlassFishLoggerWrapper(final Logger logger) {
        super(logger.getName());
        this.logger = logger;
        // warning: jul uses setters, but instead of getters it uses fields directly.
        // it makes no sense to call setters here, values are not initialized yet.
    }


    @Override
    public Logger getParent() {
        return this.logger.getParent();
    }


    @Override
    public void setParent(final Logger parent) {
        trace(GlassFishLoggerWrapper.class, () -> "setParent(" + parent + "); this: " + this);
        this.logger.setParent(parent);
        // JUL uses this field directly
        super.setParent(logger.getParent());
    }


    @Override
    public Level getLevel() {
        return this.logger.getLevel();
    }


    @Override
    public void setLevel(final Level newLevel) throws SecurityException {
        trace(GlassFishLoggerWrapper.class, () -> "setLevel(" + newLevel + "); this: " + this);
        this.logger.setLevel(newLevel);
        // JUL uses this field directly
        super.setLevel(newLevel);
    }


    @Override
    public Filter getFilter() {
        return this.logger.getFilter();
    }


    @Override
    public void setFilter(final Filter newFilter) throws SecurityException {
        this.logger.setFilter(newFilter);
    }


    @Override
    public ResourceBundle getResourceBundle() {
        return this.logger.getResourceBundle();
    }


    @Override
    public void setResourceBundle(final ResourceBundle bundle) {
        this.logger.setResourceBundle(bundle);
    }


    @Override
    public String getResourceBundleName() {
        return this.logger.getResourceBundleName();
    }


    @Override
    public boolean getUseParentHandlers() {
        return this.logger.getUseParentHandlers();
    }


    @Override
    public void setUseParentHandlers(final boolean useParentHandlers) {
        this.logger.setUseParentHandlers(useParentHandlers);
    }


    @Override
    public Handler[] getHandlers() {
        return this.logger.getHandlers();
    }


    @Override
    protected boolean isLoggableLevel(final Level level) {
        return this.logger.isLoggable(level);
    }


    @Override
    public void addHandler(final Handler handler) throws SecurityException {
        trace(GlassFishLoggerWrapper.class, () -> "addHandler(" + handler + ")");
        this.logger.addHandler(handler);
    }


    @Override
    public void removeHandler(final Handler handler) throws SecurityException {
        trace(GlassFishLoggerWrapper.class, () -> "removeHandler(" + handler + ")");
        this.logger.removeHandler(handler);
    }
}
