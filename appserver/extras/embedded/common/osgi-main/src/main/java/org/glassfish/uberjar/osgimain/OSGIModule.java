/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.uberjar.osgimain;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author bhavanishankar@dev.java.net
 */

public class OSGIModule {

    private static final Logger logger = Logger.getLogger("embedded-glassfish");

    private String location;
    private InputStream contentStream;
    private String bundleSymbolicName;
    private ExceptionHandler exceptionHandler = new ExceptionHandler();
    private boolean explicitlyClosed;

    public void close() {
        explicitlyClosed = true;
        try {
            if (contentStream != null) {
                contentStream.close();
                contentStream = null;
            }
        } catch (Exception ex) {
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    @Override
    public String toString() {
        return super.toString() + " :: location = " + location;
    }

    public String getBundleSymbolicName() {
        return bundleSymbolicName;
    }

    public void setBundleSymbolicName(String bundleSymbolicName) {
        this.bundleSymbolicName = bundleSymbolicName;
    }

    class ExceptionHandler {

        public void handle(Exception ex) {
            if (!explicitlyClosed) {
//                logger.warning(ex.getMessage());
            }
        }
    }

}
