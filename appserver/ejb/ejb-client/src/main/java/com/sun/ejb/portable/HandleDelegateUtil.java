/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.portable;

import jakarta.ejb.spi.HandleDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Common code for looking up the java:comp/HandleDelegate.
 *
 * This class can potentially be instantiated in another vendor's container so it must not refer to any non-portable
 * RI-specific classes.
 *
 * @author Kenneth Saks
 */
public class HandleDelegateUtil {

    private static final String JNDI_PROPERTY_FILE_NAME = "com.sun.ejb.portable.jndi.propertyfilename";

    // Class-level state for jndi properties override. Flag is used
    // so that we only check once for jndi properties file override.
    private static boolean checkedJndiProperties;

    // contents of file referred to by jndi properties override
    private static Properties jndiProperties;

    private static volatile HandleDelegate _cachedHandleDelegate;

    static HandleDelegate getHandleDelegate() throws NamingException {
        if (_cachedHandleDelegate == null) {
            synchronized (HandleDelegateUtil.class) {
                if (_cachedHandleDelegate == null) {
                    _cachedHandleDelegate = createHandleDelegate();
                }
            }
        }

        return _cachedHandleDelegate;
    }

    private static HandleDelegate createHandleDelegate() throws NamingException {
        HandleDelegate handleDelegate;
        try {
            InitialContext ctx = new InitialContext();
            handleDelegate = (HandleDelegate) ctx.lookup("java:comp/HandleDelegate");
        } catch (NamingException ne) {

            // If the lookup fails, it's probably because the default
            // InitialContext settings needed to access the correct
            // java:comp/HandleDelegate have been overridden in this VM.
            // In that case, check if the system value class override
            // property file is available and if so use it.
            Properties props = null;
            try {
                props = getJndiProperties();
            } catch (Exception e) {
                // Exception while attempting to access jndi property override.
                // Create new NamingException that describes the error.
                NamingException ne2 = new NamingException("Error while accessing " + JNDI_PROPERTY_FILE_NAME + " : " + e.getMessage());
                ne2.initCause(e);
                throw ne2;
            }

            if (props == null) {
                // There was no property override set.
                NamingException ne3 = new NamingException("java:comp/HandleDelegate not found. Unable to "
                        + " use jndi property file override since " + JNDI_PROPERTY_FILE_NAME + " has NOT been set");
                ne3.initCause(ne);
                throw ne3;
            }

            try {
                InitialContext ctx = new InitialContext(props);
                handleDelegate = (HandleDelegate) ctx.lookup("java:comp/HandleDelegate");
            } catch (NamingException ne4) {
                NamingException overrideEx = new NamingException(
                        "Unable to lookup HandleDelegate " + "with override properties = " + props.toString());
                overrideEx.initCause(ne4);
                throw overrideEx;
            }
        }

        return handleDelegate;
    }

    /**
     * Internal method for accessing jndi properties override. We only look for properties file at most once, whether it is
     * present or not.
     *
     */
    private static Properties getJndiProperties() throws Exception {
        synchronized (HandleDelegateUtil.class) {
            if (!checkedJndiProperties) {
                FileInputStream fis = null;
                try {
                    String jndiPropertyFileName = System.getProperty(JNDI_PROPERTY_FILE_NAME);

                    if (jndiPropertyFileName != null) {
                        fis = new FileInputStream(jndiPropertyFileName);
                        jndiProperties = new Properties();
                        jndiProperties.load(fis);
                        // Let an exception encountered here bubble up, so
                        // we can include its info in the exception propagated
                        // to the application.
                    }
                } finally {
                    // Always set to true so we don't keep doing this
                    // system property and file access multiple times
                    checkedJndiProperties = true;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
        }

        return jndiProperties;
    }
}
