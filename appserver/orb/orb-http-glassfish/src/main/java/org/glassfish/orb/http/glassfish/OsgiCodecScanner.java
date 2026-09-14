/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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


package org.glassfish.orb.http.glassfish;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Marshallers;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Finds codecs in other modules and hands them to {@link Marshallers}.
 * <p>
 * A codec announces itself with a {@code META-INF/services} file, which is all
 * {@link java.util.ServiceLoader} needs on an ordinary class path. Inside this
 * server there is no ordinary class path: every module is a bundle, and a
 * provider in one is invisible to a {@code ServiceLoader} call in another
 * unless an extender rewrites the call site. GlassFish ships one, but relying
 * on it means relying on it being active and on the weaving having happened
 * before the first lookup - and when that does not hold, the failure is
 * silence: the codec is installed, nothing finds it, and the server quietly
 * answers in another encoding.
 * <p>
 * So this does the same job explicitly. It reads the service files out of the
 * installed bundles and loads each provider with the class loader of the bundle
 * that declared it, which is the one able to see it.
 */
final class OsgiCodecScanner {

    private static final Logger LOG = System.getLogger(OsgiCodecScanner.class.getName());

    private static final String SERVICE_FILE = "META-INF/services/" + Marshaller.class.getName();

    private OsgiCodecScanner() {
    }

    /**
     * Scans the framework and registers everything it finds.
     *
     * <p>Reports what it found either way. A codec that is present and not
     * found is the failure this exists to prevent, and it is not visible from
     * the outside until an encoding silently differs, so the log has to say.
     *
     * @return the codec tokens now available, for logging by the caller
     */
    static List<String> scanAndRegister() {
        Bundle self = FrameworkUtil.getBundle(OsgiCodecScanner.class);
        if (self == null) {
            // Not running inside a framework - a unit test, or an embedded
            // use. The ordinary class path rules apply and ServiceLoader is
            // already enough.
            return Marshallers.codecs();
        }

        BundleContext context = self.getBundleContext();
        if (context == null) {
            LOG.log(Level.DEBUG, "no bundle context; leaving codec discovery to the class path");
            return Marshallers.codecs();
        }

        for (Bundle bundle : context.getBundles()) {
            for (String className : providersIn(bundle)) {
                register(bundle, className);
            }
        }

        List<String> available = Marshallers.codecs();
        LOG.log(Level.INFO, "ORB over HTTP codecs available: " + String.join(", ", available));
        return available;
    }

    private static List<String> providersIn(Bundle bundle) {
        URL entry = bundle.getEntry(SERVICE_FILE);
        if (entry == null) {
            return List.of();
        }

        List<String> names = new ArrayList<>(2);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(entry.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int comment = line.indexOf('#');
                String name = (comment < 0 ? line : line.substring(0, comment)).trim();
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "cannot read " + SERVICE_FILE + " from " + bundle.getSymbolicName(), e);
        }
        return names;
    }

    private static void register(Bundle bundle, String className) {
        try {
            Object provider = bundle.loadClass(className).getDeclaredConstructor().newInstance();
            if (provider instanceof Marshaller marshaller) {
                Marshallers.register(marshaller);
                LOG.log(Level.INFO, "registered the " + marshaller.codec()
                        + " codec from " + bundle.getSymbolicName());
            }
        } catch (Exception | LinkageError e) {
            // One broken codec must not stop the transport starting, or an
            // optional module would be able to take down every application
            // that never asked for it.
            LOG.log(Level.WARNING, "cannot use codec " + className
                    + " from " + bundle.getSymbolicName(), e);
        }
    }
}
