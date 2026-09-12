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


package org.glassfish.orb.http.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Finds the object codecs available at runtime.
 * <p>
 * The codec token already travels in every content type, so the wire has
 * always been able to carry more than one codec. What was missing was the
 * other half: something that notices a codec is present. Every call site
 * named {@link JavaSerializationMarshaller} directly, which meant the
 * pluggability was theoretical - a faster codec on the class path changed
 * nothing.
 * <p>
 * Providers are found with {@link ServiceLoader}, so adding a codec is
 * adding a jar. An application does not name the codec, import it, or
 * configure it; the highest ranked provider present wins, and the built-in
 * Java serialization codec is the floor that is always there.
 * <p>
 * Inside the GlassFish OSGi runtime this works because GlassFish ships
 * Aries SPI-Fly, which weaves {@code ServiceLoader} call sites so a
 * provider in another bundle is visible. Outside a container - a plain
 * client JVM - the ordinary class path rules apply and nothing special is
 * needed.
 */
public final class Marshallers {

    /**
     * Always available, and deliberately lowest ranked. Two peers that share
     * no other codec can still talk, which is what makes falling back safe.
     */
    private static final Marshaller BUILT_IN = new JavaSerializationMarshaller();

    /**
     * Discovery walks the class path, so it is not something to repeat per
     * invocation. Keyed weakly because an application class loader must stay
     * collectable after the application is undeployed.
     */
    private static final Map<ClassLoader, Map<String, Marshaller>> CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Codecs handed to us rather than found by us.
     * <p>
     * {@link ServiceLoader} walks a class path, and inside an OSGi framework
     * there is no single class path to walk: a provider in one bundle is
     * invisible to a call site in another unless something bridges them. A
     * container that knows how to enumerate its own modules can bridge it here
     * instead, which is a smaller thing to depend on than a weaving extender
     * being present and active.
     */
    private static final List<Marshaller> REGISTERED = new CopyOnWriteArrayList<>();

    private Marshallers() {
    }

    /**
     * Adds a codec that discovery would not find on its own.
     *
     * <p>Idempotent by codec token: registering the same codec twice leaves one,
     * and registering a second provider for a token that is already registered
     * keeps the first, so a container that scans twice does not end up with a
     * different answer the second time.
     *
     * @param marshaller the codec to make available
     */
    public static void register(Marshaller marshaller) {
        if (marshaller == null || marshaller.codec() == null || marshaller.codec().isEmpty()) {
            return;
        }
        for (Marshaller existing : REGISTERED) {
            if (existing.codec().equals(marshaller.codec())) {
                return;
            }
        }
        REGISTERED.add(marshaller);
        // Anything already computed predates this codec.
        CACHE.clear();
    }

    /**
     * @return the highest ranked codec visible to the current thread
     */
    public static Marshaller preferred() {
        return preferred(contextLoader());
    }

    /**
     * @param loader where to look for providers
     * @return the highest ranked codec, never {@code null}
     */
    public static Marshaller preferred(ClassLoader loader) {
        // discover() is ordered best-first, so the first entry is the answer.
        return discover(loader).values().iterator().next();
    }

    /**
     * @param codec the token from a content type
     * @return the provider for that token, or empty if this JVM has none
     */
    public static Optional<Marshaller> find(String codec) {
        return find(codec, contextLoader());
    }

    /**
     * @param codec  the token from a content type
     * @param loader where to look for providers
     * @return the provider for that token, or empty if none is visible
     */
    public static Optional<Marshaller> find(String codec, ClassLoader loader) {
        return Optional.ofNullable(discover(loader).get(codec));
    }

    /**
     * @return every codec token available, best first - what this peer can
     *         honestly claim to understand
     */
    public static List<String> codecs() {
        return List.copyOf(discover(contextLoader()).keySet());
    }

    private static ClassLoader contextLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader == null ? Marshallers.class.getClassLoader() : loader;
    }

    /**
     * Builds the token-to-provider map, best ranked first.
     *
     * <p>A provider that throws while loading is skipped rather than allowed
     * to take down the transport: a broken optional codec must not stop an
     * application that never asked for it.
     */
    private static Map<String, Marshaller> discover(ClassLoader loader) {
        return CACHE.computeIfAbsent(loader, Marshallers::load);
    }

    private static Map<String, Marshaller> load(ClassLoader loader) {
        List<Marshaller> found = new ArrayList<>();
        found.add(BUILT_IN);
        found.addAll(REGISTERED);
        collect(loader, found);
        if (loader != Marshallers.class.getClassLoader()) {
            // The transport's own loader may see providers the context loader
            // does not, and vice versa. Both are legitimate places to look.
            collect(Marshallers.class.getClassLoader(), found);
        }

        // Highest priority first; ties broken by token so the choice is the
        // same on every JVM rather than dependent on class path order.
        found.sort((left, right) -> {
            int byPriority = Integer.compare(right.priority(), left.priority());
            return byPriority != 0 ? byPriority : left.codec().compareTo(right.codec());
        });

        Map<String, Marshaller> byToken = new LinkedHashMap<>();
        for (Marshaller marshaller : found) {
            // putIfAbsent, because the list is already in preference order:
            // the first provider claiming a token is the best one claiming it.
            byToken.putIfAbsent(marshaller.codec(), marshaller);
        }
        return Collections.unmodifiableMap(byToken);
    }

    private static void collect(ClassLoader loader, List<Marshaller> into) {
        try {
            for (Marshaller marshaller : ServiceLoader.load(Marshaller.class, loader)) {
                if (marshaller.codec() != null && !marshaller.codec().isEmpty()) {
                    into.add(marshaller);
                }
            }
        } catch (ServiceConfigurationError | RuntimeException e) {
            // Deliberately swallowed: see the method contract above.
        }
    }
}
