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


package org.glassfish.orb.http.codec.fory;

/**
 * Lets the codec and the classes it encodes see each other.
 * <p>
 * Fory compiles a serializer per type, and the generated class names both the
 * type being serialized and Fory's own runtime. On an ordinary class path one
 * loader sees both and there is nothing to arrange. Inside this server there
 * is no such loader: the type comes from the application or the protocol
 * bundle, Fory is embedded in this one, and neither bundle can see the other's
 * classes. Code generation then fails with "Create sequential serializer
 * failed", naming the type rather than the visibility problem behind it.
 * <p>
 * This bridges the two, in that order: the application's loader answers first,
 * and Fory's own is consulted only for what the application does not have.
 * That ordering matters - an application class must never be resolved from
 * this bundle, or two copies of the same type would exist and the reference
 * tracking that keeps object graphs intact would break on them.
 */
final class BridgingClassLoader extends ClassLoader {

    private final ClassLoader codec;

    /**
     * @param application the loader that owns the types being encoded, which
     *                    stays the parent so it is always asked first
     * @param codec       the loader that owns Fory, consulted only as a
     *                    fallback
     */
    BridgingClassLoader(ClassLoader application, ClassLoader codec) {
        super(application);
        this.codec = codec;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Reached only after the parent has failed, which is what keeps
        // application types coming from the application.
        return codec.loadClass(name);
    }
}
