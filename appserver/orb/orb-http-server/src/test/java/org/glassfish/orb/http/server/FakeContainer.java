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

package org.glassfish.orb.http.server;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.glassfish.orb.http.protocol.EjbKey;

/**
 * A {@link ContainerBridge} over a handful of plain objects.
 * <p>
 * It stands in for {@code EjbContainerFacade}, and the fact that it can is the
 * point: the dispatcher asks the container only for what that interface
 * already offers, so nothing in the HTTP path needs the real container to be
 * exercised.
 */
final class FakeContainer implements ContainerBridge {

    private final Map<String, Object> statelessBeans = new ConcurrentHashMap<>();
    private final Map<String, java.util.function.Supplier<Object>> statefulFactories = new ConcurrentHashMap<>();
    private final Map<Long, Object> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> beanIds = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    volatile int released;

    void registerStateless(String beanName, Object instance) {
        statelessBeans.put(beanName, instance);
        beanIds.computeIfAbsent(beanName, k -> nextId.getAndIncrement());
    }

    void registerStateful(String beanName, java.util.function.Supplier<Object> factory) {
        statefulFactories.put(beanName, factory);
        beanIds.computeIfAbsent(beanName, k -> nextId.getAndIncrement());
    }

    @Override
    public EjbKey resolve(String appName, String moduleName, String distinctName,
                          String beanName, byte[] sessionId) throws NoSuchTargetException {
        Long id = beanIds.get(beanName);
        if (id == null) {
            throw new NoSuchTargetException("no bean named " + beanName);
        }
        if (sessionId == null) {
            return EjbKey.home(id);
        }
        return new EjbKey(id, sessionId);
    }

    @Override
    public Object getTargetObject(EjbKey key, String viewClassName) throws NoSuchTargetException {
        if (key.isHome()) {
            for (Map.Entry<String, Long> e : beanIds.entrySet()) {
                if (e.getValue() == key.ejbId()) {
                    Object bean = statelessBeans.get(e.getKey());
                    if (bean != null) {
                        return bean;
                    }
                }
            }
            throw new NoSuchTargetException("no stateless instance for ejbId " + key.ejbId());
        }
        Object session = sessions.get(sessionKey(key));
        if (session == null) {
            throw new NoSuchTargetException("no such session");
        }
        return session;
    }

    @Override
    public void releaseTargetObject(Object target) {
        released++;
    }

    @Override
    public ClassLoader classLoader(EjbKey key) {
        return FakeContainer.class.getClassLoader();
    }

    @Override
    public byte[] createSession(String appName, String moduleName, String distinctName, String beanName)
            throws NoSuchTargetException {
        java.util.function.Supplier<Object> factory = statefulFactories.get(beanName);
        if (factory == null) {
            throw new NoSuchTargetException("no stateful bean named " + beanName);
        }
        long id = beanIds.get(beanName);
        byte[] sessionId = new byte[8];
        java.nio.ByteBuffer.wrap(sessionId).putLong(nextId.getAndIncrement());
        sessions.put(sessionKey(new EjbKey(id, sessionId)), factory.get());
        return sessionId;
    }

    private static long sessionKey(EjbKey key) {
        return key.ejbId() * 1_000_003L + java.util.Arrays.hashCode(key.instanceKey());
    }
}
