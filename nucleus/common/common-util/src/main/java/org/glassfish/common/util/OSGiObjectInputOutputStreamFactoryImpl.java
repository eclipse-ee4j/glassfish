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

package org.glassfish.common.util;

import com.sun.enterprise.util.CULoggerInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import static com.sun.enterprise.util.Utility.getClassLoader;
import static java.security.AccessController.doPrivileged;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.WARNING;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiObjectInputOutputStreamFactoryImpl implements ObjectInputOutputStreamFactory {

    private static final Logger logger = CULoggerInfo.getLogger();

    private BundleContext ctx;
    PackageAdmin pkgAdm;

    private ConcurrentHashMap<String, Long> name2Id = new ConcurrentHashMap<String, Long>();

    // Since bundle id starts with 0, we use -1 to indicate a non-bundle
//    private static final long NOT_A_BUNDLE_ID = -1;
    private static final String NOT_A_BUNDLE_KEY = ":";

    public OSGiObjectInputOutputStreamFactoryImpl(BundleContext ctx) {
        this.ctx = ctx;
        ServiceReference ref = ctx.getServiceReference(PackageAdmin.class.getName());
        pkgAdm = PackageAdmin.class.cast(ctx.getService(ref));

        BundleTracker bt = new BundleTracker(ctx, Bundle.INSTALLED | Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE,
                new BundleTrackerCustomizer() {

                    @Override
                    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
                    }

                    @Override
                    public Object addingBundle(Bundle bundle, BundleEvent bundleEvent) {
                        String key = makeKey(bundle);
                        name2Id.put(key, bundle.getBundleId());
                        if (logger != null && logger.isLoggable(FINER)) {
                            logger.log(FINER, "BundleTracker.addingBundle BUNDLE " + key + " ==> " + bundle.getBundleId() + "  for "
                                    + bundle.getSymbolicName());
                        }
                        return null;
                    }

                    @Override
                    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
                        String key = makeKey(bundle);
                        Long bundleID = name2Id.remove(key);
                        if (logger.isLoggable(FINER)) {
                            logger.log(FINER, "BundleTracker.removedBundle BUNDLE " + key + "  ==> " + bundle.getSymbolicName());
                        }
                        if (bundleID == null) {
                            logger.log(WARNING, CULoggerInfo.NULL_BUNDLE, key);
                        }
                    }
                });

        bt.open();

    }

    private String makeKey(Bundle bundle) {
        return bundle.getSymbolicName() + ":" + bundle.getVersion();
    }

    @Override
    public ObjectInputStream createObjectInputStream(InputStream in) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = getClassLoader();
        }
        return new OSGiObjectInputStream(in, loader);
    }

    @Override
    public ObjectOutputStream createObjectOutputStream(OutputStream out) throws IOException {
        return new OSGiObjectOutputStream(out);
    }

    private class OSGiObjectInputStream extends ObjectInputStreamWithLoader {

        public OSGiObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
            super(in, loader);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            Class clazz = OSGiObjectInputOutputStreamFactoryImpl.this.resolveClass(this, desc);

            if (clazz == null) {
                clazz = super.resolveClass(desc);
            }

            return clazz;
        }

    }

    private class OSGiObjectOutputStream extends ObjectOutputStream {

        private OSGiObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void annotateClass(Class<?> cl) throws IOException {
            OSGiObjectInputOutputStreamFactoryImpl.this.annotateClass(this, cl);
        }
    }

    @Override
    public Class<?> resolveClass(ObjectInputStream in, final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String key = in.readUTF();

        if (!NOT_A_BUNDLE_KEY.equals(key)) {
            Long bundleId = name2Id.get(key);
            if (bundleId != null) {
                final Bundle b = ctx.getBundle(bundleId);
                String cname = desc.getName();
                if (cname.startsWith("[")) {
                    return loadArrayClass(b, cname);
                } else {
                    return loadClassFromBundle(b, cname);
                }
            }
        }

        return null;
    }

    @Override
    public void annotateClass(ObjectOutputStream out, Class<?> cl) throws IOException {
        String key = NOT_A_BUNDLE_KEY;
        Bundle b = pkgAdm.getBundle(cl);
        if (b != null) {
            key = makeKey(b);
        }
        out.writeUTF(key);
    }

    private Class loadArrayClass(Bundle b, String cname) throws ClassNotFoundException {
        // We are never called with primitive types, so we don't have to check for primitive types.
        assert (cname.charAt(0) == 'L'); // An array
        Class component; // component class
        int dcount; // dimension
        for (dcount = 1; cname.charAt(dcount) == '['; dcount++) {
        }
        assert (cname.charAt(dcount) == 'L');
        component = loadClassFromBundle(b, cname.substring(dcount + 1, cname.length() - 1));
        int dim[] = new int[dcount];
        for (int i = 0; i < dcount; i++) {
            dim[i] = 0;
        }
        return Array.newInstance(component, dim).getClass();
    }

    private Class loadClassFromBundle(final Bundle b, final String cname) throws ClassNotFoundException {
        if (System.getSecurityManager() == null) {
            return b.loadClass(cname);
        } else {
            try {
                return (Class) doPrivileged(new PrivilegedExceptionAction<Object>() {
                    @Override
                    public Object run() throws ClassNotFoundException {
                        return b.loadClass(cname);
                    }
                });
            } catch (java.security.PrivilegedActionException pae) {
                throw (ClassNotFoundException) pae.getException();
            }
        }
    }
}